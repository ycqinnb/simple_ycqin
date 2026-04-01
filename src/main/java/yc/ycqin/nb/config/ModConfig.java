package yc.ycqin.nb.config;

import net.minecraft.util.text.TextFormatting;
import net.minecraftforge.common.config.Configuration;
import net.minecraftforge.fml.client.event.ConfigChangedEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import yc.ycqin.nb.ycqin; // 替换为你的主类MODID常量

import java.io.File;

public class ModConfig {

    public static Configuration config;
    public static String categoryText = "purification_text";

    // ========== 演化进度条配置 ==========
    public static boolean evolutionBarEnabled = true;          // 是否显示演化进度条
    public static int evolutionBarPosX = 10;                   // 进度条左上角X坐标
    public static int evolutionBarPosY = -1;                    // 进度条左上角Y坐标，-1表示自动放置在屏幕底部
    public static int evolutionBarWidth = 113;                  // 进度条纹理宽度
    public static int evolutionBarHeight = 29;                  // 进度条纹理高度
    public static int evolutionBarMovingU = 23;                 // 移动条在纹理中的起始U坐标
    public static int evolutionBarMovingV = 32;                 // 移动条在纹理中的起始V坐标
    public static int evolutionBarMovingOffsetX = 23;           // 移动条相对于背景左上角的X偏移
    public static int evolutionBarMovingOffsetY = 3;            // 移动条相对于背景左上角的Y偏移

    // ========== 维度生物属性增强配置 ==========
    public static boolean dimBoostEnabled = true;               // 是否启用自定义维度生物属性增强
    public static double dimBoostMultiplier = 10.0;             // 属性增强倍数（例如10表示10倍）
    public static String[] dimBoostAttributes = {               // 要增强的属性列表（使用属性注册名）
            "generic.maxHealth",
            "generic.attackDamage",
            "generic.followRange",
            "generic.knockbackResistance",
            "generic.armor",
            "generic.armorToughness"
    };

    // ========== 附魔+1物品掉落配置 ==========
    public static boolean dropEnabled = true;                    // 是否启用附魔+1物品的掉落
    public static double dropChance = 0.1;                      // 生物死亡时掉落附魔+1物品的概率（0.0~1.0）
    public static int ecMixLevel = 8;

//净化之战
    public static int baseScore;
    public static int deathPenalty;
    public static int unattendedPenalty;
    public static int difficultyFactor;
    public static int phase1Duration;
    public static int phase2Duration;
    public static int phase3Duration;
    public static int phase4Duration;
    public static int phase5Duration;
    public static int[] baseSpawnByPlayerCount;
    public static boolean isEnhanceBoss;
    public static int EnhanceBossValue;
    public static class PurificationText {
        // 开场台词（激活信标时显示，支持坐标占位符 %d）
        public static String[] introLines;

        // 难度评估报告文本（三条）
        public static String difficultyReportTitle;      // 例如："[天幕]难度评估报告正在生成中。"
        public static String difficultyReportBase;       // 例如："基础难度：%d 修正难度：%d 最终难度：%d"
        public static String difficultyReportVictory;    // 例如："人类必胜！"
       // 例如："血债血偿，该还债了，寄生虫们！"

        // 结构破坏/修复提醒
        public static String structureBroken;            // 例如："识别区域被破坏，传输中止，请尽快修复"
        public static String structureRepaired;          // 例如："识别区域已经修复，干得好"

        // 阶段提示语（5个阶段）
        public static String[] phaseMessages;            // 每个阶段显示的内容

        // 隐藏阶段台词（4阶段转5阶段）
        public static String[] hiddenStageLines;

        // 战斗结束胜利文本（多行）
        public static String[] victoryEpilogue;          // 胜利结局的台词
        public static String victoryScoreLine;           // 结局点数显示行，例如："[天幕]结局条件检测+剩余点数：%d"
        public static String victoryUnlockLine;          // 解锁结局说明，例如："[达成这个结局说明...]"
        // 战斗结束失败文本（多行）
        public static String[] defeatEpilogue;
        public static String clearSrpEntity;
    }
    //寄生虫坑杀装置
    public static boolean trapEnabled;
    public static int trapLureRangeBase;
    public static int trapLureRangePerLevel;
    public static int[] trapLureUses;          // 按等级索引（1~10），默认值
    public static int trapAttackCooldown;      // 攻击冷却（tick）
    public static int trapStructureCheckInterval; // 结构检测间隔（tick）

