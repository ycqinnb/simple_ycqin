package yc.ycqin.nb.network;

import com.dhanantry.scapeandrunparasites.util.ParasiteEventEntity;
import com.dhanantry.scapeandrunparasites.util.ParasiteEventWorld;
import com.dhanantry.scapeandrunparasites.util.config.SRPConfigWorld;
import com.dhanantry.scapeandrunparasites.world.SRPWorldData;
import com.dhanantry.scapeandrunparasites.world.gen.feature.WorldGenParasiteNodeCore;
import net.minecraft.command.CommandBase;
import net.minecraft.command.CommandException;
import net.minecraft.command.ICommandSender;
import net.minecraft.command.WrongUsageException;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.TextComponentString;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.world.Teleporter;
import net.minecraft.world.World;
import net.minecraft.world.WorldServer;
import net.minecraftforge.common.DimensionManager;
import yc.ycqin.nb.common.dim.MirageManager;
import yc.ycqin.nb.common.dim.TeleporterDirect;
import yc.ycqin.nb.config.ModConfig;
import yc.ycqin.nb.world.WorldLevelData;

import javax.annotation.Nullable;
import java.lang.reflect.Field;
import java.util.*;

public class YcCommand extends CommandBase {
    private static final List<String> SUBCOMMANDS = Arrays.asList("ForcePlaceNode","level","mirage");

    @Override
    public String getName() {
        return "ycqin";
    }

    @Override
    public String getUsage(ICommandSender sender) {
        return "/ycqin ForcePlaceNode X Y Z TYPE 强制放置节点\n/ycqin level 强化生物相关 ";
    }

    /**
     * 权限检查：只有白名单内的玩家才允许看到并使用此命令
     */
    @Override
    public boolean checkPermission(MinecraftServer server, ICommandSender sender) {
        if (sender instanceof EntityPlayer) {
            EntityPlayer player = (EntityPlayer) sender;
            return server.getPlayerList().canSendCommands(player.getGameProfile());
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
            forcePlaceNode(world, pos, type);
            player.sendMessage(new TextComponentString(TextFormatting.GREEN + "已在 " + pos.getX() + " " + pos.getY() + " " + pos.getZ() + " 强制放置类型 " + type + " 的寄生节点"));
        }
        //else if ("forceEnd".equalsIgnoreCase(sub)) {
        //    // ========== forceEnd 子命令：强制结束净化之战 ==========
        //    World world = player.world;
        //    PurificationWorldData data = PurificationWorldData.get(world);
        //    int count = 0;
        //    // 遍历当前世界的所有信标并移除
        //    for (BlockPos beaconPos : data.getAllBeacons()) {
        //        PurificationWorldData.BeaconState state = data.getBeacon(beaconPos);
        //        if (state.dimension == world.provider.getDimension()) {
        //            state.battleActive = false;
        //            data.removeBeacon(beaconPos);
        //            count++;
        //        }
        //    }
        //    if (count > 0) {
        //        player.sendMessage(new TextComponentString(TextFormatting.GREEN + "已强制结束 " + count + " 个净化之战，并移除信标记录。"));
        //    } else {
        //        player.sendMessage(new TextComponentString(TextFormatting.YELLOW + "当前维度没有激活的净化之战。"));
        //    }
        //} else if ("getWarBlock".equalsIgnoreCase(sub)) {
        //    PurificationWorldData data = PurificationWorldData.get(player.world);
        //    for (BlockPos beaconPos : data.getAllBeacons()) {
        //        PurificationWorldData.BeaconState state = data.getBeacon(beaconPos);
        //        if (state.dimension == player.world.provider.getDimension()) {
        //            player.sendMessage(new TextComponentString("信标坐标："+state.beaconPos+"\n"+"核心坐标："+state.corePos));
        //            return;
        //        }
        //    }
        //    player.sendMessage(new TextComponentString("坐标未找到"));}
         else if ("level".equalsIgnoreCase(sub)) {
            handleLevelCommand(player, args);
        } else if ("mirage".equalsIgnoreCase(sub)) {
            if (args.length < 2) {
                throw new WrongUsageException("/ycqin mirage leave  退出幻境");
            }
            String action = args[1];
            if ("leave".equalsIgnoreCase(action)) {
                handleLeaveMirage(player);
            } else {
                throw new WrongUsageException("/ycqin mirage leave  退出幻境");
            }
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
            }  else if ("level".equalsIgnoreCase(sub)) {
                if (args.length == 2) {
                    return getListOfStringsMatchingLastWord(args, "add", "set");
                } else if (args.length == 3) {
                    if ("add".equalsIgnoreCase(args[1])) {
                        return getListOfStringsMatchingLastWord(args, "1", "10", "100", "1000");
                    } else if ("set".equalsIgnoreCase(args[1])) {
                        return getListOfStringsMatchingLastWord(args, "0", "200", "600", "1400", "3000", "6200", "12600");
                    }
                }
            } else if ("mirage".equalsIgnoreCase(sub)) {
                if (args.length == 2) {
                    return getListOfStringsMatchingLastWord(args, "leave");
                }
            }
            return Collections.emptyList();
        }

