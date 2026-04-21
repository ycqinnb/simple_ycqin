package yc.ycqin.nb.common.trait;

import com.dhanantry.scapeandrunparasites.entity.ai.misc.EntityPMalleable;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.item.ItemStack;
import slimeknights.tconstruct.library.modifiers.ModifierNBT;
import slimeknights.tconstruct.library.modifiers.ModifierTrait;
import yc.ycqin.nb.util.ParasiteHelper;

public class TraitReduceAdaptation extends ModifierTrait {

    public TraitReduceAdaptation() {
        // 最大等级 4，每级需要 16 个物品
        super("trait_reduce_adaptation", 0x7f7f7f, 5, 8);
    }

    @Override
    public void onHit(ItemStack tool, EntityLivingBase player, EntityLivingBase target,
                      float damage, boolean wasCritical) {
        if (player.world.isRemote) return;

        // 仅在攻击冷却完成时触发
        //if (((EntityPlayer)player).getCooledAttackStrength(1.0F) < 0.95F) return;

        // 获取词条等级
        ModifierNBT.IntegerNBT data = getData(tool);
        int level = data.level;
        if (level <= 0) return;

        // 目标必须是寄生虫的可塑实体
        if (!(target instanceof EntityPMalleable)) return;

        float chance = level * 0.2f;
        if (player.world.rand.nextFloat() < chance) {
            ParasiteHelper.reduceAllResistances((EntityPMalleable) target, chance,1);
        }
    }
}