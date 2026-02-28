package yc.ycqin.nb.event;

import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.TextComponentString;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.world.World;
import net.minecraftforge.event.entity.living.LivingDeathEvent;
import net.minecraftforge.event.world.BlockEvent;
import net.minecraftforge.fml.common.FMLCommonHandler;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;
import yc.ycqin.nb.config.ModConfig;
import yc.ycqin.nb.srpcore.PurificationBattleManager;
import yc.ycqin.nb.srpcore.PurificationWorldData;
import yc.ycqin.nb.register.BlocksRegister;
import yc.ycqin.nb.util.DifficultyHelper;

import java.util.HashSet;
import java.util.Set;

@Mod.EventBusSubscriber
public class PurificationHandler {

    private static final Set<Block> BEACON_BASE_BLOCKS = new HashSet<Block>() {{
        add(Blocks.IRON_BLOCK);
        add(Blocks.GOLD_BLOCK);
        add(Blocks.DIAMOND_BLOCK);
        add(Blocks.EMERALD_BLOCK);
    }};

    private static final int[] BASE_LAYER_SIZES = {3, 5, 7, 9};
    private static int tickCounter = 0;

    private static class BeaconBaseInfo {
        BlockPos bottomPos;
        int layers;
        BeaconBaseInfo(BlockPos bottomPos, int layers) {
            this.bottomPos = bottomPos;
            this.layers = layers;
        }
    }


    // ==================== 事件监听（仅用于激活，不触发提醒） ====================

    @SubscribeEvent
    public static void onBlockPlace(BlockEvent.PlaceEvent event) {
        if (event.getWorld().isRemote) return;
        World world = event.getWorld();
        BlockPos pos = event.getPos();
        Block placedBlock = event.getPlacedBlock().getBlock();

        // 如果是信标放置，尝试激活
        if (placedBlock == Blocks.BEACON) {
            tryActivateBeacon(world, pos);
        }
        // 注意：不再调用 checkIntegrityAndNotify，避免事件触发刷屏
    }

    @SubscribeEvent
    public static void onBlockBreak(BlockEvent.BreakEvent event) {
        if (event.getWorld().isRemote) return;
        // 注意：不再调用 checkIntegrityAndNotify，避免事件触发刷屏
    }

    // ==================== 定时检测（每5 tick） ====================

    @SubscribeEvent
    public static void onServerTick(TickEvent.ServerTickEvent event) {
        if (event.phase == TickEvent.Phase.END) {
            tickCounter++;
            if (tickCounter % 5 == 0) {
                for (World world : FMLCommonHandler.instance().getMinecraftServerInstance().worlds) {
                    PurificationWorldData data = PurificationWorldData.get(world);
                    for (BlockPos beaconPos : data.getAllBeacons()) {
                        PurificationWorldData.BeaconState state = data.getBeacon(beaconPos);
                        // 只处理与当前世界维度匹配的信标
                        if (state.dimension != world.provider.getDimension()) continue;

                        boolean nowIntact = isBeaconIntact(world, state);
                        // 可选的防抖逻辑（如果之前有，可保留）
                        if (nowIntact != state.wasIntact) {
                            if (!nowIntact) {
                                sendMessageToAll(world, TextFormatting.RED + "识别区域被破坏，传输中止，请尽快修复");
                            } else {
                                sendMessageToAll(world, TextFormatting.GREEN + "识别区域已经修复，干得好");
                            }
                            state.wasIntact = nowIntact;
                            data.putBeacon(beaconPos, state);
                        }
                        // 战斗逻辑
                        if (nowIntact && state.battleActive) {
                            PurificationBattleManager.tickBattle(world, beaconPos, state, nowIntact);
                        } else if (!nowIntact && state.battleActive) {
                            // 即使结构损坏，也调用tickBattle（以便继续刷怪和暂停计时）
                            PurificationBattleManager.tickBattle(world, beaconPos, state, nowIntact);
                        }
                    }
                }
            }
        }
    }

    // ==================== 核心逻辑（不变） ====================

