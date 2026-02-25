package yc.ycqin.nb;

import net.minecraft.init.Blocks;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.Mod.EventHandler;
import net.minecraftforge.fml.common.SidedProxy;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import org.apache.logging.log4j.Logger;
import org.spongepowered.asm.launch.MixinBootstrap;
import org.spongepowered.asm.mixin.Mixins;
import yc.ycqin.nb.proxy.CommonProxy;

@Mod(modid = ycqin.MODID, name = ycqin.NAME, version = ycqin.VERSION, dependencies = "required-after:draconicevolution")
public class ycqin
{
    public static final String MODID = "ycqin";
    public static final String NAME = "ycqin";
    public static final String VERSION = "1.0";

    @Mod.Instance(ycqin.MODID)
    private static ycqin instance;
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
}
