package yc.ycqin.nb.common.block;
import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import yc.ycqin.nb.register.ItemsRegister;
import yc.ycqin.nb.ycqin;
public class BlockShooter extends Block {
    public BlockShooter() {
        super(Material.IRON);
        setRegistryName("shooter");
        setUnlocalizedName(ycqin.MODID + ".shooter");
        setCreativeTab(ItemsRegister.YCQIN_TABLE);
        setHardness(2.0f);
    }
}