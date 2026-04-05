package yc.ycqin.nb.common.trait.armorTrait;

import c4.conarm.lib.modifiers.ArmorModifierTrait;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.nbt.NBTTagString;
import net.minecraft.util.DamageSource;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.TextFormatting;
import net.minecraftforge.event.entity.living.LivingHurtEvent;
import slimeknights.tconstruct.library.utils.TagUtil;
import slimeknights.tconstruct.library.utils.TinkerUtil;

import java.util.*;

public class TraitAdaptation extends ArmorModifierTrait {

    private static final float REDUCTION_PER_POINT = 0.007f; // 0.7%
    private static final int ACTIVE_MAX = 7;
    private static final int HIDDEN_MAX = 10;

    public TraitAdaptation() {
        super("trait_adaptation", 0x7f7f7f, 3, 16);
    }

    // 只负责增加适应点数，不减伤
    @Override
    public float onHurt(ItemStack armor, EntityPlayer player, DamageSource source,
                        float damage, float newDamage, LivingHurtEvent evt) {
        if (player.world.isRemote) return newDamage;

        String damageType = getDisplayDamageType(source);
        int level = getModifierLevel(armor);
        if (level > 0) {
            AdaptationData data = loadAdaptationData(armor);
            float maxReduction = getMaxReductionPerPiece(level);
            float current = data.getReduction(damageType);
            float newReduction = Math.min(current + REDUCTION_PER_POINT, maxReduction);
            data.putReduction(damageType, newReduction);
            data.rebalance();
            saveAdaptationData(armor, data);
        }
        // 不减伤，直接返回原值（可能已被全局事件修改）
        return newDamage;
    }

    // 静态方法：计算玩家所有盔甲的总减伤（加法叠加，上限100%）
    public static float getTotalReduction(EntityPlayer player, String damageType) {
        float total = 0f;
        for (ItemStack armor : player.getArmorInventoryList()) {
            if (!armor.isEmpty() && TinkerUtil.hasModifier(TagUtil.getTagSafe(armor), "trait_adaptation_armor")) {
                AdaptationData data = loadAdaptationDataStatic(armor);
                total += data.getReduction(damageType);
            }
        }
        return Math.min(total, 1.0f);
    }

    // ---------- 私有辅助方法 ----------
    private int getModifierLevel(ItemStack armor) {
        NBTTagCompound tag = TinkerUtil.getModifierTag(armor, getIdentifier());
        return tag.getInteger("level");
    }

    private float getMaxReductionPerPiece(int level) {
        switch (level) {
            case 1: return 0.05f;  // 5%
            case 2: return 0.15f;  // 15%
            case 3: return 0.25f;  // 25%
            default: return 0f;
        }
    }

    private String getDisplayDamageType(DamageSource source) {
        if (source.getTrueSource() instanceof net.minecraft.entity.EntityLivingBase) {
            net.minecraft.entity.EntityLivingBase entity = (net.minecraft.entity.EntityLivingBase) source.getTrueSource();
            ResourceLocation regName = net.minecraft.entity.EntityList.getKey(entity);
            if (regName != null) return regName.toString();
            else return entity.getName();
        } else {
            return source.getDamageType();
        }
    }

    // ---------- 数据存储（静态版本供外部调用） ----------
    private static AdaptationData loadAdaptationDataStatic(ItemStack armor) {
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

    private AdaptationData loadAdaptationData(ItemStack armor) {
        return loadAdaptationDataStatic(armor);
    }

    private void saveAdaptationData(ItemStack armor, AdaptationData data) {
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

    // ---------- 工具提示 ----------
    public static void addTooltip(ItemStack stack, List<String> tooltip) {
        AdaptationData data = loadAdaptationDataStatic(stack);
        if (!data.activeMap.isEmpty()) {
            tooltip.add(TextFormatting.DARK_PURPLE + "Current Adaptation:");
            for (String type : data.activeOrder) {
                float reduction = data.activeMap.get(type) * 100;
                tooltip.add(TextFormatting.YELLOW + "-> " + type + ": " + String.format("%.1f", reduction) + "%");
            }
        }
    }

    // ---------- 内部数据结构 ----------
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