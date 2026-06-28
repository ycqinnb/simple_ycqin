package yc.ycqin.nb.mixins.confusionApply;

import com.dhanantry.scapeandrunparasites.client.particle.SRPEnumParticle;
import com.dhanantry.scapeandrunparasites.entity.EntityToxicCloud;
import com.dhanantry.scapeandrunparasites.entity.ai.misc.EntityParasiteBase;
import com.dhanantry.scapeandrunparasites.entity.ai.misc.EntityPCosmical;
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
import yc.ycqin.nb.register.PotionsRegister;

import java.util.List;
import java.util.Map;

@Mixin(value = EntityToxicCloud.class,remap = false)
public abstract class MixinToxicCloudConfusion extends Entity {

    @Shadow private EntityParasiteBase owner;
    @Shadow private int waitTime;
    @Shadow private int duration;
    @Shadow private float radiusPerTick;
    @Shadow private float radiusOnUse;
    @Shadow private int reapplicationDelay;
    @Shadow private Map<Entity, Integer> reapplicationDelayMap;
    @Shadow private List<net.minecraft.potion.PotionEffect> effects;
    @Shadow private net.minecraft.potion.PotionType potion;
    @Shadow private int effectParticles;
    @Shadow protected abstract void setIgnoreRadius(boolean ignoreRadius);

    public MixinToxicCloudConfusion(World p_i1582_1_) {
        super(p_i1582_1_);
    }

