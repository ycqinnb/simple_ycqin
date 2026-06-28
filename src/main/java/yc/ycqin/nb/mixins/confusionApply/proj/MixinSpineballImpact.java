package yc.ycqin.nb.mixins.confusionApply.proj;

import com.dhanantry.scapeandrunparasites.entity.ai.misc.EntityParasiteBase;
import com.dhanantry.scapeandrunparasites.entity.monster.adapted.EntityEmanaAdapted;
import com.dhanantry.scapeandrunparasites.entity.monster.deterrent.EntityNak;
import com.dhanantry.scapeandrunparasites.entity.monster.primitive.EntityEmana;
import com.dhanantry.scapeandrunparasites.entity.projectile.EntityProjectileSpineball;
import com.dhanantry.scapeandrunparasites.entity.projectile.EntitySRPProjectile;
import com.dhanantry.scapeandrunparasites.util.ParasiteEventEntity;
import com.dhanantry.scapeandrunparasites.util.config.SRPConfig;
import com.dhanantry.scapeandrunparasites.util.config.SRPConfigSystems;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.init.MobEffects;
import net.minecraft.potion.PotionEffect;
import net.minecraft.util.DamageSource;
import net.minecraft.util.EnumParticleTypes;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import yc.ycqin.nb.register.PotionsRegister;

@Mixin(value = EntityProjectileSpineball.class,remap = false)
public abstract class MixinSpineballImpact extends EntitySRPProjectile {

    @Shadow(remap = false) private float damage;
    @Shadow(remap = false) private int duration;
    @Shadow(remap = false) private int amp;
    @Shadow(remap = false) private double item;
    @Shadow(remap = false) protected abstract void damageArmor(EntityLivingBase target, double percen);

    public MixinSpineballImpact(World worldIn) {
        super(worldIn);
    }

    @Inject(method = "func_70227_a", at = @At("HEAD"), cancellable = true)
    private void onImpact(RayTraceResult result, CallbackInfo ci) {
        // 只在服务端执行
        World world = ((EntityProjectileSpineball)(Object)this).world;
        if (world.isRemote) return;

        // 获取发射者
        EntityLivingBase shooter = this.shootingEntity;
        boolean confused = shooter instanceof EntityParasiteBase && ((EntityParasiteBase) shooter).isPotionActive(PotionsRegister.CONFUSION);

        // 如果没有混乱，走原方法
        if (!confused) return;

        // 混乱逻辑：允许伤害寄生虫
        if (result.entityHit != null && result.entityHit instanceof EntityLivingBase) {
            EntityLivingBase target = (EntityLivingBase) result.entityHit;
            // 注意：原方法中会跳过寄生虫（除 EntityNak），混乱时不再跳过
            // 但为了逻辑完整，我们仍然保持其他条件（例如实体存活等）
            boolean primitive = shooter instanceof EntityEmana;

            DamageSource damagesource;
            if (this.shootingEntity == null) {
                damagesource = DamageSource.causeThrownDamage((EntityProjectileSpineball)(Object)this, (EntityProjectileSpineball)(Object)this);
            } else {
                damagesource = DamageSource.causeThrownDamage((EntityProjectileSpineball)(Object)this, this.shootingEntity);
            }

            // 造成伤害
            target.attackEntityFrom(damagesource, this.damage);
            // 施加中毒效果
            target.addPotionEffect(new PotionEffect(MobEffects.POISON, this.duration, this.amp));
            // 调用最小伤害攻击（基类已支持混乱）
            this.attackEntityAsMobMinimum(target, (EntityParasiteBase) shooter);
            // 损坏护甲
            this.damageArmor(target, this.item);

            // 原始逻辑：如果目标是原始种且杀死后满足条件，进化
            if (target.getHealth() <= 0.0F && primitive && shooter.isEntityAlive()) {
                double k = ((EntityParasiteBase) shooter).getKillC();
                k++;
                ((EntityParasiteBase) shooter).setKillC(k);
                ((EntityParasiteBase) shooter).particleStatus((byte)5);
                if (k > SRPConfig.adaptedKills && ParasiteEventEntity.canSpawnNext) {
                    ParasiteEventEntity.spawnNext((EntityParasiteBase) shooter, new EntityEmanaAdapted(world), true, true);
                }
            }
        }

        // 销毁弹射物
        ((EntityProjectileSpineball)(Object)this).setDead();
        ci.cancel();
    }
}
