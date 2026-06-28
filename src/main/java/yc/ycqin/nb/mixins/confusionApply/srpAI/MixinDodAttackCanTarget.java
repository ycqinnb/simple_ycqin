package yc.ycqin.nb.mixins.confusionApply.srpAI;

import com.dhanantry.scapeandrunparasites.entity.ai.EntityAIDodAttack;
import com.dhanantry.scapeandrunparasites.entity.ai.misc.EntityPDispatcher;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.passive.EntityAnimal;
import net.minecraft.entity.monster.EntityCreeper;
import net.minecraft.entity.passive.EntityWaterMob;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import yc.ycqin.nb.register.PotionsRegister;

@Mixin(value = EntityAIDodAttack.class,remap = false)
public abstract class MixinDodAttackCanTarget {

    @Shadow(remap = false)
    private EntityPDispatcher parent;

    @Inject(method = "canTargetEntity", at = @At("HEAD"), cancellable = true, remap = false)
    private void onCanTargetEntity(EntityLivingBase in, CallbackInfoReturnable<Boolean> cir) {
        if (parent != null && parent.isPotionActive(PotionsRegister.CONFUSION)) {
            // 混乱时允许攻击所有目标（包括寄生虫），保留排除动物、苦力怕等可选
            boolean result = !(in instanceof EntityAnimal) && !(in instanceof EntityCreeper) && !(in instanceof EntityWaterMob);
            cir.setReturnValue(result);
        }
    }
}