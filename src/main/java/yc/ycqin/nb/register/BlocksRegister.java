package yc.ycqin.nb.register;

import net.minecraft.block.Block;
import net.minecraft.client.renderer.block.model.ModelResourceLocation;
import net.minecraft.item.Item;
import net.minecraft.item.ItemBlock;
import net.minecraftforge.client.event.ModelRegistryEvent;
import net.minecraftforge.client.model.ModelLoader;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import yc.ycqin.nb.common.block.*;

@Mod.EventBusSubscriber
public class BlocksRegister {
    public static final Block BLOCKFINALSPECIMEN = new BlockFinalSpecimen();
    public static final Block BLOCKSPCELL = new Blockspecimen_cell();
    public static final Block BLOCKSPFARM = new Blockspecimen_farm();
    public static final Block BLOCKSPINFECT = new Blockspecimen_infect();
    public static final Block BLOCKCROPS = new BlockCrops();

    private static Block[] blocks = {
            BLOCKFINALSPECIMEN,
            BLOCKSPCELL,
            BLOCKSPFARM,
            BLOCKSPINFECT,
            BLOCKCROPS
    };

    // 注册方块（服务端+客户端）
    @SubscribeEvent
    public static void registerBlocks(RegistryEvent.Register<Block> event) {
        for (Block block : blocks) {
            event.getRegistry().register(block);
        }
    }

    // 注册物品方块（服务端+客户端）
    @SubscribeEvent
    public static void registerItemBlocks(RegistryEvent.Register<Item> event) {
        for (Block block : blocks) {
            Item itemBlock = new ItemBlock(block).setRegistryName(block.getRegistryName());
            event.getRegistry().register(itemBlock);
        }
    }

    // 注册模型（仅客户端）
    @SideOnly(Side.CLIENT)
    @SubscribeEvent
    public static void registerModels(ModelRegistryEvent event) {
        for (Block block : blocks) {
            ModelLoader.setCustomModelResourceLocation(
                    Item.getItemFromBlock(block),
                    0,
                    new ModelResourceLocation(block.getRegistryName(), "inventory")
            );
        }
    }
}