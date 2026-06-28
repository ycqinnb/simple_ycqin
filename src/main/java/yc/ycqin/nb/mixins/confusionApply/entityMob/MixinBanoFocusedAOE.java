package yc.ycqin.nb.mixins.confusionApply.entityMob;

import com.dhanantry.scapeandrunparasites.entity.ai.misc.EntityPMalleable;
import com.dhanantry.scapeandrunparasites.entity.monster.focused.EntityBanoFocused;
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

@Mixin(value = EntityBanoFocused.class,remap = false)
public abstract class MixinBanoFocusedAOE extends EntityPMalleable {

    @Shadow(remap = false) private boolean up;
    @Shadow(remap = false) private float attackTimer;

    public MixinBanoFocusedAOE(World worldIn) {
        super(worldIn);
    }

    /**
     * 重写攻击 AOE 方法，混乱时允许攻击寄生虫同类
     */
    @Inject(method = "attackEntityAsMobAOE", at = @At("HEAD"), cancellable = true)
    private void onAttackAOE(Entity entityIn, CallbackInfoReturnable<Boolean> cir) {
        EntityBanoFocused self = (EntityBanoFocused)(Object)this;
        if (self.world.isRemote) return;

        // 检查混乱效果
        boolean confused = self.isPotionActive(PotionsRegister.CONFUSION);
        if (!confused) return; // 不混乱时走原方法

        // 完全重写 AOE 攻击逻辑
        if (this.borderOrb != 0) {
            cir.setReturnValue(false);
            return;
        }
        up = true;
        attackTimer = 0.0F;
        self.world.setEntityState(self, (byte)12);
        AxisAlignedBB aabb = new AxisAlignedBB(
                entityIn.posX, entityIn.posY, entityIn.posZ,
                entityIn.posX + 1.0, entityIn.posY + 1.0, entityIn.posZ + 1.0
        ).grow(2.0);
        boolean flag = false;
        for (EntityLivingBase mob : self.world.getEntitiesWithinAABB(EntityLivingBase.class, aabb)) {
            if (mob == self) continue;
            if (self.canEntityBeSeen(mob) && self.attackEntityAsMob(mob)) {
                flag = true;
            }
        }
        cir.setReturnValue(flag);
    }

}