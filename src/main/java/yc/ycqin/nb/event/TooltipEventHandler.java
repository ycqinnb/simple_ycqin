package yc.ycqin.nb.event;

import net.minecraft.item.ItemStack;
import net.minecraftforge.event.entity.player.ItemTooltipEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.Optional;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import slimeknights.tconstruct.library.utils.TinkerUtil;
import slimeknights.tconstruct.library.utils.TagUtil;
import yc.ycqin.nb.common.trait.armorTrait.TraitAdaptation;
import yc.ycqin.nb.enchantment.EnchantmentAdaptation;

@Mod.EventBusSubscriber
public class TooltipEventHandler {
    // 标记为可选方法，若conarm未加载则不会调用
    @Optional.Method(modid = "conarm")
    @SubscribeEvent
    public static void onItemTooltipTrait(ItemTooltipEvent event) {
        ItemStack stack = event.getItemStack();
        if (TinkerUtil.hasModifier(TagUtil.getTagSafe(stack), "trait_adaptation_armor")) {
            TraitAdaptation.addTooltip(stack, event.getToolTip());
        }
    }

    @SubscribeEvent
    public static void onItemTooltipEnchantment(ItemTooltipEvent event) {
        EnchantmentAdaptation.addTooltip(event.getItemStack(), event.getToolTip());
    }
}