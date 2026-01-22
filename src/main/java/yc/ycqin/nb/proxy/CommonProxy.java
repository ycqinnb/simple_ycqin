package yc.ycqin.nb.proxy;

import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import yc.ycqin.nb.register.ItemsRegister;

public class CommonProxy {
    public void preInit(FMLPreInitializationEvent event) {
        new ItemsRegister();
    }

    public void init(FMLInitializationEvent event) {

    }
}
