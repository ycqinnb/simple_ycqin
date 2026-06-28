package yc.ycqin.nb.mixins.confusionApply.entityMob;

import com.dhanantry.scapeandrunparasites.entity.ai.misc.EntityPFocused;
import com.dhanantry.scapeandrunparasites.entity.ai.misc.EntityParasiteBase;
import com.dhanantry.scapeandrunparasites.entity.monster.focused.EntityShycoFocused;
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

@Mixin(value = EntityShycoFocused.class,remap = false)
public abstract class MixinShycoFocusedAttack extends EntityPFocused {

    @Shadow(remap = false) private boolean up;
    @Shadow(remap = false) private float attackTimer;

    public MixinShycoFocusedAttack(World worldIn) {
        super(worldIn);
    }

    @Inject(method = "attackEntityAsMobAOE", at = @At("HEAD"), cancellable = true)
    private void onAttackEntityAsMobAOE(Entity entityIn, CallbackInfoReturnable<Boolean> cir) {
        EntityShycoFocused self = (EntityShycoFocused)(Object)this;
        // 检查混乱效果
        if (!self.isPotionActive(PotionsRegister.CONFUSION)) {
            // 没有混乱效果，正常执行原方法
            return;
        }
        // 混乱状态，自定义逻辑（完全复制原方法并修改伤害条件）
        if (this.borderOrb != 0) {
            cir.setReturnValue(false);
            return;
        }
        this.up = true;
        this.attackTimer = 0.0F;
        self.world.setEntityState(self, (byte)12);
        self.playSound(SRPSounds.SWIPE, 2.0F, 1.0F);
        AxisAlignedBB aabb = new AxisAlignedBB(entityIn.posX, entityIn.posY, entityIn.posZ,
                entityIn.posX + 1.0, entityIn.posY + 1.0, entityIn.posZ + 1.0).grow(3.0);
        boolean flag = false;
        for (EntityLivingBase mob : self.world.getEntitiesWithinAABB(EntityLivingBase.class, aabb)) {
            if (mob == self) continue;
            // 原版逻辑：寄生虫只处理当前目标，这里混乱状态下允许攻击所有寄生虫
            if (mob instanceof EntityParasiteBase) {
                // 混乱时攻击所有寄生虫（不限制当前目标）
                if (self.canEntityBeSeen(mob) && self.attackEntityAsMob(mob)) {
                    flag = true;
                }
            } else if (mob != self && self.canEntityBeSeen(mob) && self.attackEntityAsMob(mob)) {
                flag = true;
            }
        }
        cir.setReturnValue(flag);
    }
}
