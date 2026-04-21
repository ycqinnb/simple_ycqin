package yc.ycqin.nb.common.entity.ai;

import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.ai.EntityAIBase;
import net.minecraft.entity.monster.EntityMob;
import net.minecraft.potion.PotionEffect;
import net.minecraft.init.MobEffects;
import net.minecraft.util.DamageSource;
import yc.ycqin.nb.config.ModConfig;
import yc.ycqin.nb.util.EntityClassifier;
import yc.ycqin.nb.world.WorldLevelData;

public class EntityAIPullDown extends EntityAIBase {
    private final EntityMob entity;
    private EntityLivingBase target;
    private int cooldown = 0;

    public EntityAIPullDown(EntityMob entity) {
        this.entity = entity;
        this.setMutexBits(2); // 与其他移动类 AI 不冲突
    }

    @Override
    public boolean shouldExecute() {
        // 仅强化生物使用
        if (!entity.getEntityData().hasKey("yc_protectcoth")) return false;
        // 冷却控制
        if (cooldown > 0) {
            cooldown--;
            return false;
        }
        // 世界等级要求 > 3
        WorldLevelData levelData = WorldLevelData.get(entity.world);
        if (levelData.getLevel() <= 3) return false;

        // 扫描周围 16 格内的寄生虫
        double range = 16;
        for (EntityLivingBase living : entity.world.getEntitiesWithinAABB(EntityLivingBase.class,
                entity.getEntityBoundingBox().grow(range))) {
            if (living != entity && EntityClassifier.isTargetParasite(living) && !living.onGround) {
                this.target = living;
                return true;
            }
        }
        return false;
    }

    @Override
    public void startExecuting() {
        if (target == null) return;
        // 概率触发
        if (entity.getRNG().nextFloat() < ModConfig.pullDownChance) {
            // 拉向地面
            target.motionY = -15;
            target.velocityChanged = true;
            // 造成伤害
            target.attackEntityFrom(DamageSource.FALL, ModConfig.pullDownDamage);
            // 眩晕效果
            target.addPotionEffect(new PotionEffect(MobEffects.SLOWNESS, ModConfig.pullDownStun, 40));
            target.addPotionEffect(new PotionEffect(MobEffects.BLINDNESS, ModConfig.pullDownStun, 0));
        }
        cooldown = 40; // 每次尝试后冷却 2 秒
        target = null;
    }

    @Override
    public boolean shouldContinueExecuting() {
        return false; // 一次性执行
    }
}
