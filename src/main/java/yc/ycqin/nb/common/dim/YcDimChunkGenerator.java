package yc.ycqin.nb.common.dim;

import com.dhanantry.scapeandrunparasites.util.ParasiteEventWorld;
import com.dhanantry.scapeandrunparasites.util.config.SRPConfigWorld;
import com.dhanantry.scapeandrunparasites.world.SRPSaveData;
import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.EnumCreatureType;
import net.minecraft.init.Blocks;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraft.world.biome.Biome;
import net.minecraft.world.chunk.Chunk;
import net.minecraft.world.chunk.ChunkPrimer;
import net.minecraft.world.gen.IChunkGenerator;
import net.minecraft.world.gen.NoiseGeneratorPerlin;

import javax.annotation.Nullable;
import java.lang.reflect.Field;
import java.util.Arrays;
import java.util.List;
import java.util.Random;

public class YcDimChunkGenerator implements IChunkGenerator {
    private final World world;
    private final Random random;
    private final Biome biome;
    private final NoiseGeneratorPerlin noiseGen;

    // 基础方块
    private final IBlockState stone = Blocks.STONE.getDefaultState();
    private final IBlockState water = Blocks.WATER.getDefaultState();

    // 寄生方块（从 SRP 获取）
    private final IBlockState infestedStone;
    private final IBlockState infestedDirt;

    // 节点放置标志（确保只尝试一次）
    private boolean nodePlaced = false;

    public YcDimChunkGenerator(World world, Biome biome) {
        this.world = world;
        this.random = new Random(world.getSeed());
        this.biome = biome;
        this.noiseGen = new NoiseGeneratorPerlin(this.random, 4);

        // 获取 SRP 寄生方块
        Block rubble = Block.REGISTRY.getObject(new ResourceLocation("srparasites", "infestedrubble"));
        infestedStone = rubble != null ? rubble.getDefaultState() : stone; // 后备为石头

        Block stain = Block.REGISTRY.getObject(new ResourceLocation("srparasites", "infestedstain"));
        infestedDirt = stain != null ? stain.getDefaultState() : Blocks.DIRT.getDefaultState(); // 后备为泥土
    }

    @Override
    public Chunk generateChunk(int x, int z) {
        ChunkPrimer primer = new ChunkPrimer();
        if (!nodePlaced && !world.isRemote) {
            ensureNodePlaced();
        }
        // 1. 生成基础地形（平缓版）
        generateTerrain(primer, x, z);

        // 2. 应用生物群系地表装饰（会覆盖最表层）
        biome.genTerrainBlocks(world, world.rand, primer, x * 16, z * 16, 0.5);

        // 3. 将原版石头和泥土替换为寄生版本
        replaceBlocks(primer, x, z);

        // 4. 创建区块并强制设置生物群系
        Chunk chunk = new Chunk(world, primer, x, z);
        byte[] biomeArray = chunk.getBiomeArray();
        for (int i = 0; i < biomeArray.length; ++i) {
            biomeArray[i] = (byte) Biome.getIdForBiome(biome);
        }
        chunk.generateSkylightMap();
        return chunk;
    }

    /**
     * 平缓地形生成（噪声振幅减小，频率降低）
     */
    private void generateTerrain(ChunkPrimer primer, int chunkX, int chunkZ) {
        double[] heightMap = getHeightMap(chunkX, chunkZ);

        for (int localX = 0; localX < 16; ++localX) {
            for (int localZ = 0; localZ < 16; ++localZ) {
                int baseHeight = (int) heightMap[localX * 16 + localZ];
                if (baseHeight < 0) baseHeight = 0;
                if (baseHeight > 255) baseHeight = 255; // 防止越界

                // 填充石头从 y=0 到 baseHeight-1
                for (int y = 0; y < baseHeight; ++y) {
                    primer.setBlockState(localX, y, localZ, stone);
                }
                // 填充水从 baseHeight 到 62（海平面以下）
                if (baseHeight < 63) {
                    for (int y = baseHeight; y < 63; ++y) {
                        primer.setBlockState(localX, y, localZ, water);
                    }
                }
            }
        }
    }

