package yc.ycqin.nb.common.dim;

import net.minecraft.entity.Entity;
import net.minecraft.init.Blocks;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.Teleporter;
import net.minecraft.world.WorldServer;

public class TeleporterDirect extends Teleporter {
    private final BlockPos target;
    private final int originalDim;

    public TeleporterDirect(WorldServer world, BlockPos target, int originalDim) {
        super(world);
        this.target = target;
        this.originalDim = originalDim;
    }

    @Override
    public void placeInPortal(Entity entity, float rotationYaw) {
        double x = target.getX() + 0.5;
        double y = target.getY();
        double z = target.getZ() + 0.5;

        if (originalDim == 1) {
            // 末地生成黑曜石平台
            int i = MathHelper.floor(target.getX());
            int j = MathHelper.floor(target.getY()) - 1;
            int k = MathHelper.floor(target.getZ());
            for (int dx = -2; dx <= 2; dx++) {
                for (int dz = -2; dz <= 2; dz++) {
                    for (int dy = -1; dy < 3; dy++) {
                        BlockPos pos = new BlockPos(i + dx, j + dy, k + dz);
                        if (dy < 0) {
                            this.world.setBlockState(pos, Blocks.OBSIDIAN.getDefaultState());
                        } else {
                            this.world.setBlockState(pos, Blocks.AIR.getDefaultState());
                        }
                    }
                }
            }
            x = i + 0.5;
            y = j + 1;
            z = k + 0.5;
        }

        entity.setLocationAndAngles(x, y, z, entity.rotationYaw, entity.rotationPitch);
        entity.motionX = 0.0D;
        entity.motionY = 0.0D;
        entity.motionZ = 0.0D;
    }

    @Override
    public boolean placeInExistingPortal(Entity entity, float rotationYaw) {
        return false;
    }

    @Override
    public boolean makePortal(Entity entity) {
        return true;
    }

    @Override
    public void removeStalePortalLocations(long worldTime) {}
}