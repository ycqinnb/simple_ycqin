package yc.ycqin.nb.common.item;

import baubles.api.BaubleType;
import baubles.api.IBauble;
import baubles.api.cap.BaublesCapabilities;
import net.minecraft.client.resources.I18n;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.init.MobEffects;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.potion.Potion;
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
import java.util.List;
import static com.dhanantry.scapeandrunparasites.init.SRPPotions.VIRA_E;

@Optional.Interface(
        iface = "baubles.api.IBauble",
        modid = "baubles"
)
public class ItemAntidoteOrb extends Item implements IBauble {
    public ItemAntidoteOrb(){
        this.setMaxStackSize(1);
        this.setRegistryName("antidote_orb");
        this.setUnlocalizedName(ycqin.MODID+"."+"antidote_orb");
        this.setCreativeTab(ItemsRegister.YCQIN_TABLE);
    }
    @Optional.Method(
            modid = "baubles"
    )
    @Override
    public BaubleType getBaubleType(ItemStack itemStack) {
        return BaubleType.TRINKET;
    }

    @Optional.Method(
            modid = "baubles"
    )
    @Override
    public void onWornTick(ItemStack itemstack, EntityLivingBase player) {
        if (player.isPotionActive(VIRA_E)) {
            player.removePotionEffect(VIRA_E);
        }
        if (player.isPotionActive(MobEffects.POISON)) {
            player.removePotionEffect(MobEffects.POISON);
        }
    }

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
                    return (T) ItemAntidoteOrb.this;
                }
                return null;
            }
        };
    }

    @SideOnly(Side.CLIENT)
    @Override
    public void addInformation(ItemStack stack, @Nullable World worldIn, List<String> tooltip, ITooltipFlag flagIn) {
        tooltip.add(I18n.format("item.ycqin.antidote_orb.tooltip"));
    }
}