    // ----- 寄染能量转换装置 -----
    public static boolean energyConverterEnabled;
    public static int energyPerHealth;       // 每点生命值产生的能量
    public static int energyMaxStorage;      // 最大储能
    public static int energyOutputRate;       // 每次输出最大能量
    public static int energyOutputInterval;   // 输出间隔（tick）

//寄染阻断
    public static boolean isPaCoreEnabled;
    public static boolean isNoADEnabled;
    /**
     * 初始化配置文件
     */
    public static void init(File configFile) {
        if (config == null) {
            config = new Configuration(configFile);
            loadConfig();
        }
    }

    /**
     * 加载/刷新配置
     */
    public static void loadConfig() {
        // ----- 演化进度条分类 -----
        evolutionBarEnabled = config.getBoolean(
                "evolutionBarEnabled",
                "evolution_bar",
                true,
                "是否显示演化进度条"
        );
        evolutionBarPosX = config.getInt(
                "evolutionBarPosX",
                "evolution_bar",
                10,
                0,
                5000,
                "进度条左上角的X坐标"
        );
        evolutionBarPosY = config.getInt(
                "evolutionBarPosY",
                "evolution_bar",
                -1,
                -1,
                5000,
                "进度条左上角的Y坐标。如果设为-1，则自动放置在屏幕底部。"
        );
        evolutionBarWidth = config.getInt(
                "evolutionBarWidth",
                "evolution_bar",
                113,
                1,
                500,
                "进度条纹理的宽度"
        );
        evolutionBarHeight = config.getInt(
                "evolutionBarHeight",
                "evolution_bar",
                29,
                1,
                500,
                "进度条纹理的高度"
        );
        evolutionBarMovingU = config.getInt(
                "evolutionBarMovingU",
                "evolution_bar",
                23,
                0,
                500,
                "移动进度条在纹理中的起始U坐标"
        );
        evolutionBarMovingV = config.getInt(
                "evolutionBarMovingV",
                "evolution_bar",
                32,
                0,
                500,
                "移动进度条在纹理中的起始V坐标"
        );
        evolutionBarMovingOffsetX = config.getInt(
                "evolutionBarMovingOffsetX",
                "evolution_bar",
                23,
                0,
                500,
                "移动进度条相对于背景左上角的X偏移"
        );
        evolutionBarMovingOffsetY = config.getInt(
                "evolutionBarMovingOffsetY",
                "evolution_bar",
                3,
                0,
                500,
                "移动进度条相对于背景左上角的Y偏移"
        );

        // ----- 维度生物属性增强分类 -----
        dimBoostEnabled = config.getBoolean(
                "dimBoostEnabled",
                "dimension_boost",
                true,
                "是否启用自定义维度生物属性增强"
        );
        dimBoostMultiplier = config.getFloat(
                "dimBoostMultiplier",
                "dimension_boost",
                10.0f,
                0.1f,
                1000.0f,
                "属性增强倍数（例如10表示10倍）"
        );
        // 属性列表
        String[] defaultAttrs = {
                "generic.maxHealth",
                "generic.attackDamage",
                "generic.followRange",
                "generic.knockbackResistance",
                "generic.armor",
                "generic.armorToughness"
        };
        dimBoostAttributes = config.getStringList(
                "dimBoostAttributes",
                "dimension_boost",
                defaultAttrs,
                "要增强的属性列表，使用属性注册名（例如 generic.maxHealth）。"
        );

        // ----- 附魔+1物品掉落分类 -----
        dropEnabled = config.getBoolean(
                "dropEnabled",
                "item_drop",
                true,
                "是否启用超限附魔物品的掉落"
        );
        dropChance = config.getFloat(
                "dropChance",
                "item_drop",
                0.1f,
                0.0f,
                1.0f,
                "生物死亡时掉落超限附魔物品的概率（0.0~1.0）"
        );
        ecMixLevel = config.getInt(
                "ecMixLevel",
                "item_drop",
                8,
                0,
                32767,
                "超限附魔能达到的最大等级"
        );
        // 净化之战配置
        isEnhanceBoss = config.getBoolean(
                "isEnhanceBoss",
                "purification",
                false,
                "是否对5阶段boss启用属性增强"
        );
        EnhanceBossValue = config.getInt(
                "EnhanceBossValue",
                "purification",
                10,
                0,
                10000,
                "5阶段boss启用属性增强倍率"
        );

        PurificationText.introLines = config.getStringList("introLines", categoryText, new String[]{
                "§a世界净化核心解析成功。传统通道搭接成功。样本连接成功。信标对接成功，传输即将开始。",
                "§a样本识别成功，激活者基因特征识别失..成功。条件达成。允许激活天幕。“最终决战”计划开启。",
                "§6通告幸存的人类..吱吱.嘿听众朋友们大家好末世电台收到了您的联络！不要多问！要开打了！",
                "§c请幸存者们迅速集合至X: %d Y: %d Z: %d、进入黑曜石与原石组成的识别区域内、寄生虫即将发起总攻！",
                "§e在识别区域内坚持20分钟、维持传输！如果场地被破坏、修好它才能继续传输！",
                "§a不要让寄生虫们破坏传输，这场战争可不能再来！",
                "§6成败在此一举、夺回世界就靠你们了！记住，机会只有一次，无法重来！",
                TextFormatting.YELLOW + "只要找对角度，他们就会像黄油一样被切开",
                TextFormatting.LIGHT_PURPLE + "等我回家，亲爱的",
                TextFormatting.GRAY + "已经，没有什么好怕的了"
        }, "开场台词，每行一条。支持颜色代码§。可用 %d 占位符表示信标坐标（三个）。");

        PurificationText.difficultyReportTitle = config.getString("difficultyReportTitle", categoryText,
                "[天幕]难度评估报告正在生成中。", "难度评估第一行文本");
        PurificationText.difficultyReportBase = config.getString("difficultyReportBase", categoryText,
                "基础难度：%d 修正难度：%d 最终难度：%d", "难度评估第二行，%d 分别代表基础、修正、最终难度");
        PurificationText.difficultyReportVictory = config.getString("difficultyReportVictory", categoryText,
                "人类必胜！血债血偿，该还债了，寄生虫们！", "难度评估第三行");

        PurificationText.structureBroken = config.getString("structureBroken", categoryText,
                TextFormatting.RED + "识别区域被破坏，传输中止，请尽快修复", "结构被破坏时的提示");
        PurificationText.structureRepaired = config.getString("structureRepaired", categoryText,
                TextFormatting.GREEN + "识别区域已经修复，干得好", "结构修复时的提示");

        PurificationText.phaseMessages = config.getStringList("phaseMessages", categoryText, new String[]{
                "纯种之怒，卓越为路",
                "始祖降临，狂化迎击",
                "适应来袭，诡化显型",
                "同化出战，劫持现身",
                "天外来敌，衍生降临"
        }, "每个阶段的提示语，按阶段顺序，5条。");

        PurificationText.hiddenStageLines = config.getStringList("hiddenStageLines", categoryText, new String[]{
                "天幕老弟，武器好了吗？",
                "马上好，净化也马上结束了，寄生虫，轻而易举啊！",
                "坏了坏了，宇宙中的寄生虫发现我了，武器被摧毁......",
                "但，传输装置仍然完好，我们还有一线生机，加油，打败他们，人类必胜！"
        }, "第四阶段最后几秒触发的台词，每行一条。");

        PurificationText.victoryEpilogue = config.getStringList("victoryEpilogue", categoryText, new String[]{
                TextFormatting.BLUE + "本次样本已完全解析，世界已完全净化。“最终决战”计划完成，天幕转入休眠。恭喜幸存的人类",
                TextFormatting.BLUE + "恭喜你们，在这不讲道理的末世里找到了活下去的办法。",
                TextFormatting.BLUE + "狂妄的寄生兽们就像计划一样，向大地伸出了贪婪的手掌，自己暴露了庞大的身躯，不再潜伏。",
                TextFormatting.BLUE + "而你们成功注意到了失落的人类文明，留下了的些许手段。即使偏离计划，也将他们一网打尽，成功消灭了",
                TextFormatting.BLUE + "这里将不再受到寄生兽的污染，但一切的伤痕都将留在这片土地上，除非，你动手去改变它",
                TextFormatting.BLUE + "我们无法装作一切都未开始，也无法装作一切都已结束，但哪怕是些许的希望，哪怕只是片刻的安宁，人类也会挣扎着生存吧······",
                TextFormatting.BLUE + "无论时代如何变迁，无论未来如何迷茫，人类都会为了迈向未来，而在过去、亦或此刻，刻下努力的证明。",
                TextFormatting.YELLOW + "也许在不久的未来，人类又会迎接下一次的危机，也许，一切都在不断的重演。肉体可死，但思想不灭。",
                TextFormatting.YELLOW + "薪火相传，活下去吧，人类，挣扎着、哭着、笑着，迈向未来吧",
                TextFormatting.YELLOW + "称呼自己为英雄也好，称呼自己为救世主也罢。就像一直以来一样，做出你的选择，决定前进的方向。",
                TextFormatting.WHITE + "本次播报到此结束，下次再见，人类！"
        }, "胜利结局的台词列表，每行一条。");
        PurificationText.victoryScoreLine = config.getString("victoryScoreLine", categoryText,
                "[天幕]结局条件检测+剩余点数：%d", "胜利时剩余点数的行，%d 替换为点数");
        PurificationText.victoryUnlockLine = config.getString("victoryUnlockLine", categoryText,
                "[达成这个结局说明小队成员配合非常完善，即使再强大的寄生兽也没有彻底推毁人类的勇气！]" +
                        TextFormatting.BLUE + "[解锁结局：勇气赞歌]",
                "胜利结局说明");

        PurificationText.defeatEpilogue = config.getStringList("defeatEpilogue", categoryText, new String[]{
                TextFormatting.RED + "最近，我一直在做梦，梦里的世界逐渐被血红色污染。",
                TextFormatting.YELLOW + "转动的柱子扭曲着大地，亘古的君主霸占了天空。",
                TextFormatting.GREEN + "被唤作“火种“的英雄教会了人们种植与采集，教会了人们搭建净化信标，甚至教会了人们免于死亡惩罚的办法。",
                TextFormatting.RED + "然而，死亡的代价不可避免，灵魂的破损不可修复，世界的净化不可逆转。",
                TextFormatting.RED + "唯有.." + TextFormatting.OBFUSCATED + "aaey" + TextFormatting.RESET + TextFormatting.RED + "寄生永生！",
                TextFormatting.WHITE + "早上好，末日城，今天又是崭新的一天，诶等等，至少不是今天。",
                "由于在净化之战中灵魂磨损过多，火种的持有者选择了将各位转移到新世界。",
                "也许未来这块土地也会被再次污染，未来的英雄们会做出如何的选择呢？",
                TextFormatting.BLUE + "也终有人，会沉醉在湛蓝色的夜空中。",
                TextFormatting.YELLOW + "[达成这个结局说明净化之战人类方死亡次数过多或拖延时间过长，也许还有其他拯救的方法...?]",
                TextFormatting.BLUE + "[解锁结局：寄生永生]"
        }, "失败结局的台词列表，每行一条。");

        PurificationText.clearSrpEntity = config.getString(
                "clearSrpEntity",
                categoryText,
                TextFormatting.LIGHT_PURPLE + "[天幕] 清除 %d 只寄生虫！",
                "超时清除提示"
        );
        baseScore = config.getInt(
                "baseScore",
                "purification",
                100,
                0,
                1000,
                "基础点数，用于结局评估"
        );

        deathPenalty = config.getInt(
                "deathPenalty",
                "purification",
                3,
                0,
                100,
                "每死亡一次扣除的点数"
        );

        unattendedPenalty = config.getInt(
                "unattendedPenalty",
                "purification",
                2,
                0,
                100,
                "每分钟无人守点扣除的点数"
        );

        difficultyFactor = config.getInt(
                "difficultyFactor",
                "purification",
                2,
                0,
                100,
                "难度系数：最终难度乘以该值得到每分钟额外刷怪量"
        );

// 阶段持续时间（秒）
        phase1Duration = config.getInt("phase1Duration", "purification", 360, 10, 3600, "第一阶段持续时间（秒）");
        phase2Duration = config.getInt("phase2Duration", "purification", 360, 10, 3600, "第二阶段持续时间（秒）");
        phase3Duration = config.getInt("phase3Duration", "purification", 288, 10, 3600, "第三阶段持续时间（秒）");
        phase4Duration = config.getInt("phase4Duration", "purification", 96, 10, 3600, "第四阶段持续时间（秒）");
        phase5Duration = config.getInt("phase5Duration", "purification", 120, 10, 3600, "第五阶段（隐藏）持续时间（秒）");

        String[] defaultSpawn = new String[]{"5", "8", "12", "16"};
        String[] spawnStrs = config.getStringList(
                "baseSpawnByPlayerCount",
                "purification",
                defaultSpawn,
                "根据玩家人数决定的基础刷怪量，顺序为：0人,1人,2人,3人及以上。每个值应为整数。"
        );

        // 解析 String[] 为 int[]
        baseSpawnByPlayerCount = new int[spawnStrs.length];
        for (int i = 0; i < spawnStrs.length; i++) {
            try {
                baseSpawnByPlayerCount[i] = Integer.parseInt(spawnStrs[i].trim());
            } catch (NumberFormatException e) {
                baseSpawnByPlayerCount[i] = 5; // 解析失败时使用默认值
                System.out.println("[ModConfig] 解析 baseSpawnByPlayerCount 出错，使用默认值 5");
            }
        }
        isPaCoreEnabled = config.getBoolean(
                "PaCoreEnabled",
                "PaCore",
                true,
                "是否启用寄染阻断装置"
        );
        isNoADEnabled = config.getBoolean(
                "isNoADEnabled",
                "PaCore",
                true,
                "是否为“无适应性药水”添加配方（一个调度柱胞窝）"
        );

        String categoryTrap = "trap_device";

        trapEnabled = config.getBoolean(
                "trapEnabled",
                categoryTrap,
                true,
                "是否启用陷阱装置"
        );
        trapLureRangeBase = config.getInt(
                "trapLureRangeBase",
                categoryTrap,
                20,
                0,
                200,
                "诱饵基础吸引范围（格）"
        );
        trapLureRangePerLevel = config.getInt(
                "trapLureRangePerLevel",
                categoryTrap,
                10,
                0,
                1000,
                "每级诱饵额外增加范围（格）"
        );
        // 默认使用次数：1~10级依次为 10, 15, 20, 25, 30, 35, 40, 45, 50, 55
        String[] defaultUses = new String[]{"1","5","10","15","30","45","60","90","120","150"};
        String[] usesStr = config.getStringList(
                "trapLureUses",
                categoryTrap,
                defaultUses,
                "诱饵等级对应的可使用次数，按等级1~10顺序，10个值"
        );
        trapLureUses = new int[usesStr.length];
        for (int i = 0; i < usesStr.length && i < 10; i++) {
            try {
                trapLureUses[i] = Integer.parseInt(usesStr[i]);
            } catch (NumberFormatException e) {
                trapLureUses[i] = 10;
            }
        }

        trapAttackCooldown = config.getInt(
                "trapAttackCooldown",
                categoryTrap,
                20,
                1,
                200,
                "攻击冷却时间（tick），期间不会重复攻击"
        );
        trapStructureCheckInterval = config.getInt(
                "trapStructureCheckInterval",
                categoryTrap,
                20,
                5,
                100,
                "结构检测间隔（tick），数值越小检测越频繁"
        );
        String categoryEnergy = "energy_converter";
        energyConverterEnabled = config.getBoolean("energyConverterEnabled", categoryEnergy, true, "是否启用寄染能量转换装置");
        energyPerHealth = config.getInt("energyPerHealth", categoryEnergy, 100, 1, Integer.MAX_VALUE, "每点寄生虫生命值产生的能量");
        energyMaxStorage = config.getInt("energyMaxStorage", categoryEnergy, 200000, 1, Integer.MAX_VALUE, "最大能量存储");
        energyOutputRate = config.getInt("energyOutputRate", categoryEnergy, 20000, 1, Integer.MAX_VALUE, "每次向相邻方块输出的最大能量");
        energyOutputInterval = config.getInt("energyOutputInterval", categoryEnergy, 1, 1, 1000, "能量输出间隔（tick）");
        // 保存变更
        if (config.hasChanged()) {
            config.save();
        }
    }

    /**
     * 监听配置变更事件
     */
    @SubscribeEvent
    public static void onConfigChanged(ConfigChangedEvent.OnConfigChangedEvent event) {
        if (event.getModID().equals(ycqin.MODID)) {
            loadConfig();
        }
    }
}