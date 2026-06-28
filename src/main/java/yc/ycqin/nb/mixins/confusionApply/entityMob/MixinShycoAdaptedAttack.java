package yc.ycqin.nb.mixins.confusionApply.entityMob;

import com.dhanantry.scapeandrunparasites.entity.ai.misc.EntityPMalleable;
import com.dhanantry.scapeandrunparasites.entity.monster.adapted.EntityShycoAdapted;
import com.dhanantry.scapeandrunparasites.init.SRPSounds;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import yc.ycqin.nb.register.PotionsRegister;

@Mixin(EntityShycoAdapted.class)
public abstract class MixinShycoAdaptedAttack extends EntityPMalleable {

    @Shadow(remap = false) private float attackTimer;
    @Shadow(remap = false) private boolean up;

    public MixinShycoAdaptedAttack(World worldIn) {
        super(worldIn);
    }

    @Inject(method = "attackEntityAsMobAOE", at = @At("HEAD"), cancellable = true,remap = false)
    private void onAttackAOE(Entity entityIn, CallbackInfoReturnable<Boolean> cir) {
        EntityShycoAdapted self = (EntityShycoAdapted)(Object)this;
        if (!self.isPotionActive(PotionsRegister.CONFUSION)) {
            return; // 不混乱时正常执行原方法
        }
        // 混乱后自定义范围攻击，允许攻击同类
        if (this.borderOrb != 0) {
            cir.setReturnValue(false);
            return;
        }
        up = true;
        attackTimer = 0.0F;
        self.world.setEntityState(self, (byte)12);
        self.playSound(SRPSounds.SWIPE, 2.0F, 1.0F);
        AxisAlignedBB aabb = new AxisAlignedBB(entityIn.posX, entityIn.posY, entityIn.posZ, entityIn.posX+1, entityIn.posY+1, entityIn.posZ+1).grow(3.0);
        boolean flag = false;
        for (EntityLivingBase mob : self.world.getEntitiesWithinAABB(EntityLivingBase.class, aabb)) {
            if (mob != self && self.canEntityBeSeen(mob)) {
                // 移除了原版的“如果是寄生虫且不是当前目标则跳过”的条件，允许攻击所有生物
                if (self.attackEntityAsMob(mob)) {
                    flag = true;
                }
            }
        }
        cir.setReturnValue(flag);
    }
}