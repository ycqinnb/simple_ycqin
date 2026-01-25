package yc.ycqin.nb.common.item;

import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ActionResult;
import net.minecraft.util.EnumActionResult;
import net.minecraft.util.EnumHand;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.Style;
import net.minecraft.util.text.TextComponentTranslation;
import net.minecraft.world.World;
import yc.ycqin.nb.register.BlocksRegister;
import yc.ycqin.nb.register.ItemsRegister;
import yc.ycqin.nb.register.SoundRegister;
import yc.ycqin.nb.ycqin;

import java.util.Collections;
import java.util.Iterator;
import java.util.List;

import static net.minecraft.util.math.RayTraceResult.Type.BLOCK;


public class ItemYcqin extends Item {
    public ItemYcqin(){
        this.setRegistryName("ycqin");
        this.setUnlocalizedName(ycqin.MODID+"."+"ycqin");
        this.setCreativeTab(ItemsRegister.YCQIN_TABLE);
    }

    @Override
    public ActionResult<ItemStack> onItemRightClick(World world , EntityPlayer EntityPlayer, EnumHand EnumHand) {
        RayTraceResult result = rayTrace(EntityPlayer,EntityPlayer.REACH_DISTANCE.getDefaultValue());
        if (result.typeOfHit == BLOCK){
            BlockPos blockPos = result.getBlockPos();
            if (!EntityPlayer.getEntityWorld().isRemote){
                world.setBlockState(blockPos,BlocksRegister.LONG_BLOCK.getDefaultState());
                world.notifyNeighborsOfStateChange(blockPos,BlocksRegister.LONG_BLOCK,true);
                world.playSound(
                        null,
                        blockPos,
                        SoundRegister.YCQIN_SOUND,
                        SoundCategory.RECORDS,
                        1.0F, 1.0F
                );
                EntityPlayer.sendMessage(new TextComponentTranslation("666"));
            }
        }

        return new ActionResult(EnumActionResult.PASS, EntityPlayer.getHeldItem(EnumHand));
    }


    public static RayTraceResult rayTrace(EntityPlayer player, double reachDistance) {
        // 获取玩家视线方向向量
        Vec3d lookVec = player.getLookVec();

        // 计算起点（玩家眼睛位置）
        Vec3d startVec = new Vec3d(
                player.posX,
                player.posY + player.getEyeHeight(),
                player.posZ
        );

        // 计算终点
        Vec3d endVec = startVec.add(new Vec3d(
                lookVec.x * reachDistance,
                lookVec.y * reachDistance,
                lookVec.z * reachDistance
                )
        );

        // 执行射线追踪
        return player.world.rayTraceBlocks(startVec, endVec, false, true, false);
    }
}
