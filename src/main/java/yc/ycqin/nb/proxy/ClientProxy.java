package yc.ycqin.nb.proxy;

import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import yc.ycqin.nb.register.ModelsRegister;

public class ClientProxy extends CommonProxy {
    @Override
    public void preInit(FMLPreInitializationEvent event) {
        super.preInit(event);
        new ModelsRegister();
    }

    @Override
    public void init(FMLInitializationEvent event) {
        super.init(event);
    }
}
