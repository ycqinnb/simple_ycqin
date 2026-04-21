package yc.ycqin.nb.common.item;

import baubles.api.BaubleType;
import baubles.api.IBauble;
import baubles.api.cap.BaublesCapabilities;
import net.minecraft.client.resources.I18n;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.EnumFacing;
import net.minecraft.world.World;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.ICapabilityProvider;
import net.minecraftforge.fml.common.Optional;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import yc.ycqin.nb.config.ModConfig;
import yc.ycqin.nb.register.ItemsRegister;
import yc.ycqin.nb.ycqin;

import javax.annotation.Nullable;
import java.text.NumberFormat;
import java.util.List;

@Optional.Interface(
        iface = "baubles.api.IBauble",
        modid = "baubles"
)
public class ItemPullImmunityCharm extends Item implements IBauble {

    public ItemPullImmunityCharm() {
        this.setMaxStackSize(1);
        this.setRegistryName("pull_immunity_charm");
        this.setUnlocalizedName(ycqin.MODID+"."+"pull_immunity_charm");
        this.setCreativeTab(ItemsRegister.YCQIN_TABLE);
    }

    @Optional.Method(
            modid = "baubles"
    )
    @Override
    public BaubleType getBaubleType(ItemStack itemstack) {
        return BaubleType.TRINKET; // 任意槽位
    }

    // 提供 Capability，让 Baubles 识别
    @Optional.Method(
            modid = "baubles"
    )
    @Override
    public ICapabilityProvider initCapabilities(ItemStack stack, @Nullable NBTTagCompound nbt) {
        return new ICapabilityProvider() {
            @Override
            public boolean hasCapability(Capability<?> capability, EnumFacing facing) {
                return capability == BaublesCapabilities.CAPABILITY_ITEM_BAUBLE;
            }

            @Override
            public <T> T getCapability(Capability<T> capability, EnumFacing facing) {
                if (capability == BaublesCapabilities.CAPABILITY_ITEM_BAUBLE) {
                    return (T) ItemPullImmunityCharm.this;
                }
                return null;
            }
        };
    }

    @SideOnly(Side.CLIENT)
    @Override
    public void addInformation(ItemStack stack, @Nullable World worldIn, List<String> tooltip, ITooltipFlag flagIn) {
        tooltip.add(I18n.format("item.ycqin.pull_immunity_charm.tooltip"));
    }
}