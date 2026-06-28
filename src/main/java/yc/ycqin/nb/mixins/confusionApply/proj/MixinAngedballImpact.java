package yc.ycqin.nb.mixins.confusionApply.proj;

import com.dhanantry.scapeandrunparasites.entity.EntityToxicCloud;
import com.dhanantry.scapeandrunparasites.entity.ai.misc.EntityParasiteBase;
import com.dhanantry.scapeandrunparasites.entity.monster.deterrent.EntityNak;
import com.dhanantry.scapeandrunparasites.entity.projectile.EntityProjectileAngedball;
import com.dhanantry.scapeandrunparasites.entity.projectile.EntitySRPProjectile;
import com.dhanantry.scapeandrunparasites.init.SRPPotions;
import com.dhanantry.scapeandrunparasites.util.SRPAttributes;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.init.MobEffects;
import net.minecraft.potion.PotionEffect;
import net.minecraft.util.DamageSource;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import yc.ycqin.nb.register.PotionsRegister;

@Mixin(value = EntityProjectileAngedball.class,remap = false)
public abstract class MixinAngedballImpact extends EntitySRPProjectile {

    public MixinAngedballImpact(World worldIn) {
        super(worldIn);
    }

    @Inject(method = "func_70227_a", at = @At("HEAD"), cancellable = true)
    private void onImpact(RayTraceResult result, CallbackInfo ci) {
        // 检查发射者是否为寄生虫且拥有混乱效果
        if (!(shootingEntity instanceof EntityParasiteBase)) return; // 不是寄生虫发射，走原方法
        EntityParasiteBase shooter = (EntityParasiteBase) shootingEntity;
        if (!shooter.isPotionActive(PotionsRegister.CONFUSION)) return; // 无混乱，走原方法

        // 混乱状态：重写整个攻击逻辑，允许攻击所有目标（包括寄生虫）
        if (result.entityHit instanceof EntityLivingBase) {
            EntityLivingBase target = (EntityLivingBase) result.entityHit;
            if (target == shooter) return;
            // 即使是寄生虫（包括 EntityNak），混乱状态下也攻击
            DamageSource damageSource;
            if (shootingEntity == null) {
                damageSource = DamageSource.causeThrownDamage(this, this);
            } else {
                damageSource = DamageSource.causeThrownDamage(this, shootingEntity);
            }
            target.attackEntityFrom(damageSource, (float) SRPAttributes.ANGED_RANGED_ATTACK_DAMAGE);
            // 调用最小伤害攻击（确保额外效果）
            this.attackEntityAsMobMinimum(target, shooter);
        }
        // 生成毒雾（与原逻辑一致）
        EntityToxicCloud cloud = new EntityToxicCloud(shooter.world, shooter.posX, shooter.posY, shooter.posZ);
        cloud.setRadius(2.5F, 0.5F);
        cloud.setRadiusOnUse(-0.5F);
        cloud.setWaitTime(10);
        cloud.setDuration(100);
        cloud.setRadiusPerTick(-cloud.getRadius() / (float) cloud.getDuration());
        cloud.addEffect(new PotionEffect(MobEffects.POISON, 300, 0, false, false));
        cloud.addEffect(new PotionEffect(SRPPotions.CORRO_E, 100, 0, false, false));
        shooter.world.spawnEntity(cloud);
        // 销毁弹射物
        ((EntityProjectileAngedball) (Object) this).setDead();
        ci.cancel();
    }
}
