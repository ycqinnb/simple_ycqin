package yc.ycqin.nb.common.block;


import net.minecraft.block.Block;
import net.minecraft.block.ITileEntityProvider;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.resources.I18n;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import yc.ycqin.nb.common.entity.tileentity.TileEntityEnergyConverter;
import yc.ycqin.nb.config.ModConfig;
import yc.ycqin.nb.register.ItemsRegister;
import yc.ycqin.nb.ycqin;

import java.util.List;

public class BlockEnergyConverter extends Block implements ITileEntityProvider {
    public BlockEnergyConverter() {
        super(Material.IRON);
        setRegistryName("energy_converter");
        setUnlocalizedName(ycqin.MODID + ".energy_converter");
        setCreativeTab(ItemsRegister.YCQIN_TABLE);
        setHardness(2.0f);
        setHarvestLevel("pickaxe", 1);
    }

    @Override
    public TileEntity createNewTileEntity(World worldIn, int meta) {
        return new TileEntityEnergyConverter();
    }

    @Override
    public boolean isOpaqueCube(IBlockState state) {
        return false;
    }

    @Override
    public boolean isFullCube(IBlockState state) {
        return false;
    }

    @SideOnly(Side.CLIENT)
    @Override
    public void addInformation(ItemStack stack, World world, List<String> tooltip, ITooltipFlag flag) {
        tooltip.add(I18n.format("item.ycqin.energy_converter.tooltip1"));
        tooltip.add(I18n.format("item.ycqin.energy_converter.tooltip2", ModConfig.energyPerHealth));
        tooltip.add(I18n.format("item.ycqin.energy_converter.tooltip3", ModConfig.energyMaxStorage));
        tooltip.add(I18n.format("item.ycqin.energy_converter.tooltip4", ModConfig.energyOutputRate,ModConfig.energyOutputInterval));
    }
}