    @Inject(method = "func_70071_h_", at = @At("HEAD"), cancellable = true)
    private void onUpdate(CallbackInfo ci) {
        EntityToxicCloud self = (EntityToxicCloud)(Object)this;
        boolean confused = owner != null && owner.isPotionActive(PotionsRegister.CONFUSION);
        if (!confused) {
            return; // 没有混乱，走原方法
        }

        // 混乱：完全重写方法体，修改友伤判断
        boolean flag = self.shouldIgnoreRadius();
        float f = self.getRadius();
        if (self.world.isRemote) {
            // 客户端粒子效果（原样复制，不修改）
            net.minecraft.util.EnumParticleTypes enumparticletypes = self.getParticle();
            int[] aint = new int[enumparticletypes.getArgumentCount()];
            if (aint.length > 0) aint[0] = self.getParticleParam1();
            if (aint.length > 1) aint[1] = self.getParticleParam2();
            if (flag) {
                if (this.rand.nextInt(3) != 0) {
                    for (int i = 0; i < 2; ++i) {
                        float f1 = rand.nextFloat() * ((float)Math.PI * 2F);
                        float f2 = MathHelper.cos(rand.nextFloat()) * 0.2F;
                        float f3 = MathHelper.sin(f1) * f2;
                        float f4 = MathHelper.cos(f1) * f2;
                        if (enumparticletypes == net.minecraft.util.EnumParticleTypes.SPELL_MOB) {
                            int j = rand.nextBoolean() ? 16777215 : self.getColor();
                            int k = j >> 16 & 255;
                            int l = j >> 8 & 255;
                            int i1 = j & 255;
                            self.world.spawnParticle(net.minecraft.util.EnumParticleTypes.SPELL_MOB, self.posX + f3, self.posY + rand.nextFloat() * self.height, self.posZ + f4, (float)k / 255.0F, (float)l / 255.0F, (float)i1 / 255.0F);
                        } else {
                            self.world.spawnParticle(enumparticletypes, self.posX + f3, self.posY + rand.nextFloat() * height, self.posZ + f4, 0.0, 0.0, 0.0, aint);
                        }
                    }
                }
            } else {
                float f5 = (float)Math.PI * f * f;
                for (int k1 = 0; (float)k1 < f5; ++k1) {
                    if (rand.nextInt(3) == 0) {
                        float f6 = rand.nextFloat() * ((float)Math.PI * 2F);
                        float f7 = MathHelper.cos(rand.nextFloat()) * f;
                        float f8 = MathHelper.sin(f6) * f7;
                        float f9 = MathHelper.cos(f6) * f7;
                        if (enumparticletypes == net.minecraft.util.EnumParticleTypes.SPELL_MOB) {
                            int l1 = self.getColor();
                            int i2 = l1 >> 16 & 255;
                            int j2 = l1 >> 8 & 255;
                            int j1 = l1 & 255;
                            double d11 = self.posX + f8;
                            double d22 = self.posY + rand.nextFloat() * self.height;
                            double d33 = self.posZ + f9;
                            double d44 = (float)i2 / 255.0F;
                            double d55 = (float)j2 / 255.0F;
                            double d66 = (float)j1 / 255.0F;
                            if (effectParticles == 1) {
                                self.world.spawnParticle(net.minecraft.util.EnumParticleTypes.FLAME, d11, d22, d33, d44, d55, d66);
                                self.spawnParticles(SRPEnumParticle.GCLOUD, 0, 0, 0, d11, d22 + (double)0.5F, d33, d44, d55, d66);
                            } else {
                                self.world.spawnParticle(net.minecraft.util.EnumParticleTypes.SPELL_MOB, d11, d22, d33, d44, d55, d66);
                            }
                        } else {
                            self.world.spawnParticle(enumparticletypes, self.posX + f8, self.posY + rand.nextFloat() * self.height, self.posZ + f9, (0.5 - rand.nextDouble()) * 0.15, 0.01, (0.5 - rand.nextDouble()) * 0.15, aint);
                        }
                    }
                }
            }
        } else {
            // 服务端逻辑（修改友伤判断）
            if (self.ticksExisted >= waitTime + duration) {
                self.setDead();
                ci.cancel();
                return;
            }
            boolean flag1 = self.ticksExisted < waitTime;
            if (flag != flag1) {
                this.setIgnoreRadius(flag1);
            }
            if (flag1) {
                ci.cancel();
                return;
            }
            if (radiusPerTick != 0.0F) {
                f += radiusPerTick;
                if (f < 0.5F) {
                    self.setDead();
                    ci.cancel();
                    return;
                }
                self.setRadius(f, self.getHeight());
            }
            if (self.ticksExisted % 5 == 0) {
                reapplicationDelayMap.entrySet().removeIf(entry -> self.ticksExisted >= entry.getValue());
                List<net.minecraft.potion.PotionEffect> potions = new java.util.ArrayList<>();
                for (net.minecraft.potion.PotionEffect effect : potion.getEffects()) {
                    potions.add(new net.minecraft.potion.PotionEffect(effect.getPotion(), effect.getDuration() / 4, effect.getAmplifier(), effect.getIsAmbient(), effect.doesShowParticles()));
                }
                potions.addAll(effects);
                if (!potions.isEmpty()) {
                    AxisAlignedBB aabb = self.getEntityBoundingBox();
                    List<EntityLivingBase> list = self.world.getEntitiesWithinAABB(EntityLivingBase.class, aabb);
                    for (EntityLivingBase target : list) {
                        // 原条件： if (owner != null) { if (!(target instanceof EntityParasiteBase)) owner.attack... }
                        // 修改为混乱时无条件调用
                        if (owner != null && target != owner) {
                            owner.attackEntityAsMobMinimum(target, owner.getMiniDamage() / 2.0F);
                        }
                        // 原条件： if (!(target instanceof EntityPCosmical)) target.attack...
                        // 修改为混乱时无条件调用
                        target.attackEntityFrom(net.minecraft.util.DamageSource.MAGIC, 10.0F);

                        // 原条件： if (!(target instanceof EntityParasiteBase) && ...) 施加药水效果
                        // 修改为混乱时无条件施加
                        if (!reapplicationDelayMap.containsKey(target) && target.canBeHitWithPotion()) {
                            double dx = target.posX - self.posX;
                            double dz = target.posZ - self.posZ;
                            double distSq = dx * dx + dz * dz;
                            if (distSq <= f * f) {
                                reapplicationDelayMap.put(target, self.ticksExisted + reapplicationDelay);
                                for (net.minecraft.potion.PotionEffect effect : potions) {
                                    if (effect.getPotion().isInstant()) {
                                        effect.getPotion().affectEntity(owner, owner, target, effect.getAmplifier(), 0.5);
                                    } else {
                                        target.addPotionEffect(effect);
                                    }
                                }
                                if (radiusOnUse != 0.0F) {
                                    f += radiusOnUse;
                                    if (f < 0.5F) {
                                        self.setDead();
                                        ci.cancel();
                                        return;
                                    }
                                    self.setRadius(f, self.getHeight());
                                }
                            }
                        }
                    }
                }
            }
        }
        ci.cancel();
    }

    // 需要 shadow 的方法（原类中有）

}
