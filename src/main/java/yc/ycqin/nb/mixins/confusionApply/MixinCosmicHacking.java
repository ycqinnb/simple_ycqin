package yc.ycqin.nb.mixins.confusionApply;

import com.dhanantry.scapeandrunparasites.entity.ai.misc.EntityPCosmical;
import com.dhanantry.scapeandrunparasites.entity.ai.misc.EntityParasiteBase;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.math.AxisAlignedBB;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import yc.ycqin.nb.register.PotionsRegister;

@Mixin(value = EntityPCosmical.class,remap = false)
public abstract class MixinCosmicHacking {

    @Shadow(remap = false) private int borderHack;
    @Shadow(remap = false) protected abstract void setTargetedEntity(int entityId);
    @Shadow(remap = false) protected abstract boolean fullTargets();
    @Shadow(remap = false) protected abstract void resetTargets();

    @Inject(method = "cosmicHacking", at = @At("HEAD"), cancellable = true)
    private void onCosmicHacking(CallbackInfo ci) {
        EntityPCosmical self = (EntityPCosmical)(Object)this;
        if (self.world.isRemote) return;

        boolean confused = self.isPotionActive(PotionsRegister.CONFUSION);
        // 检查条件：有目标，不是克隆体，在地面，处于阴影状态
        if (self.getAttackTarget() == null || self.getCloneC() || !self.onGround || !self.getShadowStatus()) {
            return;
        }

        self.setParasiteStatus(20);
        self.getNavigator().clearPath();
        if (self.ticksExisted % 20 == 0) {
            borderHack++;
            if (borderHack >= 3) {
                if (!fullTargets()) {
                    AxisAlignedBB aabb = new AxisAlignedBB(self.posX, self.posY, self.posZ, self.posX+1, self.posY+1, self.posZ+1).grow(24);
                    for (EntityLivingBase mob : self.world.getEntitiesWithinAABB(EntityLivingBase.class, aabb)) {
                        if (mob != self && mob.isEntityAlive()) {
                            // 原条件：不是寄生虫 则加入
                            // 修改后：如果混乱，则允许所有生物；否则只允许非寄生虫
                            if (confused || !(mob instanceof EntityParasiteBase)) {
                                if (mob instanceof EntityPlayer && ((EntityPlayer)mob).isSpectator()) continue;
                                setTargetedEntity(mob.getEntityId());
                            }
                        }
                    }
                }
            } else {
                self.shadowDamageR = 0.6F;
                self.shadowDamageRCooldown = 40;
                self.world.setEntityState(self, (byte)41);
                self.particleStatus((byte)7);
            }
            if (borderHack >= 7) {
                resetTargets();
                self.setFinished((byte)33, true);
                self.setParasiteStatus(0);
                self.setInvisible(false);
                borderHack = 0;
            }
        }
        ci.cancel();
    }
}