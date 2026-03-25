package yc.ycqin.nb.common.entity.tileentity;

import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLiving;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.NetworkManager;
import net.minecraft.network.play.server.SPacketUpdateTileEntity;
import net.minecraft.pathfinding.Path;
import net.minecraft.pathfinding.PathPoint;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ITickable;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.World;
import yc.ycqin.nb.config.ModConfig;

import javax.annotation.Nullable;
import java.util.*;

public class TileEntityParasiteCore extends TileEntity implements ITickable {
    public static class CoreInfo {
        public final int dimension;
        public final BlockPos pos;

        public CoreInfo(int dimension, BlockPos pos) {
            this.dimension = dimension;
            this.pos = pos;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            CoreInfo coreInfo = (CoreInfo) o;
            return dimension == coreInfo.dimension && pos.equals(coreInfo.pos);
        }

        @Override
        public int hashCode() {
            return 31 * dimension + pos.hashCode();
        }
    }

    // 缓存所有激活的核心（维度+位置）
    private static final Set<CoreInfo> ACTIVE_CORES = new HashSet<>();

    private boolean structureValid = false;
    private int checkCooldown = 0;
    private boolean powered = false;

    // 记录区域内实体的停留 ticks，防止卡顿反复进入
    private final Map<Integer, Integer> insideTicks = new HashMap<>();

    // 奖杯方块的注册名
    private static final String TROPHY_BOOM_ORB = "srparasites:trophy_boom_orb";
    private static final String TROPHY_VOID_ORB = "srparasites:trophy_void_orb";

    // 屏障固定半径（以核心为中心，半径 64 格，即 8 个区块）
    private static final int RADIUS = 64;
    // 更新缓存
    private void updateCoreCache() {
        boolean active = structureValid && !powered;
        CoreInfo info = new CoreInfo(world.provider.getDimension(), pos);
        if (!ModConfig.isPaCoreEnabled) return;
        if (active) {
            ACTIVE_CORES.add(info);
        } else {
            ACTIVE_CORES.remove(info);
        }
    }
    // 供 Mixin 调用的静态检查方法（需要传入 World 对象）
    public static boolean isPositionProtected(World world, BlockPos target) {
        int dim = world.provider.getDimension();
        for (CoreInfo info : ACTIVE_CORES) {
            if (info.dimension == dim &&
                    target.getDistance(info.pos.getX(), info.pos.getY(), info.pos.getZ()) <= RADIUS) {
                return true;
            }
        }
        return false;
    }
    @Override
    public void update() {
        if (world.isRemote) return;

        // 红石信号检测
        boolean nowPowered = world.isBlockPowered(pos);
        if (nowPowered != powered) {
            powered = nowPowered;
            IBlockState state = world.getBlockState(pos);
            world.notifyBlockUpdate(pos, state, state, 3);
            markDirty();
        }

        // 结构检测（每 20 tick 一次）
        if (--checkCooldown <= 0) {
            checkCooldown = 20;
            boolean previous = structureValid;
            structureValid = checkStructure();
            if (previous != structureValid) {
                updateCoreCache();
                IBlockState state = world.getBlockState(pos);
                world.notifyBlockUpdate(pos, state, state, 3);
                markDirty();

                // 当结构从无效变为有效时，立即清理周围的寄生虫生物
                if (structureValid && !previous) {
                    scrubMobsAroundField();
                }
            }
        }

        // 生物驱逐逻辑：仅在结构有效且未通入红石信号时执行（每 2 tick 一次）
        if (structureValid && !powered && world.getTotalWorldTime() % 2 == 0) {
            performBarrierEffect();
        }
    }

    // ==================== 屏障核心逻辑（移植自 SRP 的 TileEntityParasiteBarrier）====================

    /**
     * 计算当前屏障的世界坐标边界（整型数组，依次为 minX, maxX, minZ, maxZ）
     */
    private int[] currentWorldBounds() {
        int minX = pos.getX() - RADIUS;
        int maxX = pos.getX() + RADIUS;
        int minZ = pos.getZ() - RADIUS;
        int maxZ = pos.getZ() + RADIUS;
        return new int[]{minX, maxX, minZ, maxZ};
    }

    /**
     * 判断实体是否位于边界内
     */
    private boolean isInside(int[] bb, Entity e) {
        double x = e.posX;
        double z = e.posZ;
        return x >= bb[0] && x <= bb[1] && z >= bb[2] && z <= bb[3];
    }

    /**
     * 判断实体是否为 SRP 寄生虫相关（通过类名匹配）
     */
    private boolean isSRP(Entity e) {
        String n = e.getClass().getName();
        return n.startsWith("com.dhanantry.scapeandrunparasites") || n.contains(".srparasites.");
    }

