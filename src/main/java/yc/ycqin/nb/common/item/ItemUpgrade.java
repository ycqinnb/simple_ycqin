package yc.ycqin.nb.common.item;

import net.minecraft.client.resources.I18n;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import yc.ycqin.nb.config.ModConfig;
import yc.ycqin.nb.register.ItemsRegister;
import yc.ycqin.nb.ycqin;

import javax.annotation.Nullable;
import java.util.List;

public class ItemUpgrade extends Item {
    public ItemUpgrade(){
        this.setRegistryName("upgrade");
        this.setUnlocalizedName(ycqin.MODID+"."+"upgrade");
        this.setCreativeTab(ItemsRegister.YCQIN_TABLE);
        this.setMaxStackSize(64);
    }

    @SideOnly(Side.CLIENT)
    @Override
    public void addInformation(ItemStack stack, @Nullable World worldIn, List<String> tooltip, ITooltipFlag flagIn) {
        tooltip.add(I18n.format("item.ycqin.upgrade.tooltip1",ModConfig.ecMixLevel));
        tooltip.add(I18n.format("item.ycqin.upgrade.tooltip2"));
    }
}