    private static boolean tryActivateBeacon(World world, BlockPos beaconPos) {
        // 全局检查：是否已有激活的净化信标（遍历所有世界）
        for (World otherWorld : FMLCommonHandler.instance().getMinecraftServerInstance().worlds) {
            PurificationWorldData otherData = PurificationWorldData.get(otherWorld);
            if (!otherData.getAllBeacons().isEmpty()) {
                world.getMinecraftServer().getPlayerList().sendMessage(new TextComponentString(
                        TextFormatting.RED + "已经有一个净化之战在进行中，无法同时激活多个！"));
                return false;
            }
        }

        BeaconBaseInfo baseInfo = validateBeaconBase(world, beaconPos);
        if (baseInfo == null) return false;

        int centerX = beaconPos.getX();
        int centerZ = beaconPos.getZ();
        int checkY = baseInfo.bottomPos.getY() - 1;

        BlockPos corePos = null;
        for (int dx = -5; dx <= 5; dx++) {
            for (int dz = -5; dz <= 5; dz++) {
                BlockPos pos = new BlockPos(centerX + dx, checkY, centerZ + dz);
                if (world.getBlockState(pos).getBlock() == BlocksRegister.BLOCKFINALSPECIMEN) {
                    corePos = pos;
                    break;
                }
            }
            if (corePos != null) break;
        }
        if (corePos == null) return false;

        generateArena(world, centerX, checkY, centerZ, corePos);

        PurificationWorldData.BeaconState state = new PurificationWorldData.BeaconState();
        state.beaconPos = beaconPos;
        state.bottomBasePos = baseInfo.bottomPos;
        state.baseLayers = baseInfo.layers;
        state.corePos = corePos;
        state.wasIntact = true;
        state.dimension = world.provider.getDimension();
        state.lastIntact = true;      // 初始化防抖字段（可选）
        state.stableCount = 0;
        PurificationWorldData.get(world).putBeacon(beaconPos, state);

        // 原有激活广播
        String msg = String.format("§6[世界净化] 净化信标在 %d, %d, %d 搭建完成！决战即将开始！",
                beaconPos.getX(), beaconPos.getY(), beaconPos.getZ());
        world.getMinecraftServer().getPlayerList().sendMessage(new TextComponentString(msg));

        // ========== 新增：逐条发送末世电台提示 ==========
        new Thread(() -> {
            String[] lines = {
                    "§a世界净化核心解析成功。传统通道搭接成功。样本连接成功。信标对接成功，传输即将开始。\n",
                    "§a样本识别成功，激活者基因特征识别失..成功。条件达成。允许激活天幕。“最终决战”计划开启。\n",
                    "§6通告幸存的人类..吱吱.嘿听众朋友们大家好末世电台收到了您的联络！不要多问！要开打了！\n",
                    "§c请幸存者们迅速集合至X:" + beaconPos.getX() + " Y:" + beaconPos.getY() + " Z:" + beaconPos.getZ() + "、进入黑曜石与原石组成的识别区域内、寄生虫即将发起总攻！\n",
                    "§e在识别区域内坚持20分钟、维持传输！如果场地被破坏、修好它才能继续传输！\n",
                    "§a不要让寄生虫们破坏传输，这场战争可不能再来！\n",
                    "§6成败在此一举、夺回世界就靠你们了！记住，机会只有一次，无法重来！\n",
                    TextFormatting.YELLOW + "只要找对角度，他们就会像黄油一样被切开\n",
                    TextFormatting.LIGHT_PURPLE + "等我回家，亲爱的\n",
                    TextFormatting.GRAY + "已经，没有什么好怕的了\n"
            };
            for (String line : lines) {
                try {
                    Thread.sleep(2000); // 每条消息间隔1秒
                } catch (InterruptedException e) {
                    return;
                }
                world.getMinecraftServer().addScheduledTask(() -> {
                    sendMessageToAll(world, line);
                });
            }
            world.getMinecraftServer().addScheduledTask(() -> {
                boolean keepInventory = world.getGameRules().getBoolean("keepInventory");
                DifficultyHelper.DifficultyResult result = DifficultyHelper.computeDifficultyDetails(world, beaconPos, keepInventory);
                sendMessageToAll(world, TextFormatting.WHITE + "[天幕]难度评估报告正在生成中。\n");
                sendMessageToAll(world, TextFormatting.WHITE + "基础难度：" + result.base + " 修正难度：" + result.correction + " 最终难度：" + result.finalDifficulty+"\n");
                sendMessageToAll(world, TextFormatting.RED + "人类必胜！\n");
                sendMessageToAll(world, TextFormatting.RED + "血债血偿，该还债了，寄生虫们！\n");
            });
            // 设置战斗激活
            state.battleActive = true;
            state.currentPhase = 1;
            state.phaseStartTime = world.getTotalWorldTime();
            state.lastMinuteTick = world.getTotalWorldTime();
            state.lastExtraSpawnTick = world.getTotalWorldTime();
            state.totalScore = ModConfig.baseScore; // 从配置读取

            PurificationWorldData.get(world).markDirty();
            try {
                Thread.sleep(500);
                PurificationBattleManager.startPhase(world, beaconPos, state, 1);
            } catch (InterruptedException e) {
                return;
            }
        }).start();
        // =============================================

        return true;
    }
    private static BeaconBaseInfo validateBeaconBase(World world, BlockPos beaconPos) {
        int baseX = beaconPos.getX();
        int baseZ = beaconPos.getZ();
        int layerIndex = 0;
        int currentY = beaconPos.getY() - 1;
        BlockPos lastValidPos = null;

        while (layerIndex < BASE_LAYER_SIZES.length) {
            int size = BASE_LAYER_SIZES[layerIndex];
            int half = size / 2;
            boolean layerValid = true;
            for (int dx = -half; dx <= half; dx++) {
                for (int dz = -half; dz <= half; dz++) {
                    BlockPos pos = new BlockPos(baseX + dx, currentY, baseZ + dz);
                    Block block = world.getBlockState(pos).getBlock();
                    if (!BEACON_BASE_BLOCKS.contains(block)) {
                        layerValid = false;
                        break;
                    }
                }
                if (!layerValid) break;
            }
            if (layerValid) {
                lastValidPos = new BlockPos(baseX, currentY, baseZ);
                layerIndex++;
                currentY--;
            } else {
                break;
            }
        }
        return lastValidPos != null ? new BeaconBaseInfo(lastValidPos, layerIndex) : null;
    }

