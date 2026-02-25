package yc.ycqin.nb.srpcore;

import net.minecraftforge.fml.common.network.NetworkRegistry;
import net.minecraftforge.fml.common.network.simpleimpl.SimpleNetworkWrapper;
import net.minecraftforge.fml.relauncher.Side;
import yc.ycqin.nb.network.ParasiteEvolutionPacket;
import yc.ycqin.nb.network.ParasiteEvolutionPacketHandler;

public class EvolutionDataManager {
    // 网络包装器（需在模组初始化时注册）
    public static final SimpleNetworkWrapper NETWORK = NetworkRegistry.INSTANCE.newSimpleChannel("ycqin_evolution");


    public static void registerPackets() {
        NETWORK.registerMessage(ParasiteEvolutionPacketHandler.class, ParasiteEvolutionPacket.class, 78, Side.CLIENT);
    }
}