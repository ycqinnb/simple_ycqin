package yc.ycqin.nb.mixins.confusionApply;

import com.dhanantry.scapeandrunparasites.entity.monster.EntityWaveShock;
import com.dhanantry.scapeandrunparasites.entity.ai.misc.EntityParasiteBase;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.util.math.AxisAlignedBB;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import yc.ycqin.nb.register.PotionsRegister;

@Mixin(value = EntityWaveShock.class,remap = false)
public abstract class MixinWaveShockAttack {

    @Shadow private EntityParasiteBase caster;
    @Shadow private int duration;
    @Shadow private double targetX, targetY, targetZ;

    @Inject(method = "func_70071_h_", at = @At("HEAD"), cancellable = true,remap = false)
    private void onUpdate(CallbackInfo ci) {
        EntityWaveShock self = (EntityWaveShock)(Object)this;
        if (self.world.isRemote) return;

        // 判断是否有混乱效果（通过 caster）
        boolean confused = this.caster != null && this.caster.isPotionActive(PotionsRegister.CONFUSION);

        // 复制原代码的核心部分，但修改条件
        if (self.getEntityWorld().isRemote) return;

        // 保留原来的移动逻辑（与原方法一致）
        if (self.ticksExisted > 20) {
            if (self.posX == self.prevPosX || self.posZ == self.prevPosZ) {
                self.setDead();
            }
            if (self.ticksExisted > 20 * duration) {
                self.setDead();
                return;
            }
        }

        self.skillBreakBlocks();
        float f = self.width / 2.0F;
        float f1 = self.height;
        AxisAlignedBB aabb = new AxisAlignedBB(
                self.posX - f, self.posY, self.posZ - f,
                self.posX + f, self.posY + f1, self.posZ + f
        ).grow(1.5, 0.2, 1.5);

        for (EntityLivingBase mob : self.world.getEntitiesWithinAABB(EntityLivingBase.class, aabb)) {
            boolean isParasite = mob instanceof EntityParasiteBase;
            // 修改条件：非寄生虫 或 (混乱且是寄生虫)
            if (!isParasite || (confused && isParasite && (mob != caster))) {
                self.func_70652_k(mob);
            }
        }

        // 移动逻辑
        if (self.getDistanceSq(targetX, targetY, targetZ) > 2.0) {
            self.getMoveHelper().setMoveTo(targetX, targetY, targetZ, 0.6);
        }
        if (self.posX == targetX && self.posZ == targetZ) {
            self.setDead();
        }

        // 取消原方法
        ci.cancel();
    }

}