    /**
     * 执行屏障效果（每 tick 调用一次）
     */
    private void performBarrierEffect() {
        if (!ModConfig.isPaCoreEnabled) return;
        int[] bb = currentWorldBounds();

        // 搜索范围：屏障边界向外扩展 48 格，确保覆盖边界附近的生物
        AxisAlignedBB search = new AxisAlignedBB(
                bb[0] - 48, 0, bb[2] - 48,
                bb[1] + 48, world.getActualHeight(), bb[3] + 48
        );

        // 1. 处理跨派系攻击（所有活体生物）
        for (EntityLiving attacker : world.getEntitiesWithinAABB(EntityLiving.class, search)) {
            clearCrossFactionAggroIfTargetInside(attacker, bb);
        }

        // 2. 获取所有 SRP 实体
        List<Entity> srpList = world.getEntitiesWithinAABB(Entity.class, search, this::isSRP);
        if (srpList.isEmpty()) {
            insideTicks.clear();
        } else {
            for (Entity e : srpList) {
                clearAggroIfTargetInside(e, bb);
                clearPathIfTouchesInside(e, bb);

                if (isInside(bb, e)) {
                    // 在区域内：尝试传送出区域
                    pushOrTeleportOutside(bb, e);
                    if (isInside(bb, e)) {
                        // 如果仍未出去，累加停留 ticks，超过 100 tick（5秒）再次强制传送
                        int t = insideTicks.getOrDefault(e.getEntityId(), 0) + 2;
                        if (t >= 100) {
                            pushOrTeleportOutside(bb, e);
                            pushOrTeleportOutside(bb, e); // 连续两次，确保离开
                            t = 0;
                        }
                        insideTicks.put(e.getEntityId(), t);
                    } else {
                        insideTicks.remove(e.getEntityId());
                    }
                } else {
                    insideTicks.remove(e.getEntityId());
                }
            }
        }
    }

    /**
     * 当结构激活时立即清理一次（清除仇恨和路径）
     */
    private void scrubMobsAroundField() {
        int[] bb = currentWorldBounds();
        AxisAlignedBB search = new AxisAlignedBB(
                bb[0] - 48, 0, bb[2] - 48,
                bb[1] + 48, world.getActualHeight(), bb[3] + 48
        );

        for (Entity e : world.getEntitiesWithinAABB(Entity.class, search, this::isSRP)) {
            clearAggroIfTargetInside(e, bb);
            clearPathIfTouchesInside(e, bb);
        }
    }

    /**
     * 清除跨派系攻击：如果攻击者或目标至少一方是 SRP，且目标在区域内，则清除攻击目标
     */
    private void clearCrossFactionAggroIfTargetInside(EntityLiving attacker, int[] bb) {
        EntityLivingBase tgt = attacker.getAttackTarget();
        if (tgt == null) {
            tgt = attacker.getRevengeTarget();
        }

        if (tgt != null) {
            boolean targetInside = tgt.posX >= bb[0] && tgt.posX <= bb[1] && tgt.posZ >= bb[2] && tgt.posZ <= bb[3];
            if (targetInside) {
                boolean attackerIsSRP = isSRP(attacker);
                boolean targetIsSRP = isSRP(tgt);
                if (attackerIsSRP || targetIsSRP) {
                    attacker.setAttackTarget(null);
                    attacker.setRevengeTarget(null);
                    if (attacker.getNavigator() != null) {
                        attacker.getNavigator().clearPath();
                    }
                }
            }
        }
    }

    /**
     * 清除实体对区域内目标的攻击仇恨
     */
    private void clearAggroIfTargetInside(Entity e, int[] bb) {
        if (e instanceof EntityLiving) {
            EntityLiving el = (EntityLiving) e;
            EntityLivingBase tgt = el.getAttackTarget();
            if (tgt != null && isInside(bb, tgt)) {
                el.setAttackTarget(null);
                el.setRevengeTarget(null);
                if (el.getNavigator() != null) {
                    el.getNavigator().clearPath();
                }
            } else {
                EntityLivingBase rev = el.getRevengeTarget();
                if (rev != null && isInside(bb, rev)) {
                    el.setRevengeTarget(null);
                    if (el.getNavigator() != null) {
                        el.getNavigator().clearPath();
                    }
                }
            }
        }
    }

    /**
     * 清除实体路径中任何穿过区域内的路径点
     */
    private void clearPathIfTouchesInside(Entity e, int[] bb) {
        if (e instanceof EntityLiving) {
            EntityLiving el = (EntityLiving) e;
            if (el.getNavigator() != null) {
                Path path = el.getNavigator().getPath();
                if (path != null) {
                    int n = path.getCurrentPathLength();
                    for (int i = 0; i < n; i++) {
                        PathPoint p = path.getPathPointFromIndex(i);
                        if (p != null && p.x >= bb[0] && p.x <= bb[1] && p.z >= bb[2] && p.z <= bb[3]) {
                            el.getNavigator().clearPath();
                            if (el.getAttackTarget() != null && isInside(bb, el.getAttackTarget())) {
                                el.setAttackTarget(null);
                                el.setRevengeTarget(null);
                            }
                            return;
                        }
                    }
                }
            }
        }
    }

