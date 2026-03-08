package yc.ycqin.nb.proxy;

import com.mrcrayfish.guns.common.WorkbenchRegistry;
import mods.flammpfeil.slashblade.ItemSlashBladeNamed;
import mods.flammpfeil.slashblade.SlashBlade;
import mods.flammpfeil.slashblade.item.ItemSlashBlade;
import mods.flammpfeil.slashblade.specialeffect.SpecialEffects;
import net.minecraft.enchantment.Enchantment;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.init.Blocks;
import net.minecraft.init.Enchantments;
import net.minecraft.init.Items;
import net.minecraft.init.PotionTypes;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.potion.PotionHelper;
import net.minecraft.potion.PotionType;
import net.minecraft.potion.PotionUtils;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.common.Loader;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import net.minecraftforge.fml.common.registry.ForgeRegistries;
import yc.ycqin.nb.common.blade.BladeBloodstainedSky;
import yc.ycqin.nb.common.blade.sa.SaLoader;
import yc.ycqin.nb.common.blade.se.MinDamageSe;
import yc.ycqin.nb.common.blade.se.SeLoad;
import yc.ycqin.nb.config.ModConfig;
import yc.ycqin.nb.event.DimensionAttributeHandler;
import yc.ycqin.nb.event.DimensionDropHandler;
import yc.ycqin.nb.gui.EvolutionHUD;
import yc.ycqin.nb.register.*;
import yc.ycqin.nb.srpcore.EvolutionDataManager;
import yc.ycqin.nb.srpcore.ParasiteEvolutionSync;

import java.util.HashMap;
import java.util.Map;

public class CommonProxy {
    public static boolean isSlashBladeLoaded = false;
    public static boolean isCgmModLoaded = false;
    public void preInit(FMLPreInitializationEvent event) {
        isSlashBladeLoaded = Loader.isModLoaded("flammpfeil.slashblade");
        isCgmModLoaded = Loader.isModLoaded("cgm");
        new ItemsRegister();
        new SoundRegister();
        new DimRegister();
        EvolutionDataManager.registerPackets();
        new RecipeRegister();
        new EntityRegister();

        // 2. 注册HUD渲染事件

        ModConfig.init(event.getSuggestedConfigurationFile());
        // 注册配置变更事件
        MinecraftForge.EVENT_BUS.register(ModConfig.class);
        if (isSlashBladeLoaded) regBlade();

    }

    public void init(FMLInitializationEvent event) {
        MinecraftForge.EVENT_BUS.register(new ParasiteEvolutionSync());
        MinecraftForge.EVENT_BUS.register(new DimensionDropHandler());
        MinecraftForge.EVENT_BUS.register(new DimensionAttributeHandler());
        registerBrewingRecipe();
        if (isCgmModLoaded) regWorldWar();
    }

    public ItemStack getStackFromName(String modId, String path, int count) {
        Item item = ForgeRegistries.ITEMS.getValue(new ResourceLocation(modId, path));
        if (item == null) {
            return ItemStack.EMPTY;
        }
        return new ItemStack(item, count);
    }

    private ItemStack getStackFromName(String modId, String path, int count, int meta) {
        Item item = ForgeRegistries.ITEMS.getValue(new ResourceLocation(modId, path));
        if (item == null) return ItemStack.EMPTY;
        return new ItemStack(item, count, meta);
    }

    private void regWorldWar(){
        ItemStack output1 = new ItemStack(BlocksRegister.BLOCKFINALSPECIMEN);
        ItemStack output2 = new ItemStack(BlocksRegister.BLOCKSPFARM);
        ItemStack output3 = new ItemStack(BlocksRegister.BLOCKSPCELL);
        ItemStack output4 = new ItemStack(BlocksRegister.BLOCKSPINFECT);

        // 定义材料（可变参数）
        ItemStack[] materials1 = new ItemStack[] {
                new ItemStack(BlocksRegister.BLOCKSPCELL, 1),
                new ItemStack(BlocksRegister.BLOCKSPINFECT, 1),
                new ItemStack(BlocksRegister.BLOCKSPFARM, 1)
        };
        ItemStack[] materials2 = new ItemStack[] {
                new ItemStack(BlocksRegister.BLOCKCROPS, 640),
                new ItemStack(Blocks.PUMPKIN, 192)
        };
        ItemStack[] materials3 = new ItemStack[] {
                getStackFromName("srparasites", "lurecomponent4", 32),
                getStackFromName("srparasites", "lurecomponent5", 32),
                getStackFromName("srparasites", "lurecomponent6", 32),
                getStackFromName("srparasites", "ada_devourer_drop", 16)
        };
        ItemStack[] materials4 = new ItemStack[] {
                getStackFromName("srparasites", "parasitesapling", 16),
                getStackFromName("srparasites", "infestedrubble", 320),
                getStackFromName("srparasites", "infestedstain", 320),
                getStackFromName("srparasites", "parasitethin", 32),
                getStackFromName("srparasites", "infestedbush", 128,3),
                getStackFromName("srparasites", "parasitemouth", 32),
                getStackFromName("srparasites", "parasiterubble", 64),
                getStackFromName("srparasites", "biomeheart", 1)
        };

        // 注册配方
        WorkbenchRegistry.registerRecipe(output1, materials1);
        WorkbenchRegistry.registerRecipe(output2, materials2);
        WorkbenchRegistry.registerRecipe(output3, materials3);
        WorkbenchRegistry.registerRecipe(output4, materials4);
    }

    private static void registerBrewingRecipe() {
        // 1. 获取反应物物品（确认物品名正确）
        Item reagentItem = Item.getByNameOrId("srparasites:dispatchern");
        if (reagentItem == null) {
            System.err.println("Failed to find reagent item: srparasites:dispatchern");
            return;
        }

        // 2. 输入药水类型（粗制的药水）
        PotionType inputPotion = PotionTypes.AWKWARD;
        if (inputPotion == null) {
            System.err.println("Failed to find input potion type: AWKWARD");
            return;
        }

        // 3. 输出药水类型：根据你查到的 NBT，注册名应为 "srparasites:res"
        PotionType outputPotion = ForgeRegistries.POTION_TYPES.getValue(new ResourceLocation("srparasites:res"));
        if (outputPotion == null) {
            System.err.println("Failed to find output potion type: srparasites:res");
            return;
        }

        // 4. 注册配方（使用正确的输出药水类型）
        PotionHelper.addMix(inputPotion, reagentItem, outputPotion);
        System.out.println("Successfully registered brewing recipe for srparasites:res");
    }

    private void regBlade(){
        new SeLoad();
        SlashBlade.InitEventBus.register(new BladeBloodstainedSky());
        SlashBlade.InitEventBus.register(new SaLoader());
    }
}
