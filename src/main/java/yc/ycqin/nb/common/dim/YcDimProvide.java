package yc.ycqin.nb.common.dim;

import net.minecraft.util.math.BlockPos;
import net.minecraft.world.DimensionType;
import net.minecraft.world.WorldProvider;
import net.minecraft.world.gen.ChunkGeneratorFlat;
import net.minecraft.world.gen.IChunkGenerator;
import net.minecraft.world.storage.WorldInfo;
import yc.ycqin.nb.register.DimRegister;

public class YcDimProvide extends WorldProvider {
    @Override
    public DimensionType getDimensionType() {
        return DimRegister.ycdim;
    }

    @Override
    public String getSaveFolder() {
        // 返回的字符串就是存档文件夹的名字
        // 游戏会把它保存在世界存档目录下的一个名为 "ycdim" 的子文件夹里
        return "ycdim";
    }

    @Override
    public void init() {
        WorldInfo worldInfo = world.getWorldInfo();
        // 重点：创建 BiomeProvider 的实例，后面会传给生成器
        this.biomeProvider = new YcDimBiome(world.getSeed(), worldInfo.getTerrainType(), worldInfo.getGeneratorOptions());
        this.hasSkyLight = false;
    }

    @Override
    public IChunkGenerator createChunkGenerator() {
        // 使用自定义的区块生成器，并传入你的生物群系
        return new YcDimChunkGenerator(world, this.biomeProvider.getBiome(new BlockPos(0, 64, 0)));
        // 这里简单获取了(0,64,0)位置的生物群系，实际上就是你的单一寄生虫群系
    }
}
