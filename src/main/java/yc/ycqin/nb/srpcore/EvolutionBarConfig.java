package yc.ycqin.nb.srpcore;

public class EvolutionBarConfig {
    // 进度条基础尺寸
    public static final int BAR_WIDTH = 113;
    public static final int BAR_HEIGHT = 29;
    public static final int MOVING_BAR_HEIGHT = 29; // 全高，不再扁

    // 显示位置（x/y偏移，可配置）
    public static int BAR_POS_X = 10;
    public static int BAR_POS_Y = -10; // 距离底部10像素
    public static boolean FILL_FULL_HEIGHT = false; // 是否让填充部分完全覆盖背景（去除边框）
    // 各阶段最大演化值（可根据需求调整）
    public static final int[] PHASE_MAX_VALUES = {
            400,           // 阶段0 -> 1
            800,           // 阶段1 -> 2
            1800,          // 阶段2 -> 3
            20000,         // 阶段3 -> 4
            200000,        // 阶段4 -> 5
            5_000_000,     // 阶段5 -> 6
            25_000_000,    // 阶段6 -> 7
            500_000_000,   // 阶段7 -> 8
            1_000_000_000, // 阶段8 -> 9
            1_800_000_000, // 阶段9 -> 10
            2_100_000_000  // 阶段10 -> 上限
    };

    // 显示控制开关
    public static boolean SHOW_EVOLUTION_BAR = true;
    public static boolean REQUIRE_DIRTY_CLOCK = false; // 是否需要手持特定物品才显示
    public static final String DIRTY_CLOCK_ITEM_ID = "ycqin:evolution_clock"; // 手持物品ID
}
