package yc.ycqin.nb.enchantment;

import net.minecraft.enchantment.Enchantment;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.enchantment.EnumEnchantmentType;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.EntityEquipmentSlot;
import net.minecraft.item.ItemArmor;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.TextFormatting;
import net.minecraftforge.fml.common.registry.ForgeRegistries;
import yc.ycqin.nb.config.ModConfig;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class EnchantmentAdaptation extends Enchantment {
    public EnchantmentAdaptation() {
        super(Rarity.VERY_RARE, EnumEnchantmentType.ARMOR, new EntityEquipmentSlot[]{
                EntityEquipmentSlot.HEAD, EntityEquipmentSlot.CHEST, EntityEquipmentSlot.LEGS, EntityEquipmentSlot.FEET});
        this.setName("adaptation");
        this.setRegistryName(new ResourceLocation("ycqin", "adaptation"));
    }

    @Override
    public boolean isTreasureEnchantment() {
        return true;
    }

    @Override
    public int getMinEnchantability(int level) {
        return 10 + (level - 1) * 20;
    }

    @Override
    public int getMaxEnchantability(int level) {
        return getMinEnchantability(level) + 30;
    }

    @Override
    public int getMaxLevel() {
        return 3;
    }

    // 禁止附魔到指定的寄生虫盔甲
    @Override
    public boolean canApply(ItemStack stack) {
        if (!(stack.getItem() instanceof ItemArmor)) return false;
        ResourceLocation regName = stack.getItem().getRegistryName();
        if (regName == null) return true;
        String fullName = regName.toString();
        return !fullName.equals("srparasites:armor_boots_sentient") &&
                !fullName.equals("srparasites:armor_pants_sentient") &&
                !fullName.equals("srparasites:armor_chest_sentient") &&
                !fullName.equals("srparasites:armor_helm_sentient") &&
                !fullName.equals("srparasites:armor_boots") &&
                !fullName.equals("srparasites:armor_pants") &&
                !fullName.equals("srparasites:armor_chest") &&
                !fullName.equals("srparasites:armor_helm");
    }

    // 获取某件盔甲上该附魔的等级（0-3）
    public static int getLevel(ItemStack stack) {
        return EnchantmentHelper.getEnchantmentLevel(ForgeRegistries.ENCHANTMENTS.getValue(new ResourceLocation("ycqin", "adaptation")), stack);
    }

    // 获取所有盔甲上该附魔的总减伤值（加法叠加，上限1.0）
    public static float getTotalReduction(EntityPlayer player, String damageType) {
        float total = 0f;
        for (ItemStack armor : player.getArmorInventoryList()) {
            int level = getLevel(armor);
            if (level > 0) {
                float current = getCurrentReduction(armor, damageType);
                total += current;
            }
        }
        return Math.min(total, 1.0f);
    }

    // 为某件盔甲增加适应点数（每次受伤调用）
    public static void addAdaptationPoint(ItemStack armor, String damageType) {
        int level = getLevel(armor);
        if (level == 0) return;
        AdaptationData data = loadAdaptationData(armor);
        float maxReduction = getMaxReductionPerPiece(level);
        float current = data.getReduction(damageType);
        float newReduction = Math.min(current + ModConfig.AdaptationIncrease, maxReduction); // 每次0.7%
        data.putReduction(damageType, newReduction);
        data.rebalance();
        saveAdaptationData(armor, data);
    }

    private static float getMaxReductionPerPiece(int level) {
        if (level > 3) return 1;
        switch (level) {
            case 1: return ModConfig.ReductionLevel1;
            case 2: return ModConfig.ReductionLevel2;
            case 3: return ModConfig.ReductionLevel3;
            default: return 0f;
        }
    }

    // 获取当前减伤值（从NBT）
    private static float getCurrentReduction(ItemStack armor, String damageType) {
        AdaptationData data = loadAdaptationData(armor);
        return data.getReduction(damageType);
    }

    // ==================== 数据存储（与匠魂词条完全一致） ====================
    private static final int ACTIVE_MAX = 7;
    private static final int HIDDEN_MAX = 10;

    private static AdaptationData loadAdaptationData(ItemStack armor) {
        AdaptationData data = new AdaptationData();
        NBTTagCompound tag = armor.getTagCompound();
        if (tag != null && tag.hasKey("AdaptationData")) {
            NBTTagCompound nbt = tag.getCompoundTag("AdaptationData");
            NBTTagList activeList = nbt.getTagList("Active", 10);
            for (int i = 0; i < activeList.tagCount(); i++) {
                NBTTagCompound entry = activeList.getCompoundTagAt(i);
                String type = entry.getString("Type");
                float value = entry.getFloat("Value");
                data.activeMap.put(type, value);
                data.activeOrder.add(type);
            }
            NBTTagList hiddenList = nbt.getTagList("Hidden", 10);
            for (int i = 0; i < hiddenList.tagCount(); i++) {
                NBTTagCompound entry = hiddenList.getCompoundTagAt(i);
                String type = entry.getString("Type");
                float value = entry.getFloat("Value");
                data.hiddenMap.put(type, value);
                data.hiddenOrder.add(type);
            }
        }
        return data;
    }

    private static void saveAdaptationData(ItemStack armor, AdaptationData data) {
        NBTTagCompound tag = armor.getTagCompound();
        if (tag == null) tag = new NBTTagCompound();
        NBTTagCompound nbt = new NBTTagCompound();

        NBTTagList activeList = new NBTTagList();
        for (String type : data.activeOrder) {
            NBTTagCompound entry = new NBTTagCompound();
            entry.setString("Type", type);
            entry.setFloat("Value", data.activeMap.get(type));
            activeList.appendTag(entry);
        }
        nbt.setTag("Active", activeList);

        NBTTagList hiddenList = new NBTTagList();
        for (String type : data.hiddenOrder) {
            NBTTagCompound entry = new NBTTagCompound();
            entry.setString("Type", type);
            entry.setFloat("Value", data.hiddenMap.get(type));
            hiddenList.appendTag(entry);
        }
        nbt.setTag("Hidden", hiddenList);

        tag.setTag("AdaptationData", nbt);
        armor.setTagCompound(tag);
    }

    // 工具提示显示
    public static void addTooltip(ItemStack stack, List<String> tooltip) {
        int level = getLevel(stack);
        if (level == 0) return;
        AdaptationData data = loadAdaptationData(stack);
        if (!data.activeMap.isEmpty()) {
            tooltip.add(TextFormatting.DARK_PURPLE + "Current Adaptation:");
            for (String type : data.activeOrder) {
                float reduction = data.activeMap.get(type) * 100;
                tooltip.add(TextFormatting.YELLOW + "-> " + type + ": " + String.format("%.1f", reduction) + "%");
            }
        } else {
            tooltip.add(TextFormatting.GRAY + "No adaptation data yet");
        }
    }

    // 内部数据结构
    private static class AdaptationData {
        LinkedHashMap<String, Float> activeMap = new LinkedHashMap<>();
        LinkedHashMap<String, Float> hiddenMap = new LinkedHashMap<>();
        List<String> activeOrder = new ArrayList<>();
        List<String> hiddenOrder = new ArrayList<>();

        float getReduction(String type) {
            if (activeMap.containsKey(type)) return activeMap.get(type);
            if (hiddenMap.containsKey(type)) return hiddenMap.get(type);
            return 0f;
        }

        void putReduction(String type, float value) {
            if (activeMap.containsKey(type)) {
                activeMap.put(type, value);
            } else if (hiddenMap.containsKey(type)) {
                hiddenMap.put(type, value);
            } else {
                if (activeMap.size() < ACTIVE_MAX) {
                    activeMap.put(type, value);
                    activeOrder.add(type);
                } else {
                    if (hiddenMap.size() < HIDDEN_MAX) {
                        hiddenMap.put(type, value);
                        hiddenOrder.add(type);
                    } else {
                        String oldest = hiddenOrder.remove(0);
                        hiddenMap.remove(oldest);
                        hiddenMap.put(type, value);
                        hiddenOrder.add(type);
                    }
                }
            }
        }

        boolean isActive(String type) {
            return activeMap.containsKey(type);
        }

        void rebalance() {
            while (true) {
                if (activeMap.isEmpty() || hiddenMap.isEmpty()) break;
                String minActiveType = null;
                float minActiveValue = Float.MAX_VALUE;
                for (Map.Entry<String, Float> entry : activeMap.entrySet()) {
                    if (entry.getValue() < minActiveValue) {
                        minActiveValue = entry.getValue();
                        minActiveType = entry.getKey();
                    }
                }
                String maxHiddenType = null;
                float maxHiddenValue = -1;
                for (Map.Entry<String, Float> entry : hiddenMap.entrySet()) {
                    if (entry.getValue() > maxHiddenValue) {
                        maxHiddenValue = entry.getValue();
                        maxHiddenType = entry.getKey();
                    }
                }
                if (maxHiddenValue > minActiveValue) {
                    activeMap.remove(minActiveType);
                    activeOrder.remove(minActiveType);
                    hiddenMap.remove(maxHiddenType);
                    hiddenOrder.remove(maxHiddenType);
                    hiddenMap.put(minActiveType, minActiveValue);
                    hiddenOrder.add(minActiveType);
                    activeMap.put(maxHiddenType, maxHiddenValue);
                    activeOrder.add(maxHiddenType);
                    while (hiddenOrder.size() > HIDDEN_MAX) {
                        String oldest = hiddenOrder.remove(0);
                        hiddenMap.remove(oldest);
                    }
                } else {
                    break;
                }
            }
            while (activeOrder.size() > ACTIVE_MAX) {
                String oldest = activeOrder.remove(0);
                activeMap.remove(oldest);
            }
            while (hiddenOrder.size() > HIDDEN_MAX) {
                String oldest = hiddenOrder.remove(0);
                hiddenMap.remove(oldest);
            }
        }
    }
}