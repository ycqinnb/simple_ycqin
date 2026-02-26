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
            "generic.movementSpeed",
            "generic.followRange",
            "generic.knockbackResistance",
            "generic.armor",
            "generic.armorToughness"
    };

    // ========== 附魔+1物品掉落配置 ==========
    public static boolean dropEnabled = true;                    // 是否启用附魔+1物品的掉落
    public static double dropChance = 0.1;                          // 生物死亡时掉落附魔+1物品的概率（0.0~1.0）
    // ========== 龙之研究配置 ==========
    public static boolean fixEnabled = true;                    //是否修复龙之研究磨床？

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
                "generic.movementSpeed",
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
        fixEnabled = config.getBoolean(
                "fixEnabled",
                "Dra",
                true,
                "是否修复龙之研究磨床"
        );

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