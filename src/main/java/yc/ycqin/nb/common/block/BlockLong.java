package yc.ycqin.nb.common.block;

import net.minecraft.block.Block;
import net.minecraft.block.material.MapColor;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.item.Item;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import yc.ycqin.nb.register.ItemsRegister;
import yc.ycqin.nb.ycqin;

import java.util.Random;

public class BlockLong extends Block {
    public BlockLong() {
        super(Material.GROUND, MapColor.DIRT);
        String name = "longblock";
        this.setRegistryName(name)
                .setUnlocalizedName(ycqin.MODID+"."+name)
                .setCreativeTab(ItemsRegister.YCQIN_TABLE)
                .setHardness(0.5f)
                .setResistance(10);
    }

    @Override
    public Item getItemDropped(IBlockState state, Random rand, int fortune) {
        return Item.getItemFromBlock(this);
    }

    @Override
    public void onBlockDestroyedByPlayer(World world, BlockPos blockPos , IBlockState iBlockState) {

    }
}
