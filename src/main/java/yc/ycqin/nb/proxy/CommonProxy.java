package yc.ycqin.nb.proxy;

import com.mrcrayfish.guns.common.WorkbenchRegistry;
import mods.flammpfeil.slashblade.SlashBlade;
import net.minecraft.init.PotionTypes;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.potion.PotionHelper;
import net.minecraft.potion.PotionType;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.common.Loader;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPostInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import net.minecraftforge.fml.common.registry.ForgeRegistries;
import yc.ycqin.nb.common.blade.BladeBloodstainedSky;
import yc.ycqin.nb.common.blade.sa.SaLoader;
import yc.ycqin.nb.common.blade.se.SeLoad;
import yc.ycqin.nb.config.ModConfig;
import yc.ycqin.nb.event.*;
import yc.ycqin.nb.event.dim.DimEvent;
import yc.ycqin.nb.event.dim.DimensionAttributeHandler;
import yc.ycqin.nb.event.dim.DropHandler;
import yc.ycqin.nb.register.*;

public class CommonProxy {
    public static boolean isSlashBladeLoaded = false;
    public static boolean isCgmModLoaded = false;
    public static boolean isTCLoaded = false;
    public static boolean isTCArmorLoaded = false;
    public static boolean isBaublesLoaded = false;
    public static boolean isOverLoaded = false;
    public static boolean isOverCourageLoaded = false;
    public TinkerTraitsRegister tinkerTraitsRegister;
    public void preInit(FMLPreInitializationEvent event) {
        isSlashBladeLoaded = Loader.isModLoaded("flammpfeil.slashblade");
        isCgmModLoaded = Loader.isModLoaded("cgm");
        isTCLoaded = Loader.isModLoaded("tconstruct");
        isTCArmorLoaded = Loader.isModLoaded("conarm");
        isBaublesLoaded = Loader.isModLoaded("baubles");
        isOverLoaded = Loader.isModLoaded("overlast");
        isOverCourageLoaded = isCourageClassPresent();

        new ItemsRegister();
        new SoundRegister();
        new DimRegister();
        new NetworkRegister();
        new RecipeRegister();
        new EntityRegister();

        ModConfig.init(event.getSuggestedConfigurationFile());
        // 注册配置变更事件
        MinecraftForge.EVENT_BUS.register(ModConfig.class);
        if (isSlashBladeLoaded) regBlade();

    }

    public void init(FMLInitializationEvent event) {
        //MinecraftForge.EVENT_BUS.register(new ParasiteEvolutionSync());
        MinecraftForge.EVENT_BUS.register(new DropHandler());
        MinecraftForge.EVENT_BUS.register(new DimensionAttributeHandler());
        MinecraftForge.EVENT_BUS.register(new AdaptationReductionHandler());
        MinecraftForge.EVENT_BUS.register(new ProtectedMobHandler());
        MinecraftForge.EVENT_BUS.register(new SyncEvent());
        MinecraftForge.EVENT_BUS.register(new DimEvent());
        registerBrewingRecipe();
        if (isCgmModLoaded) regWorldWar();
        if (isTCLoaded){
           tinkerTraitsRegister = new TinkerTraitsRegister();
        }
        if (CommonProxy.isTCArmorLoaded){
            tinkerTraitsRegister.regTCArmor();
        }
    }

    public void postInit(FMLPostInitializationEvent event) {

    }

    public static ItemStack getStackFromName(String modId, String path, int count) {
        Item item = ForgeRegistries.ITEMS.getValue(new ResourceLocation(modId, path));
        if (item == null) {
            return ItemStack.EMPTY;
        }
        return new ItemStack(item, count);
    }

    public static ItemStack getStackFromName(String modId, String path, int count, int meta) {
        Item item = ForgeRegistries.ITEMS.getValue(new ResourceLocation(modId, path));
        if (item == null) return ItemStack.EMPTY;
        return new ItemStack(item, count, meta);
    }

    private void regWorldWar(){
        ItemStack output5 = new ItemStack(BlocksRegister.BLOCKPARAASITECore);
        ItemStack[] materials5 = new ItemStack[] {
                getStackFromName("srparasites","biomepurifier",384),
                getStackFromName("srparasites","module_origin",1),
                getStackFromName("srparasites","module_dislodgement",1),
                getStackFromName("srparasites","module_vectors",1),
                getStackFromName("srparasites","module_phase",1)
        };
        // 注册配方
        WorkbenchRegistry.registerRecipe(output5, materials5);
    }

    private static void registerBrewingRecipe() {
        if (!ModConfig.isNoADEnabled) return;
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

    public static boolean isCourageClassPresent() {
        try {
            Class.forName("com.overlast.cap.sanity.SanityProvider",
                    false,
                    Thread.currentThread().getContextClassLoader());
            return true;
        } catch (ClassNotFoundException | NoClassDefFoundError e) {
            return false;
        }
    }
}
