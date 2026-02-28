package yc.ycqin.nb.srpcore;

import com.dhanantry.scapeandrunparasites.block.BlockParasiteSpreading;
import com.dhanantry.scapeandrunparasites.util.ParasiteEventEntity;
import com.dhanantry.scapeandrunparasites.util.ParasiteEventWorld;
import com.dhanantry.scapeandrunparasites.util.config.SRPConfigWorld;
import com.dhanantry.scapeandrunparasites.world.SRPSaveData;
import com.dhanantry.scapeandrunparasites.world.SRPWorldData;
import com.dhanantry.scapeandrunparasites.world.gen.feature.WorldGenParasiteNodeCore;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLiving;
import net.minecraft.entity.SharedMonsterAttributes;
import net.minecraft.entity.ai.attributes.IAttributeInstance;
import net.minecraft.entity.effect.EntityLightningBolt;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.TextComponentString;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.world.World;
import net.minecraft.world.biome.Biome;
import net.minecraftforge.fml.common.FMLCommonHandler;
import net.minecraftforge.fml.common.registry.EntityEntry;
import net.minecraftforge.fml.common.registry.ForgeRegistries;
import yc.ycqin.nb.config.ModConfig;
import yc.ycqin.nb.util.DifficultyHelper;
import yc.ycqin.nb.srpcore.PurificationWorldData;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class PurificationBattleManager {


    // 阶段名称和副标题（用于提示）
    private static final String[] PHASE_NAMES = {"纯种之怒", "始祖降临", "适应来袭", "同化出战", "天外来敌"};
    private static final String[] PHASE_SUBTITLES = {"卓越为路", "狂化迎击", "诡化显型", "劫持现身", "衍生降临"};
    // ==================== 生物种类列表（请根据实际模组填充注册名） ====================

    // 始祖种
    public static final String[] ANCESTOR_SPECIES = {
            "srparasites:anc_dreadnaut",  // 远古惧魔
            "srparasites:anc_overlord"    // 远古君魔
    };

    // 卓越种
    public static final String[] EXCELLENT_SPECIES = {
            "srparasites:bomber_heavy",   // 重型轰炸兽
            "srparasites:carrier_colony", // 聚生载体
            "srparasites:wraith",         // 幽鬼体
            "srparasites:bogle",          // 怖怪体
            "srparasites:haunter",// 逐猎兽
            "srparasites:airscrew"
                   // 援助泡
            // 以下两个需要确认是否属于卓越种（百科未列出，但lang中存在）
            //"srparasites:seeker",         // Seeker
            //"srparasites:architect"       // Architect
    };

    // 纯粹种
    public static final String[] PURE_SPECIES = {
            "srparasites:vigilante",      // 巡兽
            "srparasites:warden",         // 看守兽
            "srparasites:overseer",       // 监察兽
            "srparasites:bomber_light",   // 轻型轰炸兽
            "srparasites:marauder",       // 掠夺兽
            "srparasites:grunt",          // 步行兽
            "srparasites:monarch",        // 统御兽
    };

    // 适应种
    public static final String[] ADAPTED_SPECIES = {
            "srparasites:ada_longarms",
            "srparasites:ada_summoner",
            "srparasites:ada_manducater",
            "srparasites:ada_reeker",
            "srparasites:ada_yelloweye",
            "srparasites:ada_bolster",
            "srparasites:ada_arachnida",
            "srparasites:ada_vermin",
            "srparasites:ada_viscera"
    };

    // 原始种
    public static final String[] PRIMITIVE_SPECIES = {
            "srparasites:pri_longarms",
            "srparasites:pri_manducater",
            "srparasites:pri_summoner",
            "srparasites:pri_reeker",
            "srparasites:pri_yelloweye",
            "srparasites:pri_bolster",
            "srparasites:pri_arachnida",
            "srparasites:pri_devourer",
            "srparasites:pri_vermin",
            "srparasites:pri_burrower",
            "srparasites:pri_viscera",
            "srparasites:pri_tozoon"
    };

    // 狂化种
    public static final String[] FERAL_SPECIES = {
            "srparasites:fer_cow",
            "srparasites:fer_bear",
            "srparasites:fer_enderman",
            "srparasites:fer_human",
            "srparasites:fer_horse",
            "srparasites:fer_sheep",
            "srparasites:fer_pig",
            "srparasites:fer_villager",
            "srparasites:fer_wolf"
    };

    // 劫持种
    public static final String[] HIJACKED_SPECIES = {
            "srparasites:hi_blaze",
            "srparasites:hi_golem",
            "srparasites:hi_skeleton"
    };

    // 同化种（所有 sim_ 前缀）
    public static final String[] ASSIMILATED_SPECIES = {
            "srparasites:sim_bear",
            "srparasites:sim_squid",
            "srparasites:sim_enderman",
            "srparasites:sim_adventurer",
            "srparasites:sim_bigspider",
            "srparasites:sim_human",
            "srparasites:sim_villager",
            "srparasites:sim_cow",
            "srparasites:sim_horse",
            "srparasites:sim_pig",
            "srparasites:sim_sheep",
            "srparasites:sim_wolf",
            "srparasites:sim_dragone",
    };
    //诡化种
    public static final String[] MAR_SPECIES = {
            "srparasites:mar_sheep",
            "srparasites:mar_cow",
            "srparasites:mar_villager",
            "srparasites:mar_human",
            "srparasites:mar_enderman",
            "srparasites:mar_bear"
    };

    // 先天种
    public static final String[] CONGENITAL_SPECIES = {
            // 狂疫蚊
            "srparasites:buglin",         // 虫灵
            "srparasites:rupter",         // 裂兽
            "srparasites:mangler",        // 凶裂兽
            "srparasites:carrier_light",  // 轻型母体
            "srparasites:carrier_heavy",  // 重型母体
            "srparasites:carrier_flying"  // 飞行母体
    };
    public static final String[] PILLARS_PHASE2 = {
            "srparasites:beckon_siv",
            "srparasites:dispatcher_siv",
            "srparasites:rooter_siv"
    };

    // 五阶段特殊生物（麒麟和邪狱龙）
    public static final String[] BOSS_PHASE5 = {
            "srparasites:kirin",        // 麒麟
            "srparasites:draconite"     // 邪狱龙
    };
    /**
     * 获取指定阶段的持续时间（tick）
     */
    private static int getPhaseDurationTicks(int phase) {
        switch (phase) {
            case 1: return ModConfig.phase1Duration * 20;
            case 2: return ModConfig.phase2Duration * 20;
            case 3: return ModConfig.phase3Duration * 20;
            case 4: return ModConfig.phase4Duration * 20;
            case 5: return ModConfig.phase5Duration * 20;
            default: return 0;
        }
    }

    /**
     * 获取指定阶段的持续时间（秒）
     */
    private static int getPhaseDurationSeconds(int phase) {
        switch (phase) {
            case 1: return ModConfig.phase1Duration;
            case 2: return ModConfig.phase2Duration;
            case 3: return ModConfig.phase3Duration;
            case 4: return ModConfig.phase4Duration;
            case 5: return ModConfig.phase5Duration;
            default: return 0;
        }
    }

    /**
     * 处理阶段结束（切换阶段或战斗结束）
     */
    private static void handlePhaseEnd(World world, BlockPos beaconPos, PurificationWorldData.BeaconState state) {
        int currentPhase = state.currentPhase;

        if (currentPhase == 4) {
            // 第四阶段结束前触发隐藏剧情（如果尚未触发）
            if (!state.hiddenStageTriggered) {
                triggerHiddenStage(world, beaconPos, state);
                state.hiddenStageTriggered = true;
            }
        }

        int nextPhase = currentPhase + 1;
        if (nextPhase <= 5) {
            // 进入下一阶段
            startPhase(world, beaconPos, state, nextPhase);
        } else {
            // 所有阶段完成，战斗胜利
            finishBattle(world, beaconPos, state, true);
        }
    }

    public static void tickBattle(World world, BlockPos beaconPos, PurificationWorldData.BeaconState state, boolean structureIntact) {
        if (!state.battleActive) return;

        boolean hasPlayer = hasPlayerOnPlatform(world, beaconPos, state);
        boolean canProgress = structureIntact && hasPlayer;

        long worldTime = world.getTotalWorldTime();
        int phase = state.currentPhase;

        // ===== 1. 分钟刷怪（始终进行） =====
        if (worldTime - state.lastMinuteTick >= 1200) {
            state.lastMinuteTick = worldTime;

            // 先检查无人守点扣分（每分钟）
            if (!hasPlayerOnPlatform(world, beaconPos, state)) {
                state.totalScore -= ModConfig.unattendedPenalty;
                if (state.totalScore < 0 && !state.badEndTriggered) {
                    state.badEndTriggered = true;
                    finishBattle(world, beaconPos, state, false);
                    return; // 战斗已结束，不再继续
                }
            }

            // 再生成分钟刷怪
            spawnMinuteWave(world, beaconPos, state);
        }

        // 2. 额外增援（始终进行）
        if (phase == 2 || phase == 3) {
            if (worldTime - state.lastExtraSpawnTick >= 3000) {
                state.lastExtraSpawnTick = worldTime;
                spawnExtraReinforcement(world, beaconPos, state);
            }
        }

        // 3. 阶段计时与剧情触发
        if (canProgress) {
            // 恢复暂停状态
            if (state.pausedElapsed > 0) {
                state.phaseStartTime = worldTime - state.pausedElapsed;
                state.pausedElapsed = 0;
            }

            long elapsed = worldTime - state.phaseStartTime;
            int durationTicks = getPhaseDurationTicks(phase);
            long remainingTicks = durationTicks - elapsed;

            if (phase == 2 && !state.phase2DialogueTriggered && remainingTicks <= 4 * 20) {
                state.phase2DialogueTriggered = true;
                // 异步播放两句台词，间隔1秒
                new Thread(() -> {
                    String[] lines = {
                            "加油，天幕武器支援马上抵达\n",
                            "看卫星支援把它们全打死\n"
                    };
                    for (String line : lines) {
                        try { Thread.sleep(1500); } catch (InterruptedException e) { return; }
                        world.getMinecraftServer().addScheduledTask(() -> {
                            sendMessageToAll(world, TextFormatting.GOLD + line);
                        });
                    }
                }).start();
            }

            // 第4阶段提前9秒触发隐藏剧情
            if (phase == 4 && !state.hiddenStageTriggered && elapsed >= durationTicks - 9 * 20) {
                triggerHiddenStage(world, beaconPos, state);
                state.hiddenStageTriggered = true;
            }

            // 阶段结束检测
            if (elapsed >= durationTicks) {
                handlePhaseEnd(world, beaconPos, state);
            }
        } else {
            // 暂停时记录已耗时
            if (state.pausedElapsed == 0) {
                state.pausedElapsed = worldTime - state.phaseStartTime;
            }
        }

        // 4. 更新ActionBar
        updateActionBar(world, beaconPos, state, canProgress, structureIntact, hasPlayer);
    }

    /**
     * 检测玩家是否在11×11平台区域内
     */
    private static boolean hasPlayerOnPlatform(World world, BlockPos beaconPos, PurificationWorldData.BeaconState state) {
        int minX = state.bottomBasePos.getX() - 5;
        int maxX = state.bottomBasePos.getX() + 5;
        int minZ = state.bottomBasePos.getZ() - 5;
        int maxZ = state.bottomBasePos.getZ() + 5;

        for (EntityPlayer player : world.playerEntities) {
            if (player.isDead || player.getHealth() <= 0) continue;
            BlockPos p = player.getPosition();
            if (p.getX() >= minX && p.getX() <= maxX &&
                    p.getZ() >= minZ && p.getZ() <= maxZ) {
                return true;
            }
        }
        return false;
    }

    /**
     * 更新ActionBar显示（波数、剩余秒数、暂停原因）
     */
    private static void updateActionBar(World world, BlockPos beaconPos, PurificationWorldData.BeaconState state,
                                        boolean canProgress, boolean structureIntact, boolean hasPlayer) {
        if (world.getMinecraftServer() == null) return;

        int phase = state.currentPhase;
        String waveText = (phase <= 4) ? phase + "/4" : "5/4";

        // 计算当前已耗时（考虑暂停）
        long worldTime = world.getTotalWorldTime();
        long elapsed;
        if (canProgress) {
            elapsed = worldTime - state.phaseStartTime;
        } else {
            elapsed = state.pausedElapsed; // 暂停时使用记录的已耗时
        }
        int totalSec = getPhaseDurationSeconds(phase);
        int remainingSec = totalSec - (int)(elapsed / 20);
        if (remainingSec < 0) remainingSec = 0;

        // 暂停原因
        String pauseReason = "";
        if (!canProgress) {
            if (!structureIntact) pauseReason = " §c[结构损坏，计时暂停]";
            else if (!hasPlayer) pauseReason = " §c[无人守点，计时暂停]";
        }
        String actionBar = "";
        if (phase <= 3) {
            actionBar = String.format("§e当前波次: %s  下一波剩余: %d秒" + pauseReason, waveText, remainingSec);
        } else {
            actionBar = String.format("§e当前波次: %s  距离结束剩余剩余: %d秒" + pauseReason, waveText, remainingSec);
        }
        for (EntityPlayer player : world.playerEntities) {
            player.sendStatusMessage(new TextComponentString(actionBar), true);
        }
    }

    /**
     * 开始一个新阶段
     */
    public static void startPhase(World world, BlockPos beaconPos, PurificationWorldData.BeaconState state, int phase) {
        state.currentPhase = phase;
        state.phaseStartTime = world.getTotalWorldTime();
        state.pausedElapsed = 0; // 重置暂停记录

        // 阶段提示
        String msg = String.format("§e第%d阶段：%s，%s",
                phase, PHASE_NAMES[phase-1], PHASE_SUBTITLES[phase-1]);
        sendMessageToAll(world, msg);
        if (phase == 2) {
            spawnParasiteNodes(world, beaconPos);
            new Thread(() -> {
                String line = "节点之间一定会有间隔，而附近的节点一定就在附近！正确的废话！总之在附近\n";
                    try { Thread.sleep(10000); } catch (InterruptedException e) { return; }
                    world.getMinecraftServer().addScheduledTask(() -> {
                        sendMessageToAll(world, TextFormatting.BLUE + line);
                    });
                try { Thread.sleep(10000); } catch (InterruptedException e) { return; }
                world.getMinecraftServer().addScheduledTask(() -> {
                    sendMessageToAll(world, TextFormatting.BLUE + "天幕兄弟，你的卫星武器准备好了吗？还有6分钟，听到了吗，听众朋友们！他说还有6分钟，坚持住！");
                });

            }).start();
        };
        if (phase == 3) {
            clearParasitesInRadius(world, beaconPos);
        }
        // 生成初始怪物（Debug）
        spawnInitialWave(world, beaconPos, state);

        // 重置超时标记
        if (phase == 2) state.secondWaveTimeout = false;
        if (phase == 4) state.fourthWaveTimeout = false;

        PurificationWorldData.get(world).markDirty();
    }

    private static void spawnPhaseMobs(World world, BlockPos beaconPos, int phase, int baseCount, int extraCount) {
        int totalCount = baseCount + extraCount;
        if (totalCount <= 0) return;

        // 获取该阶段对应的生物列表（数组的数组，每个子数组代表一类生物）
        List<String[]> mobLists = new ArrayList<>();
        switch (phase) {
            case 1:
                mobLists.add(PURE_SPECIES);
                mobLists.add(EXCELLENT_SPECIES);
                mobLists.add(CONGENITAL_SPECIES);
                break;
            case 2:
                mobLists.add(ANCESTOR_SPECIES);
                mobLists.add(FERAL_SPECIES);
                // 额外生成柱子（单独处理，不计入 totalCount 或者计入？我们单独生成柱子）
                spawnPillars(world, beaconPos, phase); // 柱子生成独立调用

                break;
            case 3:
                mobLists.add(ADAPTED_SPECIES);
                mobLists.add(MAR_SPECIES);
                break;
            case 4:
                mobLists.add(ASSIMILATED_SPECIES);
                mobLists.add(HIJACKED_SPECIES);
                break;
            case 5:
                mobLists.add(PURE_SPECIES);
                mobLists.add(ADAPTED_SPECIES);
                mobLists.add(ANCESTOR_SPECIES);
                mobLists.add(PRIMITIVE_SPECIES);
                mobLists.add(FERAL_SPECIES);
                mobLists.add(ASSIMILATED_SPECIES);
                mobLists.add(HIJACKED_SPECIES);
                mobLists.add(EXCELLENT_SPECIES);
                mobLists.add(CONGENITAL_SPECIES);
                mobLists.add(MAR_SPECIES);
                // 额外生成强化麒麟和邪狱龙（单独处理）
                spawnBosses(world, beaconPos, phase);
                break;
            default:
                return;
        }

        // 如果没有怪物列表，则返回
        if (mobLists.isEmpty()) return;

        // 随机生成 totalCount 只怪物，从所有可用列表中随机选择种类
        Random rand = world.rand;
        for (int i = 0; i < totalCount; i++) {
            // 随机选择一个生物列表
            String[] chosenList = mobLists.get(rand.nextInt(mobLists.size()));
            // 从该列表中随机选择一个生物注册名
            String mobName = chosenList[rand.nextInt(chosenList.length)];
            // 生成单个生物
            spawnSingleMob(world, beaconPos, mobName);
        }
    }

    /**
     * 生成单个生物
     */
    private static void spawnSingleMob(World world, BlockPos beaconPos, String mobName) {
        EntityEntry entry = ForgeRegistries.ENTITIES.getValue(new ResourceLocation(mobName));
        if (entry == null) {
            System.out.println("[Purification] 未知生物注册名: " + mobName);
            return;
        }
        EntityLiving entity = (EntityLiving) entry.newInstance(world);
        if (entity == null) return;

        // 设置生成位置（信标周围随机偏移）
        Random rand = world.rand;
        int range = 20; // 生成范围半径
        double x = beaconPos.getX() + (rand.nextDouble() - 0.5) * 2 * range;
        double y = beaconPos.getY(); // 可适当调整高度
        double z = beaconPos.getZ() + (rand.nextDouble() - 0.5) * 2 * range;
        entity.setPosition(x, y, z);

        world.spawnEntity(entity);
    }

    /**
     * 生成二阶段柱子（固定数量或按需）
     */
    private static void spawnPillars(World world, BlockPos beaconPos, int phase) {
        // 生成所有柱子各一只（可根据需要调整数量）
        for (String pillarName : PILLARS_PHASE2) {
            spawnSingleMob(world, beaconPos, pillarName);
        }
    }

    /**
     * 生成五阶段特殊BOSS（麒麟、邪狱龙），并强化10倍属性
     */
    private static void spawnBosses(World world, BlockPos beaconPos, int phase) {
        for (String bossName : BOSS_PHASE5) {
            EntityEntry entry = ForgeRegistries.ENTITIES.getValue(new ResourceLocation(bossName));
            if (entry == null) {
                System.out.println("[Purification] 未知BOSS注册名: " + bossName);
                continue;
            }
            EntityLiving entity = (EntityLiving) entry.newInstance(world);
            if (entity == null) continue;

            // 强化属性（攻击力、防御力、生命值提升10倍）
            enhanceBoss(entity, 10.0);

            // 设置生成位置（可生成在信标附近）
            Random rand = world.rand;
            double x = beaconPos.getX() + (rand.nextDouble() - 0.5) * 20;
            double y = beaconPos.getY() + 5; // 略高一点
            double z = beaconPos.getZ() + (rand.nextDouble() - 0.5) * 20;
            entity.setPosition(x, y, z);

            world.spawnEntity(entity);
        }
    }

    /**
     * 强化BOSS属性
     * @param entity 生物实体
     * @param multiplier 倍数（10.0）
     */
    private static void enhanceBoss(EntityLiving entity, double multiplier) {
        // 攻击力（通用攻击伤害属性）
        IAttributeInstance attackAttr = entity.getAttributeMap().getAttributeInstance(SharedMonsterAttributes.ATTACK_DAMAGE);
        if (attackAttr != null) {
            double base = attackAttr.getBaseValue();
            attackAttr.setBaseValue(base * multiplier);
        }

        // 盔甲值（防御力）
        IAttributeInstance armorAttr = entity.getAttributeMap().getAttributeInstance(SharedMonsterAttributes.ARMOR);
        if (armorAttr != null) {
            double base = armorAttr.getBaseValue();
            armorAttr.setBaseValue(base * multiplier);
        }

        // 最大生命值
        IAttributeInstance healthAttr = entity.getAttributeMap().getAttributeInstance(SharedMonsterAttributes.MAX_HEALTH);
        if (healthAttr != null) {
            double base = healthAttr.getBaseValue();
            healthAttr.setBaseValue(base * multiplier);
            entity.setHealth((float) (base * multiplier)); // 设置当前生命为新的最大值
        }
    }

    private static void spawnInitialWave(World world, BlockPos beaconPos, PurificationWorldData.BeaconState state) {
        // 初始波次：使用基础刷怪量（例如玩家人数决定的量）作为 totalCount
        int playerCount = world.playerEntities.size();
        int baseCount = getBaseSpawnForPlayerCount(playerCount);
        int extraCount = 0; // 初始波次暂不加入难度额外数量，或者也可以加
        spawnPhaseMobs(world, beaconPos, state.currentPhase, baseCount, extraCount);
    }

    private static void spawnMinuteWave(World world, BlockPos beaconPos, PurificationWorldData.BeaconState state) {
        boolean keepInventory = world.getGameRules().getBoolean("keepInventory");
        DifficultyHelper.DifficultyResult diff = DifficultyHelper.computeDifficultyDetails(world, beaconPos, keepInventory);

        int playerCount = world.playerEntities.size();
        int baseSpawn = getBaseSpawnForPlayerCount(playerCount);
        int extraSpawn = diff.finalDifficulty * ModConfig.difficultyFactor;

        // 每分钟刷怪也调用 spawnPhaseMobs
        spawnPhaseMobs(world, beaconPos, state.currentPhase, baseSpawn, extraSpawn);
    }

    private static void spawnExtraReinforcement(World world, BlockPos beaconPos, PurificationWorldData.BeaconState state) {
        spawnSingleMob(world,beaconPos,"srparasites:anc_dreadnaut");
        spawnSingleMob(world,beaconPos,"srparasites:anc_overlord");
    }

    private static int getBaseSpawnForPlayerCount(int playerCount) {
        int[] arr = ModConfig.baseSpawnByPlayerCount;
        int index = Math.min(playerCount, arr.length - 1);
        return arr[index];
    }

    // ===== 隐藏阶段剧情 =====
    private static void triggerHiddenStage(World world, BlockPos beaconPos, PurificationWorldData.BeaconState state) {
        new Thread(() -> {
            String[] lines = {
                    "天幕老弟，武器好了吗？我需要支援。\n",
                    "马上好，净化也马上结束了，寄生虫，轻而易举啊！\n",
                    "坏了坏了，宇宙中的寄生虫发现我了，卫星武器被摧毁......\n",
                    "但，传输装置仍然完好，我们还有一线生机，加油，打败他们，人类必胜！\n"
            };
            for (String line : lines) {
                try { Thread.sleep(2000); } catch (InterruptedException e) { return; }
                world.getMinecraftServer().addScheduledTask(() -> {
                    sendMessageToAll(world, TextFormatting.YELLOW + line);
                });
            }
        }).start();
    }

    // ===== 战斗结束 =====
    public static void finishBattle(World world, BlockPos beaconPos, PurificationWorldData.BeaconState state, boolean victory) {
        state.battleActive = false;
        clearParasitesInRadius(world,beaconPos);
        sendMessageToAll(world,TextFormatting.BLUE+"天幕应急武器成功启用\n");
        if (victory) {
            new Thread(() -> {
                String[] lines = {
                        TextFormatting.BLUE+"本次样本已完全解析，世界已完全净化。“最终决战”计划完成，天幕转入休眠。恭喜幸存的人类\n",
                        TextFormatting.BLUE+"恭喜你们，在这不讲道理的末世里找到了活下去的办法。\n",
                        TextFormatting.BLUE+"狂妄的寄生兽们就像计划一样，向大地伸出了贪婪的手掌，自己暴露了庞大的身躯，不再潜伏。\n",
                        TextFormatting.BLUE+"而你们成功注意到了失落的人类文明，留下了的些许手段。即使偏离计划，也将他们一网打尽，成功消灭了\n",
                        TextFormatting.BLUE+"这里将不再受到寄生兽的污染，但一切的伤痕都将留在这片土地上，除非，你动手去改变它\n",
                        TextFormatting.BLUE+"我们无法装作一切都未开始，也无法装作一切都已结束，但哪怕是些许的希望，哪怕只是片刻的安宁，人类也会挣扎着生存吧······\n",
                        TextFormatting.BLUE+"无论时代如何变迁，无论未来如何迷茫，人类都会为了迈向未来，而在过去、亦或此刻，刻下努力的证明。\n",
                        TextFormatting.YELLOW+"也许在不久的未来，人类又会迎接下一次的危机，也许，一切都在不断的重演。肉体可死，但思想不灭。\n",
                        TextFormatting.YELLOW+"薪火相传，活下去吧，人类，挣扎着、哭着、笑着，迈向未来吧\n",
                        TextFormatting.YELLOW+"称呼自己为英雄也好，称呼自己为救世主也罢。就像一直以来一样，做出你的选择，决定前进的方向。\n",
                        TextFormatting.WHITE+"本次播报到此结束，下次再见，人类！\n"
                };
                for (String line : lines) {
                    try { Thread.sleep(6000); } catch (InterruptedException e) { return; }
                    world.getMinecraftServer().addScheduledTask(() -> {
                        sendMessageToAll(world, line);
                    });
                }
                sendMessageToAll(world, TextFormatting.BLUE + "[天幕]结局条件检测+\n+剩余点数："+state.totalScore);
                try { Thread.sleep(2000); } catch (InterruptedException e) { return; }
                world.getMinecraftServer().addScheduledTask(() -> {
                    sendMessageToAll(world, TextFormatting.YELLOW+"[达成这个结局说明小队成员配合非常完善，即使再强大的寄生兽也没有彻底推毁人类的勇气！]\n"+TextFormatting.BLUE+"[解锁结局：勇气赞歌]");
                });

            }).start();
            ensurePhase(world);

            // 后续：设置演化点数-200，等级-2
        } else {
            new Thread(() -> {
                String[] lines = {
                        TextFormatting.RED+"最近，我一直在做梦，梦里的世界逐渐被血红色污染。\n",
                        TextFormatting.YELLOW+"转动的柱子扭曲着大地，亘古的君主霸占了天空。\n",
                        TextFormatting.GREEN+"被唤作“火种“的英雄教会了人们种植与采集，教会了人们搭建净化信标，甚至教会了人们免于死亡惩罚的办法。\n",
                        TextFormatting.RED+"然而，死亡的代价不可避免，灵魂的破损不可修复，世界的净化不可逆转。\n",
                        TextFormatting.RED+"唯有.."+TextFormatting.OBFUSCATED+"aaey"+TextFormatting.RESET+TextFormatting.RED+"寄生永生！\n",
                        TextFormatting.WHITE+"早上好，末日城，今天又是崭新的一天，诶等等，至少不是今天。\n",
                        "由于在净化之战中灵魂磨损过多，火种的持有者选择了将各位转移到新世界。\n",
                        "也许未来这块土地也会被再次污染，未来的英雄们会做出如何的选择呢？\n",
                        TextFormatting.BLUE+"也终有人，会沉醉在湛蓝色的夜空中。\n",
                        TextFormatting.YELLOW+"[达成这个结局说明净化之战人类方死亡次数过多或拖延时间过长，也许还有其他拯救的方法...?]\n",
                        TextFormatting.BLUE+"[解锁结局：寄生永生]\n"

                };
                for (String line : lines) {
                    try { Thread.sleep(6000); } catch (InterruptedException e) { return; }
                    world.getMinecraftServer().addScheduledTask(() -> {
                        sendMessageToAll(world, line);
                    });
                }

            }).start();
            ensureFailed(world,beaconPos);
        }
        PurificationWorldData.get(world).removeBeacon(beaconPos); // 或标记但不移除
    }

    private static void sendMessageToAll(World world, String msg) {
        if (world.getMinecraftServer() != null) {
            world.getMinecraftServer().getPlayerList().sendMessage(new TextComponentString(msg));
        }
    }


    /**
     * 强制在指定位置生成一个寄生节点（无视所有限制，使用反射）
     */
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
            System.out.println("[Purification] 强制放置节点失败，尝试普通放置...");
            int result = ParasiteEventWorld.placeHeartInWorld(world, pos, type);
            if (result != 1) {
                System.out.println("[Purification] 普通放置失败，错误码：" + result);
            }
        }
    }

    private static void spawnParasiteNodes(World world, BlockPos beaconPos) {
        int targetCount = 5;
        int attempts = 0;
        int maxAttempts = 300; // 增加尝试次数确保成功
        int placed = 0;
        Random rand = world.rand;

        // 平台区域（11x11）边界
        int platformMinX = beaconPos.getX() - 5;
        int platformMaxX = beaconPos.getX() + 5;
        int platformMinZ = beaconPos.getZ() - 5;
        int platformMaxZ = beaconPos.getZ() + 5;

        while (placed < targetCount && attempts < maxAttempts) {
            // 随机半径（96~128）
            attempts++;
            int radius = 512 + rand.nextInt(465); // 81 = 128-48+1
            double angle = rand.nextDouble() * 2 * Math.PI;
            int dx = (int) (Math.cos(angle) * radius);
            int dz = (int) (Math.sin(angle) * radius);
            int x = beaconPos.getX() + dx;
            int z = beaconPos.getZ() + dz;

            // 跳过平台区域
            if (x >= platformMinX && x <= platformMaxX && z >= platformMinZ && z <= platformMaxZ) {
                continue;
            }

            // 获取地表高度
            BlockPos groundPos = world.getTopSolidOrLiquidBlock(new BlockPos(x, 0, z));
            int y = groundPos.getY();

            // 检查该坐标是否已存在节点（避免重复）
            SRPWorldData data = SRPWorldData.get(world);
            boolean exists = false;
            try {
                // 使用反射检查节点列表（也可以直接调用 data 的 getNode 方法，如果有的话）
                Field nodeXField = SRPWorldData.class.getDeclaredField("nodeX");
                nodeXField.setAccessible(true);
                List<Integer> nodeX = (List<Integer>) nodeXField.get(data);
                Field nodeYField = SRPWorldData.class.getDeclaredField("nodeY");
                nodeYField.setAccessible(true);
                List<Integer> nodeY = (List<Integer>) nodeYField.get(data);
                Field nodeZField = SRPWorldData.class.getDeclaredField("nodeZ");
                nodeZField.setAccessible(true);
                List<Integer> nodeZ = (List<Integer>) nodeZField.get(data);
                for (int i = 0; i < nodeX.size(); i++) {
                    if (nodeX.get(i) == x && nodeY.get(i) == y && nodeZ.get(i) == z) {
                        exists = true;
                        break;
                    }
                }
            } catch (Exception e) {
                // 如果反射失败，暂时当作不存在，继续尝试
                e.printStackTrace();
            }
            if (exists) continue;

            // 确定节点类型（基于生物群系）
            int type = determineNodeType(world, new BlockPos(x, y, z));

            // 强制放置节点
            forcePlaceNode(world, new BlockPos(x, y, z), type);
            placed++;
        }

        // 生成结果提示
        if (placed < targetCount) {
            sendMessageToAll(world, "§c[净化之战] 警告：仅成功生成 " + placed + "/" + targetCount + " 个寄生节点，可能影响难度修正。");
        }
    }

    private static int determineNodeType(World world, BlockPos pos) {
        Biome biome = world.getBiome(pos);
        Biome.TempCategory temp = biome.getTempCategory();

        // 原命令逻辑：
        // if (world.func_180494_b(posss).func_76736_e()) { // canSnow?
        //     if (temp == TempCategory.WARM) typeB = 1; else typeB = 3;
        // } else if (temp == TempCategory.COLD) typeB = 4;
        // else if (temp == TempCategory.WARM && !world.func_180494_b(posss).func_76738_d()) typeB = 2;
        // 映射：getEnableSnow() -> func_76736_e()，canRain() -> func_76738_d()
        if (biome.getEnableSnow()) {
            if (temp == Biome.TempCategory.WARM) {
                return 1; // 温带雪地节点
            } else {
                return 3; // 其他雪地节点
            }
        } else if (temp == Biome.TempCategory.COLD) {
            return 4; // 寒冷节点
        } else if (temp == Biome.TempCategory.WARM && !biome.canRain()) {
            return 2; // 温带无雨节点（如沙漠）
        }
        // 默认返回1（温带普通节点）
        return 1;
    }

    private static void ensurePhase(World world) {
        if (!world.isRemote) {
            SRPSaveData data = SRPSaveData.get(world, 63);
            int dim = world.provider.getDimension();
            data.setTotalKills(dim, 100 * -2, false, world, true);
            data.setEvolutionPhase(dim, (byte)-2, true, world);
            SRPSaveData co = SRPSaveData.get(world,62);
            co.setCooldown(99999999,world,world.provider.getDimension(),false);
            SRPSaveData.get(world,66).setGeneration((byte) 0,dim);
            SRPWorldData srpWorldData = SRPWorldData.get(world);
            srpWorldData.clearColonyList();
            srpWorldData.clearNodeList();
            srpWorldData.clearOriginList();
            SRPSaveData.falseLevel = 0;
            Item item = ForgeRegistries.ITEMS.getValue(new ResourceLocation("srparasites", "infestation_purifier"));
            if (item == null) {
                sendMessageToAll(world,"[Debug] 创造模式寄染净化器发放失败");
                return;
            }
            giveItemToAllPlayers(world,new ItemStack(item,1));
        }
    }
    private static void ensureFailed(World world,BlockPos pos){
        if (!world.isRemote) {
            SRPSaveData data = SRPSaveData.get(world, 63);
            int dim = world.provider.getDimension();
            data.setEvolutionPhase(dim, (byte)10, true, world);
            SRPSaveData co = SRPSaveData.get(world,62);
            co.setCooldown(0,world,world.provider.getDimension(),false);
            SRPSaveData.get(world,66).setGeneration((byte) 5,dim);
            SRPWorldData srpWorldData = SRPWorldData.get(world);
            srpWorldData.setColony(pos.getX(), pos.getY(), pos.getZ());
            srpWorldData.setOrigin(world, pos.getX(), pos.getY(), pos.getZ(),10000,1000 );
            SRPSaveData.falseLevel = 4;
        }
    }
    private static void clearParasitesInRadius(World world, BlockPos beaconPos) {
        int radius = 128;
        AxisAlignedBB aabb = new AxisAlignedBB(beaconPos).grow(radius);
        List<Entity> entities = world.getEntitiesWithinAABB(Entity.class, aabb);
        int count = 0;
        for (Entity entity : entities) {
            // 判断是否为寄生虫实体（通过包名）
            if (entity.getClass().getName().startsWith("com.dhanantry.scapeandrunparasites.entity")) {
                // 添加闪电效果（仅视觉效果，不造成伤害）
                world.addWeatherEffect(new EntityLightningBolt(world, entity.posX, entity.posY, entity.posZ, true));
                // 直接杀死
                entity.setDead();
                count++;
            }
        }
        sendMessageToAll(world, TextFormatting.LIGHT_PURPLE + "[天幕] 清除 " + count + " 只寄生虫！");
    }

    public static void giveItemToAllPlayers(World world, ItemStack stack) {
        // 只在服务端执行
        if (world.isRemote) return;

        MinecraftServer server = world.getMinecraftServer();
        if (server == null) return;

        // 获取玩家列表
        for (EntityPlayerMP player : server.getPlayerList().getPlayers()) {
            // 尝试加入背包
            if (!player.inventory.addItemStackToInventory(stack.copy())) {
                // 背包满，将物品掉落在玩家位置
                player.dropItem(stack.copy(), false);
            }
        }
    }
}
