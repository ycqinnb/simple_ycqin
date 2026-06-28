package yc.ycqin.nb.mixins.confusionApply;

import com.dhanantry.scapeandrunparasites.entity.EntityOrbScary;
import com.dhanantry.scapeandrunparasites.entity.ai.misc.EntityPMalleable;
import com.dhanantry.scapeandrunparasites.entity.ai.misc.EntityParasiteBase;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.init.SoundEvents;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import yc.ycqin.nb.register.PotionsRegister;

@Mixin(value = EntityOrbScary.class,remap = false)
public abstract class MixinOrbScaryExplode extends Entity {

    @Shadow private EntityPMalleable father;
    @Shadow private int timerDDD;

    public MixinOrbScaryExplode(World p_i1582_1_) {
        super(p_i1582_1_);
    }

    @Shadow protected abstract void setSelfeState(int state);
    @Shadow protected abstract int getSelfeState();
    @Shadow protected abstract void spawnParticles(net.minecraft.util.EnumParticleTypes particle);

    @Inject(method = "selfExplode", at = @At("HEAD"), cancellable = true,remap = false)
    private void onSelfExplode(CallbackInfo ci) {
        EntityOrbScary self = (EntityOrbScary)(Object)this;
        self.setSelfeState(2);
        if (self.getSelfeState() == 2) {
            this.timerDDD++;
            if (this.timerDDD > 35) {
                this.setSize(Math.max(0.1F, self.width - 0.8F), Math.max(0.1F, self.height - 0.32F));
                if (!self.world.isRemote) {
                    if (this.father != null) {
                        float f = self.width / 2.0F;
                        float f1 = self.height;
                        AxisAlignedBB aabb = new AxisAlignedBB(
                                self.posX - f, self.posY - f1, self.posZ - f,
                                self.posX + f, self.posY + f1, self.posZ + f
                        );
                        boolean confused = this.father.isPotionActive(PotionsRegister.CONFUSION);
                        for (EntityLivingBase mob : self.world.getEntitiesWithinAABB(EntityLivingBase.class, aabb)) {
                            if (!(mob instanceof EntityParasiteBase) || (confused && (mob != this.father))) {
                                this.father.attackEntityAsMobMinimum(mob, this.father.getMiniDamage() * 5.0F);
                            }
                        }
                    }
                } else {
                    self.spawnParticles(net.minecraft.util.EnumParticleTypes.EXPLOSION_LARGE);
                }
                self.playSound(com.dhanantry.scapeandrunparasites.init.SRPSounds.ORB_E, 1.0F, (this.rand.nextFloat() - this.rand.nextFloat()) * 0.2F + 1.0F);
                if (this.timerDDD > 45) {
                    self.setDead();
                }
            }
        }
        ci.cancel();
    }
}