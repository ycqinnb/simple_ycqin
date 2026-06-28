package yc.ycqin.nb.network;

import io.netty.buffer.ByteBuf;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;
import net.minecraftforge.fml.relauncher.Side;
import yc.ycqin.nb.client.animation.EyeAnimationHandler;

public class PacketEndBlackScreen implements IMessage {

    public PacketEndBlackScreen() {}

    @Override
    public void fromBytes(ByteBuf buf) {}

    @Override
    public void toBytes(ByteBuf buf) {}

    public static class Handler implements IMessageHandler<PacketEndBlackScreen, IMessage> {
        @Override
        public IMessage onMessage(PacketEndBlackScreen message, MessageContext ctx) {
            if (ctx.side == Side.CLIENT) {
                EyeAnimationHandler.endAnimation();
            }
            return null;
        }
    }
}
