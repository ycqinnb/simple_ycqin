package yc.ycqin.nb.common.entity.tileentity;


import com.dhanantry.scapeandrunparasites.entity.ai.misc.EntityParasiteBase;
import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.Entity;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.init.Blocks;
import net.minecraft.init.Items;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.NetworkManager;
import net.minecraft.network.play.server.SPacketUpdateTileEntity;
import net.minecraft.server.management.PlayerChunkMap;
import net.minecraft.server.management.PlayerChunkMapEntry;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.DamageSource;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumParticleTypes;
import net.minecraft.util.ITickable;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.TextComponentString;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.world.WorldServer;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.items.CapabilityItemHandler;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.items.ItemStackHandler;
import net.minecraftforge.oredict.OreDictionary;
import yc.ycqin.nb.config.ModConfig;
import yc.ycqin.nb.register.BlocksRegister;


import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;

public class TileEntityLureActivator extends TileEntity implements ITickable {
    private ItemStack lure = ItemStack.EMPTY;
    private int lureLevel = 0;          // 1~10
    private int remainingUses = 0;
    private boolean structureValid = false;
    private List<BlockPos> shooterPositions = new ArrayList<>();
    private int attackCooldown = 0;
    private long lastCheckTime = 0;

    // 内部物品处理器（用于与外部交互，仅暴露一个槽位）
    private final ItemStackHandler internalInventory = new ItemStackHandler(1) {
        @Override
        public boolean isItemValid(int slot, @Nonnull ItemStack stack) {
            // 只接受诱饵方块
            if (stack.getItem().getRegistryName() == null) return false;
            return stack.getItem().getRegistryName().toString().equals("srparasites:evolutionlure");
        }

        @Override
        protected void onContentsChanged(int slot) {
            super.onContentsChanged(slot);
            // 当内部物品变化时，同步数据到客户端
            syncToTrackingClients();
        }
    };

    // ========== Capability 实现 ==========
    @Override
    public boolean hasCapability(@Nonnull Capability<?> capability, @Nullable EnumFacing facing) {
        if (capability == CapabilityItemHandler.ITEM_HANDLER_CAPABILITY) {
            return true;
        }
        return super.hasCapability(capability, facing);
    }

    @Nullable
    @Override
    public <T> T getCapability(@Nonnull Capability<T> capability, @Nullable EnumFacing facing) {
        if (capability == CapabilityItemHandler.ITEM_HANDLER_CAPABILITY) {
            return CapabilityItemHandler.ITEM_HANDLER_CAPABILITY.cast(new IItemHandler() {
                @Override
                public int getSlots() {
                    return 1;
                }

                @Override
                public @Nonnull ItemStack getStackInSlot(int slot) {
                    return internalInventory.getStackInSlot(slot);
                }

                @Override
                public @Nonnull ItemStack insertItem(int slot, @Nonnull ItemStack stack, boolean simulate) {
                    // 只有槽位为空且物品有效时才能插入
                    if (internalInventory.getStackInSlot(slot).isEmpty() && internalInventory.isItemValid(slot, stack)) {
                        if (!simulate) {
                            // 将物品存入内部，并解析等级和次数
                            ItemStack copy = stack.copy();
                            copy.setCount(1);
                            internalInventory.setStackInSlot(slot, copy);
                            // 解析诱饵等级和剩余次数
                            int meta = copy.getMetadata();
                            int level = meta + 1;
                            if (level < 1 || level > 10) level = 1;
                            int remaining = ModConfig.trapLureUses[level - 1];
                            NBTTagCompound tag = copy.getSubCompound("LureData");
                            if (tag != null && tag.hasKey("RemainingUses")) {
                                remaining = tag.getInteger("RemainingUses");
                            }
                            // 更新内部数据
                            lure = copy;
                            lureLevel = level;
                            remainingUses = remaining;
                            markDirty();
                            syncToTrackingClients();
                        }
                        // 返回剩余物品（因为只取一个，所以剩余 stack -1）
                        ItemStack result = stack.copy();
                        result.shrink(1);
                        return result;
                    }
                    return stack; // 无法插入，原样返回
                }

                @Override
                public @Nonnull ItemStack extractItem(int slot, int amount, boolean simulate) {
                    // 不允许外部直接提取（诱饵只能由装置消耗或通过内部机制弹出）
                    return ItemStack.EMPTY;
                }

                @Override
                public int getSlotLimit(int slot) {
                    return 1;
                }

                @Override
                public boolean isItemValid(int slot, @Nonnull ItemStack stack) {
                    return internalInventory.isItemValid(slot, stack);
                }
            });
        }
        return super.getCapability(capability, facing);
    }

