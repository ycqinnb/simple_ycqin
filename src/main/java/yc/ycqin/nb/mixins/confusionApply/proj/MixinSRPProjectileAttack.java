package yc.ycqin.nb.mixins.confusionApply.proj;
import com.dhanantry.scapeandrunparasites.entity.ai.misc.EntityParasiteBase;
import com.dhanantry.scapeandrunparasites.entity.projectile.EntitySRPProjectile;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import yc.ycqin.nb.register.PotionsRegister;

@Mixin(value = EntitySRPProjectile.class,remap = false)
public abstract class MixinSRPProjectileAttack {

    /**
     * 修改 attackEntityAsMobMinimum 方法，在混乱时允许攻击寄生虫
     */
    @Inject(method = "attackEntityAsMobMinimum", at = @At("HEAD"), cancellable = true)
    private void onAttackEntityAsMobMinimum(Entity entityIn, EntityParasiteBase attacker, CallbackInfoReturnable<Boolean> cir) {
        // 检查攻击者是否拥有混乱效果
        if (attacker != null && attacker.isPotionActive(PotionsRegister.CONFUSION)) {
            // 混乱状态下，只要目标是活体，就进行攻击（不限制是否为寄生虫）
            if (entityIn instanceof EntityLivingBase && attacker.isEntityAlive()) {
                boolean result = attacker.attackEntityAsMobMinimum((EntityLivingBase) entityIn, attacker.getMiniDamage());
                cir.setReturnValue(result);
            } else {
                cir.setReturnValue(false);
            }
        }
        // 没有混乱效果则继续执行原方法，不做任何修改
    }
}
