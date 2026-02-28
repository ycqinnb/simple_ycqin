package yc.ycqin.nb.enchantment;

import net.minecraft.enchantment.Enchantment;
import net.minecraft.enchantment.EnumEnchantmentType;
import net.minecraft.inventory.EntityEquipmentSlot;
import net.minecraft.item.ItemArmor;
import net.minecraft.item.ItemStack;

public class EnchantmentMinDamage extends Enchantment {
    public EnchantmentMinDamage() {
        super(Enchantment.Rarity.RARE, EnumEnchantmentType.ALL, new EntityEquipmentSlot[]{
                EntityEquipmentSlot.MAINHAND, // 主要考虑手持时有效
                EntityEquipmentSlot.OFFHAND
        });
        this.setName("yc_min_damage");
        this.setRegistryName("ycqin.yc_min_damage");
    }

    @Override
    public int getMinEnchantability(int level) {
        return 10 + level * 5;
    }

    @Override
    public int getMaxEnchantability(int level) {
        return getMinEnchantability(level) + 30;
    }

    @Override
    public int getMaxLevel() {
        return 3; // 最高3级，可根据需要调整
    }

    /**
     * 允许附魔在任何物品上（铁砧合并）
     */
    @Override
    public boolean canApply(ItemStack stack) {
        return true; // 任何物品都可以
    }

    /**
     * 允许附魔台随机附出（可选，设为 true 则附魔台可产出）
     */
    @Override
    public boolean canApplyAtEnchantingTable(ItemStack stack) {
        return true; // 任何物品都可以
    }

    /**
     * 不与其他附魔冲突（可根据需要修改）
     */
    @Override
    protected boolean canApplyTogether(Enchantment ench) {
        return true; // 允许共存
    }
}
