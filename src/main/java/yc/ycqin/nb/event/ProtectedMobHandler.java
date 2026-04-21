package yc.ycqin.nb.event;

import com.dhanantry.scapeandrunparasites.entity.ai.misc.EntityPMalleable;
import com.dhanantry.scapeandrunparasites.entity.ai.misc.EntityParasiteBase;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.SharedMonsterAttributes;
import net.minecraft.entity.ai.attributes.AttributeModifier;
import net.minecraft.entity.ai.attributes.IAttributeInstance;
import net.minecraft.entity.monster.EntityMob;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.MobEffects;
import net.minecraft.potion.PotionEffect;
import net.minecraft.util.DamageSource;
import net.minecraft.util.math.Vec3d;
import net.minecraftforge.event.entity.EntityJoinWorldEvent;
import net.minecraftforge.event.entity.living.LivingAttackEvent;
import net.minecraftforge.event.entity.living.LivingDeathEvent;
import net.minecraftforge.event.entity.living.LivingEvent;
import net.minecraftforge.event.entity.living.LivingHurtEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import yc.ycqin.nb.common.entity.ai.*;
import yc.ycqin.nb.config.ModConfig;
import yc.ycqin.nb.util.EntityClassifier;
import yc.ycqin.nb.util.ParasiteHelper;
import yc.ycqin.nb.world.WorldLevelData;

import java.util.UUID;

public class ProtectedMobHandler {

    // 用于护盾的属性修饰符UUID
    private static final UUID SHIELD_MODIFIER = UUID.fromString("dce6e421-0cca-4a73-977b-6532c2e8ac55");

    @SubscribeEvent
    public void onEntityJoinWorld(EntityJoinWorldEvent event) {
        // 自然生成时，根据概率赋予 yc_protectcoth 标签（仅当世界等级 > 2 时）
        if (!event.getWorld().isRemote && event.getEntity() instanceof EntityLivingBase && !(event.getEntity() instanceof EntityParasiteBase)) {
            EntityLivingBase entity = (EntityLivingBase) event.getEntity();
            if (!entity.getEntityData().hasKey("yc_protectcoth")) {
                WorldLevelData levelData = WorldLevelData.get(event.getWorld());
                if (levelData.getLevel() > 2 && event.getWorld().rand.nextFloat() < ModConfig.naturalSpawnChance) {
                    // 检查是否是敌对生物
                    if (entity instanceof EntityMob) {
                        onProtectedSpawn(entity);
                    }
                }
            }
        }
    }

    /**
     * 为生物添加保护标签并应用强化属性
     */
    public static void applyProtection(EntityLivingBase entity) {
        entity.getEntityData().setBoolean("yc_protectcoth", true);
        updateDefense(entity);
        int level = WorldLevelData.get(entity.world).getLevel();
        if (level > 2) {
            float maxShield = entity.getMaxHealth() * ModConfig.protectShieldRatio * level;
            setMaxShield(entity, maxShield);
            setShield(entity, maxShield);
            setShieldCooldown(entity, 0); // 无冷却
        }
        if (level > 4) {
            entity.addPotionEffect(new PotionEffect(MobEffects.SPEED, Integer.MAX_VALUE, 3, false, false));
        }
        if (entity instanceof EntityMob && !entity.getEntityData().hasKey("yc_ai_added")) {
            entity.getEntityData().setBoolean("yc_ai_added", true);
            EntityMob mob = (EntityMob) entity;
            mob.targetTasks.taskEntries.clear();
            mob.targetTasks.addTask(3, new EntityAIHurtByTargetParasiteOnly(mob));
            // 自主索敌寄生虫（优先级次高）
            mob.targetTasks.addTask(1, new EntityAIFindParasite(mob, 20));
            // 4. 其他辅助 AI
            mob.tasks.addTask(3, new EntityAIPullDown(mob));      // 拉下空中寄生虫
        }
        entity.getEntityData().setFloat("yc_last_health", entity.getHealth());

    }

    /**
     * 更新生物的防御值（盔甲）
     */
    private static void updateDefense(EntityLivingBase entity) {
        int level = WorldLevelData.get(entity.world).getLevel();
        IAttributeInstance armorAttr = entity.getEntityAttribute(SharedMonsterAttributes.ARMOR);
        double baseArmor = armorAttr.getBaseValue();
        double newArmor = baseArmor * Math.pow(ModConfig.defenseMultiplierPerLevel, level - 1);
        armorAttr.setBaseValue(newArmor);
    }

    @SubscribeEvent
    public void onLivingAttack(LivingAttackEvent event) {
        // 格挡并弹飞逻辑：当强化生物被寄生虫攻击时
        if (event.getEntityLiving().getEntityData().hasKey("yc_protectcoth")) {
            EntityLivingBase defender = event.getEntityLiving();
            DamageSource source = event.getSource();
            if (source.getTrueSource() instanceof EntityLivingBase) {
                EntityLivingBase attacker = (EntityLivingBase) source.getTrueSource();
                if (EntityClassifier.isTargetParasite(attacker)) {
                    // 格挡概率
                    if (defender.world.rand.nextFloat() < ModConfig.blockChance) {
                        event.setCanceled(true);
                        // 弹飞并造成眩晕
                        Vec3d knockback = defender.getPositionVector().subtract(attacker.getPositionVector()).normalize();
                        attacker.addVelocity(knockback.x * 1.5, 0.5, knockback.z * 1.5);
                        // 应用眩晕效果（可使用自定义药水效果或直接设置移动速度）
                        // 简化：给寄生虫缓慢效果和失明
                        attacker.addPotionEffect(new PotionEffect(MobEffects.SLOWNESS, ModConfig.stunDuration, 40));
                        attacker.addPotionEffect(new PotionEffect(MobEffects.BLINDNESS, ModConfig.stunDuration, 0));
                    }
                }
            }
        }
    }

