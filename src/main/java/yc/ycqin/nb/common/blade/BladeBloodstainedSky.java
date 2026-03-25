package yc.ycqin.nb.common.blade;

import mods.flammpfeil.slashblade.ItemSlashBladeNamed;
import mods.flammpfeil.slashblade.SlashBlade;
import mods.flammpfeil.slashblade.item.ItemSlashBlade;
import mods.flammpfeil.slashblade.item.crafting.RecipeCustomBlade;
import mods.flammpfeil.slashblade.named.event.LoadEvent;
import mods.flammpfeil.slashblade.specialeffect.SpecialEffects;
import net.minecraft.init.Enchantments;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.IRecipe;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.oredict.ShapedOreRecipe;
import yc.ycqin.nb.common.blade.se.SeLoad;

public class BladeBloodstainedSky {
    // 刀的唯一注册名
    public static final String BLADE_NAME = "flammpfeil.slashblade.named.bloodstained_sky";

    @SubscribeEvent
    public void onInit(LoadEvent.InitEvent event) {
        ItemStack blade = new ItemStack(SlashBlade.bladeNamed, 1, 0);
        NBTTagCompound tag = new NBTTagCompound();
        blade.setTagCompound(tag);

        // 必须设置 CurrentItemName
        ItemSlashBladeNamed.CurrentItemName.set(tag, BLADE_NAME);
        // 可选：设置 CustomMaxDamage
        ItemSlashBladeNamed.CustomMaxDamage.set(tag, 250);
        ItemSlashBladeNamed.IsDefaultBewitched.set(tag,true);
        // 基础攻击力
        ItemSlashBlade.setBaseAttackModifier(tag, 1145.0F);
        // 纹理和模型（需要准备对应资源）
        ItemSlashBlade.TextureName.set(tag, "named/bloodstained_sky/bloodstained_sky");
        ItemSlashBlade.ModelName.set(tag, "named/bloodstained_sky/bloodstained_sky");
        // SA 类型（0 表示无）
        ItemSlashBlade.SpecialAttackType.set(tag, 114);
        SpecialEffects.addEffect(blade, SeLoad.mindag);
        ItemSlashBlade.KillCount.set(tag,1145);
        // 设置显示名称（红色+乱码）

        // 添加附魔
        blade.addEnchantment(Enchantments.POWER, 10);
        blade.addEnchantment(Enchantments.SHARPNESS, 20);
        blade.addEnchantment(Enchantments.UNBREAKING, 10);
        blade.addEnchantment(Enchantments.THORNS, 1);
        blade.addEnchantment(Enchantments.FIRE_PROTECTION, 1);
        blade.addEnchantment(Enchantments.RESPIRATION, 1);


        // 注册到 BladeRegistry
        SlashBlade.registerCustomItemStack(BLADE_NAME, blade);
        // 加入 NamedBlades，使刀能在创造模式物品栏显示
        ItemSlashBladeNamed.NamedBlades.add(BLADE_NAME);

        // 可选：立即验证
        System.out.println("[BladeBloodstainedSky] Registered: " + BLADE_NAME);
    }

    @SubscribeEvent
    public void onPostInit(LoadEvent.PostInitEvent event) {
        // 可选：添加合成配方
        ItemStack result = SlashBlade.getCustomBlade(BLADE_NAME);
        if (result.isEmpty()) return;

        // 示例配方：用 灵魂锭 + 钻石块 合成
        ItemStack soul = SlashBlade.findItemStack("flammpfeil.slashblade", "proudsoul", 1);
        ItemStack specimen_infect = SlashBlade.findItemStack("ycqin", "specimen_infect", 1);
        ItemStack specimen_cell = SlashBlade.findItemStack("ycqin", "specimen_cell", 1);
        IRecipe recipe = new ShapedOreRecipe(new ResourceLocation("ycqin", BLADE_NAME), result,
                "#A#",
                "ASA",
                "#A#",
                '#', specimen_infect, // 钻石块
                'S', soul,
                'A', specimen_cell
        );
        SlashBlade.addRecipe(BLADE_NAME, recipe);
    }
}
