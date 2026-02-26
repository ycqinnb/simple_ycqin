package yc.ycqin.nb.register;

import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.IRecipe;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import yc.ycqin.nb.recipe.EnchantUpgradeRecipe;

@Mod.EventBusSubscriber
public class RecipeRegister {

    public RecipeRegister(){
        MinecraftForge.EVENT_BUS.register(this);
    }

    @SubscribeEvent
    public static void registerRecipes(RegistryEvent.Register<IRecipe> event) {
        // 获取你的核心物品的ItemStack
        ItemStack upgrade = new ItemStack(ItemsRegister.UPGRADE); // 替换为你的物品实例

        // 创建自定义配方实例
        EnchantUpgradeRecipe recipe = new EnchantUpgradeRecipe(upgrade);
        recipe.setRegistryName(new ResourceLocation("ycqin", "enchant_upgrade")); // 唯一注册名

        // 注册
        event.getRegistry().register(recipe);
    }
}
