package yc.ycqin.nb.mixins;

import com.dhanantry.scapeandrunparasites.entity.ai.misc.EntityParasiteBase;
import com.dhanantry.scapeandrunparasites.entity.monster.derived.EntityHeblu;
import com.dhanantry.scapeandrunparasites.entity.monster.derived.EntityKirin;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityList;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.EntityEquipmentSlot;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.common.FMLLog;
import net.minecraftforge.fml.common.registry.EntityEntry;
import net.minecraftforge.fml.common.registry.EntityRegistry;
import net.minecraftforge.fml.common.registry.ForgeRegistries;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyVariable;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import yc.ycqin.nb.common.trait.armorTrait.TraitMinDamageProtect;
import yc.ycqin.nb.config.ModConfig;
import yc.ycqin.nb.enchantment.EnchantmentMinDamageProtect;
import yc.ycqin.nb.proxy.CommonProxy;
import yc.ycqin.nb.register.ModEnchantments;
import yc.ycqin.nb.register.PotionsRegister;

import java.util.Objects;

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
        int level;
        if (CommonProxy.isTCArmorLoaded) {
            level = EnchantmentHelper.getEnchantmentLevel(ModEnchantments.MIN_DAMAGE_PROTECT, chest) + TraitMinDamageProtect.getTotalProtectionLevel(target);
        } else {
            level = EnchantmentHelper.getEnchantmentLevel(ModEnchantments.MIN_DAMAGE_PROTECT, chest);
        }
        if (level > 0) {
            // 每级减少 1 点最小伤害，最低为 0
            level = (int) (level * ModConfig.MinDamageProtectMultiplierPerLevel);
            float newMinDamage = minimumDamage - level;
            return newMinDamage;
        }

        return minimumDamage;
    }

    /**
     * 当寄生虫自身拥有混乱效果时，允许攻击同类（其他寄生虫）
     */
    @Inject(method = "func_70686_a", at = @At("HEAD"), cancellable = true,remap = false)
    private void onCanAttack(Class<? extends EntityLivingBase> targetClass, CallbackInfoReturnable<Boolean> cir) {
        EntityParasiteBase self = (EntityParasiteBase) (Object) this;
        // 检查混乱效果
        if (self.isPotionActive(PotionsRegister.CONFUSION)) {
            // 获取目标类的注册名
            EntityEntry entry = ForgeRegistries.ENTITIES.getValue(EntityList.getKey(targetClass));
            if (entry != null) {
                String name = entry.getRegistryName().toString();
                // 如果是寄生虫模组的实体（注册名包含 srparasites），则允许攻击
                if (name.startsWith("srparasites")) {
                    if ((self instanceof EntityHeblu && Objects.equals(targetClass.toString(), "com.dhanantry.scapeandrunparasites.entity.monster.derived.EntityHeblu")) || (self instanceof EntityKirin && Objects.equals(targetClass.toString(), "com.dhanantry.scapeandrunparasites.entity.monster.derived.EntityKirin"))){
                        cir.setReturnValue(false);
                    }
                    cir.setReturnValue(true);
                }
            }
        }
    }
}