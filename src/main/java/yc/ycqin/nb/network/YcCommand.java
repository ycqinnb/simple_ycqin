package yc.ycqin.nb.network;

import net.minecraft.command.CommandBase;
import net.minecraft.command.CommandException;
import net.minecraft.command.ICommandSender;
import net.minecraft.command.WrongUsageException;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.TextComponentString;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.world.World;
import net.minecraftforge.fml.common.registry.ForgeRegistries;
import yc.ycqin.nb.srpcore.PurificationBattleManager;
import yc.ycqin.nb.srpcore.PurificationWorldData;

import javax.annotation.Nullable;
import java.util.*;

public class YcCommand extends CommandBase {
    private static final List<String> SUBCOMMANDS = Arrays.asList("ForcePlaceNode","forceEnd","getWarBlock");


    @Override
    public String getName() {
        return "ycqin"; // 命令名称，可以起一个隐蔽的名字
    }

    @Override
    public String getUsage(ICommandSender sender) {
        return "/ycqin ForcePlaceNode X Y Z TYPE 强制放置节点\n/ycqin forceEnd 强制结束净化之战\n/ycqin getWarBlock 获取净化之战关键方块坐标 ";
    }

    /**
     * 权限检查：只有白名单内的玩家才允许看到并使用此命令
     */
    @Override
    public boolean checkPermission(MinecraftServer server, ICommandSender sender) {
        if (sender instanceof EntityPlayer) {
            EntityPlayer player = (EntityPlayer) sender;
            // 管理员（OP）始终有权限
            if (server.getPlayerList().canSendCommands(player.getGameProfile())) {
                return true;
            }
        }
        // 非玩家（如控制台）默认无权限，可根据需要修改
        return false;
    }

    @Override
    public void execute(MinecraftServer server, ICommandSender sender, String[] args) throws CommandException {
        if (!(sender instanceof EntityPlayer)) {
            throw new CommandException("只有玩家可以使用此命令");
        }
        EntityPlayer player = (EntityPlayer) sender;

        if (args.length < 1) {
            throw new WrongUsageException(getUsage(sender));
        }

        String sub = args[0];
         if ("ForcePlaceNode".equalsIgnoreCase(sub)) {
            // ========== ForcePlaceNode 子命令 ==========
            if (args.length < 5) {
                throw new WrongUsageException("/ycqin ForcePlaceNode <x> <y> <z> <type>");
            }
            BlockPos pos = parseBlockPos(sender, args, 1, true);
            int type = parseInt(args[4]);
            if (type < 1 || type > 4) {
                throw new CommandException("节点类型必须为1~4之间的整数");
            }
            World world = sender.getEntityWorld();
            PurificationBattleManager.forcePlaceNode(world, pos, type);
            player.sendMessage(new TextComponentString(TextFormatting.GREEN + "已在 " + pos.getX() + " " + pos.getY() + " " + pos.getZ() + " 强制放置类型 " + type + " 的寄生节点"));
        } else if ("forceEnd".equalsIgnoreCase(sub)) {
            // ========== forceEnd 子命令：强制结束净化之战 ==========
            World world = player.world;
            PurificationWorldData data = PurificationWorldData.get(world);
            int count = 0;
            // 遍历当前世界的所有信标并移除
            for (BlockPos beaconPos : data.getAllBeacons()) {
                PurificationWorldData.BeaconState state = data.getBeacon(beaconPos);
                if (state.dimension == world.provider.getDimension()) {
                    state.battleActive = false;
                    data.removeBeacon(beaconPos);
                    count++;
                }
            }
            if (count > 0) {
                player.sendMessage(new TextComponentString(TextFormatting.GREEN + "已强制结束 " + count + " 个净化之战，并移除信标记录。"));
            } else {
                player.sendMessage(new TextComponentString(TextFormatting.YELLOW + "当前维度没有激活的净化之战。"));
            }
        } else if ("getWarBlock".equalsIgnoreCase(sub)) {
            PurificationWorldData data = PurificationWorldData.get(player.world);
            for (BlockPos beaconPos : data.getAllBeacons()) {
                PurificationWorldData.BeaconState state = data.getBeacon(beaconPos);
                if (state.dimension == player.world.provider.getDimension()) {
                    player.sendMessage(new TextComponentString("信标坐标："+state.beaconPos+"\n"+"核心坐标："+state.corePos));
                    return;
                }
            }
            player.sendMessage(new TextComponentString("坐标未找到"));
        } else {
            throw new WrongUsageException(getUsage(sender));
        }
    }

    @Override
    public List<String> getTabCompletions(MinecraftServer server, ICommandSender sender, String[] args, @Nullable BlockPos targetPos) {
        if (args.length == 1) {
            // 补全子命令
            return getListOfStringsMatchingLastWord(args, SUBCOMMANDS);
        } else if (args.length >= 2) {
            String sub = args[0];
            if ("ForcePlaceNode".equalsIgnoreCase(sub)) {
                if (args.length >= 2 && args.length <= 4) {
                    // 补全坐标 x y z
                    return getTabCompletionCoordinate(args, 1, targetPos);
                } else if (args.length == 5) {
                    // 补全节点类型 1~4
                    return getListOfStringsMatchingLastWord(args, "1", "2", "3", "4");
                }
            } else if ("item".equalsIgnoreCase(sub)) {
                // item 子命令不提供任何补全
                return Collections.emptyList();
            } else if ("forceEnd".equalsIgnoreCase(sub)) {
                // forceEnd 没有后续参数，不提供补全
                return Collections.emptyList();
            } else if ("getWarBlock".equalsIgnoreCase(sub)) {
                // forceEnd 没有后续参数，不提供补全
                return Collections.emptyList();
            }
        }
        return Collections.emptyList();
    }

    @Override
    public boolean isUsernameIndex(String[] args, int index) {
        return false;
    }
}
