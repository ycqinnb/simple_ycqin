package yc.ycqin.nb.register;

import net.minecraftforge.fml.common.network.NetworkRegistry;
import net.minecraftforge.fml.common.network.simpleimpl.SimpleNetworkWrapper;
import net.minecraftforge.fml.relauncher.Side;
import yc.ycqin.nb.network.ParasiteEvolutionPacket;
import yc.ycqin.nb.network.ParasiteEvolutionPacketHandler;

public class NetworkRegister {
    public static SimpleNetworkWrapper NETWORK;
    public NetworkRegister(){
        NETWORK = NetworkRegistry.INSTANCE.newSimpleChannel("ycqin");
        NETWORK.registerMessage(ParasiteEvolutionPacketHandler.class, ParasiteEvolutionPacket.class, 78, Side.CLIENT);
    }
}
