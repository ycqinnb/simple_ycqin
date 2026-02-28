package yc.ycqin.nb.util;

import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import yc.ycqin.nb.config.ModConfig; // 假设你的配置类路径

import java.util.HashSet;
import java.util.Set;

public class DifficultyHelper {

    // 默认扫描半径，可在配置中覆盖
    public static final int DEFAULT_SCAN_RADIUS = 5;

    // 缓存感染方块和细胞方块的Block对象，避免反复查找
    private static Block infectionBlock = null;
    private static Block cellBlock = null;

    public static class DifficultyResult {
        public final int base;           // 基础难度
        public final int correction;      // 修正难度
        public final int finalDifficulty; // 最终难度

        public DifficultyResult(int base, int correction, int finalDifficulty) {
            this.base = base;
            this.correction = correction;
            this.finalDifficulty = finalDifficulty;
        }
    }

    /**
     * 计算最终难度
     * @param world 世界
     * @param beaconPos 信标位置（中心）
     * @param keepInventory 是否开启死亡不掉落（从gamerule获取）
     * @return 最终难度（非负整数）
     */
    public static DifficultyResult computeDifficultyDetails(World world, BlockPos beaconPos, boolean keepInventory) {
        // 基础难度：玩家数量/4（向下取整）
        int playerCount = world.playerEntities.size();
        int base = playerCount / 4;

        // 修正难度
        int correction = 0;
        if (keepInventory) {
            correction += 2;
        }

        // 扫描周围方块
        int scanRadius = DEFAULT_SCAN_RADIUS; // 可从配置读取：ModConfig.purification.difficultyScanRadius
        for (int dx = -scanRadius; dx <= scanRadius; dx++) {
            for (int dz = -scanRadius; dz <= scanRadius; dz++) {
                for (int dy = -scanRadius; dy <= scanRadius; dy++) {
                    BlockPos pos = beaconPos.add(dx, dy, dz);
                    IBlockState state = world.getBlockState(pos);
                    if (isInfectionBlock(state)) {
                        correction += 1;
                    } else if (isCellBlock(state)) {
                        correction -= 1;
                    }
                }
            }
        }

        int finalDifficulty = Math.max(0, base + correction);
        return new DifficultyResult(base, correction, finalDifficulty);
    }

    /**
     * 判断是否为感染方块
     */
    public static boolean isInfectionBlock(IBlockState state) {
        if (infectionBlock == null) {
            // 尝试获取 srparasites:infestedrubble，如果不存在则返回false
            infectionBlock = Block.getBlockFromName("ycqin:specimen_infect");
            if (infectionBlock == null) {
                // 可以记录一次警告，但为避免刷屏只尝试一次
                System.out.println("[Purification] Warning: ycqin：specimen_infect not found, infection blocks will be ignored.");
            }
        }
        return infectionBlock != null && state.getBlock() == infectionBlock;
    }

    /**
     * 判断是否为细胞方块
     */
    public static boolean isCellBlock(IBlockState state) {
        if (cellBlock == null) {
            // 尝试获取 srparasites:infestedstone（根据实际调整，也可能是其他方块）
            cellBlock = Block.getBlockFromName("ycqin:specimen_cell");
            if (cellBlock == null) {
                System.out.println("[Purification] Warning: ycqin:specimen_cell not found, cell blocks will be ignored.");
            }
        }
        return cellBlock != null && state.getBlock() == cellBlock;
    }
}