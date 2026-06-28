package yc.ycqin.nb.mixins.confusionApply.proj;

import com.dhanantry.scapeandrunparasites.entity.projectile.EntityProjectileAlafhaBall;
import com.dhanantry.scapeandrunparasites.entity.ai.misc.EntityParasiteBase;
import com.dhanantry.scapeandrunparasites.entity.projectile.EntitySRPProjectile;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.SharedMonsterAttributes;
import net.minecraft.init.SoundEvents;
import net.minecraft.util.EnumParticleTypes;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import yc.ycqin.nb.register.PotionsRegister;

@Mixin(value = EntityProjectileAlafhaBall.class,remap = false)
public abstract class MixinAlafhaBallExplode extends EntitySRPProjectile {

    public MixinAlafhaBallExplode(World worldIn) {
        super(worldIn);
    }

    @Shadow
    protected abstract void spawnLingeringCloud();

    @Inject(method = "func_70227_a", at = @At("HEAD"), cancellable = true)
    private void onImpact(RayTraceResult result, CallbackInfo ci) {
        if (shootingEntity == null || !(shootingEntity instanceof EntityParasiteBase)) return;
        EntityParasiteBase shooter = (EntityParasiteBase) shootingEntity;
        if (!shooter.isPotionActive(PotionsRegister.CONFUSION)) return; // 不混乱时走原逻辑

        // 重写爆炸逻辑，允许伤害同类
        AxisAlignedBB aabb = shooter.getEntityBoundingBox().grow(3.0);
        for (EntityLivingBase target : shooter.world.getEntitiesWithinAABB(EntityLivingBase.class, aabb)) {
            if (target != shooter) {
                shooter.attackEntityAsMobMinimum(target, (float) shooter.getEntityAttribute(SharedMonsterAttributes.ATTACK_DAMAGE).getBaseValue());
            }
        }
        // 播放音效和粒子（可保留原逻辑或简化）
        shooter.world.playSound(null, shooter.posX, shooter.posY, shooter.posZ, SoundEvents.ENTITY_GENERIC_EXPLODE, SoundCategory.HOSTILE, 4.0F, 1.0F);
        this.spawnLingeringCloud();
        shooter.world.spawnParticle(EnumParticleTypes.EXPLOSION_HUGE, shooter.posX, shooter.posY, shooter.posZ, 0, 0, 0);
        ci.cancel();
    }
}