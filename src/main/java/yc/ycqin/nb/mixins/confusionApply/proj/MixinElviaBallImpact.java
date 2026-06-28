package yc.ycqin.nb.mixins.confusionApply.proj;

import com.dhanantry.scapeandrunparasites.entity.ai.misc.EntityParasiteBase;

import com.dhanantry.scapeandrunparasites.entity.projectile.EntityProjectileElviaBall;
import com.dhanantry.scapeandrunparasites.entity.projectile.EntitySRPProjectile;
import com.dhanantry.scapeandrunparasites.util.SRPAttributes;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.util.DamageSource;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import yc.ycqin.nb.register.PotionsRegister;

@Mixin(value = EntityProjectileElviaBall.class,remap = false)
public abstract class MixinElviaBallImpact extends EntitySRPProjectile {

    public MixinElviaBallImpact(World worldIn) {
        super(worldIn);
    }

    @Inject(method = "func_70227_a", at = @At("HEAD"), cancellable = true)
    private void onImpact(RayTraceResult result, CallbackInfo ci) {
        // 检查射击者是否为寄生虫且拥有混乱效果
        if (shootingEntity instanceof EntityParasiteBase && ((EntityParasiteBase) shootingEntity).isPotionActive(PotionsRegister.CONFUSION)) {
            // 混乱状态下，允许攻击任何生物（包括寄生虫）
            if (result.entityHit instanceof EntityLivingBase) {
                DamageSource damagesource;
                if (shootingEntity == null) {
                    damagesource = DamageSource.causeThrownDamage(this, this);
                } else {
                    damagesource = DamageSource.causeThrownDamage(this, shootingEntity);
                }
                result.entityHit.attackEntityFrom(damagesource, (float) SRPAttributes.ELVIA_ATTACK_DAMAGE);
                this.attackEntityAsMobMinimum((EntityLivingBase) result.entityHit, (EntityParasiteBase) shootingEntity);
            }
            // 移除弹射物
            ((EntityProjectileElviaBall) (Object) this).setDead();
            ci.cancel();
        }
        // 否则继续执行原方法
    }
}