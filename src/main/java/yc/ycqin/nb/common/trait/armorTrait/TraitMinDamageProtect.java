package yc.ycqin.nb.common.trait.armorTrait;

import c4.conarm.lib.modifiers.ArmorModifierTrait;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.inventory.EntityEquipmentSlot;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import slimeknights.tconstruct.library.utils.TinkerUtil;

public class TraitMinDamageProtect extends ArmorModifierTrait {
    public TraitMinDamageProtect() {
        super("trait_min_damage_protect", 0x7f7f7f, 3, 64);
    }

    /**
     * 获取目标实体所有盔甲上此词条的总等级
     * @param entity 目标实体（通常是玩家）
     * @return 总等级（0-12）
     */
    public static int getTotalProtectionLevel(EntityLivingBase entity) {
        int total = 0;
        // 遍历四个盔甲槽位
        EntityEquipmentSlot[] slots = {
                EntityEquipmentSlot.HEAD,
                EntityEquipmentSlot.CHEST,
                EntityEquipmentSlot.LEGS,
                EntityEquipmentSlot.FEET
        };
        for (EntityEquipmentSlot slot : slots) {
            ItemStack armor = entity.getItemStackFromSlot(slot);
            if (!armor.isEmpty()) {
                NBTTagCompound modTag = TinkerUtil.getModifierTag(armor, "trait_min_damage_protect_armor");
                int level = modTag.getInteger("level");
                total += level;
            }
        }
        return total;
    }
}

