package yc.ycqin.nb.mixins.confusionApply.proj;

import com.dhanantry.scapeandrunparasites.entity.ai.misc.EntityParasiteBase;
import com.dhanantry.scapeandrunparasites.entity.projectile.EntityProjectileAncientball;
import com.dhanantry.scapeandrunparasites.entity.monster.deterrent.EntityNak;
import com.dhanantry.scapeandrunparasites.entity.projectile.EntitySRPProjectile;
import com.dhanantry.scapeandrunparasites.init.SRPPotions;
import com.dhanantry.scapeandrunparasites.util.SRPAttributes;
import net.minecraft.entity.EntityAreaEffectCloud;
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

@Mixin(value = EntityProjectileAncientball.class,remap = false)
public abstract class MixinProjectileAncientball extends EntitySRPProjectile {


    public MixinProjectileAncientball(World worldIn) {
        super(worldIn);
    }

    @Inject(method = "func_70227_a", at = @At("HEAD"), cancellable = true)
    private void onImpact(RayTraceResult result, CallbackInfo ci) {
        EntityProjectileAncientball self = (EntityProjectileAncientball) (Object) this;
        World world = self.world;
        if (world.isRemote) {
            ci.cancel();
            return;
        }

        EntityLivingBase shooter = this.shootingEntity;
        boolean confused = (shooter instanceof EntityParasiteBase) && ((EntityParasiteBase) shooter).isPotionActive(PotionsRegister.CONFUSION);
        if (!confused) return;

        // 处理直接击中的实体
        if (result.entityHit != null && result.entityHit instanceof EntityLivingBase) {
            EntityLivingBase target = (EntityLivingBase) result.entityHit;
            // 造成伤害
            float damage = SRPAttributes.ORONCO_ATTACK_DAMAGE;
            DamageSource damageSource = (shooter == null) ? DamageSource.causeThrownDamage(self, self) : DamageSource.causeThrownDamage(self, shooter);
            target.attackEntityFrom(damageSource, damage);
            target.addPotionEffect(new PotionEffect(MobEffects.INSTANT_DAMAGE, 60, 0));

            // 调用最小伤害攻击（如果射手是寄生虫）
            if (shooter instanceof EntityParasiteBase) {
                this.attackEntityAsMobMinimum(target, ((EntityParasiteBase) shooter));
            }
        }

        // 生成区域效果云
        EntityAreaEffectCloud cloud = new EntityAreaEffectCloud(world, self.posX, self.posY, self.posZ);
        cloud.setRadius(self.width * 4.0F);
        cloud.setRadiusOnUse(-0.5F);
        cloud.setWaitTime(5);
        cloud.setDuration(cloud.getDuration() / 2);
        cloud.setRadiusPerTick(-cloud.getRadius() / (float) cloud.getDuration());
        cloud.addEffect(new PotionEffect(MobEffects.INSTANT_DAMAGE, 300, 0));
        cloud.addEffect(new PotionEffect(SRPPotions.COTH_E, 3600, 0, false, false));
        world.spawnEntity(cloud);

        self.setDead();
        ci.cancel();
    }
}
