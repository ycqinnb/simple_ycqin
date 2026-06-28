package yc.ycqin.nb.mixins.confusionApply.entityMob;


import com.dhanantry.scapeandrunparasites.entity.ai.misc.EntityPPure;
import com.dhanantry.scapeandrunparasites.entity.monster.inborn.EntityLodo;
import com.dhanantry.scapeandrunparasites.entity.monster.pure.EntityOrch;
import com.dhanantry.scapeandrunparasites.entity.ai.misc.EntityParasiteBase;
import com.dhanantry.scapeandrunparasites.util.config.SRPConfigMobs;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import yc.ycqin.nb.register.PotionsRegister;

@Mixin(value = EntityOrch.class,remap = false)
public abstract class MixinOrch extends EntityPPure {

    @Shadow(remap = false) private boolean up;
    @Shadow(remap = false) private float attackTimer;

    public MixinOrch(World worldIn) {
        super(worldIn);
    }

    @Inject(method = "attackEntityAsMobAOE", at = @At("HEAD"), cancellable = true)
    private void onAttackAOE(Entity entityIn, CallbackInfoReturnable<Boolean> cir) {
        EntityOrch self = (EntityOrch)(Object)this;
        if (!self.isPotionActive(PotionsRegister.CONFUSION)) {
            return; // 未混乱时走原方法
        }

        // 混乱时完全重写，允许攻击同类
        if (this.borderOrb != 0) {
            cir.setReturnValue(false);
            return;
        }
        this.up = true;
        this.attackTimer = 0.0F;
        self.world.setEntityState(self, (byte)12);
        this.playSound(com.dhanantry.scapeandrunparasites.init.SRPSounds.SWIPE, 2.0F, 1.0F);
        AxisAlignedBB aabb = new AxisAlignedBB(
                entityIn.posX, entityIn.posY, entityIn.posZ,
                entityIn.posX + 1.0, entityIn.posY + 1.0, entityIn.posZ + 1.0
        ).grow(2.0);
        boolean flag = false;
        for (EntityLivingBase mob : self.world.getEntitiesWithinAABB(EntityLivingBase.class, aabb)) {
            if (mob == self) continue;
            // 混乱时攻击所有生物（包括寄生虫），不移除当前目标
            if (self.canEntityBeSeen(mob) && this.func_70652_k(mob)) {
                flag = true;
            }
        }
        cir.setReturnValue(flag);
    }


    @Inject(method = "skillLeap", at = @At("HEAD"), cancellable = true)
    private void onSkillLeap(CallbackInfo ci) {
        EntityOrch self = (EntityOrch)(Object)this;
        boolean confused = self.isPotionActive(PotionsRegister.CONFUSION);
        // 未混乱时走原逻辑
        if (!confused) return;

        // 混乱状态下的完整重写（包含生成 lodo 的逻辑）
        if (this.leapMotionY != 0.0F) {
            if (self.getAttackTarget() != null && self.shouldWorkTask() && !self.isPotionActive(net.minecraft.init.MobEffects.SLOWNESS) && self.getParasiteStatus() <= 2) {
                EntityLivingBase target = self.getAttackTarget();
                if (this.attacking == 0) {
                    ++this.attacking;
                    this.targetX = target.posX;
                    this.targetZ = target.posZ;
                }
            }

            if (this.attacking >= 1) {
                ++this.attacking;
                this.skillBreakBlocks();

                // 生成 EntityLodo（与原逻辑一致）
                if (this.attacking % 5 == 0 && this.attacking < 40 && SRPConfigMobs.lodoEnabled) {
                    EntityLodo lodo = new EntityLodo(self.world);
                    lodo.copyLocationAndAnglesFrom(self); // 直接复制位置和朝向
                    self.world.spawnEntity(lodo);
                }

                if (this.attacking == 2 && self.onGround) {
                    self.setParasiteStatus(10);
                    self.getNavigator().clearPath();
                    double dx = this.targetX - self.posX;
                    double dz = this.targetZ - self.posZ;
                    double len = MathHelper.sqrt(dx * dx + dz * dz);
                    if (len < 0.01) len = 1.0;
                    self.motionY = this.leapMotionY;
                    self.motionX += dx / len * this.jumpSpeed * 0.9 + self.motionX * 0.3;
                    self.motionZ += dz / len * this.jumpSpeed * 0.9 + self.motionZ * 0.3;
                }

                if (this.attacking > 2 && self.onGround) {
                    if (this.jumpR != 0) {
                        AxisAlignedBB aabb = self.getEntityBoundingBox().grow(this.jumpR, 2.0, this.jumpR);
                        for (EntityLivingBase mob : self.world.getEntitiesWithinAABB(EntityLivingBase.class, aabb)) {
                            if (mob != self) {
                                // 混乱时允许攻击所有生物（包括寄生虫）
                                mob.knockBack(mob, 2.5F, self.posX - mob.posX, self.posZ - mob.posZ);
                                this.func_70652_k(mob);
                            }
                        }
                    }
                    self.setParasiteStatus(0);
                    this.attacking = 0;
                    if (this.type >= 31 && this.height > 2.0F) {
                        self.playSound(com.dhanantry.scapeandrunparasites.init.SRPSounds.HITGROUND, 15.0F, 1.0F);
                    }

                    this.SkillLeapFlag = true;
                }
            }
        }
        ci.cancel();
    }
}
