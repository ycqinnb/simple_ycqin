package yc.ycqin.nb.common.block;


import net.minecraft.block.Block;
import net.minecraft.block.ITileEntityProvider;
import net.minecraft.block.material.MapColor;
import net.minecraft.client.resources.I18n;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumBlockRenderType;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import yc.ycqin.nb.common.entity.tileentity.TileEntityParasiteCore;
import yc.ycqin.nb.config.ModConfig;
import yc.ycqin.nb.register.ItemsRegister;
import yc.ycqin.nb.ycqin;
import net.minecraft.block.BlockContainer;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.world.World;

import javax.annotation.Nullable;
import java.util.List;

public class BlockParasiteCore extends Block implements ITileEntityProvider {

    public BlockParasiteCore() {
        super(Material.IRON);
        setHardness(5.0F);
        setResistance(2000.0F);
        setCreativeTab(ItemsRegister.YCQIN_TABLE);
        setRegistryName("parasite_core");
        setUnlocalizedName(ycqin.MODID + ".parasite_core");
    }

    @Nullable
    @Override
    public TileEntity createNewTileEntity(World worldIn, int meta) {
        return new TileEntityParasiteCore();
    }

    @Override
    public boolean hasTileEntity(IBlockState state) {
        return true;
    }

    @Override
    public EnumBlockRenderType getRenderType(IBlockState state) {
        return EnumBlockRenderType.MODEL;
    }

    @SideOnly(Side.CLIENT)
    @Override
    public void addInformation(ItemStack stack, World world, List<String> tooltip, ITooltipFlag flag) {
        tooltip.add(I18n.format("item.ycqin.parasite_core.tooltip1"));
        tooltip.add(I18n.format("item.ycqin.parasite_core.tooltip2"));
        tooltip.add(I18n.format("item.ycqin.parasite_core.tooltip3"));
    }
}