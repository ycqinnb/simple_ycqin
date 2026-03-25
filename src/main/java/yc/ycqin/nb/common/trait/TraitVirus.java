package yc.ycqin.nb.common.trait;

import com.google.common.collect.ImmutableList;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.potion.Potion;
import net.minecraft.potion.PotionEffect;
import net.minecraftforge.fml.common.registry.ForgeRegistries;
import slimeknights.tconstruct.library.modifiers.ModifierNBT;
import slimeknights.tconstruct.library.modifiers.ModifierTrait;
import yc.ycqin.nb.proxy.CommonProxy;

import java.util.List;

public class TraitVirus extends ModifierTrait {

    public TraitVirus() {
        // 最大等级 4，每级需要 16 个物品
        super("trait_virus", 0x7f7f7f, 4, 32);
    }

    @Override
    public void onHit(ItemStack tool, EntityLivingBase player, EntityLivingBase target,
                      float damage, boolean wasCritical) {
        if (player.world.isRemote) return;

        // 仅在攻击冷却完成时触发
        //if (((EntityPlayer)player).getCooledAttackStrength(1.0F) < 0.95F) return;

        ModifierNBT.IntegerNBT data = getData(tool);
        int level = data.level;
        if (level <= 0) return;

        Potion viral = ForgeRegistries.POTIONS.getValue(new net.minecraft.util.ResourceLocation("srparasites", "viral"));
        if (viral == null) return;

        // 持续时间：10 秒 = 200 tick（可调整）
        int duration = 200;
        int amplifier = level - 1;

        target.addPotionEffect(new PotionEffect(viral, duration, amplifier, false, true));
    }
}
