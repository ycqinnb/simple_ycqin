package yc.ycqin.nb.mixins.confusionApply.proj;

import com.dhanantry.scapeandrunparasites.entity.projectile.EntityProjectileLenciaBall;
import com.dhanantry.scapeandrunparasites.entity.ai.misc.EntityParasiteBase;
import com.dhanantry.scapeandrunparasites.entity.monster.deterrent.EntityNak;
import com.dhanantry.scapeandrunparasites.entity.projectile.EntitySRPProjectile;
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

@Mixin(value = EntityProjectileLenciaBall.class,remap = false)
public abstract class MixinEntityProjectileLenciaBall extends EntitySRPProjectile {

    public MixinEntityProjectileLenciaBall(World worldIn) {
        super(worldIn);
    }

    /**
     * 修改 onImpact (func_70227_a) 方法：混乱时允许攻击寄生虫（包括 EntityNak）
     */
    @Inject(method = "func_70227_a", at = @At("HEAD"), cancellable = true)
    private void onImpact(RayTraceResult result, CallbackInfo ci) {
        EntityProjectileLenciaBall self = (EntityProjectileLenciaBall)(Object)this;
        boolean confused = (shootingEntity instanceof EntityParasiteBase) &&
                ((EntityParasiteBase)shootingEntity).isPotionActive(PotionsRegister.CONFUSION);

        if (!confused) return; // 非混乱时原方法执行

        // 混乱时完全替换 impact 逻辑（复制原代码并修改寄生虫判断）
        if (!self.world.isRemote) {
            if (result.entityHit != null && result.entityHit instanceof EntityLivingBase) {
                // 原条件：如果是寄生虫且不是 Nak，则直接返回不伤害
                // 修改后：混乱时允许伤害所有寄生虫（包括 Nak）
                DamageSource damagesource = (shootingEntity == null) ? DamageSource.causeThrownDamage(self, self) : DamageSource.causeThrownDamage(self, shootingEntity);
                result.entityHit.attackEntityFrom(damagesource, (float)com.dhanantry.scapeandrunparasites.util.SRPAttributes.LENCIA_ATTACK_DAMAGE);
                // 调用基类的最小伤害攻击（可选）
                if (shootingEntity instanceof EntityParasiteBase) {
                    this.attackEntityAsMobMinimum((EntityLivingBase)result.entityHit, (EntityParasiteBase)shootingEntity);
                }
            }

            // 创建爆炸（无论是否击中，原逻辑都执行）
            boolean flag = net.minecraftforge.event.ForgeEventFactory.getMobGriefingEvent(self.world, self) &&
                    com.dhanantry.scapeandrunparasites.util.config.SRPConfigMobs.lenciaGriefing;
            com.dhanantry.scapeandrunparasites.util.ParasiteEventEntity.createExplosion(self.world, shootingEntity, self.posX, self.posY, self.posZ, 10.0F, flag);
            self.setDead();
        }
        ci.cancel();
    }
}