        return Collections.emptyList();
    }

    @Override
    public boolean isUsernameIndex(String[] args, int index) {
        return false;
    }

    private void handleLevelCommand(EntityPlayer player, String[] args) throws CommandException {
        WorldLevelData data = WorldLevelData.get(player.world);

        if (!ModConfig.isEnabledProtect) {
            player.sendMessage(new TextComponentString(TextFormatting.RED + "未开启强化生物"));
        }

        if (args.length < 2) {
            player.sendMessage(new TextComponentString(TextFormatting.GREEN + "当前世界强化等级: " + data.getLevel()));
            player.sendMessage(new TextComponentString(TextFormatting.GREEN + "当前点数: " + data.getPoints()));
            return;
        }

        String action = args[1].toLowerCase();

        switch (action) {
            case "add":
                if (args.length < 3) throw new WrongUsageException("/ycqin level add <点数>");
                int addPoints = parseInt(args[2]);
                data.addPoints(addPoints);
                player.sendMessage(new TextComponentString(TextFormatting.GREEN + "已添加 " + addPoints + " 点，当前点数: " + data.getPoints() + "，等级: " + data.getLevel()));
                break;
            case "set":
                if (args.length < 3) throw new WrongUsageException("/ycqin level set <点数>");
                int newPoints = parseInt(args[2]);
                data.setPoints(newPoints);
                player.sendMessage(new TextComponentString(TextFormatting.GREEN + "已设置点数为 " + newPoints + "，当前等级: " + data.getLevel()));
                break;
            default:
                throw new WrongUsageException("/ycqin level <add|set> [数值]");
        }
    }


    public static void forcePlaceNode(World world, BlockPos pos, int type) {
        try {
            SRPWorldData data = SRPWorldData.get(world);

            // 反射获取私有字段
            Field nodeXField = SRPWorldData.class.getDeclaredField("nodeX");
            Field nodeYField = SRPWorldData.class.getDeclaredField("nodeY");
            Field nodeZField = SRPWorldData.class.getDeclaredField("nodeZ");
            Field nodeTField = SRPWorldData.class.getDeclaredField("nodeT");
            Field nodeAField = SRPWorldData.class.getDeclaredField("nodeA");

            nodeXField.setAccessible(true);
            nodeYField.setAccessible(true);
            nodeZField.setAccessible(true);
            nodeTField.setAccessible(true);
            nodeAField.setAccessible(true);

            List<Integer> nodeX = (List<Integer>) nodeXField.get(data);
            List<Integer> nodeY = (List<Integer>) nodeYField.get(data);
            List<Integer> nodeZ = (List<Integer>) nodeZField.get(data);
            List<Byte> nodeT = (List<Byte>) nodeTField.get(data);
            List<Integer> nodeA = (List<Integer>) nodeAField.get(data);

            // 如果该位置已存在节点，先移除（避免重复）
            for (int i = 0; i < nodeX.size(); i++) {
                if (nodeX.get(i) == pos.getX() && nodeY.get(i) == pos.getY() && nodeZ.get(i) == pos.getZ()) {
                    nodeX.remove(i);
                    nodeY.remove(i);
                    nodeZ.remove(i);
                    nodeT.remove(i);
                    nodeA.remove(i);
                    break;
                }
            }

            // 添加新节点
            nodeX.add(pos.getX());
            nodeY.add(pos.getY());
            nodeZ.add(pos.getZ());
            nodeT.add((byte) type);
            nodeA.add(1); // 激活状态

            // 标记数据已修改
            data.markDirty();

            // 生成节点核心方块
            WorldGenParasiteNodeCore gen = new WorldGenParasiteNodeCore(false, 1, type);
            //尝试调用 generate 方法（根据你的环境可能需要反射调用 func_180709_b）
            try {
                gen.generate(world, new Random(), pos);
            } catch (NoSuchMethodError e) {
                // 如果 generate 方法不存在，尝试使用反射调用 func_180709_b
                java.lang.reflect.Method generateMethod = WorldGenParasiteNodeCore.class.getMethod("func_180709_b", World.class, Random.class, BlockPos.class);
                generateMethod.invoke(gen, world, new Random(), pos);
            }

            // 发送节点生成警告（可选）
            ParasiteEventEntity.alertAllPlayerDim(world, SRPConfigWorld.nodeWarning, 100);
        } catch (Exception e) {
            e.printStackTrace();
            // 如果反射失败，回退到普通方法（可能受到限制）
            // 这里可以记录错误，但战斗继续
            System.out.println("强制放置节点失败，尝试普通放置...");
            int result = ParasiteEventWorld.placeHeartInWorld(world, pos, type);
            if (result != 1) {
                System.out.println("普通放置失败，错误码：" + result);
            }
        }
    }

    private void handleLeaveMirage(EntityPlayer player) {
        if (!MirageManager.isMirageDimension(player.dimension)) {
            player.sendMessage(new TextComponentString(TextFormatting.YELLOW + "你不在幻境中。"));
            return;
        }
        int originalDim = MirageManager.getOriginalDim(player.dimension);
        WorldServer targetWorld = player.getServer().getWorld(originalDim);
        if (targetWorld == null) {
            // 尝试加载世界
            DimensionManager.initDimension(originalDim);
            targetWorld = player.getServer().getWorld(originalDim);
            if (targetWorld == null) {
                player.sendMessage(new TextComponentString(TextFormatting.RED + "目标世界加载失败，请联系管理员。"));
                return;
            }
        }

        // 保留当前坐标，清除幻境标记并传送
        BlockPos currentPos = player.getPosition();
        player.getEntityData().removeTag("inMirage");
        ((EntityPlayerMP) player).changeDimension(originalDim, new TeleporterDirect(targetWorld, currentPos, 0));
        player.sendMessage(new TextComponentString(TextFormatting.GREEN + "你已离开幻境。"));
    }
}
