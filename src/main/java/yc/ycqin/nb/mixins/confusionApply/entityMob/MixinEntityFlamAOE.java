package yc.ycqin.nb.mixins.confusionApply.entityMob;

import com.dhanantry.scapeandrunparasites.entity.EntityToxicCloud;
import com.dhanantry.scapeandrunparasites.entity.ai.misc.EntityPPreeminent;
import com.dhanantry.scapeandrunparasites.entity.ai.misc.EntityPPrimitive;
import com.dhanantry.scapeandrunparasites.entity.ai.misc.EntityParasiteBase;
import com.dhanantry.scapeandrunparasites.entity.monster.pure.preeminent.EntityFlam;
import com.dhanantry.scapeandrunparasites.init.SRPPotions;
import com.dhanantry.scapeandrunparasites.init.SRPSounds;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.MoverType;
import net.minecraft.init.MobEffects;
import net.minecraft.potion.PotionEffect;
import net.minecraft.util.SoundEvent;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import yc.ycqin.nb.register.PotionsRegister;

@Mixin(value = EntityFlam.class,remap = false)
public abstract class MixinEntityFlamAOE extends EntityPPreeminent {
    public MixinEntityFlamAOE(World worldIn) {
        super(worldIn);
    }

    @Shadow protected abstract void spawnGore();

    @Inject(method = "selfExplode", at = @At("HEAD"), cancellable = true)
    private void onSelfExplode(CallbackInfo ci) {
        EntityFlam self = (EntityFlam)(Object)this;
        boolean confused = self.isPotionActive(PotionsRegister.CONFUSION);
        if (!confused) return;
        if (self.world.isRemote) {
            self.spawnEffectsGore();
            ci.cancel();
            return;
        }



        spawnGore();
        self.playSound(SRPSounds.MOBEXPLOTION, 1.0F, 1.0F);
        this.dead = true;
        self.setDead();

        AxisAlignedBB aabb = new AxisAlignedBB(
                self.posX, self.posY - 2.0, self.posZ,
                self.posX + 1.0, self.posY + 1.0, self.posZ + 1.0
        ).grow(4.0);

        for (EntityLivingBase mob : self.world.getEntitiesWithinAABB(EntityLivingBase.class, aabb)) {
            if (!(mob instanceof EntityParasiteBase) || confused) {
                self.func_70652_k(mob);
            }
        }

        EntityToxicCloud cloud = new EntityToxicCloud(self.world, self.posX, self.posY - 2.0, self.posZ);
        cloud.setRadius(self.width * 3.5F, 0.5F);
        cloud.setWaitTime(10);
        cloud.setDuration(cloud.getDuration() / 2);
        cloud.setRadiusPerTick(-cloud.getRadius() / (float)cloud.getDuration());
        cloud.addEffect(new PotionEffect(MobEffects.POISON, 300, 2));
        cloud.addEffect(new PotionEffect(MobEffects.WITHER, 300, 2));
        cloud.addEffect(new PotionEffect(SRPPotions.COTH_E, 3600, 2, false, false));
        self.world.spawnEntity(cloud);

        ci.cancel();
    }
}
