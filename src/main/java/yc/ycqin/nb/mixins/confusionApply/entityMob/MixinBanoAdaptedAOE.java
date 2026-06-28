package yc.ycqin.nb.mixins.confusionApply.entityMob;

import com.dhanantry.scapeandrunparasites.entity.ai.misc.EntityPMalleable;
import com.dhanantry.scapeandrunparasites.entity.monster.adapted.EntityBanoAdapted;
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

@Mixin(value = EntityBanoAdapted.class,remap = false)
public abstract class MixinBanoAdaptedAOE extends EntityPMalleable {

    @Shadow(remap = false) private float attackTimer;
    @Shadow(remap = false) private boolean up;

    public MixinBanoAdaptedAOE(World worldIn) {
        super(worldIn);
    }

    @Inject(method = "attackEntityAsMobAOE", at = @At("HEAD"), cancellable = true)
    private void onAttackAOE(Entity entityIn, CallbackInfoReturnable<Boolean> cir) {
        EntityBanoAdapted self = (EntityBanoAdapted)(Object)this;
        if (self.world.isRemote) return;

        // 检查混乱效果
        boolean confused = self.isPotionActive(PotionsRegister.CONFUSION);
        if (!confused) return; // 非混乱状态走原方法

        if (borderOrb != 0) {
            cir.setReturnValue(false);
            return;
        }
        up = true;
        attackTimer = 0.0F;
        self.world.setEntityState(self, (byte)12);

        // 范围攻击逻辑：以目标为中心半径 2 格
        AxisAlignedBB aabb = new AxisAlignedBB(
                entityIn.posX, entityIn.posY, entityIn.posZ,
                entityIn.posX + 1.0, entityIn.posY + 1.0, entityIn.posZ + 1.0
        ).grow(2.0);
        boolean flag = false;
        for (EntityLivingBase mob : self.world.getEntitiesWithinAABB(EntityLivingBase.class, aabb)) {
            if (mob == self) continue;
            if (self.canEntityBeSeen(mob)) {
                // 混乱状态下允许攻击所有生物（包括寄生虫）
                if (self.attackEntityAsMob(mob)) {
                    flag = true;
                }
            }
        }
        cir.setReturnValue(flag);
    }
}