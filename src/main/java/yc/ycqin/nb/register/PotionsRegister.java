package yc.ycqin.nb.register;

import net.minecraft.init.Items;
import net.minecraft.init.PotionTypes;
import net.minecraft.item.ItemStack;
import net.minecraft.potion.Potion;
import net.minecraft.potion.PotionEffect;
import net.minecraft.potion.PotionHelper;
import net.minecraft.potion.PotionType;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.registry.ForgeRegistries;
import yc.ycqin.nb.common.potion.PotionConfusion;
import yc.ycqin.nb.common.potion.PotionStun;

@Mod.EventBusSubscriber(modid = "ycqin")
public class PotionsRegister {
    public static final Potion STUN;
    public static PotionType STUN_TYPE;
    public static final Potion CONFUSION;
    public static PotionType CONFUSION_TYPE;

    static {
        STUN = new PotionStun();
        STUN_TYPE = new PotionType("ycqin:stun", new PotionEffect(STUN, 20000, 0));
        STUN_TYPE.setRegistryName("ycqin:stun");

        CONFUSION = new PotionConfusion();
        CONFUSION_TYPE = new PotionType("ycqin:confusion", new PotionEffect(CONFUSION, 20000, 0));
        CONFUSION_TYPE.setRegistryName("ycqin:confusion");
    }


    @SubscribeEvent
    public static void onEventE(RegistryEvent.Register<Potion> event) {
        event.getRegistry().registerAll(
                STUN,CONFUSION
        );
    }

    @SubscribeEvent
    public static void onEventP(RegistryEvent.Register<PotionType> event){
        event.getRegistry().registerAll(
                STUN_TYPE,CONFUSION_TYPE
        );
    }


}