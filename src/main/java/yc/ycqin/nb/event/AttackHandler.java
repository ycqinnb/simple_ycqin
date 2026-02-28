package yc.ycqin.nb.event;

import com.dhanantry.scapeandrunparasites.entity.ai.misc.EntityParasiteBase;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.EntityEquipmentSlot;
import net.minecraft.item.ItemStack;
import net.minecraft.util.DamageSource;
import net.minecraftforge.event.entity.living.LivingHurtEvent;
import net.minecraftforge.event.entity.player.AttackEntityEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import yc.ycqin.nb.register.ModEnchantments;

import javax.annotation.Nullable;

@Mod.EventBusSubscriber(modid = "ycqin")
public class AttackHandler {
    @SubscribeEvent
    public static void onPlayerAttack(AttackEntityEvent event) {
        // 获取攻击者
        EntityPlayer player = event.getEntityPlayer();
        Entity target = event.getTarget();
        if (target.world.isRemote) return;
        if (!(target instanceof EntityLivingBase)) return; // 只对生物有效
        EntityLivingBase livingTarget = (EntityLivingBase) target;
        ItemStack weapon = player.getHeldItemMainhand();
        int level = EnchantmentHelper.getEnchantmentLevel(ModEnchantments.MIN_DAMAGE, weapon);
        if (level <= 0) return; // 没有附魔，交给原版处理
        attackWithMinimumDamage(livingTarget,level,player,event);
    }

    public static boolean attackWithMinimumDamage(EntityLivingBase target, float minDamage, @Nullable EntityLivingBase attacker,AttackEntityEvent event) {
        // 只在服务端执行
        if (target.world.isRemote) return false;

        // 基础有效性检查
        if (!target.isEntityAlive()) return false;

        // 应用最小伤害保护附魔（胸甲）
        float finalDamage = minDamage;
        if (target instanceof EntityPlayer) {
            EntityPlayer player = (EntityPlayer) target;
            ItemStack chest = player.getItemStackFromSlot(EntityEquipmentSlot.CHEST);
            int level = EnchantmentHelper.getEnchantmentLevel(ModEnchantments.MIN_DAMAGE_PROTECT, chest);
            if (level > 0) {
                finalDamage = Math.max(0, finalDamage - level); // 每级减少1点
            }
        }

        // 伤害必须大于0才有效
        if (finalDamage <= 0) return false;

        // 直接扣除生命值
        float newHealth = target.getHealth() - finalDamage;
        if (newHealth < 0) newHealth = 0;
        target.setHealth(newHealth);

        // 如果目标死亡，触发死亡事件
        if (newHealth <= 0) {
            DamageSource source;
            if (attacker != null) {
                if (attacker instanceof EntityPlayer){
                    source = DamageSource.causePlayerDamage((EntityPlayer) attacker);
                } else {
                    source = DamageSource.causeMobDamage(attacker);
                }
            } else {
                source = DamageSource.OUT_OF_WORLD;
            }
            target.onDeath(source);
            event.setCanceled(true);
            // Minecraft 会随后处理实体移除，无需额外调用 setDead()
        }

        return true;
    }
}