    /**
     * 将实体推送到区域外（最近的水平边界外 0.6 格）
     */
    private void pushOrTeleportOutside(int[] bb, Entity e) {
        double x = e.posX;
        double z = e.posZ;
        double dxMin = Math.abs(x - bb[0]);
        double dxMax = Math.abs(bb[1] - x);
        double dzMin = Math.abs(z - bb[2]);
        double dzMax = Math.abs(bb[3] - z);

        int edge = 0;
        double best = dxMin;
        if (dxMax < best) { best = dxMax; edge = 1; }
        if (dzMin < best) { best = dzMin; edge = 2; }
        if (dzMax < best) { edge = 3; }

        double OUT = 0.6;
        double tx = x, tz = z;
        switch (edge) {
            case 0: tx = bb[0] - OUT; break;
            case 1: tx = bb[1] + OUT; break;
            case 2: tz = bb[2] - OUT; break;
            default: tz = bb[3] + OUT; break;
        }

        // 找到该位置的地面高度
        BlockPos top = world.getTopSolidOrLiquidBlock(new BlockPos(MathHelper.floor(tx), 0, MathHelper.floor(tz)));
        double ty = top.getY() + 0.1;

        e.setPosition(tx, ty, tz);
        e.motionX *= 0.1;
        e.motionZ *= 0.1;
        e.motionY = Math.min(e.motionY, 0.0);
        e.fallDistance = 0.0F;
    }

    // ==================== 结构检测====================

    private boolean checkStructure() {
        World w = world;
        BlockPos center = pos;
        BlockPos corner = center.add(-4, 0, -4);

        BlockPos[] corners = { corner.add(0,0,0), corner.add(8,0,0), corner.add(0,0,8), corner.add(8,0,8) };
        BlockPos[] edges = { corner.add(4,0,0), corner.add(4,0,8), corner.add(0,0,4), corner.add(8,0,4) };

        for (BlockPos p : corners) {
            String id = world.getBlockState(p).getBlock().getRegistryName().toString();
            if (!id.equals(TROPHY_VOID_ORB)) return false;
        }
        for (BlockPos p : edges) {
            String id = world.getBlockState(p).getBlock().getRegistryName().toString();
            if (!id.equals(TROPHY_BOOM_ORB)) return false;
        }
        return true;
    }

    // ==================== 同步与渲染相关 ====================

    public boolean isStructureValid() {
        return structureValid;
    }

    public boolean isPowered() {
        return powered;
    }

    @Override
    public NBTTagCompound writeToNBT(NBTTagCompound compound) {
        super.writeToNBT(compound);
        compound.setBoolean("structureValid", structureValid);
        return compound;
    }

    @Override
    public void readFromNBT(NBTTagCompound compound) {
        super.readFromNBT(compound);
        structureValid = compound.getBoolean("structureValid");
    }

    @Override
    public NBTTagCompound getUpdateTag() {
        NBTTagCompound tag = super.getUpdateTag();
        tag.setBoolean("structureValid", structureValid);
        tag.setBoolean("powered", powered);
        return tag;
    }

    @Override
    public void handleUpdateTag(NBTTagCompound tag) {
        super.handleUpdateTag(tag);
        structureValid = tag.getBoolean("structureValid");
        powered = tag.getBoolean("powered");
        if (world != null && world.isRemote) {
            world.markBlockRangeForRenderUpdate(
                    pos.add(-64, -5, -64),
                    pos.add(65, 5, 65)
            );
        }
    }

    @Nullable
    @Override
    public SPacketUpdateTileEntity getUpdatePacket() {
        return new SPacketUpdateTileEntity(this.pos, 0, this.getUpdateTag());
    }

    @Override
    public void onDataPacket(NetworkManager net, SPacketUpdateTileEntity pkt) {
        handleUpdateTag(pkt.getNbtCompound());
    }

    @Override
    public AxisAlignedBB getRenderBoundingBox() {
        return new AxisAlignedBB(
                pos.getX() - 64, 0, pos.getZ() - 64,
                pos.getX() + 65, 255, pos.getZ() + 65
        );
    }

    @Override
    public void onLoad() {
        super.onLoad();
        if (!world.isRemote) {
            updateCoreCache();   // 初次加入世界时同步状态
        }
    }

    @Override
    public void invalidate() {
        super.invalidate();
        if (!world.isRemote) {
            ACTIVE_CORES.remove(new CoreInfo(world.provider.getDimension(), pos));
        }
    }
}