    private static void generateArena(World world, int centerX, int y, int centerZ, BlockPos corePos) {
        // 1. 处理核心11x11区域（圆石/黑曜石）
        for (int dx = -5; dx <= 5; dx++) {
            for (int dz = -5; dz <= 5; dz++) {
                BlockPos pos = new BlockPos(centerX + dx, y, centerZ + dz);
                if (pos.equals(corePos)) continue; // 保留核心方块
                boolean isBorder = (dx == -5 || dx == 5 || dz == -5 || dz == 5);
                Block newBlock = isBorder ? Blocks.OBSIDIAN : Blocks.COBBLESTONE;
                world.setBlockState(pos, newBlock.getDefaultState(), 3);
            }
        }

        // 2. 处理扩展33x33区域（寄染石头）
        Block infestedRubble = Block.getBlockFromName("srparasites:infestedrubble");
        if (infestedRubble == null) {
            System.out.println("Warning: srparasites:infestedrubble not found, skipping extended arena generation.");
            return;
        }

        for (int dx = -16; dx <= 16; dx++) {
            for (int dz = -16; dz <= 16; dz++) {
                // 跳过核心11x11区域（-5~5）
                if (dx >= -5 && dx <= 5 && dz >= -5 && dz <= 5) continue;

                BlockPos pos = new BlockPos(centerX + dx, y, centerZ + dz);
                // 只替换空气方块
                if (world.isAirBlock(pos)) {
                    world.setBlockState(pos, infestedRubble.getDefaultState(), 3);
                }
            }
        }
    }

