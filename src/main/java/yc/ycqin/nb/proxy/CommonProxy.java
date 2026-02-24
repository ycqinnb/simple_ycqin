package yc.ycqin.nb.proxy;

import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import yc.ycqin.nb.register.DimRegister;
import yc.ycqin.nb.register.ItemsRegister;
import yc.ycqin.nb.register.SoundRegister;

public class CommonProxy {
    public void preInit(FMLPreInitializationEvent event) {
        new ItemsRegister();
        new SoundRegister();
        new DimRegister();
    }

    public void init(FMLInitializationEvent event) {

    }
}