    // ========== 数据同步（精确到追踪玩家） ==========
    @Override
    public NBTTagCompound getUpdateTag() {
        NBTTagCompound tag = super.getUpdateTag();
        writeToSyncNBT(tag);
        return tag;
    }

    @Override
    public void handleUpdateTag(NBTTagCompound tag) {
        super.handleUpdateTag(tag);
        readFromSyncNBT(tag);
    }

    @Nullable
    @Override
    public SPacketUpdateTileEntity getUpdatePacket() {
        NBTTagCompound tag = new NBTTagCompound();
        writeToSyncNBT(tag);
        return new SPacketUpdateTileEntity(pos, 1, tag);
    }

    @Override
    public void onDataPacket(NetworkManager net, SPacketUpdateTileEntity pkt) {
        readFromSyncNBT(pkt.getNbtCompound());
    }

    private void writeToSyncNBT(NBTTagCompound tag) {
        tag.setTag("Lure", lure.writeToNBT(new NBTTagCompound()));
        tag.setInteger("LureLevel", lureLevel);
        tag.setInteger("RemainingUses", remainingUses);
        tag.setBoolean("StructureValid", structureValid);
        tag.setTag("InternalInv", internalInventory.serializeNBT());
    }

    private void readFromSyncNBT(NBTTagCompound tag) {
        if (tag.hasKey("Lure")) {
            lure = new ItemStack(tag.getCompoundTag("Lure"));
        } else {
            lure = ItemStack.EMPTY;
        }
        lureLevel = tag.getInteger("LureLevel");
        remainingUses = tag.getInteger("RemainingUses");
        structureValid = tag.getBoolean("StructureValid");
        if (tag.hasKey("InternalInv")) {
            internalInventory.deserializeNBT(tag.getCompoundTag("InternalInv"));
        }
    }

