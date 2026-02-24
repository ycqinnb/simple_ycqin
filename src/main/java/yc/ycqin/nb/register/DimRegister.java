package yc.ycqin.nb.register;

import net.minecraft.world.DimensionType;
import net.minecraftforge.common.DimensionManager;
import yc.ycqin.nb.common.dim.YcDimProvide;

public class DimRegister {
    public static DimensionType ycdim;
    public DimRegister() {
        // 参数分别是：内部名称、存档后缀、维度ID、WorldProvider类、是否强制加载该维度
        ycdim = DimensionType.register("yc_dim", "yc_dim_spr", 78, YcDimProvide.class, false);
        // 向Forge的维度管理器注册
        DimensionManager.registerDimension(78, ycdim);
    }
}
