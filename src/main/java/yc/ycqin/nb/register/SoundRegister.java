package yc.ycqin.nb.register;

import net.minecraft.util.ResourceLocation;
import net.minecraft.util.SoundEvent;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.registry.GameRegistry;
import yc.ycqin.nb.ycqin;

@Mod.EventBusSubscriber
public class SoundRegister {

    // 声音常量定义
    @GameRegistry.ObjectHolder(ycqin.MODID + ":111")
    public static final SoundEvent YCQIN_SOUND = null;

    public SoundRegister(){
        MinecraftForge.EVENT_BUS.register(this);
    }

    /**
     * 创建声音事件的辅助方法
     */
    private static SoundEvent createSoundEvent(String name) {
        ResourceLocation location = new ResourceLocation(ycqin.MODID, name);
        return new SoundEvent(location).setRegistryName(location);
    }

    @SubscribeEvent
    public static void registerSounds(RegistryEvent.Register<SoundEvent> event) {
        event.getRegistry().registerAll(
                //createSoundEvent("111")
        );
    }
}
