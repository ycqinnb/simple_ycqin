package yc.ycqin.nb.enchantment;

import net.minecraft.enchantment.Enchantment;
import net.minecraft.enchantment.EnumEnchantmentType;
import net.minecraft.inventory.EntityEquipmentSlot;
import net.minecraft.item.ItemArmor;
import net.minecraft.item.ItemStack;

public class EnchantmentMinDamageProtect extends Enchantment {

    public EnchantmentMinDamageProtect() {
        super(Rarity.UNCOMMON, EnumEnchantmentType.ARMOR_CHEST, new EntityEquipmentSlot[]{EntityEquipmentSlot.CHEST});
        this.setName("min_damage_protect");
        this.setRegistryName("min_damage_protect");
    }

    @Override
    public int getMinEnchantability(int level) {
        return 10 + level * 5;
    }

    @Override
    public int getMaxEnchantability(int level) {
        return this.getMinEnchantability(level) + 20;
    }

    @Override
    public int getMaxLevel() {
        return 3;
    }


    @Override
    public boolean canApply(ItemStack stack) {
        if (stack.getItem() instanceof ItemArmor) {
            ItemArmor armor = (ItemArmor) stack.getItem();
            return armor.armorType == EntityEquipmentSlot.CHEST;
        }
        return false;
    }

    @Override
    public boolean canApplyAtEnchantingTable(ItemStack stack) {
        // 与 canApply 保持一致，或根据需要允许附魔台产出
        return canApply(stack);
    }
}
