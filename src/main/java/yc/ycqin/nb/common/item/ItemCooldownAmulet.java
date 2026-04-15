package yc.ycqin.nb.common.item;

import baubles.api.BaubleType;
import baubles.api.IBauble;
import baubles.api.cap.BaublesCapabilities;
import com.mrcrayfish.guns.object.Gun;
import net.minecraft.client.resources.I18n;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.entity.EntityLivingBase;
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
import static com.dhanantry.scapeandrunparasites.init.SRPPotions.FEAR_E;

@Optional.Interface(
        iface = "baubles.api.IBauble",
        modid = "baubles"
)
public class ItemCooldownAmulet extends Item implements IBauble {

    public ItemCooldownAmulet() {
        this.setMaxStackSize(1);
        this.setRegistryName("cooldown_amulet");
        this.setUnlocalizedName(ycqin.MODID+"."+"cooldown_amulet");
        this.setCreativeTab(ItemsRegister.YCQIN_TABLE);
    }
    @Optional.Method(
            modid = "baubles"
    )
    @Override
    public BaubleType getBaubleType(ItemStack itemstack) {
        return BaubleType.AMULET; // 放在护符槽
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
                    return (T) ItemCooldownAmulet.this;
                }
                return null;
            }
        };
    }
    @Optional.Method(
            modid = "baubles"
    )
    @Override
    public void onWornTick(ItemStack itemstack, EntityLivingBase player) {
        if (player.isPotionActive(FEAR_E)) {
            player.removePotionEffect(FEAR_E);
        }
    }

    @SideOnly(Side.CLIENT)
    @Override
    public void addInformation(ItemStack stack, @Nullable World worldIn, List<String> tooltip, ITooltipFlag flagIn) {
        tooltip.add(I18n.format("item.ycqin.cooldown_amulet.tooltip", NumberFormat.getPercentInstance().format(ModConfig.OrbReduction)));
    }
}
