package yc.ycqin.nb.network;

import net.minecraft.client.Minecraft;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;
import yc.ycqin.nb.gui.EvolutionHUD;

public class ParasiteEvolutionPacketHandler implements IMessageHandler<ParasiteEvolutionPacket, IMessage> {
    @Override
    public IMessage onMessage(ParasiteEvolutionPacket message, MessageContext ctx) {
        if (ctx.side.isClient()) {
            Minecraft.getMinecraft().addScheduledTask(() -> {
                System.out.println("[YcDim] Client received packet: phase=" + message.getPhase() + " points=" + message.getTotalPoints());
                EvolutionHUD.INSTANCE.getEvolutionBar().updateParasiteData(
                        message.getPhase(),
                        message.getTotalPoints(),
                        message.getNextPoints()
                );
            });
        }
        return null;
    }
}