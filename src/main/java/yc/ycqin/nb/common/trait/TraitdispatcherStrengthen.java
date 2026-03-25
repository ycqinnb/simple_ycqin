package yc.ycqin.nb.common.trait;

import com.google.common.collect.ImmutableList;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.potion.Potion;
import net.minecraft.potion.PotionEffect;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.World;
import net.minecraftforge.fml.common.registry.ForgeRegistries;
import slimeknights.tconstruct.library.modifiers.ModifierNBT;
import slimeknights.tconstruct.library.modifiers.ModifierTrait;
import slimeknights.tconstruct.library.utils.ToolHelper;
import yc.ycqin.nb.proxy.CommonProxy;

import java.util.List;

public class TraitdispatcherStrengthen extends ModifierTrait {

    public TraitdispatcherStrengthen() {
        super("trait_dispatch_strengthen", 0x7f7f7f, 4, 16); // 最大4级，每级16个物品
    }



    @Override
    public void onUpdate(ItemStack tool, World world, Entity entity, int itemSlot, boolean isSelected) {
        if (entity.world.isRemote || ToolHelper.isBroken(tool) || !isSelected) return;
        ModifierNBT.IntegerNBT data = getData(tool);
        int level = data.level;
        if (level <= 0) return;
        Potion speed = ForgeRegistries.POTIONS.getValue(new ResourceLocation("minecraft:speed"));
        Potion regen = ForgeRegistries.POTIONS.getValue(new ResourceLocation("minecraft:regeneration"));
        Potion strength = ForgeRegistries.POTIONS.getValue(new ResourceLocation("minecraft:strength"));

        // 持续时间（刻），10秒 = 200 tick
        int duration = 200;

        EntityLivingBase target;
        if (entity instanceof EntityLivingBase) {
            target = (EntityLivingBase) entity;
            target.addPotionEffect(new PotionEffect(speed, duration, level - 1, false, true));
            target.addPotionEffect(new PotionEffect(regen, duration, level - 1, false, true));
            target.addPotionEffect(new PotionEffect(strength, duration, level - 1, false, true));
        }
    }
}