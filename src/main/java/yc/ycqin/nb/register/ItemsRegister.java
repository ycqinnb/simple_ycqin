package yc.ycqin.nb.register;

import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import yc.ycqin.nb.common.item.*;
import yc.ycqin.nb.proxy.CommonProxy;

@Mod.EventBusSubscriber
public class ItemsRegister {
    public static final CreativeTabs YCQIN_TABLE = new CreativeTabs("ycqin") {
        @Override
        public ItemStack getTabIconItem() {
            return new ItemStack(YCQIN);
        }
    };

    public static final Item YCQIN = new ItemYcqin();
    public static final Item UPGRADE = new ItemUpgrade();
    public static final Item ROOTERDROP = new ItemRooterDrop();
    public static Item CooldownAmulet;
    public static Item ANTIDOTEORB;
    public ItemsRegister() {
        MinecraftForge.EVENT_BUS.register(this);
    }
    @SubscribeEvent
    public static void registerItems(RegistryEvent.Register<Item> event) {
        event.getRegistry().registerAll(
                YCQIN,UPGRADE,ROOTERDROP
        );
        if (CommonProxy.isBaublesLoaded){
            CooldownAmulet = new ItemCooldownAmulet();
            ANTIDOTEORB = new ItemAntidoteOrb();

            event.getRegistry().register(CooldownAmulet);
            event.getRegistry().register(ANTIDOTEORB);
        }
    }
}
