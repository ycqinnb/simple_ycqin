package yc.ycqin.nb.proxy;

import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import yc.ycqin.nb.gui.EvolutionHUD;
import yc.ycqin.nb.register.DimRegister;
import yc.ycqin.nb.register.ItemsRegister;
import yc.ycqin.nb.register.SoundRegister;
import yc.ycqin.nb.srpcore.EvolutionDataManager;
import yc.ycqin.nb.srpcore.ParasiteEvolutionSync;

public class CommonProxy {
    public void preInit(FMLPreInitializationEvent event) {
        new ItemsRegister();
        new SoundRegister();
        new DimRegister();
        EvolutionDataManager.registerPackets();

        // 2. 注册HUD渲染事件
        MinecraftForge.EVENT_BUS.register(EvolutionHUD.INSTANCE);


    }

    public void init(FMLInitializationEvent event) {
        MinecraftForge.EVENT_BUS.register(new ParasiteEvolutionSync());
    }
}
