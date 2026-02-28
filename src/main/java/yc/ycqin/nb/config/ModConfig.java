package yc.ycqin.nb.config;

import net.minecraftforge.common.config.Configuration;
import net.minecraftforge.fml.client.event.ConfigChangedEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import yc.ycqin.nb.ycqin; // 替换为你的主类MODID常量

import java.io.File;

public class ModConfig {

    public static Configuration config;

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
    // ========== 龙之研究配置 ==========
    public static boolean fixEnabled = true;                    //是否修复龙之研究磨床？

    public static int baseScore;
    public static int deathPenalty;
    public static int unattendedPenalty;
    public static int difficultyFactor;
    public static int phase1Duration;
    public static int phase2Duration;
    public static int phase3Duration;
    public static int phase4Duration;
    public static int phase5Duration;
    public static int[] baseSpawnByPlayerCount; // 需要解析字符串

    /**
     * 初始化配置文件（在preInit中调用）
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
                "要增强的属性列表，使用属性注册名（例如 generic.maxHealth）。可用的属性名请参考原版和模组。"
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
        fixEnabled = config.getBoolean(
                "fixEnabled",
                "Dra",
                true,
                "是否修复龙之研究磨床"
        );
        // 净化之战配置
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
        // 保存变更
        if (config.hasChanged()) {
            config.save();
        }
    }

    /**
     * 监听配置变更事件，实现热加载（修改配置文件后按F3+T重新加载）
     */
    @SubscribeEvent
    public static void onConfigChanged(ConfigChangedEvent.OnConfigChangedEvent event) {
        if (event.getModID().equals(ycqin.MODID)) {
            loadConfig();
        }
    }
}