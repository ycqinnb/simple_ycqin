package yc.ycqin.nb.mixins.confusionApply;

import com.dhanantry.scapeandrunparasites.entity.ai.misc.EntityPMalleable;
import com.dhanantry.scapeandrunparasites.entity.ai.misc.EntityParasiteBase;
import com.dhanantry.scapeandrunparasites.entity.ai.misc.EntityPCosmical;
import com.dhanantry.scapeandrunparasites.init.SRPPotions;
import com.dhanantry.scapeandrunparasites.util.config.SRPConfig;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.MobEffects;
import net.minecraft.potion.PotionEffect;
import net.minecraft.util.CooldownTracker;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import yc.ycqin.nb.register.PotionsRegister;

@Mixin(EntityPMalleable.class)
public abstract class MixinScaryOrbEffect extends EntityParasiteBase {
    @Shadow(remap = false) protected int orbItemCool;


    public MixinScaryOrbEffect(World worldIn) {
        super(worldIn);
    }

    @Inject(method = "scaryOrbEffect", at = @At("HEAD"), cancellable = true, remap = false)
    private void onScaryOrbEffect(EntityLivingBase target, int totalMobs, CallbackInfoReturnable<Boolean> cir) {
        EntityPMalleable self = (EntityPMalleable)(Object)this;
        boolean confused = self.isPotionActive(PotionsRegister.CONFUSION);
        if (!confused) return;
        // 玩家处理逻辑
        if (target instanceof EntityPlayer) {
            EntityPlayer player = (EntityPlayer)target;
            if (player.capabilities.disableDamage) {
                cir.setReturnValue(false);
                return;
            }
            player.addExhaustion(this.foodSteal * (float)SRPConfig.orbFoodMult);
            if (this.orbItemCool > 0) {
                CooldownTracker tracker = player.getCooldownTracker();
                for (int i = 0; i < player.inventory.mainInventory.size(); ++i) {
                    net.minecraft.item.ItemStack stack = player.inventory.mainInventory.get(i);
                    if (!stack.isEmpty() && tracker.getCooldown(stack.getItem(), 1.0F) == 0.0F) {
                        tracker.setCooldown(stack.getItem(), this.orbItemCool);
                    }
                }
            }
            this.attackEntityAsMobFood(player, false, this.foodRootNumber, this.foodRott);
        } else {
            if (target == self) {
                cir.setReturnValue(false);
                return;
            }
            if (target instanceof EntityPCosmical && ((EntityPCosmical)target).getCloneC()) {
                cir.setReturnValue(false);
                return;
            }
        }

        target.addPotionEffect(new PotionEffect(SRPPotions.COTH_E, 1200, 3, false, false));
        // 原版条件：不是寄生虫时攻击
        // 修改后：混乱 或 不是寄生虫时攻击
        if (confused || !(target instanceof EntityParasiteBase)) {
            this.attackEntityAsMobMinimum(target, this.getMiniDamage() * 0.5F);
        }

        SRPPotions.applyStackPotion(MobEffects.POISON, target, 100, 0);
        cir.setReturnValue(true);
    }
}
