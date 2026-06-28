package yc.ycqin.nb.common.dim;

import net.minecraft.block.state.IBlockState;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraft.world.WorldServer;
import net.minecraft.world.biome.BiomeProvider;
import net.minecraft.world.chunk.Chunk;

import net.minecraft.world.chunk.storage.ExtendedBlockStorage;
import net.minecraft.entity.EnumCreatureType;
import javax.annotation.Nullable;
import java.util.List;
import net.minecraft.init.Blocks;
import net.minecraft.world.gen.IChunkGenerator;

public class MirageChunkGenerator implements IChunkGenerator {
    private final World world;
    private final int originalDim;
    private final BiomeProvider biomeProvider;
    private final WorldServer originalWorld;

    public MirageChunkGenerator(World world, int originalDim, BiomeProvider biomeProvider) {
        this.world = world;
        this.originalDim = originalDim;
        this.biomeProvider = biomeProvider;
        // 获取原维度世界实例
        if (world instanceof WorldServer) {
            this.originalWorld = ((WorldServer) world).getMinecraftServer().getWorld(originalDim);
        } else {
            this.originalWorld = null;
        }
    }

    @Override
    public Chunk generateChunk(int x, int z) {
        Chunk chunk = new Chunk(world, x, z);
        if (originalWorld == null) return chunk;

        // 强制加载原维度的对应区块（如果未加载，会从磁盘读取或生成）
        Chunk originalChunk = originalWorld.getChunkFromChunkCoords(x, z);
        ExtendedBlockStorage[] sections = originalChunk.getBlockStorageArray();

        for (int i = 0; i < sections.length; i++) {
            if (sections[i] != null) {
                ExtendedBlockStorage copy = new ExtendedBlockStorage(i * 16, world.provider.hasSkyLight());
                for (int y = 0; y < 16; y++) {
                    for (int dx = 0; dx < 16; dx++) {
                        for (int dz = 0; dz < 16; dz++) {
                            IBlockState state = sections[i].get(dx, y, dz);
                            if (state != Blocks.AIR.getDefaultState()) {
                                copy.set(dx, y, dz, state);
                            }
                        }
                    }
                }
                chunk.getBlockStorageArray()[i] = copy;
            }
        }

        // 复制生物群系数组
        byte[] originalBiomes = originalChunk.getBiomeArray();
        byte[] newBiomes = chunk.getBiomeArray();
        System.arraycopy(originalBiomes, 0, newBiomes, 0, newBiomes.length);

        // 复制方块实体（箱子、熔炉等）
        for (TileEntity te : originalChunk.getTileEntityMap().values()) {
            BlockPos pos = te.getPos();
            TileEntity newTe = TileEntity.create(world, te.serializeNBT());
            if (newTe != null) {
                chunk.addTileEntity(pos, newTe);
            }
        }

        chunk.setLightPopulated(true);
        chunk.setModified(true);
        return chunk;
    }

    @Override public void populate(int x, int z) {}
    @Override public boolean generateStructures(Chunk chunkIn, int x, int z) { return false; }
    @Override public List<net.minecraft.world.biome.Biome.SpawnListEntry> getPossibleCreatures(EnumCreatureType creatureType, BlockPos pos) {
        return world.getBiome(pos).getSpawnableList(creatureType);
    }
    @Nullable @Override public BlockPos getNearestStructurePos(World worldIn, String structureName, BlockPos position, boolean findUnexplored) { return null; }
    @Override public void recreateStructures(Chunk chunkIn, int x, int z) {}
    @Override public boolean isInsideStructure(World worldIn, String structureName, BlockPos pos) { return false; }
}