package yc.ycqin.nb.mixins.confusionApply.srpAI;

import com.dhanantry.scapeandrunparasites.entity.ai.EntityAIKirinBlink;
import com.dhanantry.scapeandrunparasites.entity.monster.derived.EntityKirin;
import com.dhanantry.scapeandrunparasites.init.SRPSounds;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.math.AxisAlignedBB;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import yc.ycqin.nb.register.PotionsRegister;

import java.util.List;


@Mixin(value = EntityAIKirinBlink.class,remap = false)
public abstract class MixinKirinBlinkLifeSteal {

    @Shadow(remap = false) private EntityKirin kirin;

    @Inject(method = "doBlinkLifeSteal", at = @At("HEAD"), cancellable = true)
    private void onDoBlinkLifeSteal(CallbackInfo ci) {
        if (kirin.world.isRemote) return;

        boolean confused = kirin.isPotionActive(PotionsRegister.CONFUSION);
        double radius = 5.0;
        AxisAlignedBB box = new AxisAlignedBB(kirin.posX - radius, kirin.posY - radius, kirin.posZ - radius,
                kirin.posX + radius, kirin.posY + radius, kirin.posZ + radius);
        List<EntityLivingBase> nearby = kirin.world.getEntitiesWithinAABB(EntityLivingBase.class, box, e -> {
            if (e == null || e == kirin || !e.isEntityAlive()) return false;
            // 原逻辑：只选非SRP实体
            // 修改后：混乱时选所有活体，否则只选非SRP
            if (confused) return true;
            return !e.getClass().getName().startsWith("com.dhanantry.scapeandrunparasites.entity");
        });
        if (nearby.isEmpty()) {
            kirin.world.playSound(null, kirin.posX, kirin.posY, kirin.posZ, SRPSounds.ALAFHA_HURT, SoundCategory.HOSTILE, 0.7F, 0.9F + kirin.getRNG().nextFloat() * 0.2F);
        } else {
            EntityLivingBase target = nearby.get(0);
            float currentHealth = target.getHealth();
            if (currentHealth > 0) {
                float stolen = currentHealth * 0.5F;
                float newHealth = currentHealth - stolen;
                if (newHealth < 0) newHealth = 0;
                target.setHealth(newHealth);
                // 强制受击动画
                target.hurtResistantTime = 10;
                target.hurtTime = 10;
                kirin.world.playSound(null, target.posX, target.posY, target.posZ, SRPSounds.CRUX_HURT, SoundCategory.HOSTILE, 1.0F, 0.8F + kirin.getRNG().nextFloat() * 0.4F);
                if (stolen > 0) {
                    kirin.heal(stolen);
                }
            }
        }
        ci.cancel();
    }
}