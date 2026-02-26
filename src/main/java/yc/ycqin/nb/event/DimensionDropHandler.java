package yc.ycqin.nb.event;

import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.world.World;
import net.minecraftforge.event.entity.living.LivingDeathEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import yc.ycqin.nb.config.ModConfig;
import yc.ycqin.nb.register.ItemsRegister;

import java.util.Random;

public class DimensionDropHandler {

    private static final Random RANDOM = new Random();
    private static final float DROP_CHANCE = 0.05f; // 10%概率，可调整或配置

    @SubscribeEvent
    public void onLivingDeath(LivingDeathEvent event) {
        // 只在服务端执行
        if (!ModConfig.dropEnabled) return;

        if (event.getEntity().world.isRemote) return;

        EntityLivingBase entity = event.getEntityLiving();
        World world = entity.world;

        // 判断维度：通过维度ID或WorldProvider类型
        // 方法1：检查维度ID（假设你的维度ID是静态常量，如 DimRegister.DIM_ID）
        if (world.provider.getDimension() != 78) return;

        // 方法2：检查WorldProvider类型（如果你有自定义类）
        // if (!(world.provider instanceof YcDimProvide)) return;

        // 随机概率
        if (RANDOM.nextFloat() > ModConfig.dropChance) return;

        // 获取要掉落的物品（假设物品已注册）
        Item dropItem = ItemsRegister.UPGRADE; // 替换为你的物品实例
        if (dropItem == null) return;

        // 生成物品实体
        ItemStack dropStack = new ItemStack(dropItem, 1);
        EntityItem entityItem = new EntityItem(world, entity.posX, entity.posY, entity.posZ, dropStack);
        world.spawnEntity(entityItem);
    }
}
