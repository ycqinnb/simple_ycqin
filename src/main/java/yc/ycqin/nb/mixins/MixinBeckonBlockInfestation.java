package yc.ycqin.nb.mixins;

import com.dhanantry.scapeandrunparasites.block.PurifyMappings;
import com.dhanantry.scapeandrunparasites.block.BlockInfestationPurifier;
import com.dhanantry.scapeandrunparasites.util.convert.BeckonBlockInfestation;
import net.minecraft.block.state.IBlockState;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import yc.ycqin.nb.common.entity.tileentity.TileEntityParasiteCore;

import java.lang.reflect.Method;
import java.util.Random;

@Mixin(BeckonBlockInfestation.class)
public class MixinBeckonBlockInfestation {

    // 缓存反射方法，避免每次调用都反射
    private static Method tryCopyCommonPropsMethod;

    static {
        try {
            tryCopyCommonPropsMethod = BlockInfestationPurifier.class.getDeclaredMethod(
                    "tryCopyCommonProps",
                    IBlockState.class,
                    IBlockState.class
            );
            tryCopyCommonPropsMethod.setAccessible(true);
        } catch (NoSuchMethodException e) {
            // 打印错误，但后续降级处理
            System.err.println("[NB] Failed to reflect tryCopyCommonProps: " + e.getMessage());
        }
    }

    @Inject(
            method = "beckonInfestation",
            at = @At("HEAD"),
            cancellable = true,
            remap = false
    )
    private static void onBeckonInfestation(World world, BlockPos pos, Random rand, int stage, boolean fromVenkrol, CallbackInfo ci) {
        // 检查是否在屏障保护范围内
        if (TileEntityParasiteCore.isPositionProtected(world,pos)) {
            ci.cancel(); // 阻止感染传播

            if (!world.isRemote) {
                // 小范围净化（5x5x5）
                BlockPos min = pos.add(-2, -2, -2);
                BlockPos max = pos.add(2, 2, 2);
                for (BlockPos p : BlockPos.getAllInBox(min, max)) {
                    IBlockState state = world.getBlockState(p);
                    if (PurifyMappings.isSrp(state)) {
                        IBlockState vanilla = PurifyMappings.mapToVanillaState(state);
                        if (vanilla != null) {
                            IBlockState finalState = vanilla;
                            // 尝试复制原方块的属性（如朝向、阶段等）
                            if (tryCopyCommonPropsMethod != null) {
                                try {
                                    finalState = (IBlockState) tryCopyCommonPropsMethod.invoke(null, state, vanilla);
                                } catch (Exception e) {
                                    // 反射失败，使用 vanilla 并记录错误
                                    System.err.println("[NB] Failed to invoke tryCopyCommonProps: " + e.getMessage());
                                }
                            }
                            world.setBlockState(p, finalState, 3); // 更新并同步客户端
                        }
                    }
                }
            }
        }
    }
}