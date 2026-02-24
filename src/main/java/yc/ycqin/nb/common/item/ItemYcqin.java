package yc.ycqin.nb.common.item;

import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.server.MinecraftServer;
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
import net.minecraft.world.Teleporter;
import net.minecraft.world.World;
import net.minecraft.world.WorldServer;
import yc.ycqin.nb.register.BlocksRegister;
import yc.ycqin.nb.register.ItemsRegister;
import yc.ycqin.nb.register.SoundRegister;
import yc.ycqin.nb.ycqin;

import java.util.Collections;
import java.util.Iterator;
import java.util.List;

import static net.minecraft.util.math.RayTraceResult.Type.BLOCK;


public class ItemYcqin extends Item {

    private static final String LAST_DIM_TAG = "LastDimensionId";

    public ItemYcqin(){
        this.setRegistryName("ycqin");
        this.setUnlocalizedName(ycqin.MODID+"."+"ycqin");
        this.setCreativeTab(ItemsRegister.YCQIN_TABLE);
        this.setMaxStackSize(1);

    }

    @Override
    public ActionResult<ItemStack> onItemRightClick(World worldIn, EntityPlayer playerIn, EnumHand handIn) {
        ItemStack itemStack = playerIn.getHeldItem(handIn);

        if (!worldIn.isRemote) { // 服务端
            // 安全转换：检查并转换为 EntityPlayerMP
            if (playerIn instanceof EntityPlayerMP) {
                EntityPlayerMP playerMP = (EntityPlayerMP) playerIn;

                int currentDim = playerMP.dimension;
                int targetDim;

                if (currentDim == 78) {
                    targetDim = playerMP.getEntityData().getInteger(LAST_DIM_TAG);
                    if (targetDim == 78 || targetDim == 0) {
                        targetDim = 0;
                    }
                } else {
                    playerMP.getEntityData().setInteger(LAST_DIM_TAG, currentDim);
                    targetDim = 78;
                }

                teleportPlayerToDimension(playerMP, targetDim); // 现在传入的是 EntityPlayerMP
            }
        }

        return new ActionResult<>(EnumActionResult.SUCCESS, itemStack);
    }

    private void teleportPlayerToDimension(EntityPlayerMP player, int dimensionId) {
        // 获取Minecraft服务器实例
        MinecraftServer server = player.getServer();
        if (server == null) return;

        // 获取目标维度的世界服务器
        WorldServer targetWorld = server.getWorld(dimensionId);

        // 使用Teleporter进行传送，传送位置为玩家当前位置（坐标会自动转换）
        player.changeDimension(dimensionId, new Teleporter(targetWorld) {
            @Override
            public void placeInPortal(Entity entity, float rotationYaw) {
                // 自定义：不创建传送门，直接使用玩家进入时的位置
                // 如果不做特殊处理，可以留空或简单调用父类方法
            }
        });
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
