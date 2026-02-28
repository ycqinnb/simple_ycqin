package yc.ycqin.nb.recipe;

import net.minecraft.inventory.InventoryCrafting;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.IRecipe;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.world.World;
import net.minecraftforge.registries.IForgeRegistryEntry;
import yc.ycqin.nb.config.ModConfig;

public class EnchantUpgradeRecipe extends IForgeRegistryEntry.Impl<IRecipe> implements IRecipe {

    private final ItemStack upgradeCore; // 核心物品的ItemStack，用于比较

    public EnchantUpgradeRecipe(ItemStack upgradeCore) {
        this.upgradeCore = upgradeCore;
    }

    @Override
    public boolean matches(InventoryCrafting inv, World worldIn) {
        boolean hasCore = false;
        boolean hasEnchanted = false;

        for (int i = 0; i < inv.getSizeInventory(); i++) {
            ItemStack stack = inv.getStackInSlot(i);
            if (!stack.isEmpty()) {
                // 检查是否是核心物品（忽略数量，只要物品类型相同）
                if (stack.isItemEqual(upgradeCore)) {
                    if (hasCore) return false; // 只能有一个核心
                    hasCore = true;
                }
                // 检查是否带附魔（有ench标签且不为空）
                else if (stack.hasTagCompound() && stack.getEnchantmentTagList() != null && stack.getEnchantmentTagList().tagCount() > 0) {
                    if (hasEnchanted) return false; // 只能有一个附魔物品
                    hasEnchanted = true;
                } else {
                    return false; // 出现其他物品，无效
                }
            }
        }
        return hasCore && hasEnchanted;
    }

    @Override
    public ItemStack getCraftingResult(InventoryCrafting inv) {
        // 找到附魔物品
        ItemStack enchantedItem = ItemStack.EMPTY;
        for (int i = 0; i < inv.getSizeInventory(); i++) {
            ItemStack stack = inv.getStackInSlot(i);
            if (!stack.isEmpty() && !stack.isItemEqual(upgradeCore)) {
                enchantedItem = stack.copy(); // 复制一份作为输出
                break;
            }
        }
        if (enchantedItem.isEmpty()) return ItemStack.EMPTY;

        // 获取附魔列表
        NBTTagList enchantments = enchantedItem.getEnchantmentTagList();
        if (enchantments == null) return ItemStack.EMPTY;

        // 创建新的附魔列表
        NBTTagList newEnchantments = new NBTTagList();
        for (int i = 0; i < enchantments.tagCount(); i++) {
            NBTTagCompound ench = enchantments.getCompoundTagAt(i).copy(); // 复制原附魔
            short lvl = ench.getShort("lvl");
            if (lvl < ModConfig.ecMixLevel) {
                lvl++; // 等级+1（直接加，无视上限）
            }
            ench.setShort("lvl", lvl);
            newEnchantments.appendTag(ench);
        }

        // 替换附魔标签
        enchantedItem.setTagInfo("ench", newEnchantments);
        return enchantedItem;
    }

    @Override
    public boolean canFit(int width, int height) {
        return width * height >= 2; // 至少需要2个格子
    }

    @Override
    public ItemStack getRecipeOutput() {
        return ItemStack.EMPTY; // 输出动态变化，返回空
    }
}
