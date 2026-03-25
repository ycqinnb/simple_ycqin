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
import yc.ycqin.nb.register.ItemsRegister;

import java.util.List;

public class TraitrooterStrengthen extends ModifierTrait {

    public TraitrooterStrengthen() {
        // 最大等级 4，每级需要 16 个物品
        super("trait_rooter_strengthen", 0x7f7f7f, 4, 16);
    }

    @Override
    public void onHit(ItemStack tool, EntityLivingBase player, EntityLivingBase target,
                      float damage, boolean wasCritical) {
        if (player.world.isRemote) return;

        // 仅在攻击冷却完成时触发
        if (((EntityPlayer)player).getCooledAttackStrength(1.0F) < 0.95F) return;

        // 获取词条等级
        ModifierNBT.IntegerNBT data = getData(tool);
        int level = data.level;
        if (level <= 0) return;

        // 获取伤害吸收药水效果（minecraft:absorption）
        Potion absorption = ForgeRegistries.POTIONS.getValue(new net.minecraft.util.ResourceLocation("minecraft:absorption"));
        if (absorption == null) return;

        // 持续时间：10 秒 = 200 tick
        int duration = 200;
        // 药水效果等级参数从 0 开始，所以等级 1 对应 amplifier = 0（2 点吸收），等级 4 对应 amplifier = 3（8 点吸收）
        int amplifier = level - 1;

        player.addPotionEffect(new PotionEffect(absorption, duration, amplifier, false, true));
    }
}
