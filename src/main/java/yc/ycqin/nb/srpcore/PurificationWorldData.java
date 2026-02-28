package yc.ycqin.nb.srpcore;

import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraft.world.storage.WorldSavedData;
import net.minecraftforge.common.util.Constants;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class PurificationWorldData extends WorldSavedData {
    private static final String DATA_NAME = "PurificationBeacons";

    private Map<BlockPos, BeaconState> beacons = new HashMap<>();

    public PurificationWorldData() {
        super(DATA_NAME);
    }

    public PurificationWorldData(String name) {
        super(name);
    }

    public static PurificationWorldData get(World world) {
        PurificationWorldData data = (PurificationWorldData) world.loadData(PurificationWorldData.class, DATA_NAME);
        if (data == null) {
            data = new PurificationWorldData();
            world.setData(DATA_NAME, data);
        }
        return data;
    }

    public void putBeacon(BlockPos pos, BeaconState state) {
        beacons.put(pos, state);
        markDirty();
    }

    public void removeBeacon(BlockPos pos) {
        beacons.remove(pos);
        markDirty();
    }

    public BeaconState getBeacon(BlockPos pos) {
        return beacons.get(pos);
    }

    public Set<BlockPos> getAllBeacons() {
        return new HashSet<>(beacons.keySet());
    }

    public boolean hasBeacon(BlockPos pos) {
        return beacons.containsKey(pos);
    }

    @Override
    public void readFromNBT(NBTTagCompound nbt) {
        NBTTagList list = nbt.getTagList("beacons", Constants.NBT.TAG_COMPOUND);
        beacons.clear();
        for (int i = 0; i < list.tagCount(); i++) {
            NBTTagCompound tag = list.getCompoundTagAt(i);
            BlockPos pos = BlockPos.fromLong(tag.getLong("pos"));
            BeaconState state = new BeaconState();
            state.deserializeNBT(tag.getCompoundTag("state"));
            beacons.put(pos, state);
        }
    }

    @Override
    public NBTTagCompound writeToNBT(NBTTagCompound compound) {
        NBTTagList list = new NBTTagList();
        for (Map.Entry<BlockPos, BeaconState> entry : beacons.entrySet()) {
            NBTTagCompound tag = new NBTTagCompound();
            tag.setLong("pos", entry.getKey().toLong());
            tag.setTag("state", entry.getValue().serializeNBT());
            list.appendTag(tag);
        }
        compound.setTag("beacons", list);
        return compound;
    }

    public static class BeaconState {
        public BlockPos beaconPos;
        public BlockPos bottomBasePos;
        public int baseLayers;
        public BlockPos corePos;
        public boolean wasIntact;
        public int dimension;

        // ===== 新增战斗状态字段 =====
        public boolean battleActive;          // 战斗是否正在进行
        public int currentPhase;              // 当前阶段 (1-5)
        public long phaseStartTime;           // 阶段开始的世界时间（tick）
        public int totalScore;                // 剩余点数（后续用于结局）
        public int playerDeaths;              // 累计死亡次数
        public long lastMinuteTick;           // 上次分钟刷怪的时间
        public long lastExtraSpawnTick;       // 上次额外增援的时间（用于始祖种）
        public boolean secondWaveTimeout;     // 第二阶段超时标记
        public boolean fourthWaveTimeout;     // 第四阶段超时标记
        public boolean hiddenStageTriggered;  // 是否已触发隐藏阶段剧情
        public long pausedElapsed; // 暂停时记录已耗时
        public boolean badEndTriggered;
        public boolean phase2DialogueTriggered;

        // ===========================

        // 防抖字段（可选）
        public transient int stableCount;
        public transient boolean lastIntact;

        public NBTTagCompound serializeNBT() {
            NBTTagCompound tag = new NBTTagCompound();
            tag.setLong("beaconPos", beaconPos.toLong());
            tag.setLong("bottomBasePos", bottomBasePos.toLong());
            tag.setInteger("baseLayers", baseLayers);
            tag.setLong("corePos", corePos.toLong());
            tag.setBoolean("wasIntact", wasIntact);
            tag.setInteger("dimension", dimension);

            // 新增战斗状态持久化
            tag.setBoolean("battleActive", battleActive);
            tag.setInteger("currentPhase", currentPhase);
            tag.setLong("phaseStartTime", phaseStartTime);
            tag.setInteger("totalScore", totalScore);
            tag.setInteger("playerDeaths", playerDeaths);
            tag.setLong("lastMinuteTick", lastMinuteTick);
            tag.setLong("lastExtraSpawnTick", lastExtraSpawnTick);
            tag.setBoolean("secondWaveTimeout", secondWaveTimeout);
            tag.setBoolean("fourthWaveTimeout", fourthWaveTimeout);
            tag.setBoolean("hiddenStageTriggered", hiddenStageTriggered);
            tag.setLong("pausedElapsed", pausedElapsed);
            tag.setBoolean("badEndTriggered", badEndTriggered);
            tag.setBoolean("phase2DialogueTriggered", phase2DialogueTriggered);

            return tag;
        }

        public void deserializeNBT(NBTTagCompound tag) {
            beaconPos = BlockPos.fromLong(tag.getLong("beaconPos"));
            bottomBasePos = BlockPos.fromLong(tag.getLong("bottomBasePos"));
            baseLayers = tag.getInteger("baseLayers");
            corePos = BlockPos.fromLong(tag.getLong("corePos"));
            wasIntact = tag.getBoolean("wasIntact");
            dimension = tag.getInteger("dimension");

            // 新增战斗状态读取
            battleActive = tag.getBoolean("battleActive");
            currentPhase = tag.getInteger("currentPhase");
            phaseStartTime = tag.getLong("phaseStartTime");
            totalScore = tag.getInteger("totalScore");
            playerDeaths = tag.getInteger("playerDeaths");
            lastMinuteTick = tag.getLong("lastMinuteTick");
            lastExtraSpawnTick = tag.getLong("lastExtraSpawnTick");
            secondWaveTimeout = tag.getBoolean("secondWaveTimeout");
            fourthWaveTimeout = tag.getBoolean("fourthWaveTimeout");
            hiddenStageTriggered = tag.getBoolean("hiddenStageTriggered");
            pausedElapsed = tag.getLong("pausedElapsed");
            badEndTriggered = tag.getBoolean("badEndTriggered");
            phase2DialogueTriggered = tag.getBoolean("phase2DialogueTriggered");

        }
    }
}
