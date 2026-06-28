package yc.ycqin.nb.mixins.confusionApply;


import com.dhanantry.scapeandrunparasites.entity.EntityOrbBoom;
import com.dhanantry.scapeandrunparasites.entity.ai.misc.EntityParasiteBase;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.init.SoundEvents;
import net.minecraft.util.DamageSource;
import net.minecraft.util.math.AxisAlignedBB;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import yc.ycqin.nb.register.PotionsRegister;

import java.util.List;


@Mixin(value = EntityOrbBoom.class,remap = false)
public abstract class MixinOrbBoomSelfExplode {

    @Shadow(remap = false) private com.dhanantry.scapeandrunparasites.entity.ai.misc.EntityPMalleable father;
    @Shadow(remap = false) protected int timerDDD;

    @Inject(method = "selfExplode", at = @At("HEAD"), cancellable = true)
    private void onSelfExplode(CallbackInfo ci) {
        EntityOrbBoom self = (EntityOrbBoom)(Object)this;
        if (self.world.isRemote) return;
        if (father == null) return;
        boolean confused = father.isPotionActive(PotionsRegister.CONFUSION);
        // 完全重写爆炸逻辑
        if (timerDDD > 1) {
            float f = self.width / 2.0F;
            float f1 = self.height;
            AxisAlignedBB aabb = new AxisAlignedBB(self.posX - f, self.posY - f1, self.posZ - f,
                    self.posX + f, self.posY + f1, self.posZ + f);
            for (EntityLivingBase mob : self.world.getEntitiesWithinAABB(EntityLivingBase.class, aabb)) {
                if (!(mob instanceof EntityParasiteBase) || (confused && mob != father)) {
                    father.attackEntityAsMobMinimum(mob, father.getMiniDamage() * 5.0F);
                }
            }
            self.playSound(SoundEvents.ENTITY_GENERIC_EXPLODE, 1.0F, 1.0F);
            self.setDead();
        }
        ci.cancel();
    }

    @Inject(method = "orbDoing", at = @At("HEAD"), cancellable = true)
    private void onOrbDoing(CallbackInfo ci) {
        EntityOrbBoom self = (EntityOrbBoom) (Object) this;
        // 检查混乱效果
        boolean confused = father != null && father.isPotionActive(PotionsRegister.CONFUSION);
        if (!confused) {
            return;
        }

        // 混乱逻辑：复制原方法并修改条件
        if (!self.world.isRemote) {
            if (self.ticksExisted % 10 == 0) {
                float f = self.width / 2.0F;
                float f1 = self.height;
                AxisAlignedBB aabb = new AxisAlignedBB(
                        self.posX - f, self.posY - f1, self.posZ - f,
                        self.posX + f, self.posY + f1, self.posZ + f
                );
                List<EntityLivingBase> moblist = self.world.getEntitiesWithinAABB(EntityLivingBase.class, aabb);
                if (father != null) {
                    for (EntityLivingBase mob : moblist) {
                        father.attackEntityAsMob(mob);
                    }
                } else {
                    for (EntityLivingBase mob : moblist) {
                        mob.attackEntityFrom(DamageSource.MAGIC, 10.0F);
                    }
                }
            }
        }
        ci.cancel(); // 取消原方法执行
    }
}