    @SubscribeEvent
    public void onLivingHurt(LivingHurtEvent event) {
        EntityLivingBase target = event.getEntityLiving();
        DamageSource source = event.getSource();
        Entity trueSource = source.getTrueSource();
        if (target.getEntityData().hasKey("yc_protectcoth")) {
            float amount = event.getAmount();
            float maxHealth = target.getMaxHealth();
            // 限伤：单次伤害不超过生命值的 damageCapRatio
            float maxAllowed = maxHealth * ModConfig.damageCapRatio;
            if (amount > maxAllowed) {
                amount = maxAllowed;
            }

            // 护盾处理（仅对寄生体伤害）
            if (trueSource instanceof EntityLivingBase && EntityClassifier.isTargetParasite((EntityLivingBase) trueSource)) {
                int level = WorldLevelData.get(target.world).getLevel();
                if (level > 2 && !isShieldOnCooldown(target)) {
                    float shield = getShield(target);
                    if (shield >= amount) {
                        // 伤害转为治疗效果
                        target.setHealth(target.getHealth() + amount);
                        amount = 0;
                    } else {
                        // 伤害大于护盾，伤害减半，护盾清空，进入冷却
                        amount = amount / 2;
                        setShield(target, 0);
                        startShieldCooldown(target);
                    }
                }
            }
            event.setAmount(amount);
        }

        // 2. 强化生物攻击寄生虫时的额外伤害
        if (trueSource instanceof EntityLivingBase && ((EntityLivingBase) trueSource).getEntityData().hasKey("yc_protectcoth")) {
            if (EntityClassifier.isTargetParasite(target)) {
                int level = WorldLevelData.get(target.world).getLevel();
                float extra = ModConfig.extraDamageBase * (float) Math.pow(ModConfig.extraDamageMultiplierPerLevel, level - 1);
                event.setAmount(event.getAmount() + extra);
                // 移除寄生虫的伤害适
                if (target instanceof EntityPMalleable){
                    ParasiteHelper.reduceAllResistances((EntityPMalleable) target, 1.0f, 2);
                }

            }
        }
    }

    @SubscribeEvent
    public void onLivingDeath(LivingDeathEvent event) {
        EntityLivingBase dead = event.getEntityLiving();
        // 强化生物死亡时扣除点数
        if (dead.getEntityData().hasKey("yc_protectcoth")) {
            if (dead.getHealth() > 0){
                event.setCanceled(true);
                return;
            }
            WorldLevelData.get(dead.world).addPoints(ModConfig.pointsOnProtectedDeath);
        }
        // 如果死因是强化生物击杀，则增加点数
        if (event.getSource().getTrueSource() instanceof EntityLivingBase) {
            EntityLivingBase killer = (EntityLivingBase) event.getSource().getTrueSource();
            if (killer.getEntityData().hasKey("yc_protectcoth")) {
                if (EntityClassifier.isTargetParasite(dead)) {
                    WorldLevelData.get(killer.world).addPoints(ModConfig.pointsOnParasiteKill);
                }
            }
        }
    }

    @SubscribeEvent
    public void onLivingUpdate(LivingEvent.LivingUpdateEvent event) {
        EntityLivingBase entity = event.getEntityLiving();
        if (entity.getEntityData().hasKey("yc_protectcoth") && !entity.world.isRemote) {
            updateShieldCooldown(entity);
        }
    }

    // 强化生物生成时增加点数（在裂解装置生成时调用，也需要在自然生成时调用）
    // 自然生成时增加点数：可以在 applyProtection 中增加
    public static void onProtectedSpawn(EntityLivingBase entity) {
        WorldLevelData.get(entity.world).addPoints(ModConfig.pointsOnProtectedSpawn);
        applyProtection(entity);
    }

    public static void setShield(EntityLivingBase entity, float value) {
        entity.getEntityData().setFloat("yc_shield", value);
    }
    public static float getShield(EntityLivingBase entity) {
        return entity.getEntityData().getFloat("yc_shield");
    }
    public static void setMaxShield(EntityLivingBase entity, float value) {
        entity.getEntityData().setFloat("yc_max_shield", value);
    }
    public static float getMaxShield(EntityLivingBase entity) {
        return entity.getEntityData().getFloat("yc_max_shield");
    }
    public static void setShieldCooldown(EntityLivingBase entity, long cooldownEnd) {
        entity.getEntityData().setLong("yc_shield_cooldown", cooldownEnd);
    }
    public static long getShieldCooldown(EntityLivingBase entity) {
        return entity.getEntityData().getLong("yc_shield_cooldown");
    }
    public static boolean isShieldOnCooldown(EntityLivingBase entity) {
        return getShieldCooldown(entity) > entity.world.getTotalWorldTime();
    }
    public static void resetShield(EntityLivingBase entity) {
        float maxShield = getMaxShield(entity);
        setShield(entity, maxShield);
    }
    public static void startShieldCooldown(EntityLivingBase entity) {
        setShieldCooldown(entity, entity.world.getTotalWorldTime() + 200); // 10秒 = 200 ticks
    }
    public static void updateShieldCooldown(EntityLivingBase entity) {
        if (!isShieldOnCooldown(entity) && getShield(entity) <= 0 && getMaxShield(entity) > 0) {
            // 冷却结束，回满护盾
            resetShield(entity);
        }
    }
    public static void addShield(EntityLivingBase entity, float amount) {
        setShield(entity, getShield(entity) + amount);
    }
    public static void subtractShield(EntityLivingBase entity, float amount) {
        float current = getShield(entity);
        setShield(entity, Math.max(0, current - amount));
    }
}
