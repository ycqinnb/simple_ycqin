package yc.ycqin.nb.network;

import io.netty.buffer.ByteBuf;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.init.MobEffects;
import net.minecraft.potion.PotionEffect;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.WorldServer;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;
import yc.ycqin.nb.common.dim.MirageManager;
import yc.ycqin.nb.common.dim.TeleporterDirect;
import yc.ycqin.nb.register.NetworkRegister;

public class PacketEyeTrigger implements IMessage {

    public PacketEyeTrigger() {}

    @Override
    public void fromBytes(ByteBuf buf) {}

    @Override
    public void toBytes(ByteBuf buf) {}

    public static class Handler implements IMessageHandler<PacketEyeTrigger, IMessage> {
        @Override
        public IMessage onMessage(PacketEyeTrigger message, MessageContext ctx) {
            ctx.getServerHandler().player.getServerWorld().addScheduledTask(() -> {
                EntityPlayer player = ctx.getServerHandler().player;
                MinecraftServer server = player.getServer();
                MirageManager.ensureMirageWorld(server, 0); // 0 = 主世界
                int mirageMainDim = MirageManager.getMirageDim(0);
                WorldServer mirageWorld = player.getServer().getWorld(mirageMainDim);
                BlockPos spawn = MirageManager.getRespawnPoint((EntityPlayerMP) player, 0, mirageWorld);
                player.getEntityData().setBoolean("inMirage", true);
                player.changeDimension(mirageMainDim, new TeleporterDirect(mirageWorld, spawn, 0));

// 延迟发送，确保世界切换完成
                player.getServer().addScheduledTask(() -> {
                    try { Thread.sleep(2000); } catch (InterruptedException ignored) {}
                    NetworkRegister.NETWORK.sendTo(new PacketEndBlackScreen(), (EntityPlayerMP) player);
                    player.addPotionEffect(new PotionEffect(MobEffects.BLINDNESS, 40, 0, false, false));
                });
            });
            return null;
        }
    }
}