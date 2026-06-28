package yc.ycqin.nb.mixins.confusionApply;

import com.dhanantry.scapeandrunparasites.entity.EntityDamage;
import com.dhanantry.scapeandrunparasites.entity.ai.misc.EntityParasiteBase;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.util.DamageSource;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import yc.ycqin.nb.register.PotionsRegister;

@Mixin(value = EntityDamage.class,remap = false)
public abstract class MixinEntityDamage {

    @Shadow protected float mobDamage;
    @Shadow protected boolean pulling;
    @Shadow protected float str;
    @Shadow public abstract EntityLivingBase getCaster();
    @Shadow private void knockBack2(net.minecraft.entity.Entity entityIn, float strength, double xRatio, double zRatio) {}

    @Inject(method = "damage", at = @At("HEAD"), cancellable = true)
    private void onDamage(EntityLivingBase target, CallbackInfo ci) {
        EntityLivingBase caster = getCaster();
        boolean confused = caster != null && caster.isPotionActive(PotionsRegister.CONFUSION);
        // 原条件是 !(target instanceof EntityParasiteBase)
        // 新条件：混乱时无视类型，否则仅攻击非寄生虫
        if ((confused || !(target instanceof EntityParasiteBase)) && target.isEntityAlive() && !target.getIsInvulnerable() && target != caster) {
            if (caster == null) {
                target.attackEntityFrom(DamageSource.GENERIC.setDamageBypassesArmor(), mobDamage);
            } else {
                if (caster.isRidingOrBeingRiddenBy(target)) {
                    ci.cancel();
                    return;
                }
                if (pulling) {
                    if (target instanceof net.minecraft.entity.player.EntityPlayer) {
                        knockBack2(target, str, target.posX - caster.posX, target.posZ - caster.posZ);
                    } else {
                        knockBack2(target, str / 3.0F, target.posX - caster.posX, target.posZ - caster.posZ);
                    }
                } else {
                    knockBack2(target, str, caster.posX - target.posX, caster.posZ - target.posZ);
                }
                caster.attackEntityAsMob(target);
            }
        }
        ci.cancel(); // 阻止原方法执行
    }
}