package yc.ycqin.nb.common.dim;

import net.minecraft.world.DimensionType;
import net.minecraft.world.WorldProvider;
import net.minecraft.world.gen.IChunkGenerator;
import yc.ycqin.nb.client.render.dim.SkyRendererEye;
import yc.ycqin.nb.register.DimRegister;

public class YcDimProvide extends WorldProvider {

    @Override
    public DimensionType getDimensionType() {
        return DimRegister.ycdim;
    }

    @Override
    public String getSaveFolder() {
        return "ycdim";
    }

    @Override
    public void init() {
        this.biomeProvider = new MixedBiomeProvider(world.getSeed());
        // 关键改动：开启 hasSkyLight 让原版调用天空渲染器
        this.hasSkyLight = true;   // 必须为 true，否则天空渲染器不会被调用
        if (world.isRemote) {
            this.setSkyRenderer(SkyRendererEye.INSTANCE);
        }
    }

    @Override
    public IChunkGenerator createChunkGenerator() {
        return new YcDimChunkGenerator(world, biomeProvider);
    }

    // 永久黑夜
    @Override
    public float calculateCelestialAngle(long worldTime, float partialTicks) {
        return 0.5F; // 午夜
    }

    @Override
    public boolean hasSkyLight() {
        return super.hasSkyLight();
    }

    @Override
    public boolean isDaytime() {
        return false;
    }

    @Override
    public int getMoonPhase(long worldTime) {
        return 0;
    }

    @Override
    public float getSunBrightness(float partialTicks) {
        return 0.0F; // 无太阳光
    }

    @Override
    public float getStarBrightness(float partialTicks) {
        return 0.0F; // 不需要星星
    }

    // 如果原版想用默认天空，我们返回 false 避免干扰
    @Override
    public boolean isSkyColored() {
        return false;
    }
}