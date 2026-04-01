package yc.ycqin.nb.common.entity.tileentity;

import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.NetworkManager;
import net.minecraft.network.play.server.SPacketUpdateTileEntity;
import net.minecraft.server.management.PlayerChunkMapEntry;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.ITickable;
import net.minecraft.world.WorldServer;
import net.minecraftforge.energy.CapabilityEnergy;
import net.minecraftforge.energy.IEnergyStorage;
import yc.ycqin.nb.config.ModConfig;

import javax.annotation.Nullable;

public class TileEntityEnergyConverter extends TileEntity implements ITickable {
    private int energy = 0;
    private final int maxEnergy = ModConfig.energyMaxStorage;
    private int outputCooldown = 0;

    private final IEnergyStorage energyStorage = new IEnergyStorage() {
        @Override
        public int receiveEnergy(int maxReceive, boolean simulate) {
            return 0; // 不接受外部输入
        }

        @Override
        public int extractEnergy(int maxExtract, boolean simulate) {
            int extract = Math.min(energy, maxExtract);
            if (!simulate) {
                energy -= extract;
                markDirty();
                syncToTrackingClients();
            }
            return extract;
        }

        @Override
        public int getEnergyStored() { return energy; }
        @Override
        public int getMaxEnergyStored() { return maxEnergy; }
        @Override
        public boolean canExtract() { return true; }
        @Override
        public boolean canReceive() { return false; }
    };

    public void generateEnergy(int amount) {
        if (amount <= 0) return;
        energy += amount;
        if (energy > maxEnergy) energy = maxEnergy;
        markDirty();
        syncToTrackingClients();
    }

    // 自动输出逻辑
    @Override
    public void update() {
        if (world.isRemote) return;
        if (outputCooldown > 0) {
            outputCooldown--;
            return;
        }
        outputCooldown = ModConfig.energyOutputInterval;

        if (energy <= 0) return;

        // 遍历六个方向
        for (EnumFacing face : EnumFacing.VALUES) {
            TileEntity te = world.getTileEntity(pos.offset(face));
            if (te != null && te.hasCapability(CapabilityEnergy.ENERGY, face.getOpposite())) {
                IEnergyStorage receiver = te.getCapability(CapabilityEnergy.ENERGY, face.getOpposite());
                if (receiver != null && receiver.canReceive()) {
                    int extracted = energyStorage.extractEnergy(ModConfig.energyOutputRate, true);
                    int received = receiver.receiveEnergy(extracted, false);
                    if (received > 0) {
                        energyStorage.extractEnergy(received, false);
                        if (energy <= 0) break;
                    }
                }
            }
        }
    }

    // ========== 数据同步 ==========
    @Override
    public NBTTagCompound getUpdateTag() {
        NBTTagCompound tag = super.getUpdateTag();
        tag.setInteger("Energy", energy);
        return tag;
    }

    @Override
    public void handleUpdateTag(NBTTagCompound tag) {
        super.handleUpdateTag(tag);
        energy = tag.getInteger("Energy");
    }

    @Nullable
    @Override
    public SPacketUpdateTileEntity getUpdatePacket() {
        NBTTagCompound tag = new NBTTagCompound();
        tag.setInteger("Energy", energy);
        return new SPacketUpdateTileEntity(pos, 1, tag);
    }

    @Override
    public void onDataPacket(NetworkManager net, SPacketUpdateTileEntity pkt) {
        energy = pkt.getNbtCompound().getInteger("Energy");
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

    // ========== 持久化 ==========
    @Override
    public NBTTagCompound writeToNBT(NBTTagCompound compound) {
        super.writeToNBT(compound);
        compound.setInteger("Energy", energy);
        return compound;
    }

    @Override
    public void readFromNBT(NBTTagCompound compound) {
        super.readFromNBT(compound);
        energy = compound.getInteger("Energy");
    }

    // 需要在类中导入 WorldServer 和 EntityPlayerMP，添加 import
}