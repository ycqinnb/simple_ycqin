package yc.ycqin.nb.mixins.confusionApply;

import com.dhanantry.scapeandrunparasites.entity.ai.misc.EntityParasiteBase;
import com.dhanantry.scapeandrunparasites.entity.monster.EntityWave;
import net.minecraft.block.Block;
import net.minecraft.block.BlockLiquid;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.init.Blocks;
import net.minecraft.util.EnumParticleTypes;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import yc.ycqin.nb.register.PotionsRegister;

@Mixin(value = EntityWave.class,remap = false)
public abstract class MixinEntityWave extends EntityParasiteBase {

    @Shadow private EntityLivingBase target;
    @Shadow private int duration;

    public MixinEntityWave(World worldIn) {
        super(worldIn);
    }

    @Inject(method = "func_70071_h_", at = @At("HEAD"), cancellable = true)
    private void onUpdate(CallbackInfo ci) {
        EntityWave self = (EntityWave) (Object) this;
        // 只处理混乱状态
        if (!self.isPotionActive(PotionsRegister.CONFUSION)) {
            return; // 非混乱，执行原方法
        }

        // 以下为混乱状态下的完整重写逻辑，允许伤害寄生虫
        if (self.world.isRemote) {
            IBlockState state = self.world.getBlockState(self.getPosition().down());
            if (state.getBlock() != Blocks.AIR) {
                int id = Block.getStateId(state);
                for (int i = 0; i < 15; i++) {
                    self.world.spawnParticle(EnumParticleTypes.BLOCK_CRACK,
                            self.posX + (this.rand.nextFloat() * self.width * 2.0F) - self.width,
                            self.posY,
                            self.posZ + (this.rand.nextFloat() * self.width * 2.0F) - self.width,
                            this.rand.nextGaussian() * 0.02,
                            this.rand.nextGaussian() + 20.0,
                            this.rand.nextGaussian() * 0.02,
                            new int[]{id});
                }
            }
            ci.cancel();
            return;
        }

        // 服务端逻辑
        if (target != null && !target.isEntityAlive()) {
            self.setDead();
            ci.cancel();
            return;
        }

        if (self.ticksExisted > 40) {
            if (self.posX == self.prevPosX || self.posZ == self.prevPosZ) {
                self.setDead();
            }
            if (self.ticksExisted > 20 * duration) {
                self.setDead();
                ci.cancel();
                return;
            }
        }

        if (self.world.getBlockState(self.getPosition()).getBlock() instanceof BlockLiquid) {
            self.setDead();
            ci.cancel();
            return;
        }

        float f = self.width / 2.0F;
        float f1 = self.height;
        AxisAlignedBB aabb = new AxisAlignedBB(
                self.posX - f, self.posY, self.posZ - f,
                self.posX + f, self.posY + f1, self.posZ + f
        ).grow(0.4, 0.2, 0.4);

        for (EntityLivingBase mob : self.world.getEntitiesWithinAABB(EntityLivingBase.class, aabb)) {
            // 混乱状态下，不再检查 mob instanceof EntityParasiteBase，直接造成伤害
            self.attackEntityAsMobMinimum(mob, MiniDamage);
        }

        ci.cancel(); // 阻止原方法执行
    }
}
