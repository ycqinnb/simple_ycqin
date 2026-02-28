package yc.ycqin.nb.proxy;

import com.mrcrayfish.guns.common.WorkbenchRegistry;
import net.minecraft.init.Blocks;
import net.minecraft.init.Items;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import net.minecraftforge.fml.common.registry.ForgeRegistries;
import yc.ycqin.nb.config.ModConfig;
import yc.ycqin.nb.event.DimensionAttributeHandler;
import yc.ycqin.nb.event.DimensionDropHandler;
import yc.ycqin.nb.gui.EvolutionHUD;
import yc.ycqin.nb.register.*;
import yc.ycqin.nb.srpcore.EvolutionDataManager;
import yc.ycqin.nb.srpcore.ParasiteEvolutionSync;

public class CommonProxy {
    public void preInit(FMLPreInitializationEvent event) {
        new ItemsRegister();
        new SoundRegister();
        new DimRegister();
        EvolutionDataManager.registerPackets();
        new RecipeRegister();

        // 2. 注册HUD渲染事件

        ModConfig.init(event.getSuggestedConfigurationFile());
        // 注册配置变更事件
        MinecraftForge.EVENT_BUS.register(ModConfig.class);



    }

    public void init(FMLInitializationEvent event) {
        MinecraftForge.EVENT_BUS.register(new ParasiteEvolutionSync());
        MinecraftForge.EVENT_BUS.register(new DimensionDropHandler());
        MinecraftForge.EVENT_BUS.register(new DimensionAttributeHandler());
        // 定义输出物品
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
                getStackFromName("srparasites", "infestedbush", 128),
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

    public ItemStack getStackFromName(String modId, String path, int count) {
        Item item = ForgeRegistries.ITEMS.getValue(new ResourceLocation(modId, path));
        if (item == null) {
            return ItemStack.EMPTY;
        }
        return new ItemStack(item, count);
    }
}
