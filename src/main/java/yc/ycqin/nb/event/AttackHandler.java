package yc.ycqin.nb.event;

import com.dhanantry.scapeandrunparasites.entity.ai.misc.EntityParasiteBase;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.EntityEquipmentSlot;
import net.minecraft.item.ItemStack;
import net.minecraft.potion.Potion;
import net.minecraft.potion.PotionEffect;
import net.minecraft.util.DamageSource;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.event.entity.living.LivingHurtEvent;
import net.minecraftforge.event.entity.player.AttackEntityEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.registry.ForgeRegistries;
import yc.ycqin.nb.common.trait.armorTrait.TraitMinDamageProtect;
import yc.ycqin.nb.proxy.CommonProxy;
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
        attackWithMinimumDamage(livingTarget,level,player);
    }

    public static boolean attackWithMinimumDamage(EntityLivingBase target, float minDamage, @Nullable EntityLivingBase attacker) {
        // 只在服务端执行
        if (target.world.isRemote) return false;

        // 基础有效性检查
        if (!target.isEntityAlive()) return false;


        float finalDamage = minDamage;

        // 检查病毒效果，增加伤害倍数
        Potion viralPotion = ForgeRegistries.POTIONS.getValue(new ResourceLocation("srparasites", "viral"));
        if (viralPotion != null) {
            PotionEffect effect = target.getActivePotionEffect(viralPotion);
            if (effect != null) {
                int amplifier = effect.getAmplifier(); // 0-based
                int virusLevel = amplifier + 1;
                float multiplier = 1 + virusLevel; // 例如病毒等级4 => 倍数5
                finalDamage = finalDamage * multiplier;
            }
        }

        if (target instanceof EntityPlayer) {
            EntityPlayer player = (EntityPlayer) target;
            ItemStack chest = player.getItemStackFromSlot(EntityEquipmentSlot.CHEST);
            int level;
            if (CommonProxy.isTCArmorLoaded){
                level = EnchantmentHelper.getEnchantmentLevel(ModEnchantments.MIN_DAMAGE_PROTECT, chest) + TraitMinDamageProtect.getTotalProtectionLevel(target);
            } else {
                level = EnchantmentHelper.getEnchantmentLevel(ModEnchantments.MIN_DAMAGE_PROTECT, chest);
            }
            if (level > 0) {
                finalDamage = Math.max(0, finalDamage - level); // 每级减少1点
            }
        }



        // 伤害必须大于0才有效
        if (finalDamage <= 0) return false;

        // 直接扣除生命值
        float newHealth = target.getHealth() - finalDamage;
        if (newHealth <= 0 || Float.isNaN(newHealth)) newHealth = 0;
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
        }

        return true;
    }
}
