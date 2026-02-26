package yc.ycqin.nb.common.item;

import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.world.World;
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
        tooltip.add("§e将其与任意带附魔物品合成使所有附魔等级加1（无限制）");
        tooltip.add("§e在寄生虫维度击杀生物概率掉落");
        // 你可以使用颜色代码 § 来设置颜色，例如 §7 为灰色，§e 为黄色
    }
}
