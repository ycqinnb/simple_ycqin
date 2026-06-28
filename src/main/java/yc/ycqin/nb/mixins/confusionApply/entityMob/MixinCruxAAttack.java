package yc.ycqin.nb.mixins.confusionApply.entityMob;

import com.dhanantry.scapeandrunparasites.entity.monster.crude.EntityCruxA;
import com.dhanantry.scapeandrunparasites.entity.ai.misc.EntityParasiteBase;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.util.math.AxisAlignedBB;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import yc.ycqin.nb.register.PotionsRegister;

@Mixin(value = EntityCruxA.class,remap = false)
public abstract class MixinCruxAAttack {

    @Shadow(remap = false) private boolean upM;
    @Shadow(remap = false) private float attackTimerM;

    @Inject(method = "attackEntityAsMobAOE", at = @At("HEAD"), cancellable = true)
    private void onAttackAOE(Entity entityIn, CallbackInfoReturnable<Boolean> cir) {
        EntityCruxA self = (EntityCruxA)(Object)this;
        boolean confused = self.isPotionActive(PotionsRegister.CONFUSION);
        if (!confused) return;
        // 原方法开始的几行
        this.upM = true;
        this.attackTimerM = 0.0F;
        self.world.setEntityState(self, (byte)22);
        self.playSound(com.dhanantry.scapeandrunparasites.init.SRPSounds.SWIPE, 2.0F, 1.0F);

        AxisAlignedBB aabb = new AxisAlignedBB(entityIn.posX, entityIn.posY, entityIn.posZ,
                entityIn.posX + 1.0, entityIn.posY + 1.0, entityIn.posZ + 1.0).grow(1.0);
        boolean flag = false;

        for (EntityLivingBase mob : self.world.getEntitiesWithinAABB(EntityLivingBase.class, aabb)) {
            if (mob == self) continue;
            if (!self.canEntityBeSeen(mob)) continue;

            boolean isParasite = mob instanceof EntityParasiteBase;
            if (isParasite && !confused) {
                // 原逻辑：如果是当前目标则清除并返回失败，否则跳过
                if (self.getAttackTarget() == mob) {
                    self.setAttackTarget(null);
                    cir.setReturnValue(false);
                    return;
                }
                // 否则不攻击，继续循环
            } else {
                // 非寄生虫 或者 混乱状态下的寄生虫，尝试攻击
                if (self.attackEntityAsMob(mob)) {
                    flag = true;
                }
            }
        }
        cir.setReturnValue(flag);
    }
}