package yc.ycqin.nb.mixins.confusionApply.entityMob;

import com.dhanantry.scapeandrunparasites.entity.ai.misc.EntityPPure;
import com.dhanantry.scapeandrunparasites.entity.monster.pure.EntityEsor;
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

@Mixin(value = EntityEsor.class,remap = false)
public abstract class MixinEsorAOEAttack extends EntityPPure {

    @Shadow(remap = false) private boolean up;
    @Shadow(remap = false) private float attackTimer;

    public MixinEsorAOEAttack(World worldIn) {
        super(worldIn);
    }

    @Inject(method = "attackEntityAsMobAOE", at = @At("HEAD"), cancellable = true)
    private void onAttackEntityAsMobAOE(Entity entityIn, CallbackInfoReturnable<Boolean> cir) {
        EntityEsor self = (EntityEsor)(Object)this;

        // 仅当拥有混乱效果时修改行为
        if (!self.isPotionActive(PotionsRegister.CONFUSION)) {
            return; // 原逻辑
        }

        // 混乱状态：重写整个方法，允许攻击所有生物（包括寄生虫）
        if (this.borderOrb != 0) {
            cir.setReturnValue(false);
            return;
        }

        this.up = true;
        this.attackTimer = 0.0F;
        self.world.setEntityState(self, (byte)12);
        self.playSound(SRPSounds.SWIPE, 2.0F, 1.0F);

        AxisAlignedBB aabb = new AxisAlignedBB(
                entityIn.posX, entityIn.posY, entityIn.posZ,
                entityIn.posX + 1.0, entityIn.posY + 1.0, entityIn.posZ + 1.0
        ).grow(2.0);

        boolean flag = false;
        for (EntityLivingBase mob : self.world.getEntitiesWithinAABB(EntityLivingBase.class, aabb)) {
            if (mob != self && self.canEntityBeSeen(mob)) {
                if (self.attackEntityAsMob(mob)) {
                    flag = true;
                }
            }
        }
        cir.setReturnValue(flag);
    }
}