package yc.ycqin.nb.mixins.confusionApply.proj;

import com.dhanantry.scapeandrunparasites.entity.projectile.EntityProjectileDragonE;
import com.dhanantry.scapeandrunparasites.entity.ai.misc.EntityParasiteBase;
import com.dhanantry.scapeandrunparasites.entity.EntityToxicCloud;
import com.dhanantry.scapeandrunparasites.entity.projectile.EntitySRPProjectile;
import com.dhanantry.scapeandrunparasites.init.SRPPotions;
import com.dhanantry.scapeandrunparasites.util.SRPAttributes;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.init.MobEffects;
import net.minecraft.potion.PotionEffect;
import net.minecraft.util.DamageSource;
import net.minecraft.util.EnumParticleTypes;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import yc.ycqin.nb.register.PotionsRegister;

@Mixin(value = EntityProjectileDragonE.class,remap = false)
public abstract class MixinProjectileDragonE extends EntitySRPProjectile {

    public MixinProjectileDragonE(World worldIn) {
        super(worldIn);
    }

    /**
     * 在 func_70227_a (onImpact) 开头注入，如果射击者拥有混乱效果则使用修改后的逻辑
     */
    @Inject(method = "func_70227_a", at = @At("HEAD"), cancellable = true)
    private void onImpact(RayTraceResult result, CallbackInfo ci) {
        // 检查射击者是否存在且拥有混乱效果
        if (shootingEntity == null || !(shootingEntity instanceof EntityParasiteBase)) return;
        EntityParasiteBase shooter = (EntityParasiteBase) shootingEntity;
        if (!shooter.isPotionActive(PotionsRegister.CONFUSION)) return;

        // ---- 混乱状态下重写整个 onImpact 逻辑 ----
        EntityProjectileDragonE self = (EntityProjectileDragonE) (Object) this;
        if (self.world.isRemote) {
            ci.cancel();
            return;
        }

        // 范围伤害（允许攻击所有生物）
        AxisAlignedBB aabb = new AxisAlignedBB(self.posX, self.posY, self.posZ,
                self.posX + 1.0, self.posY + 1.0, self.posZ + 1.0).grow(4.0);
        for (EntityLivingBase mob : self.world.getEntitiesWithinAABB(EntityLivingBase.class, aabb)) {
            // 混乱后移除了 !(mob instanceof EntityParasiteBase) 的限制，改为所有生物都受到伤害
            // 但原版有特殊处理 EntityNak（这里也保留，但混乱下可以不管，为保持一致，保留原条件但添加混乱绕过）
            // 因为混乱下我们希望攻击所有，所以直接无条件造成伤害
            // 同时保留原版的病毒效果和伤害
            SRPPotions.applyStackPotion(SRPPotions.VIRA_E, mob, 200, 1);
            DamageSource damagesource;
            if (shootingEntity == null) {
                damagesource = DamageSource.causeThrownDamage(self, self);
            } else {
                damagesource = DamageSource.causeThrownDamage(self, shootingEntity);
            }
            mob.attackEntityFrom(damagesource, (float) SRPAttributes.INFDRAGONE_RANGED_ATTACK_DAMAGE);
            this.attackEntityAsMobMinimum(mob, shooter);
        }

        // 生成毒雾（与原逻辑一致）
        EntityToxicCloud cloud = new EntityToxicCloud(self.world, self.posX, self.posY, self.posZ);
        cloud.setRadius(3.5F, 0.5F);
        cloud.setRadiusOnUse(-0.5F);
        cloud.setWaitTime(10);
        cloud.setDuration(100);
        cloud.setParticle(EnumParticleTypes.DRAGON_BREATH);
        cloud.setRadiusPerTick(-cloud.getRadius() / (float) cloud.getDuration());
        cloud.addEffect(new PotionEffect(MobEffects.POISON, 300, 0));
        cloud.addEffect(new PotionEffect(MobEffects.WITHER, 1, 1));
        self.world.spawnEntity(cloud);

        // 销毁弹射物
        self.setDead();

        ci.cancel();
    }
}
