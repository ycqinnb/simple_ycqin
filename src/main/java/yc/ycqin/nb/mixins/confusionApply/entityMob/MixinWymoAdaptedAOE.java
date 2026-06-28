package yc.ycqin.nb.mixins.confusionApply.entityMob;

import com.dhanantry.scapeandrunparasites.entity.monster.adapted.EntityWymoAdapted;
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

@Mixin(value = EntityWymoAdapted.class,remap = false)
public abstract class MixinWymoAdaptedAOE {

    @Shadow private boolean up;
    @Shadow private float attackTimer;
    @Shadow public abstract boolean func_70652_k(Entity entity); // 攻击单个实体

    @Inject(method = "attackEntityAsMobAOE", at = @At("HEAD"), cancellable = true)
    private void onAttackAOE(Entity entityIn, CallbackInfoReturnable<Boolean> cir) {
        EntityWymoAdapted self = (EntityWymoAdapted)(Object)this;
        // 检查混乱效果
        if (!self.isPotionActive(PotionsRegister.CONFUSION)) {
            return; // 不混乱，执行原方法
        }

        this.up = true;
        this.attackTimer = 0.0F;
        self.world.setEntityState(self, (byte)12);
        self.playSound(SRPSounds.SWIPE, 2.0F, 1.0F);
        AxisAlignedBB aabb = new AxisAlignedBB(
                entityIn.posX, entityIn.posY, entityIn.posZ,
                entityIn.posX + 1.0, entityIn.posY + 1.0, entityIn.posZ + 1.0
        ).grow(1.5);
        boolean flag = false;
        for (EntityLivingBase mob : self.world.getEntitiesWithinAABB(EntityLivingBase.class, aabb)) {
            // 原条件：不是寄生虫 且 视线可见 且 攻击成功
            // 修改为：混乱时允许所有生物，否则只允许非寄生虫
            if (self.canEntityBeSeen(mob) && this.func_70652_k(mob)) {
                flag = true;
            }
        }
        cir.setReturnValue(flag);
    }
}