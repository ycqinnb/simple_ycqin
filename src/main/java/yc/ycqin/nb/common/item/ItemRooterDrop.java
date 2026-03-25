package yc.ycqin.nb.common.item;

import net.minecraft.item.Item;
import yc.ycqin.nb.register.ItemsRegister;
import yc.ycqin.nb.ycqin;

public class ItemRooterDrop extends Item {
    public ItemRooterDrop(){
        this.setRegistryName("rooter_drop");
        this.setUnlocalizedName(ycqin.MODID+"."+"rooter_drop");
        this.setCreativeTab(ItemsRegister.YCQIN_TABLE);
        this.setMaxStackSize(16);
    }
}
