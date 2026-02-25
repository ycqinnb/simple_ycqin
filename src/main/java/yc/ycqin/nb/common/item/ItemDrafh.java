package yc.ycqin.nb.common.item;

import net.minecraft.entity.Entity;
import net.minecraft.entity.boss.EntityDragon;
import net.minecraft.entity.item.EntityEnderCrystal;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ActionResult;
import net.minecraft.util.EnumActionResult;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.TextComponentString;
import net.minecraft.world.World;
import net.minecraft.world.WorldProvider;
import net.minecraft.world.WorldProviderEnd;
import net.minecraft.world.WorldServer;
import net.minecraft.world.end.DragonFightManager;
import net.minecraftforge.fml.relauncher.ReflectionHelper;
import yc.ycqin.nb.mixins.MixinDragonFightManager;
import yc.ycqin.nb.register.ItemsRegister;
import yc.ycqin.nb.ycqin;

import javax.annotation.Nullable;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

import static net.minecraftforge.fml.common.ObfuscationReflectionHelper.findField;

public class ItemDrafh extends Item {

    private DragonFightManager drMa;
    private static List<EntityEnderCrystal> customCrystals = null;

    public ItemDrafh(){
        this.setRegistryName("fuhuomodilong");
        this.setUnlocalizedName(ycqin.MODID+"."+"fuhuomodilong");
        this.setCreativeTab(ItemsRegister.YCQIN_TABLE);
        this.setMaxStackSize(64);
    }

    @Override
    public ActionResult<ItemStack> onItemRightClick(World worldIn, EntityPlayer playerIn, EnumHand handIn) {
        ItemStack itemStack = playerIn.getHeldItem(handIn);

        if (worldIn.isRemote) {
            return new ActionResult<>(EnumActionResult.PASS, itemStack);
        }

        EntityPlayerMP playerMP = (EntityPlayerMP) playerIn;

        // 检查是否在末地
        if (playerMP.dimension != 1) {
            playerMP.sendMessage(new TextComponentString("§c必须在末地使用！"));
            return new ActionResult<>(EnumActionResult.FAIL, itemStack);
        }
        // 尝试通过 WorldProviderEnd 中的 dragonFightManager 复活
        boolean success = tryRespawnViaProvider(worldIn, playerMP);

        // 消耗物品（非创造模式）
        if (!playerMP.capabilities.isCreativeMode) {
            itemStack.shrink(1);
        }

        return new ActionResult<>(EnumActionResult.SUCCESS, itemStack);
    }

    /**
     * 通过末地 WorldProvider 中的 dragonFightManager 字段触发复活
     */
    private boolean tryRespawnViaProvider(World endWorld, EntityPlayerMP player) {
        try {
            WorldProviderEnd provider = (WorldProviderEnd) endWorld.provider;
            // 检查是否是末地提供者（类名可能因混淆变化，但一般就是 WorldProviderEnd）
            if (!provider.getClass().getSimpleName().equals("WorldProviderEnd")) {
                player.sendMessage(new TextComponentString("§c末地世界提供者类型异常。"));
                return false;
            }
            drMa = provider.getDragonFightManager();
            // 在 (0,91,0) 四周生成四个末影水晶
            WorldServer end = player.getServer().getWorld(1);
            List<EntityEnderCrystal> crystals = new ArrayList<>();
            BlockPos[] positions = {
                    new BlockPos(4, 91, 0),   // 东
                    new BlockPos(-4, 91, 0),  // 西
                    new BlockPos(0, 91, 4),   // 南
                    new BlockPos(0, 91, -4)   // 北
            };
            for (BlockPos pos : positions) {
                EntityEnderCrystal crystal = new EntityEnderCrystal(end);
                crystal.setLocationAndAngles(pos.getX() + 0.5, pos.getY(), pos.getZ() + 0.5, 0, 0);
                crystal.setBeamTarget(new BlockPos(0, 128, 0)); // 光束指向返回传送门中心
                end.spawnEntity(crystal);
                crystals.add(crystal);
            }
            customCrystals = crystals;
            drMa.respawnDragon();
            player.sendMessage(new TextComponentString("§a末影龙复活仪式已启动！"));
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            player.sendMessage(new TextComponentString("§c异常：" + e.getMessage()));
            return false;
        }
    }

    public static List<EntityEnderCrystal> getAndClearCustomCrystals() {
        List<EntityEnderCrystal> crystals = customCrystals;
        customCrystals = null;
        return crystals;
    }

}
