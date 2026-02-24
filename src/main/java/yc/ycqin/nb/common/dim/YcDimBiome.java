package yc.ycqin.nb.common.dim;

import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.WorldType;
import net.minecraft.world.biome.Biome;
import net.minecraft.world.biome.BiomeProvider;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public class YcDimBiome extends BiomeProvider {
    private static final Biome PARASITE_BIOME;

    static {
        PARASITE_BIOME = Biome.REGISTRY.getObject(new ResourceLocation("srparasites", "biomeparasite_shrouded"));
        if (PARASITE_BIOME == null) {
            // 如果找不到，立即抛出异常，避免后续使用 null 引发更隐蔽的错误
            throw new RuntimeException("SRP biome 'srparasites:biomeparasite_shrouded' not found! Check the registry name.");
        }
    }

    private final Biome biome;

    public YcDimBiome(long seed, WorldType worldType, String options) {
        super();
        this.biome = PARASITE_BIOME;
    }

    @Override
    public List<Biome> getBiomesToSpawnIn() {
        return Collections.singletonList(biome);
    }

    @Override
    public Biome getBiome(BlockPos pos) {
        return biome;
    }

    @Override
    public Biome[] getBiomes(Biome[] oldBiomeArray, int x, int z, int width, int depth, boolean cacheFlag) {
        if (oldBiomeArray == null || oldBiomeArray.length < width * depth) {
            oldBiomeArray = new Biome[width * depth];
        }
        Arrays.fill(oldBiomeArray, biome);
        return oldBiomeArray;
    }

    @Override
    public Biome[] getBiomesForGeneration(Biome[] biomes, int x, int z, int width, int height) {
        if (biomes == null || biomes.length < width * height) {
            biomes = new Biome[width * height];
        }
        Arrays.fill(biomes, biome);
        return biomes;
    }
}