    /**
     * 生成更平缓的高度图
     */
    private double[] getHeightMap(int chunkX, int chunkZ) {
        double[] heightMap = new double[16 * 16];
        for (int localX = 0; localX < 16; ++localX) {
            for (int localZ = 0; localZ < 16; ++localZ) {
                int realX = chunkX * 16 + localX;
                int realZ = chunkZ * 16 + localZ;
                // 降低频率（0.005），减小振幅（8），使地形起伏更平缓
                double noiseVal = noiseGen.getValue(realX * 0.005, realZ * 0.005);
                heightMap[localX * 16 + localZ] = 60 + (noiseVal * 8);
            }
        }
        return heightMap;
    }

    /**
     * 替换原版石头和泥土为寄生版本
     */
    private void replaceBlocks(ChunkPrimer primer, int chunkX, int chunkZ) {
        for (int localX = 0; localX < 16; ++localX) {
            for (int localZ = 0; localZ < 16; ++localZ) {
                for (int y = 0; y < 256; ++y) {
                    IBlockState state = primer.getBlockState(localX, y, localZ);
                    if (state.getBlock() == Blocks.STONE) {
                        primer.setBlockState(localX, y, localZ, infestedStone);
                    } else if (state.getBlock() == Blocks.DIRT && state == Blocks.DIRT.getDefaultState()) {
                        // 只替换普通泥土（metadata 0），不替换草方块、砂土等
                        primer.setBlockState(localX, y, localZ, infestedDirt);
                    }
                }
            }
        }
    }

    @Override
    public void populate(int x, int z) {
        BlockPos chunkPos = new BlockPos(x * 16, 0, z * 16);
        Random rand = new Random(world.getSeed());
        long r = rand.nextLong() / 2L * 2L + 1L;
        long r1 = rand.nextLong() / 2L * 2L + 1L;
        rand.setSeed((long) x * r + (long) z * r1 ^ world.getSeed());

        // 生物群系装饰（生成植物、结构等）
        biome.decorate(world, rand, chunkPos);

    }

    private void ensureNodePlaced() {
        //BlockPos nodePos = new BlockPos(0, 10, 0);

        // 1. 设置演化阶段为10
        ensurePhase10(world);

        nodePlaced = true;

        // 3. 放置节点（类型1为默认节点）
        //int result = ParasiteEventWorld.placeHeartInWorld(world, nodePos, 1);
        //if (result == 1 || result == 8) { // 成功 或 太靠近另一个节点（视为已存在）
        //    nodePlaced = true;
        //    System.out.println("[YcDim] Node placed successfully at (0,10,0)");
       // } else {
        //    System.out.println("[YcDim] Failed to place node at (0,10,0), error code: " + result);
       //     // 不设置 nodePlaced，以便后续再次尝试（但避免无限重试，这里保留false）
       // }
    }

    private void ensurePhase10(World world) {
        if (!world.isRemote) {
            SRPSaveData data = SRPSaveData.get(world, 63);
            int dim = world.provider.getDimension();
            data.setEvolutionPhase(dim, (byte)10, true, world);
        }
    }


    @Override
    public boolean generateStructures(Chunk chunk, int x, int z) {
        return false;
    }

    @Override
    public List<Biome.SpawnListEntry> getPossibleCreatures(EnumCreatureType creatureType, BlockPos pos) {
        Biome biome = world.getBiome(pos);
        return biome.getSpawnableList(creatureType);
    }

    @Nullable
    @Override
    public BlockPos getNearestStructurePos(World worldIn, String structureName, BlockPos position, boolean findUnexplored) {
        return null;
    }

    @Override
    public void recreateStructures(Chunk chunk, int x, int z) {
    }

    @Override
    public boolean isInsideStructure(World worldIn, String structureName, BlockPos pos) {
        return false;
    }

    private static void addDimensionToSRPWhitelist(int dimId) {
        try {
            // 反射获取 blackListedDimensionsNodes 字段
            Field field = SRPConfigWorld.class.getField("blackListedDimensionsNodes");
            field.setAccessible(true);
            int[] original = (int[]) field.get(null);
            // 检查是否已存在
            for (int id : original) {
                if (id == dimId) return;
            }
            // 创建新数组
            int[] newArray = Arrays.copyOf(original, original.length + 1);
            newArray[original.length] = dimId;
            field.set(null, newArray);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}