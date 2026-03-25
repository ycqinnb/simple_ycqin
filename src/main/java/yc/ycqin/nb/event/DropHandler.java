package yc.ycqin.nb.event;

import com.dhanantry.scapeandrunparasites.entity.ai.misc.EntityPRooter;
import com.dhanantry.scapeandrunparasites.entity.monster.deterrent.nexus.EntityLeem;
import com.dhanantry.scapeandrunparasites.entity.monster.deterrent.nexus.EntityLeemSII;
import com.dhanantry.scapeandrunparasites.entity.monster.deterrent.nexus.EntityLeemSIII;
import com.dhanantry.scapeandrunparasites.entity.monster.deterrent.nexus.EntityLeemSIV;
import com.dhanantry.scapeandrunparasites.world.SRPSaveData;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.world.World;
import net.minecraftforge.event.entity.living.LivingDeathEvent;
import net.minecraftforge.event.entity.living.LivingDropsEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import yc.ycqin.nb.config.ModConfig;
import yc.ycqin.nb.register.ItemsRegister;

import java.util.Random;

public class DropHandler {

    private static final Random RANDOM = new Random();

    @SubscribeEvent
    public void onLivingDeath(LivingDeathEvent event) {
        // 只在服务端执行


        if (event.getEntity().world.isRemote) return;

        EntityLivingBase entity = event.getEntityLiving();
        World world = entity.world;

        if (world.provider.getDimension() == 78) {
            if (RANDOM.nextFloat() > ModConfig.dropChance) return;

            // 获取要掉落的物品（假设物品已注册）
            Item dropItem = ItemsRegister.UPGRADE; // 替换为你的物品实例
            if (dropItem == null) return;
            SRPSaveData.get(world,63).setEvolutionPhase(world.provider.getDimension(), (byte)10, true, world);
            if (!ModConfig.dropEnabled) return;
            // 生成物品实体
            ItemStack dropStack = new ItemStack(dropItem, 1);
            EntityItem entityItem = new EntityItem(world, entity.posX, entity.posY, entity.posZ, dropStack);
            world.spawnEntity(entityItem);
        }
    }

    @SubscribeEvent
    public void onLivingDrops(LivingDropsEvent event) {
        // 只在服务端执行，避免双倍掉落
        if (event.getEntity().world.isRemote) return;

        // 判断实体类型
        if (event.getEntity() instanceof EntityLeem) {
            // 10% 概率掉落 1~3 个
            if (event.getEntity().world.rand.nextFloat() < 0.10f) {
                int count = 1 + event.getEntity().world.rand.nextInt(3); // 1-3
                addDrop(event, count);
            }
        }
        else if (event.getEntity() instanceof EntityLeemSII) {
            // 30% 概率掉落 1~7 个
            if (event.getEntity().world.rand.nextFloat() < 0.30f) {
                int count = 1 + event.getEntity().world.rand.nextInt(7); // 1-7
                addDrop(event, count);
            }
        }
        else if (event.getEntity() instanceof EntityLeemSIII) {
            // 50% 概率掉落 1~10 个
            if (event.getEntity().world.rand.nextFloat() < 0.50f) {
                int count = 1 + event.getEntity().world.rand.nextInt(10); // 1-10
                addDrop(event, count);
            }
        }
        else if (event.getEntity() instanceof EntityLeemSIV) {
            // 90% 概率掉落 1~30 个
            if (event.getEntity().world.rand.nextFloat() < 0.90f) {
                int count = 1 + event.getEntity().world.rand.nextInt(30); // 1-30
                addDrop(event, count);
            }
        }
    }

    private void addDrop(LivingDropsEvent event, int amount) {
        ItemStack drop = new ItemStack(ItemsRegister.ROOTERDROP, amount);
        EntityItem entityItem = new EntityItem(
                event.getEntity().world,
                event.getEntity().posX,
                event.getEntity().posY,
                event.getEntity().posZ,
                drop
        );
        event.getDrops().add(entityItem);
    }
}
