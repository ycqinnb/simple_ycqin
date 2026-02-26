package yc.ycqin.nb.register;

import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.init.Items;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import yc.ycqin.nb.common.item.ItemDrafh;
import yc.ycqin.nb.common.item.ItemUpgrade;
import yc.ycqin.nb.common.item.ItemYcqin;
import yc.ycqin.nb.ycqin;

@Mod.EventBusSubscriber
public class ItemsRegister {
    public static final CreativeTabs YCQIN_TABLE = new CreativeTabs("ycqin") {
        @Override
        public ItemStack getTabIconItem() {
            return new ItemStack(YCQIN);
        }
    };

    public static final Item YCQIN = new ItemYcqin();
    public static final Item Drafh = new ItemDrafh();
    public  static final Item UPGRADE = new ItemUpgrade();
    public ItemsRegister() {
        MinecraftForge.EVENT_BUS.register(this);
    }
    @SubscribeEvent
    public static void registerItems(RegistryEvent.Register<Item> event) {
        event.getRegistry().registerAll(
                YCQIN,Drafh,UPGRADE
        );
    }
}
