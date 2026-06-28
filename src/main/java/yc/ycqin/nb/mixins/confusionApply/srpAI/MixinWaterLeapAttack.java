package yc.ycqin.nb.mixins.confusionApply.srpAI;

import com.dhanantry.scapeandrunparasites.entity.ai.EntityAIWaterLeapAtTargetStatus;
import com.dhanantry.scapeandrunparasites.entity.ai.misc.EntityParasiteBase;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.SharedMonsterAttributes;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.MathHelper;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import yc.ycqin.nb.register.PotionsRegister;

@Mixin(EntityAIWaterLeapAtTargetStatus.class)
public abstract class MixinWaterLeapAttack {

    @Shadow(remap = false) private EntityParasiteBase leaper;
    @Shadow(remap = false) private EntityLivingBase leapTarget;
    @Shadow(remap = false) private float leapMotionY;
    @Shadow(remap = false) private float targetY;
    @Shadow(remap = false) private double jumpSpeed;
    @Shadow(remap = false) private int jCooldown;
    @Shadow(remap = false) private int jumpR;
    @Shadow(remap = false) private int attackTimer;
    @Shadow(remap = false) private int attacking;
    @Shadow(remap = false) private double targetX;
    @Shadow(remap = false) private double targetZ;

    @Inject(method = "func_75246_d", at = @At("HEAD"), cancellable = true,remap = false)
    private void onUpdateTask(CallbackInfo ci) {
        // 检查混乱状态
        boolean confused = leaper.isPotionActive(PotionsRegister.CONFUSION);
        if (!confused) {
            return; // 不混乱，正常执行原方法
        }

        // 混乱状态下执行修改后的逻辑（原方法复制并修改）
        if (leaper.getAttackTarget() != null && leaper.shouldWorkTask()) {
            if (!leaper.getAttackTarget().isDead && leaper.getParasiteStatus() <= 2) {
                EntityLivingBase entitylivingbase = leaper.getAttackTarget();
                ++attackTimer;
                if (attackTimer >= jCooldown && attacking == 0) {
                    ++attacking;
                    targetX = entitylivingbase.posX;
                    targetZ = entitylivingbase.posZ;
                    targetY = (float)(entitylivingbase.posY - leaper.posY) * 0.07F;
                    if (targetY <= 0.0F) {
                        targetY = 0.0F;
                    }
                }
            } else if (attackTimer > 0) {
                --attackTimer;
            }
        } else if (attackTimer > 0) {
            --attackTimer;
        }

        if (attacking >= 1) {
            ++attacking;
            if (attacking == 2 && leaper.onGround) {
                leaper.setParasiteStatus(10);
                if (leaper.getNavigator().getPath() != null) {
                    leaper.getNavigator().clearPath();
                }
                double d0 = targetX - leaper.posX;
                double d1 = targetZ - leaper.posZ;
                double f = MathHelper.sqrt(d0 * d0 + d1 * d1);
                leaper.motionY = (double)leapMotionY + (double)targetY;
                leaper.motionX += d0 / f * jumpSpeed * 0.9 + leaper.motionX * 0.3;
                leaper.motionZ += d1 / f * jumpSpeed * 0.9 + leaper.motionZ * 0.3;
            }

            if (attacking >= 3 && leaper.onGround) {
                if (jumpR != 0) {
                    float damage = (float)leaper.getEntityAttribute(SharedMonsterAttributes.ATTACK_DAMAGE).getAttributeValue();
                    AxisAlignedBB aabb = new AxisAlignedBB(leaper.posX, leaper.posY, leaper.posZ,
                            leaper.posX + 1.0, leaper.posY + 1.0, leaper.posZ + 1.0).grow(jumpR, 2.0, jumpR);
                    for (EntityLivingBase mob : leaper.world.getEntitiesWithinAABB(EntityLivingBase.class, aabb)) {
                        if (mob != leaper) {
                            mob.knockBack(mob, 2.5F, leaper.posX - mob.posX, leaper.posZ - mob.posZ);
                            leaper.attackEntityAsMob(mob);
                        }
                    }
                }
                attacking = 0;
                attackTimer = 0;
                leaper.setParasiteStatus(2);
            }
        }
        ci.cancel(); // 取消原方法执行
    }
}
