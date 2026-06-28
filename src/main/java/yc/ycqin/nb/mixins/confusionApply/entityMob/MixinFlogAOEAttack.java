package yc.ycqin.nb.mixins.confusionApply.entityMob;

import com.dhanantry.scapeandrunparasites.entity.monster.pure.EntityFlog;
import com.dhanantry.scapeandrunparasites.entity.ai.misc.EntityParasiteBase;
import com.dhanantry.scapeandrunparasites.init.SRPSounds;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.util.math.AxisAlignedBB;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import yc.ycqin.nb.register.PotionsRegister;

@Mixin(value = EntityFlog.class,remap = false)
public abstract class MixinFlogAOEAttack {

    @Shadow private boolean up;
    @Shadow private float attackTimer;

    @Inject(method = "attackEntityAsMobAOE", at = @At("HEAD"), cancellable = true)
    private void onAttackAOE(Entity entityIn, CallbackInfoReturnable<Boolean> cir) {
        EntityFlog self = (EntityFlog)(Object)this;
        boolean confused = self.isPotionActive(PotionsRegister.CONFUSION);
        if (!confused) return;
        // 设置攻击动画
        this.up = true;
        this.attackTimer = 0.0F;
        self.world.setEntityState(self, (byte)12);

        boolean flag = false;

        self.playSound(SRPSounds.SWIPE, 2.0F, 1.0F);

        AxisAlignedBB aabb = new AxisAlignedBB(
                entityIn.posX, entityIn.posY, entityIn.posZ,
                entityIn.posX + 1.0, entityIn.posY + 1.0, entityIn.posZ + 1.0
        ).grow(2.0);

        for (EntityLivingBase mob : self.world.getEntitiesWithinAABB(EntityLivingBase.class, aabb)) {
            if (mob == self) continue;

            if (mob instanceof EntityParasiteBase) {
                if (confused) {
                    // 混乱状态：攻击所有寄生虫，无论是否是当前目标
                    if (self.canEntityBeSeen(mob) && self.func_70652_k(mob)) {
                        flag = true;
                    }
                } else {
                    // 原版逻辑：如果是当前攻击目标，清除目标并返回失败
                    if (self.getAttackTarget() == mob) {
                        self.setAttackTarget(null);
                        cir.setReturnValue(false);
                        return;
                    }
                    // 非当前目标直接跳过，不攻击
                }
            } else {
                // 非寄生虫：原逻辑攻击
                if (mob != self && self.canEntityBeSeen(mob) && self.func_70652_k(mob)) {
                    flag = true;
                }
            }
        }
        cir.setReturnValue(flag);
    }
}