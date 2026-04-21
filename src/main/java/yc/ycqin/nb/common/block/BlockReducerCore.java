package yc.ycqin.nb.common.block;

import net.minecraft.block.Block;
import net.minecraft.block.ITileEntityProvider;
import net.minecraft.block.material.MapColor;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.resources.I18n;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumBlockRenderType;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import yc.ycqin.nb.common.entity.tileentity.TileEntityReducerCore;
import yc.ycqin.nb.register.ItemsRegister;
import yc.ycqin.nb.ycqin;

import javax.annotation.Nullable;
import java.util.List;

public class BlockReducerCore extends Block implements ITileEntityProvider {
    public BlockReducerCore() {
        super(Material.IRON);
        setRegistryName("reducer_core");
        setHardness(3.0f);
        setHarvestLevel("pickaxe", 2);
        setCreativeTab(ItemsRegister.YCQIN_TABLE);
        setUnlocalizedName(ycqin.MODID + ".reducer_core");
    }
    @Override
    public TileEntity createNewTileEntity(World worldIn, int meta) {
        return new TileEntityReducerCore();
    }
    @Override
    public EnumBlockRenderType getRenderType(IBlockState state) {
        return EnumBlockRenderType.MODEL;
    }

    @SideOnly(Side.CLIENT)
    @Override
    public void addInformation(ItemStack stack, World world, List<String> tooltip, ITooltipFlag flag) {
        tooltip.add(I18n.format("item.ycqin.reducer_core.tooltip1"));
        tooltip.add(I18n.format("item.ycqin.reducer_core.tooltip2"));
    }
}
