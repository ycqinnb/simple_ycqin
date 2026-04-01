package yc.ycqin.nb;

import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.init.Blocks;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.Mod.EventHandler;
import net.minecraftforge.fml.common.SidedProxy;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPostInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import net.minecraftforge.fml.common.event.FMLServerStartingEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.PlayerEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;
import org.apache.logging.log4j.Logger;
import org.spongepowered.asm.launch.MixinBootstrap;
import org.spongepowered.asm.mixin.Mixins;
import yc.ycqin.nb.network.YcCommand;
import yc.ycqin.nb.proxy.CommonProxy;
import yc.ycqin.nb.srpcore.EvolutionDataManager;

@Mod(modid = ycqin.MODID, name = ycqin.NAME, version = ycqin.VERSION)
public class ycqin
{
    public static final String MODID = "ycqin";
    public static final String NAME = "ycqin";
    public static final String VERSION = "1.7";

    @Mod.Instance(ycqin.MODID)
    public static ycqin instance;
    @SidedProxy
            (clientSide = "yc.ycqin.nb.proxy.ClientProxy",
                    serverSide = "yc.ycqin.nb.proxy.CommonProxy"
            )
    private static CommonProxy proxy;

    public static Logger logger;

    @EventHandler
    public void preInit(FMLPreInitializationEvent event)
    {

        logger = event.getModLog();
        proxy.preInit(event);
    }

    @EventHandler
    public void init(FMLInitializationEvent event)
    {
        // some example code

        logger.info("DIRT BLOCK >> {}", Blocks.DIRT.getRegistryName());
        proxy.init(event);
    }

    @EventHandler
    public void postInit(FMLPostInitializationEvent event){
        proxy.postInit(event);
    }
    @EventHandler
    public static void onServerStarting(FMLServerStartingEvent event) {
        event.registerServerCommand(new YcCommand());
    }
}
