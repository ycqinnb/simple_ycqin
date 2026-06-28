package yc.ycqin.nb.mixins.confusionApply.entityMob;

import com.dhanantry.scapeandrunparasites.entity.ai.misc.EntityPPrimitive;
import com.dhanantry.scapeandrunparasites.entity.monster.primitive.EntityShyco;
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

@Mixin(value = EntityShyco.class,remap = false)
public abstract class MixinEntityShyco extends EntityPPrimitive {

    @Shadow(remap = false) private boolean up;
    @Shadow(remap = false) private float attackTimer;

    public MixinEntityShyco(World worldIn) {
        super(worldIn);
    }

    /**
     * 修改范围攻击逻辑，当拥有混乱效果时允许攻击同类寄生虫。
     * 原方法：只攻击非寄生虫生物。
     * 混乱时：攻击所有非自身生物（包括寄生虫）。
     */
    @Inject(method = "attackEntityAsMobAOE", at = @At("HEAD"), cancellable = true)
    private void onAttackAOE(Entity entityIn, CallbackInfoReturnable<Boolean> cir) {
        EntityShyco self = (EntityShyco) (Object) this;
        // 如果没有混乱效果，直接放行原方法
        if (!self.isPotionActive(PotionsRegister.CONFUSION)) {
            return;
        }

        // 混乱状态下的自定义攻击逻辑
        if (borderOrb != 0) {
            cir.setReturnValue(false);
            return;
        }

        up = true;
        attackTimer = 0.0F;
        self.world.setEntityState(self, (byte) 12);
        self.playSound(SRPSounds.SWIPE, 2.0F, 1.0F);

        // 以目标为中心，半径 1.5 格的 AABB
        AxisAlignedBB aabb = new AxisAlignedBB(
                entityIn.posX, entityIn.posY, entityIn.posZ,
                entityIn.posX + 1.0, entityIn.posY + 1.0, entityIn.posZ + 1.0
        ).grow(1.5);

        boolean flag = false;
        for (EntityLivingBase mob : self.world.getEntitiesWithinAABB(EntityLivingBase.class, aabb)) {
            if (mob == self) continue;               // 不攻击自己
            if (self.canEntityBeSeen(mob)) {
                // 直接调用原版攻击（会触发 attackEntityAsMobMinimum，我们已经修改过混乱伤害）
                if (self.attackEntityAsMob(mob)) {
                    flag = true;
                }
            }
        }

        cir.setReturnValue(flag);
    }
}