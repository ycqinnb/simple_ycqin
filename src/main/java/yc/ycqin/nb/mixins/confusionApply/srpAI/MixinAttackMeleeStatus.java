package yc.ycqin.nb.mixins.confusionApply.srpAI;

import com.dhanantry.scapeandrunparasites.entity.ai.EntityAIAttackMeleeStatus;
import com.dhanantry.scapeandrunparasites.entity.ai.misc.EntityParasiteBase;
import net.minecraft.entity.EntityLivingBase;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import yc.ycqin.nb.register.PotionsRegister;

@Mixin(value = EntityAIAttackMeleeStatus.class,remap = false)
public abstract class MixinAttackMeleeStatus {

    @Shadow
    protected EntityParasiteBase attacker;

    @Inject(method = "isTargetParasite", at = @At("HEAD"), cancellable = true, remap = false)
    private void onIsTargetParasite(EntityLivingBase target, CallbackInfoReturnable<Boolean> cir) {
        if (attacker != null && attacker.isPotionActive(PotionsRegister.CONFUSION)) {
            // 混乱状态下允许攻击同类，返回 false（不是目标寄生虫）
            cir.setReturnValue(false);
        }
    }
}