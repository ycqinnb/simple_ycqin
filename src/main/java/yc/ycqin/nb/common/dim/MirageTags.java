package yc.ycqin.nb.common.dim;

import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;

public class MirageTags {
    // 物品的幻境标记
    public static final String MIRAGE_ITEM = "MirageOrigin";

    // 给物品打上标签
    public static NBTTagCompound markMirage(NBTTagCompound existing) {
        if (existing == null) existing = new NBTTagCompound();
        existing.setBoolean(MIRAGE_ITEM, true);
        return existing;
    }

    // 检查是否有标签
    public static boolean isMirageItem(NBTTagCompound nbt) {
        return nbt != null && nbt.getBoolean(MIRAGE_ITEM);
    }

    public static void markMirage(ItemStack stack) {
        if (stack.isEmpty()) return;
        if (!stack.hasTagCompound()) {
            stack.setTagCompound(new NBTTagCompound());
        }
        stack.getTagCompound().setBoolean(MIRAGE_ITEM, true);
    }

    public static boolean isMirage(ItemStack stack) {
        return !stack.isEmpty() && stack.hasTagCompound() && stack.getTagCompound().getBoolean(MIRAGE_ITEM);
    }
}