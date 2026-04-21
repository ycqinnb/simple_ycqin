package yc.ycqin.nb.common.entity.tileentity;

import com.dhanantry.scapeandrunparasites.entity.ai.misc.EntityParasiteBase;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLiving;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.init.Blocks;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.NetworkManager;
import net.minecraft.network.play.server.SPacketUpdateTileEntity;
import net.minecraft.server.management.PlayerChunkMapEntry;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumParticleTypes;
import net.minecraft.util.ITickable;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.WorldServer;
import yc.ycqin.nb.config.ModConfig;
import yc.ycqin.nb.event.ProtectedMobHandler;
import yc.ycqin.nb.util.EntityClassifier;
import yc.ycqin.nb.util.ParasiteHelper;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class TileEntityReducerCore extends TileEntity implements ITickable {
    // 结构位置
    private List<BlockPos> beaconPositions = new ArrayList<>();
    private boolean structureValid = false;
    private BlockPos platformCenter;  // 3x3黑曜石平台中心位置

    // 裂解流程状态
    private EntityLivingBase currentTarget = null;
    private int processProgress = 0;      // 0 = 空闲, 1 ~ duration = 进行中
    private boolean isProcessing = false;

    private long lastCheckTime = 0;
    private long lastScanTime = 0;

    private double targetStartY;      // 记录目标起始Y坐标
    private double targetEndY;        // 记录目标结束Y坐标
    private boolean lifting = true;   // 是否正在上升
    private int liftDuration = 20;    // 上升时间（tick）
    private int fallDuration = 20;    // 下落时间

    // ========== 数据同步 ==========
    @Override
    public NBTTagCompound getUpdateTag() {
        NBTTagCompound tag = super.getUpdateTag();
        tag.setBoolean("StructureValid", structureValid);
        tag.setBoolean("IsProcessing", isProcessing);
        if (currentTarget != null) {
            tag.setInteger("TargetId", currentTarget.getEntityId());
        }
        return tag;
    }

    @Override
    public void handleUpdateTag(NBTTagCompound tag) {
        super.handleUpdateTag(tag);
        structureValid = tag.getBoolean("StructureValid");
        isProcessing = tag.getBoolean("IsProcessing");
        // 目标同步需要额外处理，在客户端通过 Entity 查找
    }

    @Nullable
    @Override
    public SPacketUpdateTileEntity getUpdatePacket() {
        NBTTagCompound tag = new NBTTagCompound();
        tag.setBoolean("StructureValid", structureValid);
        tag.setBoolean("IsProcessing", isProcessing);
        return new SPacketUpdateTileEntity(pos, 1, tag);
    }

    @Override
    public void onDataPacket(NetworkManager net, SPacketUpdateTileEntity pkt) {
        structureValid = pkt.getNbtCompound().getBoolean("StructureValid");
        isProcessing = pkt.getNbtCompound().getBoolean("IsProcessing");
    }

    private void syncToTrackingClients() {
        if (world == null || world.isRemote) return;
        WorldServer ws = (WorldServer) world;
        SPacketUpdateTileEntity packet = getUpdatePacket();
        if (packet == null) return;
        PlayerChunkMapEntry trackingEntry = ws.getPlayerChunkMap().getEntry(this.pos.getX() >> 4, this.pos.getZ() >> 4);
        if (trackingEntry != null) {
            for (EntityPlayerMP player : trackingEntry.getWatchingPlayers()) {
                player.connection.sendPacket(packet);
            }
        }
    }

    // ========== 持久化 ==========
    @Override
    public NBTTagCompound writeToNBT(NBTTagCompound compound) {
        super.writeToNBT(compound);
        compound.setBoolean("StructureValid", structureValid);
        compound.setBoolean("IsProcessing", isProcessing);
        compound.setInteger("ProcessProgress", processProgress);
        if (platformCenter != null) {
            compound.setLong("PlatformCenter", platformCenter.toLong());
        }
        return compound;
    }

    @Override
    public void readFromNBT(NBTTagCompound compound) {
        super.readFromNBT(compound);
        structureValid = compound.getBoolean("StructureValid");
        isProcessing = compound.getBoolean("IsProcessing");
        processProgress = compound.getInteger("ProcessProgress");
        if (compound.hasKey("PlatformCenter")) {
            platformCenter = BlockPos.fromLong(compound.getLong("PlatformCenter"));
        }
    }

    // ========== 核心逻辑 ==========
    @Override
    public void update() {
        if (world.isRemote) return;
        if (!ModConfig.reducerEnabled) return;

        long worldTime = world.getTotalWorldTime();

        // 周期性检测结构
        if (worldTime - lastCheckTime >= 40) {  // 每2秒检测一次
            lastCheckTime = worldTime;
            checkStructure();
            if (structureValid != structureValid) {
                syncToTrackingClients();
            }
        }

        if (!structureValid) return;

        // 处理裂解流程
        if (isProcessing) {
            processTick();
        } else {
            // 扫描目标（每10 tick）
            if (worldTime - lastScanTime >= ModConfig.reducerScanInterval) {
                lastScanTime = worldTime;
                scanAndStartProcess();
            }
        }
    }

    /**
     * 检测多方块结构
     * 结构要求：
     * 1. 核心方块上方放置3x3黑曜石平台（核心正上方为平台中心）
     * 2. 在平台上方10格高度的四个角各放置一个信标，信标面向核心方向
     */
    private void checkStructure() {
        beaconPositions.clear();
        boolean valid = true;

        // 获取平台中心位置（核心正上方）
        platformCenter = pos.up();

        // 检查3x3黑曜石平台
        for (int dx = -1; dx <= 1; dx++) {
            for (int dz = -1; dz <= 1; dz++) {
                BlockPos checkPos = platformCenter.add(dx, 0, dz);
                IBlockState state = world.getBlockState(checkPos);
                if (state.getBlock() != Blocks.OBSIDIAN) {
                    valid = false;
                    break;
                }
            }
            if (!valid) break;
        }

        if (!valid) {
            structureValid = false;
            return;
        }

        // 检查四个角的信标（平台上方10格高度）
        int yOffset = 10;
        int[][] corners = {{-5, -5}, {5, -5}, {-5, 5}, {5, 5}};

        for (int[] corner : corners) {
            BlockPos beaconPos = new BlockPos(
                    platformCenter.getX() + corner[0],
                    platformCenter.getY() + yOffset,
                    platformCenter.getZ() + corner[1]
            );
            IBlockState state = world.getBlockState(beaconPos);
            if (state.getBlock() != Blocks.BEACON) {
                valid = false;
                break;
            }
            beaconPositions.add(beaconPos);
        }

        structureValid = valid;
    }

    /**
     * 扫描平台上的目标并开始裂解流程
     */
    private void scanAndStartProcess() {
        if (platformCenter == null) return;

        // 扫描3x3平台区域（包含平台上方一格）
        AxisAlignedBB scanBox = new AxisAlignedBB(platformCenter).grow(1, 1, 1);
        List<Entity> entities = world.getEntitiesWithinAABB(Entity.class, scanBox);

        for (Entity entity : entities) {
            if (entity instanceof EntityLivingBase && !entity.isDead) {
                EntityLivingBase target = (EntityLivingBase) entity;

                // 检查是否可以开始裂解
                if (canStartProcess(target)) {
                    startProcess(target);
                    return;  // 一次只处理一个
                }
            }
        }
    }

    /**
     * 判断目标是否可以被裂解
     */
    private boolean canStartProcess(EntityLivingBase target) {
        // 不能是自己（防止意外）
        if (target == null) return false;

        // 如果目标已经有 yc_protectcoth NBT，说明已经是强化生物，不再裂解
        NBTTagCompound data = target.getEntityData();
        if (data.hasKey("yc_protectcoth")) {
            return false;
        }

        // 检查是否是特殊可还原实体（被感染的人类等）
        if (EntityClassifier.isRevertible(target)) {
            return true;
        }

        // 检查是否是目标寄生虫种类
        if (target instanceof EntityParasiteBase) {
            return true;
        }

        return false;
    }

    /**
     * 开始裂解流程
     */
    private void startProcess(EntityLivingBase target) {
        this.currentTarget = target;
        this.processProgress = 0;
        this.isProcessing = true;
        this.lifting = true;
        this.targetStartY = target.posY;
        this.targetEndY = target.posY + 5; // 上升5格
        this.syncToTrackingClients();
        ((EntityLiving)target).setNoAI(true);
    }

    /**
     * 裂解流程的 tick 更新
     */
    private void processTick() {
        if (currentTarget == null || currentTarget.isDead) {
            resetProcess();
            return;
        }

        // 生成光柱
        if (world.getTotalWorldTime() % ModConfig.reducerBeamParticleInterval == 0) {
            for (BlockPos beaconPos : beaconPositions) {
                spawnBeamParticle(beaconPos, currentTarget);
            }
        }

        // 吸附升降逻辑
        if (lifting) {
            double t = (double) processProgress / liftDuration;
            if (t >= 1.0) {
                lifting = false;
                processProgress = 0;
            } else {
                double newY = targetStartY + (targetEndY - targetStartY) * t;
                currentTarget.posY = newY;
                currentTarget.setPosition(currentTarget.posX, newY, currentTarget.posZ);
            }
        } else {
            double t = (double) processProgress / fallDuration;
            if (t >= 1.0) {
                // 下落完成，执行转化并爆炸效果
                completeProcessWithExplosion();
                return;
            } else {
                double newY = targetEndY + (targetStartY - targetEndY) * t;
                currentTarget.posY = newY;
                currentTarget.setPosition(currentTarget.posX, newY, currentTarget.posZ);
            }
        }

        processProgress++;
    }

    private void completeProcessWithExplosion() {
        // 生成TNT爆炸粒子效果
        WorldServer ws = (WorldServer) world;
        ws.spawnParticle(EnumParticleTypes.EXPLOSION_HUGE, currentTarget.posX, currentTarget.posY, currentTarget.posZ, 1.0, (double) 0, 0, 0, 0);
        ws.spawnParticle(EnumParticleTypes.SMOKE_LARGE, currentTarget.posX, currentTarget.posY, currentTarget.posZ, 20, 0.5, 0.5, 0.5, 0.1);
        ws.playEvent(null, 2000, new BlockPos(currentTarget.posX, currentTarget.posY, currentTarget.posZ), 0); // TNT爆炸音效

        // 执行实际转化
        if (EntityClassifier.isRevertible(currentTarget)) {
            handleRevertTarget(currentTarget);
        } else {
            handleParasiteTarget(currentTarget);
        }

        resetProcess();
    }

    /**
     * 生成光柱粒子效果
     */
    private void spawnBeamParticle(BlockPos from, Entity target) {
        double x1 = from.getX() + 0.5;
        double y1 = from.getY() + 0.5;
        double z1 = from.getZ() + 0.5;
        double x2 = target.posX;
        double y2 = target.posY + target.height / 2;
        double z2 = target.posZ;

        double dx = x2 - x1;
        double dy = y2 - y1;
        double dz = z2 - z1;
        double length = Math.sqrt(dx * dx + dy * dy + dz * dz);
        if (length < 0.01) return;
        dx /= length;
        dy /= length;
        dz /= length;

        WorldServer ws = (WorldServer) world;
        for (double d = 0; d <= length; d += 0.2) {
            double x = x1 + dx * d;
            double y = y1 + dy * d;
            double z = z1 + dz * d;
            ws.spawnParticle(EnumParticleTypes.END_ROD, x, y, z, 0, 0, 0, 0, 0, new int[0]);
        }
    }

    /**
     * 处理可还原目标（被感染的人类等）
     */
    private void handleRevertTarget(EntityLivingBase target) {
        EntityLivingBase reverted = ParasiteHelper.revertInfectedEntity(target);
        if (reverted == null) {
            handleParasiteTarget(target);
        }
    }

    /**
     * 处理寄生虫目标，生成强化生物
     */
    private void handleParasiteTarget(EntityLivingBase target) {
        int spawnCount = EntityClassifier.getSpawnCountForType(target);

        Random rand = world.rand;
        for (int i = 0; i < spawnCount; i++) {
            // 从配置列表中随机选择一个生物类型
            String mobName = ModConfig.reducerProtectedMobs[rand.nextInt(ModConfig.reducerProtectedMobs.length)];
            EntityLivingBase spawned = EntityClassifier.spawnProtectedMob(world, target.posX, target.posY, target.posZ, mobName);
            if (spawned != null) {
                ProtectedMobHandler.onProtectedSpawn(spawned);
            }
        }

        // 移除原目标
        target.setDead();
    }

    /**
     * 重置裂解流程状态
     */
    private void resetProcess() {
        this.currentTarget = null;
        this.processProgress = 0;
        this.isProcessing = false;
        syncToTrackingClients();
    }
}