    private static boolean isBeaconIntact(World world, PurificationWorldData.BeaconState state) {
        // 添加维度日志，方便查看是哪个世界在检测
        BlockPos beaconPos = state.beaconPos;
        BlockPos bottom = state.bottomBasePos;      // 最底层中心
        int arenaY = bottom.getY() - 1;
        BlockPos corePos = state.corePos;

        // 1. 信标本身
        Block beaconBlock = world.getBlockState(beaconPos).getBlock();
        if (beaconBlock != Blocks.BEACON) {
            System.out.println("[" + world.provider.getDimension() + "] Beacon missing at " + beaconPos + ", found: " + beaconBlock.getRegistryName());
            return false;
        }

        // 2. 第一层底座（紧贴信标下方，3x3）必须完整且为有效方块
        BlockPos firstLayerCenter = beaconPos.down();
        for (int dx = -1; dx <= 1; dx++) {
            for (int dz = -1; dz <= 1; dz++) {
                BlockPos basePos = new BlockPos(firstLayerCenter.getX() + dx, firstLayerCenter.getY(), firstLayerCenter.getZ() + dz);
                Block block = world.getBlockState(basePos).getBlock();
                if (!BEACON_BASE_BLOCKS.contains(block)) {
                    System.out.println("[" + world.provider.getDimension() + "] First layer base missing at " + basePos + ", found: " + block.getRegistryName());
                    return false;
                }
            }
        }

        // 3. 守卫场地检测
        for (int dx = -5; dx <= 5; dx++) {
            for (int dz = -5; dz <= 5; dz++) {
                BlockPos groundPos = new BlockPos(bottom.getX() + dx, arenaY, bottom.getZ() + dz);
                BlockPos abovePos = groundPos.up();

                Block groundBlock = world.getBlockState(groundPos).getBlock();
                if (groundPos.equals(corePos)) {
                    if (groundBlock != BlocksRegister.BLOCKFINALSPECIMEN) {
                        System.out.println("[" + world.provider.getDimension() + "] Core block missing at " + groundPos + ", found: " + groundBlock.getRegistryName());
                        return false;
                    }
                } else {
                    if (groundBlock != Blocks.COBBLESTONE && groundBlock != Blocks.OBSIDIAN && groundBlock != BlocksRegister.BLOCKSPCELL && groundBlock != BlocksRegister.BLOCKSPINFECT) {
                        System.out.println("[" + world.provider.getDimension() + "] Arena ground wrong at " + groundPos + ", found: " + groundBlock.getRegistryName());
                        return false;
                    }
                }

                IBlockState aboveState = world.getBlockState(abovePos);
                Block aboveBlock = aboveState.getBlock();
                if (!aboveState.getBlock().isAir(aboveState, world, abovePos) && aboveBlock != Blocks.TORCH && aboveBlock != BlocksRegister.BLOCKSPCELL && aboveBlock != BlocksRegister.BLOCKSPINFECT) {
                    // 判断是否属于底座层
                    boolean isBaseLayer = false;
                    int baseMinY = bottom.getY();
                    int baseMaxY = beaconPos.getY() - 1;
                    if (abovePos.getY() >= baseMinY && abovePos.getY() <= baseMaxY) {
                        int layerIndex = abovePos.getY() - baseMinY; // 从0开始（最底层）
                        int totalLayers = baseMaxY - baseMinY + 1;
                        int layerSize = BASE_LAYER_SIZES[totalLayers - 1 - layerIndex];
                        int half = layerSize / 2;
                        int dx2 = abovePos.getX() - bottom.getX();
                        int dz2 = abovePos.getZ() - bottom.getZ();
                        if (dx2 >= -half && dx2 <= half && dz2 >= -half && dz2 <= half) {
                            isBaseLayer = true;
                        }
                    }
                    if (!isBaseLayer) {
                        System.out.println("[" + world.provider.getDimension() + "] Obstacle above arena at " + abovePos + ", block: " + aboveBlock.getRegistryName());
                        return false;
                    }
                }
            }
        }
        return true;
    }

    private static void sendMessageToAll(World world, String msg) {
        if (world.getMinecraftServer() != null) {
            world.getMinecraftServer().getPlayerList().sendMessage(new TextComponentString(msg));
        }
    }

    @SubscribeEvent
    public static void onLivingDeath(LivingDeathEvent event) {
        if (event.getEntity().world.isRemote) return;
        if (!(event.getEntity() instanceof EntityPlayer)) return;

        EntityPlayer player = (EntityPlayer) event.getEntity();
        World world = player.world;
        PurificationWorldData data = PurificationWorldData.get(world);

        // 遍历当前世界的所有净化信标
        for (BlockPos beaconPos : data.getAllBeacons()) {
            PurificationWorldData.BeaconState state = data.getBeacon(beaconPos);
            if (state.dimension != world.provider.getDimension()) continue;
            if (!state.battleActive) continue;

            // 扣除点数
            state.totalScore -= ModConfig.deathPenalty;
            state.playerDeaths++;
            data.markDirty();

            // 检查是否触发失败结局
            if (state.totalScore < 0 && !state.badEndTriggered) {
                state.badEndTriggered = true;
                PurificationBattleManager.finishBattle(world, beaconPos, state, false);
            }
            break; // 一个维度同时只能有一个战斗，找到即停止
        }
    }
}