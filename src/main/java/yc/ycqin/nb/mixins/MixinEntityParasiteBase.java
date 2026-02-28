package yc.ycqin.nb.mixins;

import com.dhanantry.scapeandrunparasites.entity.ai.misc.EntityParasiteBase;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.EntityEquipmentSlot;
import net.minecraft.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;
import yc.ycqin.nb.enchantment.EnchantmentMinDamageProtect;
import yc.ycqin.nb.register.ModEnchantments;

@Mixin(EntityParasiteBase.class)
public abstract class MixinEntityParasiteBase {

    /**
     * 在 attackEntityAsMobMinimum 方法开头修改 minimumDamage 参数的值
     */
    @ModifyVariable(
            method = "attackEntityAsMobMinimum",
            at = @At("HEAD"),
            argsOnly = true, // 声明修改的是方法参数，而非局部变量
            remap = false
    )
    private float modifyMinimumDamage(float minimumDamage, EntityLivingBase target) {
        // 仅当目标是玩家时生效
        if (!(target instanceof EntityPlayer)) {
            return minimumDamage;
        }

        EntityPlayer player = (EntityPlayer) target;
        ItemStack chest = player.getItemStackFromSlot(EntityEquipmentSlot.CHEST);
        if (chest.isEmpty()) {
            return minimumDamage;
        }

        int level = EnchantmentHelper.getEnchantmentLevel(ModEnchantments.MIN_DAMAGE_PROTECT, chest);
        if (level > 0) {
            // 每级减少 1 点最小伤害，最低为 0
            float newMinDamage = Math.max(0.0F, minimumDamage - level);
            return newMinDamage;
        }

        return minimumDamage;
    }
}