    public void syncToTrackingClients() {
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

    private boolean isGlassBlock(IBlockState state) {
        Block block = state.getBlock();
        // 快速匹配原版玻璃
        if (block == Blocks.GLASS || block == Blocks.STAINED_GLASS) return true;

        // 创建物品栈前，先确保方块有对应的物品（某些方块如空气、流体可能没有）
        Item item = Item.getItemFromBlock(block);
        if (item == null || item == Items.AIR) return false; // 无效，直接返回false

        int meta = block.getMetaFromState(state);
        ItemStack stack = new ItemStack(block, 1, meta);
        // 检查栈是否有效
        if (stack.isEmpty()) return false;

        // 遍历矿词
        for (int id : OreDictionary.getOreIDs(stack)) {
            String name = OreDictionary.getOreName(id);
            if (name.equals("blockGlass") || name.equals("blockGlassColorless") || name.equals("paneGlass")) {
                return true;
            }
        }
        return false;
    }

    private void addEnergyFromParasite(EntityParasiteBase parasite) {
        if (!ModConfig.energyConverterEnabled) return;
        BlockPos upPos = pos.up();
        TileEntity te = world.getTileEntity(upPos);
        if (te instanceof TileEntityEnergyConverter) {
            float health = parasite.getMaxHealth();
            // 处理 NaN、无穷大、负数
            if (Float.isNaN(health) || Float.isInfinite(health) || health <= 0) {
                health = 0f; // 默认值，可根据需要调整
            }
            if (health >= (float) Integer.MAX_VALUE /ModConfig.energyPerHealth) {
                health = (float) Integer.MAX_VALUE /ModConfig.energyPerHealth;
            }
            int energy = (int) (health * ModConfig.energyPerHealth);
            ((TileEntityEnergyConverter) te).generateEnergy(energy);
        }
    }

    // ========== 持久化 NBT ==========
    @Override
    public NBTTagCompound writeToNBT(NBTTagCompound compound) {
        super.writeToNBT(compound);
        writeToSyncNBT(compound);
        return compound;
    }

    @Override
    public void readFromNBT(NBTTagCompound compound) {
        super.readFromNBT(compound);
        readFromSyncNBT(compound);
    }

    // ========== 游戏逻辑 ==========
    @Override
    public void update() {
        if (world.isRemote) return;
        if (!ModConfig.trapEnabled) return;

        long worldTime = world.getTotalWorldTime();
        if (worldTime - lastCheckTime >= ModConfig.trapStructureCheckInterval) {
            lastCheckTime = worldTime;
            checkStructure();
        }

        if (!structureValid) return;

        // 检查诱饵是否耗尽，如果是则尝试输出到相邻容器
        if (remainingUses <= 0 && !lure.isEmpty()) {
            outputLureToAdjacentContainer();
            clearLure(false); // 清空内部数据
            return;
        }

        if (lure.isEmpty()) return;

        if (attackCooldown > 0) {
            attackCooldown--;
        } else {
            attackNearbyParasites();
        }
        attractParasites();
    }

    private void outputLureToAdjacentContainer() {
        // 遍历六个方向，寻找有物品栏能力的容器
        for (EnumFacing face : EnumFacing.VALUES) {
            BlockPos adjacent = pos.offset(face);
            TileEntity te = world.getTileEntity(adjacent);
            if (te != null && te.hasCapability(CapabilityItemHandler.ITEM_HANDLER_CAPABILITY, face.getOpposite())) {
                IItemHandler handler = te.getCapability(CapabilityItemHandler.ITEM_HANDLER_CAPABILITY, face.getOpposite());
                if (handler != null) {
                    ItemStack output = lure.copy();
                    output.setCount(1);
                    NBTTagCompound tag = output.getOrCreateSubCompound("LureData");
                    tag.setInteger("RemainingUses", remainingUses);
                    // 尝试插入到容器
                    ItemStack remaining = handler.insertItem(0, output, false);
                    if (remaining.isEmpty()) {
                        // 成功插入
                        return;
                    }
                }
            }
        }
        // 没有相邻容器，掉落在地上
        ItemStack dropStack = lure.copy();
        dropStack.setCount(1);
        NBTTagCompound tag = dropStack.getOrCreateSubCompound("LureData");
        tag.setInteger("RemainingUses", remainingUses);
        EntityItem drop = new EntityItem(world, pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5, dropStack);
        world.spawnEntity(drop);
    }

    private void checkStructure() {
        shooterPositions.clear();
        boolean valid = true;

        int[][] offsets = {{-10, -10}, {10, -10}, {-10, 10}, {10, 10}};
        int yOffset = 10;

        for (int[] off : offsets) {
            BlockPos corner = new BlockPos(pos.getX() + off[0], pos.getY() + yOffset, pos.getZ() + off[1]);
            IBlockState magmaState = world.getBlockState(corner);
            if (magmaState.getBlock() != Blocks.LAVA && magmaState.getBlock() != Blocks.FLOWING_LAVA) {
                valid = false;
                break;
            }
            BlockPos below = corner.down();
            if (!isGlassBlock(world.getBlockState(below))) {
                valid = false;
                break;
            }

            BlockPos[] dirs = {corner.up(), corner.north(), corner.south(), corner.west(), corner.east()};
            for (BlockPos p : dirs) {
                if (!(world.getBlockState(p).getBlock() == BlocksRegister.BLOCKSHOOTER)) {
                    valid = false;
                    break;
                }
                shooterPositions.add(p);
            }
            if (!valid) break;
        }

        structureValid = valid;
    }

    private void attractParasites() {
        int range = ModConfig.trapLureRangeBase + lureLevel * ModConfig.trapLureRangePerLevel;
        AxisAlignedBB aabb = new AxisAlignedBB(pos).grow(range);
        List<EntityParasiteBase> parasites = world.getEntitiesWithinAABB(EntityParasiteBase.class, aabb);
        for (EntityParasiteBase p : parasites) {
            //p.setAttackTarget(null);
            p.getNavigator().tryMoveToXYZ(pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5, 1.2);
        }
    }

    private void attackNearbyParasites() {
        AxisAlignedBB aabb = new AxisAlignedBB(pos).grow(10);
        List<EntityParasiteBase> parasites = world.getEntitiesWithinAABB(EntityParasiteBase.class, aabb);
        if (parasites.isEmpty()) return;

        for (EntityParasiteBase p : parasites) {
            if (p.getHealth() <= 0) return;
            for (BlockPos shooter : shooterPositions) {
                spawnParticleBeam(shooter, p);
            }
            addEnergyFromParasite(p);
            p.setHealth(0);
            p.onDeath(DamageSource.LAVA);
            remainingUses--;
            if (remainingUses <= 0) {
                break;
            }
        }
        attackCooldown = ModConfig.trapAttackCooldown;
        syncToTrackingClients();
    }

    private void spawnParticleBeam(BlockPos from, Entity target) {
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
        for (double d = 0; d <= length; d += 0.4) {
            double x = x1 + dx * d;
            double y = y1 + dy * d;
            double z = z1 + dz * d;
            ws.spawnParticle(EnumParticleTypes.FLAME, x, y, z, 0, 0, 0, 0, 0, new int[0]);
        }
    }

    // ========== 交互方法（玩家右键） ==========
    public void onRightClick(EntityPlayer player) {
        if (world.isRemote) return;

        ItemStack held = player.getHeldItemMainhand();
        if (held.isEmpty()) {
            // 取出诱饵（仅当内部有诱饵且无外部请求时，玩家空手右键取出）
            if (!lure.isEmpty()) {
                ItemStack output = lure.copy();
                output.setCount(1);
                NBTTagCompound tag = output.getOrCreateSubCompound("LureData");
                tag.setInteger("RemainingUses", remainingUses);
                if (!player.inventory.addItemStackToInventory(output)) {
                    EntityItem drop = new EntityItem(world, pos.getX() + 0.5, pos.getY() + 1.5, pos.getZ() + 0.5, output);
                    world.spawnEntity(drop);
                }
                clearLure(false);
                player.sendMessage(new TextComponentString(TextFormatting.GREEN + "已取出诱饵"));
                syncToTrackingClients();
            }
            return;
        }

        // 判断是否为诱饵
        if (held.getItem().getRegistryName() == null || !held.getItem().getRegistryName().toString().equals("srparasites:evolutionlure")) {
            player.sendMessage(new TextComponentString(TextFormatting.RED + "只能放入诱饵！"));
            return;
        }

        checkStructure();
        if (!structureValid) {
            player.sendMessage(new TextComponentString(TextFormatting.RED + "结构不完整，无法激活！"));
            return;
        }

        if (!lure.isEmpty()) {
            player.sendMessage(new TextComponentString(TextFormatting.YELLOW + "已有诱饵，请先取出"));
            return;
        }

        int meta = held.getMetadata();
        int level = meta + 1;
        if (level < 1 || level > 10) level = 1;

        int remaining = ModConfig.trapLureUses[level - 1];
        NBTTagCompound tag = held.getSubCompound("LureData");
        if (tag != null && tag.hasKey("RemainingUses")) {
            remaining = tag.getInteger("RemainingUses");
            if (remaining <= 0) {
                player.sendMessage(new TextComponentString(TextFormatting.RED + "诱饵已耗尽！"));
                return;
            }
        }

        // 存储诱饵（只保留一个）
        ItemStack single = held.copy();
        single.setCount(1);
        this.lure = single;
        this.lureLevel = level;
        this.remainingUses = remaining;
        internalInventory.setStackInSlot(0, single); // 同步到内部物品栏

        held.shrink(1);
        markDirty();
        syncToTrackingClients();
        player.sendMessage(new TextComponentString(TextFormatting.GREEN + "已放入 " + level + " 级诱饵，剩余使用次数: " + remaining));
    }

    // ========== 辅助方法 ==========
    public ItemStack getLure() {
        return lure;
    }

    private void clearLure(boolean drop) {
        if (!lure.isEmpty() && drop) {
            ItemStack dropStack = lure.copy();
            dropStack.setCount(1);
            NBTTagCompound tag = dropStack.getOrCreateSubCompound("LureData");
            tag.setInteger("RemainingUses", remainingUses);
            EntityItem dropItem = new EntityItem(world, pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5, dropStack);
            world.spawnEntity(dropItem);
        }
        lure = ItemStack.EMPTY;
        lureLevel = 0;
        remainingUses = 0;
        internalInventory.setStackInSlot(0, ItemStack.EMPTY);
        markDirty();
        syncToTrackingClients();
    }

    // 方块破坏时调用
    public void dropLure() {
        if (!world.isRemote && !lure.isEmpty()) {
            ItemStack dropStack = lure.copy();
            dropStack.setCount(1);
            NBTTagCompound tag = dropStack.getOrCreateSubCompound("LureData");
            tag.setInteger("RemainingUses", remainingUses);
            EntityItem drop = new EntityItem(world, pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5, dropStack);
            world.spawnEntity(drop);
        }
        clearLure(false);
    }
}