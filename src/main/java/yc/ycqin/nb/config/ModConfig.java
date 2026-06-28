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
    public static boolean isDimPopulateEnabled;

    // ========== 附魔+1物品掉落配置 ==========
    public static boolean dropEnabled = true;                    // 是否启用附魔+1物品的掉落
    public static double dropChance = 0.1;                      // 生物死亡时掉落附魔+1物品的概率（0.0~1.0）
    public static int ecMixLevel = 8;

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

    //饰品配置
    public static float OrbReduction;
    //附魔及词条配置
    public static float ReductionLevel1;
    public static float ReductionLevel2;
    public static float ReductionLevel3;

    public static float MinDamageMultiplierPerLevel;
    public static float MinDamageProtectMultiplierPerLevel;

    public static float AdaptationIncrease;

    public static boolean isEnabledProtect;
    // ========== 裂解还原装置配置 ==========
    public static boolean reducerEnabled;                    // 是否启用装置
    public static int reducerBeamParticleInterval;           // 光柱粒子间隔（tick），默认2
    public static int reducerScanInterval;                   // 扫描目标间隔（tick），默认10

    // 各寄生体种类对应生成的强化生物数量
    public static int reducerSpawnCount_Derivative;          // 衍生种
    public static int reducerSpawnCount_Ancient;             // 远古种
    public static int reducerSpawnCount_Deceptive;           // 诡化种
    public static int reducerSpawnCount_Abomination;         // 憎恶种
    public static int reducerSpawnCount_Excellent;           // 卓越种
    public static int reducerSpawnCount_Pure;                // 纯粹种
    public static int reducerSpawnCount_Adapted;             // 适应种
    public static int reducerSpawnCount_Feral;               // 狂化种
    public static int reducerSpawnCount_Primitive;           // 原始种
    public static int reducerSpawnCount_Hijacked;            // 劫持种
    public static int reducerSpawnCount_Congenital;          // 先天种
    public static int reducerSpawnCount_Deterrent;           // 威慑种
    public static int reducerSpawnCount_Connective;          // 连结种
    public static int reducerSpawnCount_Crude;               // 粗制种


    public static String[] reducerProtectedMobs;
    public static String[] protectedMobBlacklist;
    // ----- 世界强化等级配置 -----
    public static int upgradePoints1to2 = 200;
    public static int upgradePoints2to3 = 400;
    public static int upgradePoints3to4 = 800;
    public static int upgradePoints4to5 = 1600;
    public static int upgradePoints5to6 = 3200;
    public static int upgradePoints6to7 = 6400;

    public static int pointsOnProtectedSpawn = 1;      // 强化生物生成时增加的点数
    public static int pointsOnProtectedDeath = -2;     // 强化生物死亡时减少的点数
    public static int pointsOnParasiteKill = 1;        // 强化生物杀死寄生虫时增加的点数

    public static float protectShieldRatio = 0.5f;     // 护盾值 = 最大生命值 * 护盾比例 * 世界等级
    public static float blockChance = 0.3f;            // 格挡概率
    public static int stunDuration = 100;              // 眩晕时间（tick）
    public static float damageCapRatio = 0.25f;        // 单次伤害上限比例（生命值的百分比）
    public static float extraDamageBase = 1.0f;        // 对寄生虫额外伤害基础值
    public static float extraDamageMultiplierPerLevel = 2.0f; // 每级乘数
    public static float defenseMultiplierPerLevel = 2.0f;     // 防御值每级乘数
    public static float pullDownChance = 0.5f;         // 拉下空中寄生虫的概率
    public static int pullDownDamage = 5;              // 拉下时造成的伤害
    public static int pullDownStun = 60;               // 拉下后眩晕时间
    public static float naturalSpawnChance = 0.05f;    // 自然生成强化生物的概率

    public static boolean mirageEnabled;
    public static boolean mirageCleanItems;   // 离开幻境时是否清空带标记的物品




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
        isDimPopulateEnabled = config.getBoolean(
                "isDimPopulateEnabled",
                "dimension_boost",
                true,
                "是否允许寄染维度生成时调用装饰（可能会增加性能损耗，因为寄生虫群系的生成装饰很卡）"
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

        OrbReduction = config.getFloat("OrbReduction","bauble",0.5F,0,1,"饰品减小orb对物品施加冷却的倍率（例如0.5减少50%）");

        ReductionLevel1 = config.getFloat("ReductionLevel1","enAndTr",0.05f,0,1,"每件装备1级适应性附魔或词条单个伤害来源最大可减免伤害");
        ReductionLevel2 = config.getFloat("ReductionLevel2","enAndTr",0.15f,0,1,"每件装备2级适应性附魔或词条单个伤害来源最大可减免伤害");
        ReductionLevel3 = config.getFloat("ReductionLevel3","enAndTr",0.25f,0,1,"每件装备3级适应性附魔或词条单个伤害来源最大可减免伤害");

        MinDamageMultiplierPerLevel = config.getFloat("MinDamageMultiplierPerLevel","enAndTr",1f,0,10000,"每1级最小伤害可造成的伤害");
        MinDamageProtectMultiplierPerLevel = config.getFloat("MinDamageProtectMultiplierPerLevel","enAndTr",1f,0,10000,"每1级最小伤害保护可防御的伤害");

        AdaptationIncrease = config.getFloat("AdaptationIncrease","enAndTr",0.007f,0,1,"每次受击增加适应性数值");

        String categoryReducer = "protect_mob";

        isEnabledProtect = config.getBoolean("isEnabledProtect", categoryReducer, true, "是否启用强化生物相关内容");

        reducerEnabled = config.getBoolean("reducerEnabled", categoryReducer, true, "是否启用裂解还原装置");
        reducerBeamParticleInterval = config.getInt("reducerBeamParticleInterval", categoryReducer, 2, 1, 20, "光柱粒子间隔（tick）");
        reducerScanInterval = config.getInt("reducerScanInterval", categoryReducer, 10, 5, 100, "扫描目标间隔（tick）");

        reducerSpawnCount_Derivative = config.getInt("reducerSpawnCount_Derivative", categoryReducer, 5, 0, 20, "衍生种裂解后生成的强化生物数量");
        reducerSpawnCount_Ancient = config.getInt("reducerSpawnCount_Ancient", categoryReducer, 3, 0, 20, "远古种裂解后生成的强化生物数量");
        reducerSpawnCount_Deceptive = config.getInt("reducerSpawnCount_Deceptive", categoryReducer, 2, 0, 20, "诡化种裂解后生成的强化生物数量");
        reducerSpawnCount_Abomination = config.getInt("reducerSpawnCount_Abomination", categoryReducer, 3, 0, 20, "憎恶种裂解后生成的强化生物数量");
        reducerSpawnCount_Excellent = config.getInt("reducerSpawnCount_Excellent", categoryReducer, 3, 0, 20, "卓越种裂解后生成的强化生物数量");
        reducerSpawnCount_Pure = config.getInt("reducerSpawnCount_Pure", categoryReducer, 2, 0, 20, "纯粹种裂解后生成的强化生物数量");
        reducerSpawnCount_Adapted = config.getInt("reducerSpawnCount_Adapted", categoryReducer, 1, 0, 20, "适应种裂解后生成的强化生物数量");
        reducerSpawnCount_Feral = config.getInt("reducerSpawnCount_Feral", categoryReducer, 1, 0, 20, "狂化种裂解后生成的强化生物数量");
        reducerSpawnCount_Primitive = config.getInt("reducerSpawnCount_Primitive", categoryReducer, 1, 0, 20, "原始种裂解后生成的强化生物数量");
        reducerSpawnCount_Hijacked = config.getInt("reducerSpawnCount_Hijacked", categoryReducer, 1, 0, 20, "劫持种裂解后生成的强化生物数量");
        reducerSpawnCount_Congenital = config.getInt("reducerSpawnCount_Congenital", categoryReducer, 1, 0, 20, "先天种裂解后生成的强化生物数量");
        reducerSpawnCount_Deterrent = config.getInt("reducerSpawnCount_Deterrent", categoryReducer, 2, 0, 20, "威慑种裂解后生成的强化生物数量");
        reducerSpawnCount_Connective = config.getInt("reducerSpawnCount_Connective", categoryReducer, 5, 0, 20, "连结种裂解后生成的强化生物数量");
        reducerSpawnCount_Crude = config.getInt("reducerSpawnCount_Crude", categoryReducer, 1, 0, 20, "粗制种裂解后生成的强化生物数量");

        String[] defaultMobs = {"minecraft:zombie", "minecraft:skeleton", "minecraft:spider","minecraft:cave_spider","minecraft:wither_skeleton","minecraft:slime","minecraft:enderman"};
        reducerProtectedMobs = config.getStringList("reducerProtectedMobs", categoryReducer, defaultMobs, "裂解装置可生成的强化生物列表，使用实体注册名，可在此扩展");

        protectedMobBlacklist = config.getStringList("protectedMobBlacklist", categoryReducer, new String[0],
                "强化生物黑名单，每行一个。支持完整注册名（如：minecraft:zombie）或模组ID（如：minecraft）");

        // 世界强化等级阈值配置
        upgradePoints1to2 = config.getInt("upgradePoints1to2", categoryReducer, 200, 1, 100000, "升到2级所需点数");
        upgradePoints2to3 = config.getInt("upgradePoints2to3", categoryReducer, 400, 1, 100000, "升到3级所需点数");
        upgradePoints3to4 = config.getInt("upgradePoints3to4", categoryReducer, 800, 1, 100000, "升到4级所需点数");
        upgradePoints4to5 = config.getInt("upgradePoints4to5", categoryReducer, 1600, 1, 100000, "升到5级所需点数");
        upgradePoints5to6 = config.getInt("upgradePoints5to6", categoryReducer, 3200, 1, 100000, "升到6级所需点数");
        upgradePoints6to7 = config.getInt("upgradePoints6to7", categoryReducer, 6400, 1, 100000, "升到7级所需点数");

// 事件点数变化
        pointsOnProtectedSpawn = config.getInt("pointsOnProtectedSpawn", categoryReducer, 1, -10000, 10000, "强化生物生成时增加的点数");
        pointsOnProtectedDeath = config.getInt("pointsOnProtectedDeath", categoryReducer, -2, -10000, 10000, "强化生物死亡时减少的点数（负数）");
        pointsOnParasiteKill = config.getInt("pointsOnParasiteKill", categoryReducer, 1, -10000, 10000, "强化生物杀死寄生虫时增加的点数");

// 战斗能力配置
        protectShieldRatio = config.getFloat("protectShieldRatio", categoryReducer, 0.5f, 0, 10, "护盾值 = 最大生命值 * 护盾比例 * 世界等级");
        blockChance = config.getFloat("blockChance", categoryReducer, 0.3f, 0, 1, "格挡寄生虫攻击的概率");
        stunDuration = config.getInt("stunDuration", categoryReducer, 100, 0, 1000, "格挡后眩晕时间（tick）");
        damageCapRatio = config.getFloat("damageCapRatio", categoryReducer, 0.25f, 0, 1, "单次伤害上限比例（生命值的百分比）");
        extraDamageBase = config.getFloat("extraDamageBase", categoryReducer, 1.0f, 0, 10000, "对寄生虫额外伤害基础值");
        extraDamageMultiplierPerLevel = config.getFloat("extraDamageMultiplierPerLevel", categoryReducer, 2.0f, 1, 100, "每级额外伤害乘数");
        defenseMultiplierPerLevel = config.getFloat("defenseMultiplierPerLevel", categoryReducer, 2.0f, 1, 100, "每级防御值乘数");
        pullDownChance = config.getFloat("pullDownChance", categoryReducer, 0.5f, 0, 1, "拉下空中寄生虫的概率");
        pullDownDamage = config.getInt("pullDownDamage", categoryReducer, 5, 0, 1000, "拉下时造成的伤害");
        pullDownStun = config.getInt("pullDownStun", categoryReducer, 60, 0, 1000, "拉下后眩晕时间（tick）");
        naturalSpawnChance = config.getFloat("naturalSpawnChance", categoryReducer, 0.05f, 0, 1, "自然生成强化生物的概率（等级>2时）");
        // 保存变更
        String category = "mirage";
        config.addCustomCategoryComment(category, "幻境维度相关设置");
        mirageEnabled = config.getBoolean("mirageEnabled", category, true, "是否启用幻境维度");
        mirageCleanItems = config.getBoolean("mirageCleanItems", category, true,
                "离开幻境时是否清除所有在幻境中获得/产生的物品（推荐开启，防止刷物品）");


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