package yc.ycqin.nb.common.trait;

import com.google.common.collect.ImmutableList;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import slimeknights.tconstruct.library.modifiers.IModifierDisplay;
import slimeknights.tconstruct.library.modifiers.ModifierNBT;
import slimeknights.tconstruct.library.modifiers.ModifierTrait;
import yc.ycqin.nb.event.AttackHandler;
import yc.ycqin.nb.proxy.CommonProxy;

import java.util.List;


public class TraitMinDamage extends ModifierTrait {
    private static final int MAX_LEVEL = 10;
    private static final int ITEMS_PER_LEVEL = 4;

    public TraitMinDamage() {
        super("trait_min_damage", 0x00ff00, MAX_LEVEL, ITEMS_PER_LEVEL);
    }

    @Override
    public float damage(ItemStack tool, EntityLivingBase player, EntityLivingBase target,
                        float damage, float newDamage, boolean isCritical) {
        // 获取当前修饰器的数据
        ModifierNBT.IntegerNBT data = getData(tool);
        int level = data.level;  // 当前等级
        if (level > 0) {
            AttackHandler.attackWithMinimumDamage(target, level, player);
        }
        return newDamage;
    }


}
