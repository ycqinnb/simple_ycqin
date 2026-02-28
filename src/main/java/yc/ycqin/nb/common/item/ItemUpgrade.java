package yc.ycqin.nb.common.item;

import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.world.World;
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

    @Override
    public void addInformation(ItemStack stack, @Nullable World worldIn, List<String> tooltip, ITooltipFlag flagIn) {
        // 添加介绍文字（可以有多行）
        tooltip.add("§e将其与任意带附魔物品合成使所有附魔等级加1"+"上限："+ ModConfig.ecMixLevel);
        tooltip.add("§e在寄生虫维度击杀生物概率掉落");
    }
}
