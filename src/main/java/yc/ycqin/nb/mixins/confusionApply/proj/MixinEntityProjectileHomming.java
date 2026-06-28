package yc.ycqin.nb.mixins.confusionApply.proj;

import com.dhanantry.scapeandrunparasites.entity.projectile.EntityProjectileHomming;
import com.dhanantry.scapeandrunparasites.entity.ai.misc.EntityParasiteBase;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.util.DamageSource;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import yc.ycqin.nb.register.PotionsRegister;

import java.util.List;

@Mixin(value = EntityProjectileHomming.class,remap = false)
public abstract class MixinEntityProjectileHomming {

    @Shadow private EntityLivingBase owner;
    @Shadow private Entity target;
    @Shadow private float damage;

    /**
     * 修改 onUpdate (func_70071_h_) 方法：混乱时允许攻击寄生虫
     */
    @Inject(method = "func_70071_h_", at = @At("HEAD"), cancellable = true)
    private void onUpdate(CallbackInfo ci) {
        EntityProjectileHomming self = (EntityProjectileHomming)(Object)this;
        World world = self.world;
        if (world.isRemote) return;

        // 检查混乱效果
        boolean confused = (owner instanceof EntityParasiteBase) &&
                ((EntityParasiteBase)owner).isPotionActive(PotionsRegister.CONFUSION);

        // 非混乱时，不干预，走原方法
        if (!confused) return;

        // --- 混乱时完全替换 onUpdate 逻辑（复制原代码并修改条件） ---
        // 原代码中目标更新和移动部分保持不变，只需修改伤害循环中的条件
        if (target == null) {
            self.setDead();
            ci.cancel();
            return;
        }

        // 移动控制（原逻辑）
        if (!target.isDead) {
            self.getMoveHelper().setMoveTo(target.posX, target.posY - target.height * 1.5, target.posZ, 1.5);
        }

        // 伤害检测：半径 2 格内所有非自身、非投射物实体
        AxisAlignedBB aabb = self.getEntityBoundingBox().grow(2.0);
        List<EntityLivingBase> moblist = world.getEntitiesWithinAABB(EntityLivingBase.class, aabb);
        boolean flag = false;

        for (EntityLivingBase mob : moblist) {
            if (mob == null || mob instanceof EntityProjectileHomming) continue;
            // 原条件：只攻击非寄生虫
            // 修改后：混乱时攻击所有生物（包括寄生虫）
            DamageSource damagesource = (owner == null) ? DamageSource.causeThrownDamage(self, self) : DamageSource.causeThrownDamage(self, owner);
            if (mob.attackEntityFrom(damagesource, damage)) {
                flag = true;
            }
        }

        if (flag) {
            self.setDead();
        }

        if (self.ticksExisted > 200) {
            self.setDead();
        }

        ci.cancel();
    }

    /**
     * 修改 bulletHit (func_70227_a) 方法：混乱时允许攻击寄生虫
     */
    @Inject(method = "bulletHit", at = @At("HEAD"), cancellable = true)
    private void onBulletHit(net.minecraft.util.math.RayTraceResult result, CallbackInfo ci) {
        EntityProjectileHomming self = (EntityProjectileHomming)(Object)this;
        boolean confused = (owner instanceof EntityParasiteBase) &&
                ((EntityParasiteBase)owner).isPotionActive(PotionsRegister.CONFUSION);

        if (!confused) return; // 非混乱时原方法执行

        // 混乱时替换 bulletHit 逻辑
        if (result.entityHit == null) {
            // 命中空气，产生烟尘粒子
            self.world.spawnParticle(net.minecraft.util.EnumParticleTypes.SMOKE_NORMAL,
                    self.posX, self.posY, self.posZ, 0, 0, 0);
        } else {
            // 命中实体，造成伤害（允许攻击寄生虫）
            if (!(result.entityHit instanceof EntityParasiteBase) || confused) {
                DamageSource damagesource = (owner == null) ? DamageSource.causeThrownDamage(self, self) : DamageSource.causeThrownDamage(self, owner);
                result.entityHit.attackEntityFrom(damagesource, damage);
            }
            self.setDead();
        }
        ci.cancel();
    }
}