package yc.ycqin.nb.common.item;

import baubles.api.BaubleType;
import baubles.api.IBauble;
import baubles.api.cap.BaublesCapabilities;
import com.dhanantry.scapeandrunparasites.init.SRPPotions;
import com.overlast.cap.courage.CourageProvider;
import com.overlast.cap.courage.ICourage;
import net.minecraft.client.resources.I18n;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
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
import yc.ycqin.nb.proxy.CommonProxy;
import yc.ycqin.nb.register.ItemsRegister;
import yc.ycqin.nb.util.OverHelper;
import yc.ycqin.nb.ycqin;

import javax.annotation.Nullable;
import java.text.NumberFormat;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

import static com.dhanantry.scapeandrunparasites.init.SRPPotions.FEAR_E;

@Optional.Interface(
        iface = "baubles.api.IBauble",
        modid = "baubles"
)
public class ItemCooldownAmulet extends Item implements IBauble {

    // 记录每个玩家上次增加勇气时的世界时间（tick）
    public static final Map<UUID, Long> lastCourageTime = new ConcurrentHashMap<>();

    public ItemCooldownAmulet() {
        this.setMaxStackSize(1);
        this.setRegistryName("cooldown_amulet");
        this.setUnlocalizedName(ycqin.MODID + "." + "cooldown_amulet");
        this.setCreativeTab(ItemsRegister.YCQIN_TABLE);
    }

    @Optional.Method(modid = "baubles")
    @Override
    public BaubleType getBaubleType(ItemStack itemstack) {
        return BaubleType.AMULET;
    }

    @Optional.Method(modid = "baubles")
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

    @Optional.Method(modid = "baubles")
    @Override
    public void onWornTick(ItemStack itemstack, EntityLivingBase player) {
        // 移除恐惧效果
        if (player.isPotionActive(FEAR_E)) {
            player.removePotionEffect(FEAR_E);
        }
        if (!CommonProxy.isOverLoaded || !CommonProxy.isOverCourageLoaded) return;
        if (player instanceof EntityPlayer) {
            EntityPlayer ep = (EntityPlayer) player;
            World world = ep.world;

            // 1. 原有功能：理智回满
            OverHelper.setSanity(ep, OverHelper.getMaxSanity(ep));

            // 2. 新增：每 5 秒增加 2 点勇气（仅服务端执行）
            if (!world.isRemote) {
                long currentTime = world.getTotalWorldTime();
                UUID uuid = ep.getUniqueID();
                Long lastTime = lastCourageTime.get(uuid);
                if (lastTime == null || (currentTime - lastTime) >= 100) { // 100 ticks = 5秒
                    ICourage courage = ep.getCapability(CourageProvider.COURAGE_CAP, null);
                    if (courage != null) {
                        courage.increase(2.0F);
                        lastCourageTime.put(uuid, currentTime); // 更新时间戳
                    }
                }
            }
        }
    }

    @SideOnly(Side.CLIENT)
    @Override
    public void addInformation(ItemStack stack, @Nullable World worldIn, List<String> tooltip, ITooltipFlag flagIn) {
        tooltip.add(I18n.format("item.ycqin.cooldown_amulet.tooltip", NumberFormat.getPercentInstance().format(ModConfig.OrbReduction)));
        if (CommonProxy.isOverLoaded){
            tooltip.add(I18n.format("item.ycqin.cooldown_amulet.tooltip2"));
        }
    }
}