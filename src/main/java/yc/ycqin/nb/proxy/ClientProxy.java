package yc.ycqin.nb.proxy;

import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.client.registry.RenderingRegistry;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import yc.ycqin.nb.common.entity.EntitySlashOrbBoom;
import yc.ycqin.nb.common.entity.EntitySlashOrbVoid;
import yc.ycqin.nb.gui.EvolutionHUD;
import yc.ycqin.nb.register.ModelsRegister;
import yc.ycqin.nb.render.RenderSlashOrbScary;
import yc.ycqin.nb.render.RenderSlashOrbVoid;

public class ClientProxy extends CommonProxy {
    @Override
    public void preInit(FMLPreInitializationEvent event) {
        super.preInit(event);
        new ModelsRegister();
        MinecraftForge.EVENT_BUS.register(EvolutionHUD.INSTANCE);
        if (CommonProxy.isSlashBladeLoaded) {
            RenderingRegistry.registerEntityRenderingHandler(EntitySlashOrbBoom.class, RenderSlashOrbScary::new);
            RenderingRegistry.registerEntityRenderingHandler(EntitySlashOrbVoid.class, RenderSlashOrbVoid::new);
        }
    }

    @Override
    public void init(FMLInitializationEvent event) {
        super.init(event);
    }
}
