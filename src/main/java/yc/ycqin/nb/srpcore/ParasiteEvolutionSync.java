package yc.ycqin.nb.srpcore;


import com.dhanantry.scapeandrunparasites.network.SRPCommandEvolution;
import com.dhanantry.scapeandrunparasites.world.SRPSaveData;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.world.World;
import net.minecraftforge.fml.common.FMLCommonHandler;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.PlayerEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;

import yc.ycqin.nb.network.ParasiteEvolutionPacket;

public class ParasiteEvolutionSync {
    private static int tickCounter = 0;

    @SubscribeEvent
    public void onServerTick(TickEvent.ServerTickEvent event) {
        if (event.phase == TickEvent.Phase.END) {
            tickCounter++;
            if (tickCounter >= 100) {
                tickCounter = 0;
                System.out.println("[YcDim] Server tick: sending to all players");
                syncAllPlayers();
            }
        }
    }

    @SubscribeEvent
    public void onPlayerChangedDimension(PlayerEvent.PlayerChangedDimensionEvent event) {
        if (!event.player.world.isRemote) {
            System.out.println("[YcDim] Player changed dimension, sending evolution data.");
            sendDataToPlayer((EntityPlayerMP) event.player);
        }
    }

    @SubscribeEvent
    public void onPlayerLogin(PlayerEvent.PlayerLoggedInEvent event) {
        if (!event.player.world.isRemote) {
            System.out.println("[YcDim] Player logged in, sending evolution data.");
            sendDataToPlayer((EntityPlayerMP) event.player);
        }
    }

    private void syncAllPlayers() {
        for (EntityPlayerMP player : FMLCommonHandler.instance().getMinecraftServerInstance().getPlayerList().getPlayers()) {
            sendDataToPlayer(player);
        }
    }

    private void sendDataToPlayer(EntityPlayerMP player) {
        World world = player.world;
        int dim = player.dimension;

        SRPSaveData data = SRPSaveData.get(world, 59);
        int phase = data.getEvolutionPhase(dim);
        int total = data.getTotalKills(dim);
        int next = getNextPhasePoints(phase);

        // 强制发送，不检查缓存
        EvolutionDataManager.NETWORK.sendTo(new ParasiteEvolutionPacket(dim, phase, total, next), player);
        System.out.println("[YcDim] Sent evolution data to player " + player.getName() +
                ": dim=" + dim + " phase=" + phase + " total=" + total + " next=" + next);
    }

     private int getNextPhasePoints(int currentPhase) {
         return SRPCommandEvolution.getNeededPoints((byte)(currentPhase+1));
     }
}