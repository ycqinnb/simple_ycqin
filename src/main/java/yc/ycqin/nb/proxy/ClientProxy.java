package yc.ycqin.nb.proxy;

import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import yc.ycqin.nb.gui.EvolutionHUD;
import yc.ycqin.nb.register.ModelsRegister;

public class ClientProxy extends CommonProxy {
    @Override
    public void preInit(FMLPreInitializationEvent event) {
        super.preInit(event);
        new ModelsRegister();
        MinecraftForge.EVENT_BUS.register(EvolutionHUD.INSTANCE);
    }

    @Override
    public void init(FMLInitializationEvent event) {
        super.init(event);
    }
}
