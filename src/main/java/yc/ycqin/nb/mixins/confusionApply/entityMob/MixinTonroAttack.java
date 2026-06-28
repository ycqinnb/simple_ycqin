package yc.ycqin.nb.mixins.confusionApply.entityMob;

import com.dhanantry.scapeandrunparasites.entity.ai.misc.EntityParasiteBase;
import com.dhanantry.scapeandrunparasites.entity.monster.deterrent.EntityTonro;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.MoverType;
import net.minecraft.util.math.AxisAlignedBB;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import yc.ycqin.nb.register.PotionsRegister;

@Mixin(value = EntityTonro.class,remap = false)
public abstract class MixinTonroAttack {

    @Shadow private float attackTimer;
    @Shadow private boolean up;

    /**
     * 重写 attackEntityAsMobAOE 方法，添加混乱检查
     */
    @Inject(method = "attackEntityAsMobAOE", at = @At("HEAD"), cancellable = true)
    private void onAttackAOE(Entity entityIn, CallbackInfoReturnable<Boolean> cir) {
        EntityTonro self = (EntityTonro)(Object)this;
        // 如果没有混乱效果，走原方法
        if (!self.isPotionActive(PotionsRegister.CONFUSION)) {
            return;
        }

        // 混乱状态下，允许攻击同类（移除 instanceof 限制）
        this.up = true;
        this.attackTimer = 0.0F;
        self.world.setEntityState(self, (byte)12);
        self.playSound(com.dhanantry.scapeandrunparasites.init.SRPSounds.SWIPE, 3.0F, 1.0F);
        AxisAlignedBB aabb = new AxisAlignedBB(entityIn.posX, entityIn.posY, entityIn.posZ,
                entityIn.posX + 1.0, entityIn.posY + 1.0, entityIn.posZ + 1.0).grow(2.0);
        boolean flag = false;
        for (EntityLivingBase mob : self.world.getEntitiesWithinAABB(EntityLivingBase.class, aabb)) {
            if (mob != self && self.canEntityBeSeen(mob) && !(mob instanceof EntityParasiteBase) || self.isPotionActive(PotionsRegister.CONFUSION)) {
                // 原条件：!(mob instanceof EntityParasiteBase)
                // 修改后：混乱时允许所有生物，否则只允许非寄生虫
                if (self.attackEntityAsMob(mob)) {
                    flag = true;
                }
            }
        }
        cir.setReturnValue(flag);
    }
}