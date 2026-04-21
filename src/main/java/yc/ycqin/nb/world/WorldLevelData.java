package yc.ycqin.nb.world;

import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.world.World;
import net.minecraft.world.storage.WorldSavedData;
import yc.ycqin.nb.config.ModConfig;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.world.World;
import net.minecraft.world.storage.WorldSavedData;
import yc.ycqin.nb.config.ModConfig;

public class WorldLevelData extends WorldSavedData {
    private static final String DATA_NAME = "ycqin_world_level";
    private int points = 0;               // 当前点数

    public WorldLevelData() { super(DATA_NAME); }
    public WorldLevelData(String name) { super(name); }

    public static WorldLevelData get(World world) {
        World mainWorld = world.getMinecraftServer().getWorld(0);
        WorldLevelData data = (WorldLevelData) mainWorld.loadData(WorldLevelData.class, DATA_NAME);
        if (data == null) {
            data = new WorldLevelData();
            mainWorld.setData(DATA_NAME, data);
        }
        return data;
    }

    public int getPoints() { return points; }

    /**
     * 根据当前点数计算等级（1~7）
     */
    public int getLevel() {
        int level = 1;
        int[] thresholds = computeThresholds();
        for (int i = 1; i <= 7; i++) {
            if (points >= thresholds[i-1]) level = i;
        }
        return level;
    }

    /**
     * 增减点数（可为负），并自动保存
     */
    public void addPoints(int delta) {
        if (delta == 0) return;
        points += delta;
        if (points < 0) points = 0;
        markDirty();
    }

    /**
     * 直接设置点数
     */
    public void setPoints(int newPoints) {
        if (newPoints < 0) newPoints = 0;
        this.points = newPoints;
        markDirty();
    }

    /**
     * 计算每个等级所需的最低点数（累加）
     */
    private int[] computeThresholds() {
        int[] thresholds = new int[7];
        thresholds[0] = 0;                 // 1级
        thresholds[1] = ModConfig.upgradePoints1to2;
        thresholds[2] = thresholds[1] + ModConfig.upgradePoints2to3;
        thresholds[3] = thresholds[2] + ModConfig.upgradePoints3to4;
        thresholds[4] = thresholds[3] + ModConfig.upgradePoints4to5;
        thresholds[5] = thresholds[4] + ModConfig.upgradePoints5to6;
        thresholds[6] = thresholds[5] + ModConfig.upgradePoints6to7;
        return thresholds;
    }

    @Override
    public void readFromNBT(NBTTagCompound nbt) {
        points = nbt.getInteger("Points");
        if (points < 0) points = 0;
    }

    @Override
    public NBTTagCompound writeToNBT(NBTTagCompound compound) {
        compound.setInteger("Points", points);
        return compound;
    }
}