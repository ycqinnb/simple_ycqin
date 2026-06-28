package yc.ycqin.nb.srpcore;

import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraft.world.WorldServer;
import yc.ycqin.nb.common.dim.MirageManager;
import yc.ycqin.nb.common.dim.TeleporterDirect;

public class EndPortalHelper {

    /**
     * 只在实体位于幻境末地时返回 true
     */
    public static boolean shouldRedirect(World world, Entity entity) {
        if (world.isRemote) return false;
        if (!(entity instanceof EntityPlayerMP)) return false;
        int dim = entity.dimension;
        return MirageManager.isMirageDimension(dim) && MirageManager.getOriginalDim(dim) == 1;
    }

    /**
     * 将玩家传送到幻境主世界的重生点
     */
    public static void handleEndPortal(World world, Entity entity) {
        System.out.println("[MirageEnd] Redirect called");
        if (world.isRemote) return;
        EntityPlayerMP player = (EntityPlayerMP) entity;
        int mirageMain = MirageManager.getMirageDim(0);
        WorldServer mirageWorld = player.mcServer.getWorld(mirageMain);
        if (mirageWorld == null) return;
        BlockPos respawn = MirageManager.getRespawnPoint(player, 0, mirageWorld);
        player.changeDimension(mirageMain, new TeleporterDirect(mirageWorld, respawn, 0));
    }
}