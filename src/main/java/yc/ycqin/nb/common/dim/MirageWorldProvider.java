package yc.ycqin.nb.common.dim;

import net.minecraft.world.DimensionType;
import net.minecraft.world.WorldProvider;
import net.minecraft.world.gen.IChunkGenerator;
import net.minecraft.world.storage.WorldInfo;
import yc.ycqin.nb.register.DimRegister;

public class MirageWorldProvider extends WorldProvider {
    private int originalDim = 0;

    @Override
    public DimensionType getDimensionType() {
        return DimRegister.mirageDim;
    }

    @Override
    public String getSaveFolder() {
        return "mirage_" + originalDim;
    }

    @Override
    public void init() {
        int myDim = this.world.provider.getDimension();
        this.originalDim = MirageManager.getOriginalDim(world.provider.getDimension());

        WorldInfo info = world.getWorldInfo();
        this.biomeProvider = new net.minecraft.world.biome.BiomeProvider(info);
        this.hasSkyLight = (originalDim == 0);
    }

    @Override
    public IChunkGenerator createChunkGenerator() {
        return new MirageChunkGenerator(world, originalDim, biomeProvider);
    }
}