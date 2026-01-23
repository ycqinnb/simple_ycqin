package yc.ycqin.nb.common.item;

import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.item.Item;
import yc.ycqin.nb.register.ItemsRegister;
import yc.ycqin.nb.ycqin;


public class ItemYcqin extends Item {
    public ItemYcqin(){
        this.setRegistryName("ycqin");
        this.setUnlocalizedName(ycqin.MODID+"."+"ycqin");
        this.setCreativeTab(ItemsRegister.YCQIN_TABLE);
    }
}
