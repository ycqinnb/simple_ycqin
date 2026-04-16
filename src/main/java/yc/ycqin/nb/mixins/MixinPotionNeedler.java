package yc.ycqin.nb.mixins;

import com.dhanantry.scapeandrunparasites.potion.PotionNeedler;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.EntityEquipmentSlot;
import net.minecraft.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyArg;
import yc.ycqin.nb.common.trait.armorTrait.TraitMinDamageProtect;
import yc.ycqin.nb.proxy.CommonProxy;
import yc.ycqin.nb.register.ModEnchantments;

@Mixin(PotionNeedler.class)
public class MixinPotionNeedler {

    @ModifyArg(
            method = "effectNeedler",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/entity/EntityLivingBase;func_70606_j(F)V"
            ),
            index = 0,
            remap = false
    )
    private float modifySetHealthArgument(float newHealth) {
        // 注意：@ModifyArg 的参数顺序是：先原方法参数，再目标方法参数
        // 这里 newHealth 是即将传入 setHealth 的值
        EntityLivingBase entity = (EntityLivingBase)(Object)this;
        float current = entity.getHealth();
        if (newHealth < current) {
            float damage = current - newHealth;
            return current - applyMinimumDamageReduction(damage, entity);   // 返回修改后的新生命值
        }
        return newHealth;
    }

    private float applyMinimumDamageReduction(float damage, EntityLivingBase target) {
        // 你的减伤逻辑（从胸甲附魔读取）
        if (!(target instanceof EntityPlayer)) return damage;
        EntityPlayer player = (EntityPlayer) target;
        ItemStack chest = player.getItemStackFromSlot(EntityEquipmentSlot.CHEST);
        if (chest.isEmpty()) return damage;
        int level;
        if (CommonProxy.isTCArmorLoaded){
            level = EnchantmentHelper.getEnchantmentLevel(ModEnchantments.MIN_DAMAGE_PROTECT, chest) + TraitMinDamageProtect.getTotalProtectionLevel(target);
        } else {
            level = EnchantmentHelper.getEnchantmentLevel(ModEnchantments.MIN_DAMAGE_PROTECT, chest);
        }
        if (level > 0) {
            return damage - level;
        }
        return damage;
    }
}