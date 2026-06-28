package yc.ycqin.nb.mixins.confusionApply.entityMob;

import com.dhanantry.scapeandrunparasites.entity.monster.focused.EntityBanoFocused;
import com.dhanantry.scapeandrunparasites.entity.ai.misc.EntityParasiteBase;
import com.dhanantry.scapeandrunparasites.init.SRPSounds;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.init.MobEffects;
import net.minecraft.potion.PotionEffect;
import net.minecraft.util.math.AxisAlignedBB;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import yc.ycqin.nb.register.PotionsRegister;

@Mixin(value = EntityBanoFocused.class,remap = false)
public abstract class MixinBanoFocusedSmash {

    @Shadow(remap = false) private int border;
    @Shadow(remap = false) private boolean skillEnraged;

    /**
     * 重写 smash 技能，混乱时允许攻击寄生虫同类
     */
    @Inject(method = "smash", at = @At("HEAD"), cancellable = true)
    private void onSmash(CallbackInfo ci) {
        EntityBanoFocused self = (EntityBanoFocused)(Object)this;
        if (self.world.isRemote) return;

        boolean confused = self.isPotionActive(PotionsRegister.CONFUSION);
        if (!confused) return;
        // 复制原 smash 逻辑，修改友伤判断
        if (!self.onGround) {
            self.setFinished((byte)1, true);
            self.setParasiteStatus(0);
            border = 0;
            ci.cancel();
            return;
        }

        if (border == 2) {
            float v = self.getRNG().nextFloat() * 0.4F + 1.0F;
            self.playSound(SRPSounds.ATTACKBANO, 4.0F, v);
        }

        if (border < 20) {
            self.world.setEntityState(self, (byte)100);
            self.setParasiteStatus(25);
            self.getNavigator().clearPath();
            self.addPotionEffect(new PotionEffect(MobEffects.SLOWNESS, 110, 100, false, false));
        }

        border++;
        if (border >= 20) {
            self.setParasiteStatus(3);
            if (border == 20) {
                self.playSound(SRPSounds.SWIPE, 5.0F, 1.0F);
            }
            if (border % 7 == 0) {
                self.playSound(SRPSounds.SWIPE, 5.0F, 1.0F);
            }
            AxisAlignedBB aabb = new AxisAlignedBB(self.posX, self.posY, self.posZ,
                    self.posX + 1.0, self.posY + 1.0, self.posZ + 1.0).grow(7.0, 3.0, 7.0);
            for (EntityLivingBase mob : self.world.getEntitiesWithinAABB(EntityLivingBase.class, aabb)) {
                if (mob != self && self.canEntityBeSeen(mob)) {
                    // 原条件：!(mob instanceof EntityParasiteBase)
                    // 修改后：混乱时允许任何生物
                    if (confused || !(mob instanceof EntityParasiteBase)) {
                        self.attackEntityAsMob(mob);
                        mob.knockBack(mob, 2.0F, mob.posX - self.posX, mob.posZ - self.posZ);
                    }
                }
            }
        }

        if (border > 100) {
            self.setFinished((byte)1, true);
            self.setParasiteStatus(0);
            border = 0;
        }

        ci.cancel();
    }
}