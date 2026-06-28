package yc.ycqin.nb.mixins.confusionApply.entityMob;

import com.dhanantry.scapeandrunparasites.entity.monster.primitive.EntityWymo;
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

@Mixin(value = EntityWymo.class,remap = false)
public abstract class MixinWymoAOE {

    @Shadow(remap = false) private boolean up;
    @Shadow(remap = false) private float attackTimer;

    @Inject(method = "attackEntityAsMobAOE", at = @At("HEAD"), cancellable = true)
    private void onAttackAOE(Entity entityIn, CallbackInfoReturnable<Boolean> cir) {
        EntityWymo self = (EntityWymo)(Object)this;
        // 如果没有混乱效果，则走原方法
        if (!self.isPotionActive(PotionsRegister.CONFUSION)) {
            return;
        }
        this.up = true;
        this.attackTimer = 0.0F;
        self.world.setEntityState(self, (byte)12);
        self.playSound(SRPSounds.SWIPE, 2.0F, 1.0F);
        AxisAlignedBB aabb = new AxisAlignedBB(entityIn.posX, entityIn.posY, entityIn.posZ,
                entityIn.posX + 1.0, entityIn.posY + 1.0, entityIn.posZ + 1.0).grow(1.5);
        boolean flag = false;
        for (EntityLivingBase mob : self.world.getEntitiesWithinAABB(EntityLivingBase.class, aabb)) {
            // 原条件为 !(mob instanceof EntityParasiteBase) && 视线检查 && 攻击成功
            // 现在混乱时去掉寄生虫限制，直接尝试攻击所有非自身的目标
            if (mob != self && self.canEntityBeSeen(mob) && self.func_70652_k(mob)) {
                flag = true;
            }
        }
        cir.setReturnValue(flag);
    }
}