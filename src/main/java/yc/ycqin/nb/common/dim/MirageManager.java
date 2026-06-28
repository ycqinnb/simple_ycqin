package yc.ycqin.nb.common.dim;

import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.WorldServer;
import net.minecraftforge.common.DimensionManager;
import yc.ycqin.nb.config.ModConfig;
import yc.ycqin.nb.register.DimRegister;

import java.util.*;

public class MirageManager {
    private static final Map<Integer, Integer> originalToMirage = new HashMap<>();
    private static final Map<Integer, Integer> mirageToOriginal = new HashMap<>();
    private static final Set<UUID> processingPlayers = new HashSet<>();

    // 保持原维度加载的计数器（避免重复调用 keepLoaded）
    private static final Map<Integer, Integer> keepLoadedCount = new HashMap<>();

    private static int computeMirageId(int originalDim) {
        int base = 100;
        int candidate = originalDim + base;
        Set<Integer> used = new HashSet<>(Arrays.asList(DimensionManager.getIDs()));
        while (used.contains(candidate) && !mirageToOriginal.containsKey(candidate)) {
            candidate += 100;
        }
        return candidate;
    }

    public static int getMirageDim(int originalDim) {
        Integer cached = originalToMirage.get(originalDim);
        if (cached != null) return cached;
        int mirageId = computeMirageId(originalDim);
        originalToMirage.put(originalDim, mirageId);
        mirageToOriginal.put(mirageId, originalDim);
        return mirageId;
    }

    public static int getOriginalDim(int mirageDim) {
        return mirageToOriginal.getOrDefault(mirageDim, mirageDim);
    }

    public static boolean isMirageDimension(int dim) {
        return mirageToOriginal.containsKey(dim);
    }

    /**
     * 确保幻境世界加载，并强制保持对应的原维度不卸载
     */
    public static void ensureMirageWorld(MinecraftServer server, int originalDim) {
        if (!ModConfig.mirageEnabled) return;
        int mirageId = getMirageDim(originalDim);
        if (!DimensionManager.isDimensionRegistered(mirageId)) {
            DimensionManager.registerDimension(mirageId, DimRegister.mirageDim);
        }
        if (DimensionManager.getWorld(mirageId) == null) {
            DimensionManager.initDimension(mirageId);
        }

        // 保持原维度一直加载，防止复制区块时反复加载卸载
        keepOriginalLoaded(server, originalDim);
    }

    private static void keepOriginalLoaded(MinecraftServer server, int originalDim) {
        int count = keepLoadedCount.getOrDefault(originalDim, 0);
        if (count == 0) {
            DimensionManager.keepDimensionLoaded(originalDim, true);
        }
        keepLoadedCount.put(originalDim, count + 1);
    }

    public static void releaseOriginalDimension(MinecraftServer server, int originalDim) {
        int count = keepLoadedCount.getOrDefault(originalDim, 0);
        if (count <= 0) return;
        if (count == 1) {
            DimensionManager.keepDimensionLoaded(originalDim, false);
            keepLoadedCount.remove(originalDim);
        } else {
            keepLoadedCount.put(originalDim, count - 1);
        }
    }

    // ---- 防重入 ----
    public static boolean isProcessing(EntityPlayerMP player) {
        return processingPlayers.contains(player.getUniqueID());
    }

    public static void setProcessing(EntityPlayerMP player, boolean processing) {
        if (processing) {
            processingPlayers.add(player.getUniqueID());
        } else {
            processingPlayers.remove(player.getUniqueID());
        }
    }

    // ---- 出生点 ----
    public static BlockPos getRespawnPoint(EntityPlayerMP player, int dimId, WorldServer targetWorld) {
        BlockPos bedPos = player.getBedLocation(dimId);
        return bedPos != null ? bedPos : targetWorld.getSpawnPoint();
    }
}