package yc.ycqin.nb.mixins.confusionApply.proj;

import com.dhanantry.scapeandrunparasites.entity.ai.misc.EntityCanPullMobs;
import com.dhanantry.scapeandrunparasites.entity.ai.misc.EntityParasiteBase;
import com.dhanantry.scapeandrunparasites.entity.projectile.EntityProjectilePullball;
import com.dhanantry.scapeandrunparasites.entity.monster.crude.EntityLeer;
import com.dhanantry.scapeandrunparasites.entity.projectile.EntitySRPProjectile;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.init.Blocks;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import yc.ycqin.nb.register.PotionsRegister;

@Mixin(value = EntityProjectilePullball.class,remap = false)
public abstract class MixinProjectilePullball extends EntitySRPProjectile {

    @Shadow(remap = false)
    private EntityCanPullMobs spider;

    public MixinProjectilePullball(World worldIn) {
        super(worldIn);
    }

    @Inject(method = "func_70071_h_", at = @At("HEAD"), cancellable = true)
    private void onUpdate(CallbackInfo ci) {
        // 检查施法者是否有混乱效果
        boolean confused = shootingEntity != null && shootingEntity.isPotionActive(PotionsRegister.CONFUSION);
        EntityProjectilePullball self = (EntityProjectilePullball) (Object) this;
        if (!confused) return;
        if (self.world.isRemote) {
            return;
        }

        if (self.ticksExisted == 5) {
            self.motionX *= spider.getAcceleration();
            self.motionZ *= spider.getAcceleration();
        }

        if (shootingEntity == null) {
            self.setDead();
            ci.cancel();
            return;
        }

        if (spider.hasTargetedEntity() && !(shootingEntity instanceof EntityLeer)) {
            self.setDead();
            ci.cancel();
            return;
        }

        AxisAlignedBB aabb = new AxisAlignedBB(self.posX, self.posY, self.posZ,
                self.posX + 1.0, self.posY + 1.0, self.posZ + 1.0).grow(2.0);

        for (EntityLivingBase mob : self.world.getEntitiesWithinAABB(EntityLivingBase.class, aabb)) {
            // 修改条件：混乱时允许所有目标，否则只允许非寄生虫
            if ((confused || !(mob instanceof EntityParasiteBase))
                    && shootingEntity.canEntityBeSeen(mob)
                    && spider.checkAttackTarget(mob)
                    && mob.isEntityAlive()) {
                spider.setPStatus(3);
                spider.setPullingMobEffects(mob);
                spider.setTargetedEntity(mob.getEntityId());
                spider.resetPullSkill();
                self.setDead();
                ci.cancel();
                return;
            }
        }

        ci.cancel();
    }
}
