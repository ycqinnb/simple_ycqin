package yc.ycqin.nb.mixins.confusionApply;

import com.dhanantry.scapeandrunparasites.entity.EntityOrbVoid;
import com.dhanantry.scapeandrunparasites.entity.ai.misc.EntityPCosmical;
import com.dhanantry.scapeandrunparasites.entity.ai.misc.EntityPPreeminent;
import com.dhanantry.scapeandrunparasites.entity.ai.misc.EntityPStationary;
import com.dhanantry.scapeandrunparasites.entity.ai.misc.EntityParasiteBase;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.DamageSource;
import net.minecraft.util.EnumParticleTypes;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import yc.ycqin.nb.register.PotionsRegister;

@Mixin(value = EntityOrbVoid.class,remap = false)
public abstract class MixinOrbVoidSelfExplode extends Entity {

    @Shadow(remap = false) private com.dhanantry.scapeandrunparasites.entity.ai.misc.EntityPMalleable father;
    @Shadow(remap = false) private int timerDDD;
    @Shadow(remap = false) private double str;

    public MixinOrbVoidSelfExplode(World p_i1582_1_) {
        super(p_i1582_1_);
    }

    @Shadow protected abstract void setSelfeState(int state);
    @Shadow protected abstract int getSelfeState();
    @Shadow(remap = false) private int rad;

    @Inject(method = "selfExplode", at = @At("HEAD"), cancellable = true)
    private void onSelfExplode(CallbackInfo ci) {
        EntityOrbVoid self = (EntityOrbVoid)(Object)this;
        self.setSelfeState(2);
        if (self.getSelfeState() == 2) {
            timerDDD++;
            if (timerDDD > 80) {
                this.setSize(Math.max(0.1F, self.width - 0.8F), Math.max(0.1F, self.height - 0.32F));
                if (!self.world.isRemote) {
                    if (father != null) {
                        float f = self.width / 2.0F;
                        float f1 = self.height;
                        AxisAlignedBB aabb = new AxisAlignedBB(
                                self.posX - f, self.posY - f1, self.posZ - f,
                                self.posX + f, self.posY + f1, self.posZ + f
                        );
                        boolean confused = father.isPotionActive(PotionsRegister.CONFUSION);
                        for (EntityLivingBase mob : self.world.getEntitiesWithinAABB(EntityLivingBase.class, aabb)) {
                            if (!(mob instanceof EntityParasiteBase) || (confused && mob != father)) {
                                father.attackEntityAsMobMinimum(mob, father.getMiniDamage() * 5.0F);
                            }
                        }
                    }
                } else {
                    int par = self.getFuseState();
                    par += par / 2;
                    for(int i = 0; i <= par; ++i) {
                        self.world.spawnParticle(EnumParticleTypes.PORTAL,
                                self.posX + (this.rand.nextDouble() - 0.5) * self.width * 2.0,
                                self.posY + this.rand.nextDouble() * 2.0 * self.height,
                                self.posZ + (this.rand.nextDouble() - 0.5) * self.width * 2.0,
                                this.rand.nextGaussian(), 0.0, this.rand.nextGaussian());
                    }
                }
                self.playSound(com.dhanantry.scapeandrunparasites.init.SRPSounds.ORB_E, 1.0F, 1.0F);
                if (timerDDD > 90) {
                    self.setDead();
                }
            }
        }
        ci.cancel();
    }

    @Inject(method = "pullEntity", at = @At("HEAD"), cancellable = true)
    private void onPullEntity(EntityLivingBase target, CallbackInfo ci) {
        EntityOrbVoid self = (EntityOrbVoid)(Object)this;
        if (self.world.isRemote) return;

        boolean confused = father != null && father.isPotionActive(PotionsRegister.CONFUSION);
        double ti = target.getDistanceSq(self);

        // 如果目标距离小于4，直接拉入球心并造成伤害（原逻辑）
        if (ti < 4.0) {
            target.setPosition(self.posX, self.posY, self.posZ);
            target.motionX = target.motionY = target.motionZ = 0;
            // 伤害逻辑：如果混乱或目标非寄生虫，则攻击
            if ((confused && target != father) || !(target instanceof EntityParasiteBase)) {
                father.attackEntityAsMobMinimum(target, father.getMiniDamage() / 10.0F);
                target.attackEntityFrom(DamageSource.MAGIC, 10.0F);
            }
            ci.cancel(); // 取消原方法，因为我们已经处理
            return;
        }

        // 拉拽逻辑（原样，但伤害条件修改）
        double dx = self.posX - target.posX;
        double dy = self.posY - target.posY;
        double dz = self.posZ - target.posZ;
        double dist = Math.sqrt(dx*dx + dy*dy + dz*dz);
        if (dist == 0) return;
        dx /= dist; dy /= dist; dz /= dist;
        target.motionX += dx * str;
        target.motionY += dy * str;
        target.motionZ += dz * str;

        // 距离小于25时造成的持续伤害
        if (ti < 25.0) {
            if (confused || !(target instanceof EntityParasiteBase)) {
                father.attackEntityAsMobMinimum(target, father.getMiniDamage() / 10.0F);
                target.attackEntityFrom(DamageSource.MAGIC, 10.0F);
            }
        }
        ci.cancel();
    }

    @Inject(method = "orbDoing", at = @At("HEAD"), cancellable = true)
    private void onOrbDoing(CallbackInfo ci) {
        EntityOrbVoid self = (EntityOrbVoid)(Object)this;
        if (self.world.isRemote) return;

        boolean confused = father != null && father.isPotionActive(PotionsRegister.CONFUSION);

        float f = self.width / 2.0F;
        float f1 = self.height;
        self.resetTargetedEntity();
        AxisAlignedBB aabb = new AxisAlignedBB(
                self.posX - f, self.posY - f1, self.posZ - f,
                self.posX + f, self.posY + f1, self.posZ + f
        ).grow(rad);

        for (EntityLivingBase target : self.world.getEntitiesWithinAABB(EntityLivingBase.class, aabb)) {
            if (father == null || father == target) continue;
            // 原排除逻辑：如果不是混乱状态且目标属于被排除的三类寄生虫，则跳过
            if (!confused && (target instanceof EntityPCosmical || target instanceof EntityPStationary || target instanceof EntityPPreeminent)) {
                continue;
            }
            if (target instanceof EntityPlayer) {
                EntityPlayer player = (EntityPlayer)target;
                if (!player.capabilities.disableDamage) {
                    self.setTargetedEntity(target.getEntityId());
                }
            } else {
                self.pullEntity(target);
            }
        }
        ci.cancel();
    }

}