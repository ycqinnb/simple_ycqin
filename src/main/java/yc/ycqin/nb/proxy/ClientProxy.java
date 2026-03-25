package yc.ycqin.nb.proxy;

import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.client.registry.ClientRegistry;
import net.minecraftforge.fml.client.registry.RenderingRegistry;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPostInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import slimeknights.mantle.client.book.repository.FileRepository;
import slimeknights.tconstruct.library.book.TinkerBook;
import yc.ycqin.nb.client.book.BookTransformerAppendModifiers;
import yc.ycqin.nb.common.entity.EntitySlashOrbBoom;
import yc.ycqin.nb.common.entity.EntitySlashOrbVoid;

import yc.ycqin.nb.common.entity.EntitySummonSlash;
import yc.ycqin.nb.common.entity.tileentity.TileEntityParasiteCore;
import yc.ycqin.nb.gui.EvolutionHUD;
import yc.ycqin.nb.register.ModelsRegister;

import yc.ycqin.nb.client.render.RenderParasiteCore;
import yc.ycqin.nb.client.render.RenderSlashOrbScary;
import yc.ycqin.nb.client.render.RenderSlashOrbVoid;
import yc.ycqin.nb.client.render.RenderSummonSlash;

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
        if (CommonProxy.isTCLoaded){
            RenderingRegistry.registerEntityRenderingHandler(EntitySummonSlash.class, RenderSummonSlash::new);
        }
        ClientRegistry.bindTileEntitySpecialRenderer(TileEntityParasiteCore.class, new RenderParasiteCore());
    }

    @Override
    public void init(FMLInitializationEvent event) {
        super.init(event);
    }

    @Override
    public void postInit(FMLPostInitializationEvent event) {
        super.postInit(event);
        if (CommonProxy.isTCLoaded) {
            TinkerBook.INSTANCE.addTransformer(new BookTransformerAppendModifiers(new FileRepository("tconstruct:book")));
        }
    }
}
