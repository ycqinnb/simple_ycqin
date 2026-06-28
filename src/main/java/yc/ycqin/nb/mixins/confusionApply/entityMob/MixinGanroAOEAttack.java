package yc.ycqin.nb.mixins.confusionApply.entityMob;

import com.dhanantry.scapeandrunparasites.entity.ai.misc.EntityPPure;
import com.dhanantry.scapeandrunparasites.entity.monster.pure.EntityGanro;
import com.dhanantry.scapeandrunparasites.entity.ai.misc.EntityParasiteBase;
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

@Mixin(value = EntityGanro.class,remap = false)
public abstract class MixinGanroAOEAttack extends EntityPPure {

    @Shadow(remap = false) private boolean up;
    @Shadow(remap = false) private float attackTimer;

    public MixinGanroAOEAttack(World worldIn) {
        super(worldIn);
    }

    @Inject(method = "attackEntityAsMobAOE", at = @At("HEAD"), cancellable = true)
    private void onAttackAOE(Entity entityIn, CallbackInfoReturnable<Boolean> cir) {
        EntityGanro self = (EntityGanro)(Object)this;
        // 混乱时重写逻辑
        boolean confused = self.isPotionActive(PotionsRegister.CONFUSION);
        if (!confused) {
            return; // 不混乱时走原方法
        }
        // 混乱时执行自定义逻辑
        if (borderOrb != 0) {
            cir.setReturnValue(false);
            return;
        }
        up = true;
        attackTimer = 0.0F;
        self.world.setEntityState(self, (byte)12);
        playSound(com.dhanantry.scapeandrunparasites.init.SRPSounds.SWIPE, 2.0F, 1.0F);
        AxisAlignedBB aabb = new AxisAlignedBB(entityIn.posX, entityIn.posY, entityIn.posZ,
                entityIn.posX + 1.0, entityIn.posY + 1.0, entityIn.posZ + 1.0).grow(2.0);
        boolean flag = false;
        for (EntityLivingBase mob : self.world.getEntitiesWithinAABB(EntityLivingBase.class, aabb)) {
            // 混乱时允许攻击所有生物（包括寄生虫），但排除自身
            if (mob == self) continue;
            if (mob instanceof EntityParasiteBase) {
                if (self.canEntityBeSeen(mob) && self.attackEntityAsMob(mob)) {
                    flag = true;
                }
            } else {
                // 非寄生虫，正常攻击
                if (self.canEntityBeSeen(mob) && self.attackEntityAsMob(mob)) {
                    flag = true;
                }
            }
        }
        cir.setReturnValue(flag);
    }
}