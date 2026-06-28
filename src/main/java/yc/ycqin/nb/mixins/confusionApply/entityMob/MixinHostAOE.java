package yc.ycqin.nb.mixins.confusionApply.entityMob;

import com.dhanantry.scapeandrunparasites.entity.monster.crude.EntityHost;
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

@Mixin(value = EntityHost.class,remap = false)
public abstract class MixinHostAOE {

    @Shadow(remap = false) private boolean up;
    @Shadow(remap = false) private float attackTimer;

    @Inject(method = "attackEntityAsMobAOE", at = @At("HEAD"), cancellable = true)
    private void onAttackAOE(Entity entityIn, CallbackInfoReturnable<Boolean> cir) {
        EntityHost self = (EntityHost)(Object)this;
        // 检查攻击者是否拥有混乱效果
        boolean confused = self.isPotionActive(PotionsRegister.CONFUSION);
        if (!confused) return;
        // 完全重写原方法，保留所有逻辑，仅修改友伤条件
        this.up = true;
        this.attackTimer = 0.0F;
        self.world.setEntityState(self, (byte)13);
        boolean flag = false;

        if (self.getBurrowed() && !(self.height < 1.0F)) {
            self.playSound(com.dhanantry.scapeandrunparasites.init.SRPSounds.SWIPE, 1.0F, 1.0F);
            self.playSound(com.dhanantry.scapeandrunparasites.init.SRPSounds.SWIPE, 1.0F, 1.25F);
            self.playSound(com.dhanantry.scapeandrunparasites.init.SRPSounds.SWIPE, 2.0F, 1.0F);

            AxisAlignedBB aabb = new AxisAlignedBB(self.posX, self.posY, self.posZ, self.posX + 1.0, self.posY + 1.0, self.posZ + 1.0).grow(3.0);
            for (EntityLivingBase mob : self.world.getEntitiesWithinAABB(EntityLivingBase.class, aabb)) {
                if (mob != self && self.canEntityBeSeen(mob)) {
                    if (self.attackEntityAsMob(mob)) {
                        flag = true;
                    }
                }
            }
            cir.setReturnValue(flag);
        } else {
            cir.setReturnValue(false);
        }
    }
}
