package yc.ycqin.nb.mixins.confusionApply.srpAI;

import com.dhanantry.scapeandrunparasites.entity.ai.EntityAIFlightAttack;
import com.dhanantry.scapeandrunparasites.entity.ai.misc.EntityParasiteBase;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import yc.ycqin.nb.register.PotionsRegister;

@Mixin(value = EntityAIFlightAttack.class,remap = false)
public abstract class MixinFlightAttackTarget {

    @Shadow(remap = false) protected abstract boolean canAttackPlayer(EntityPlayer in);
    @Shadow(remap = false) private EntityParasiteBase parent;
    @Shadow(remap = false) protected abstract boolean canTargetEntity(EntityLivingBase in);

    @Redirect(method = "attackSurr", at = @At(value = "INVOKE", target = "Lcom/dhanantry/scapeandrunparasites/entity/ai/EntityAIFlightAttack;canTargetEntity(Lnet/minecraft/entity/EntityLivingBase;)Z"))
    private boolean redirectCanTarget(EntityAIFlightAttack ai, EntityLivingBase target) {
        if (parent.isPotionActive(PotionsRegister.CONFUSION) && target != parent) {
            return true; // 混乱时允许任何生物为目标
        }
        return canTargetEntity(target);
    }

    @Shadow private int delay;
    @Shadow private double distance;
    @Shadow private boolean xRay;
    @Shadow protected abstract void attackSurr();

    @Inject(method = "func_75246_d", at = @At("HEAD"), cancellable = true)
    private void onUpdate(CallbackInfo ci) {
        boolean confused = this.parent.isPotionActive(PotionsRegister.CONFUSION);
        this.parent.reduceIdleChance(2, 1);
        if (this.parent.getAttackTarget() != null) {
            // 修改原条件：如果混乱则不跳过寄生虫目标
            if (!confused && this.parent.getAttackTarget() instanceof EntityParasiteBase) {
                this.parent.setAttackTarget(null);
                this.delay = 0;
                this.parent.setParasiteStatus(0);
                if (this.parent.getParasiteType() != 61) {
                    this.parent.getMoveHelper().setMoveTo(this.parent.posX, this.parent.posY, this.parent.posZ, 1.0);
                }
                ci.cancel();
                return;
            }
            if (this.parent.world.getEntityByID(this.parent.getAttackTarget().getEntityId()) == null) {
                this.parent.setAttackTarget(null);
                this.delay = 0;
                this.parent.setParasiteStatus(0);
                if (this.parent.getParasiteType() != 61) {
                    this.parent.getMoveHelper().setMoveTo(this.parent.posX, this.parent.posY, this.parent.posZ, 1.0);
                }
                ci.cancel();
                return;
            }
            EntityLivingBase target = this.parent.getAttackTarget();
            if (target instanceof EntityPlayer && !canAttackPlayer((EntityPlayer)target)) {
                this.parent.setAttackTarget(null);
                this.delay = 0;
                this.parent.setParasiteStatus(0);
                ci.cancel();
                return;
            }
            if (!this.parent.canEntityBeSeen(target)) {
                if (this.xRay) {
                    if (this.parent.getRNG().nextInt(5) == 0) {
                        ++this.delay;
                    }
                } else {
                    ++this.delay;
                }
            } else if (target.getDistanceSq(this.parent) >= this.distance) {
                ++this.delay;
            } else {
                this.delay = 0;
            }
            if (this.delay >= 6) {
                this.parent.setAttackTarget(null);
                this.parent.setParasiteStatus(0);
                this.delay = 0;
                if (this.parent.getParasiteType() != 61) {
                    this.parent.getMoveHelper().setMoveTo(this.parent.posX, this.parent.posY, this.parent.posZ, 1.0);
                }
            }
        } else {
            this.attackSurr();
        }
        ci.cancel();
    }
}
