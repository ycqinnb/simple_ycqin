package yc.ycqin.nb.register;

import net.minecraft.enchantment.Enchantment;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import yc.ycqin.nb.enchantment.EnchantmentMinDamage;
import yc.ycqin.nb.enchantment.EnchantmentMinDamageProtect;

@Mod.EventBusSubscriber(modid = "ycqin") // 请将 modid 替换为你的实际 modid
public class ModEnchantments {

    public static final EnchantmentMinDamageProtect MIN_DAMAGE_PROTECT = new EnchantmentMinDamageProtect();
    public static final EnchantmentMinDamage MIN_DAMAGE = new EnchantmentMinDamage();

    @SubscribeEvent
    public static void registerEnchantments(RegistryEvent.Register<Enchantment> event) {
        event.getRegistry().register(MIN_DAMAGE_PROTECT);
        event.getRegistry().register(MIN_DAMAGE);
    }
}