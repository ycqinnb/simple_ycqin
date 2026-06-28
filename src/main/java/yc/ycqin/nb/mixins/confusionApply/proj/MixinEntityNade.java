package yc.ycqin.nb.mixins.confusionApply.proj;
import com.dhanantry.scapeandrunparasites.entity.ai.misc.EntityParasiteBase;
import com.dhanantry.scapeandrunparasites.entity.projectile.EntityNade;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.SharedMonsterAttributes;
import net.minecraft.util.DamageSource;
import net.minecraft.util.math.AxisAlignedBB;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import yc.ycqin.nb.register.PotionsRegister;

@Mixin(value = EntityNade.class,remap = false)
public abstract class MixinEntityNade {

    @Shadow(remap = false) private EntityParasiteBase father;
    @Shadow(remap = false) private int timerDDD;
    @Shadow protected abstract void setSelfeState(int state);
    @Shadow protected abstract int getSelfeState();

    @Inject(method = "selfExplode", at = @At("HEAD"), cancellable = true)
    private void onSelfExplode(CallbackInfo ci) {
        EntityNade self = (EntityNade)(Object)this;
        if (self.world.isRemote) return;

        boolean confused = father != null && father.isPotionActive(PotionsRegister.CONFUSION);
        if (!confused) {
            // 没有混乱效果，走原方法逻辑
            return;
        }

        // 混乱状态下重写爆炸逻辑
        self.setSelfeState(2);
        if (self.getSelfeState() == 2) {
            ++timerDDD;
            if (!self.world.isRemote) {
                if (father != null && father.isEntityAlive()) {
                    float f = self.width / 2.0F;
                    float f1 = self.height;
                    AxisAlignedBB aabb = new AxisAlignedBB(
                            self.posX - f, self.posY, self.posZ - f,
                            self.posX + f, self.posY + f1, self.posZ + f
                    );
                    // 伤害所有生物（包括寄生虫）
                    for (EntityLivingBase mob : self.world.getEntitiesWithinAABB(EntityLivingBase.class, aabb)) {
                        mob.attackEntityFrom(DamageSource.GENERIC, (float)father.getEntityAttribute(SharedMonsterAttributes.ATTACK_DAMAGE).getBaseValue());
                        father.attackEntityAsMobMinimum(mob, father.getMiniDamage());
                    }
                }
            } else {
                int par = self.getFuseState();
                par += par / 2;
            }
            if (timerDDD > self.getStartState()) {
                self.setDead();
            }
        }
        ci.cancel();
    }
}