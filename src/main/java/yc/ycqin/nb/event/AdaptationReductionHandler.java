package yc.ycqin.nb.event;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.util.DamageSource;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.event.entity.living.LivingHurtEvent;
import net.minecraftforge.fml.common.Optional;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import yc.ycqin.nb.common.trait.armorTrait.TraitAdaptation;
import yc.ycqin.nb.enchantment.EnchantmentAdaptation;

public class AdaptationReductionHandler {

    @SubscribeEvent
    @Optional.Method(modid = "conarm")
    public void onLivingHurtTRAIT(LivingHurtEvent event) {
        if (!(event.getEntity() instanceof EntityPlayer)) return;
        EntityPlayer player = (EntityPlayer) event.getEntity();
        if (player.world.isRemote) return;

        String damageType = getDisplayDamageType(event.getSource());
        float totalReduction = TraitAdaptation.getTotalReduction(player, damageType);
        if (totalReduction >= 1) {
            event.setAmount(0);
            return;
        }
        if (totalReduction > 0) {
            float newDamage = event.getAmount() * (1 - totalReduction);
            event.setAmount(newDamage);
        }
    }

    @SubscribeEvent
    public void onLivingHurtENCHANTMENT(LivingHurtEvent event) {
        if (!(event.getEntity() instanceof EntityPlayer)) return;
        EntityPlayer player = (EntityPlayer) event.getEntity();
        if (player.world.isRemote) return;

        String damageType = getDisplayDamageType(event.getSource());

        // 1. 为每件具有该附魔的盔甲增加适应点数
        for (ItemStack armor : player.getArmorInventoryList()) {
            int level = EnchantmentAdaptation.getLevel(armor);
            if (level > 0) {
                EnchantmentAdaptation.addAdaptationPoint(armor, damageType);
            }
        }

        // 2. 计算总减伤并应用
        float totalReduction = EnchantmentAdaptation.getTotalReduction(player, damageType);
        if (totalReduction > 0) {
            float newDamage = event.getAmount() * (1 - totalReduction);
            event.setAmount(newDamage);
        }
    }

    private String getDisplayDamageType(DamageSource source) {
        if (source.getTrueSource() instanceof net.minecraft.entity.EntityLivingBase) {
            net.minecraft.entity.EntityLivingBase entity = (net.minecraft.entity.EntityLivingBase) source.getTrueSource();
            ResourceLocation regName = net.minecraft.entity.EntityList.getKey(entity);
            if (regName != null) return regName.toString();
            else return entity.getName();
        } else {
            return source.getDamageType();
        }
    }
}