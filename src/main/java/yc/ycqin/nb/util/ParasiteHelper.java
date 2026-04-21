package yc.ycqin.nb.util;

import com.dhanantry.scapeandrunparasites.entity.ai.misc.EntityPMalleable;
import com.dhanantry.scapeandrunparasites.util.config.SRPConfigSystems;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityList;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.World;
import net.minecraftforge.common.util.Constants;
import yc.ycqin.nb.event.ProtectedMobHandler;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class ParasiteHelper {
    /**
     * 以指定概率减少实体所有适应类型的点数。
     * @param entity 目标寄生虫实体
     * @param chance 触发概率（0.0 ~ 1.0）
     */
    public static void reduceAllResistances(EntityPMalleable entity, float chance,int reduce) {
        if (entity == null || entity.world.isRemote) {
            return; // 仅在服务端执行
        }

        // 随机判定是否触发
        if (entity.getRNG().nextFloat() >= chance) {
            return;
        }

        // 获取适应列表（直接引用，可安全修改）
        ArrayList<String> resistanceS = entity.getResistanceS();
        ArrayList<Integer> resistanceI = entity.getResistanceI();

        // 从后向前遍历，避免删除元素时索引错位
        for (int i = resistanceS.size() - 1; i >= 0; i--) {
            int newValue = resistanceI.get(i) - reduce;
            if (newValue <= 0) {
                resistanceS.remove(i);
                resistanceI.remove(i);
            } else {
                resistanceI.set(i, newValue);
            }
        }
    }

    public static EntityLivingBase revertInfectedEntity(EntityLivingBase infectedEntity) {
        if (infectedEntity == null || infectedEntity.world.isRemote) return null;

        World world = infectedEntity.world;
        NBTTagCompound entityData = infectedEntity.getEntityData();

        // 1. 尝试读取存储的原始实体完整 NBT
        if (entityData.hasKey("original_entity_nbt", Constants.NBT.TAG_COMPOUND)) {
            NBTTagCompound originalNbt = entityData.getCompoundTag("original_entity_nbt");
            Entity originalEntity = EntityList.createEntityFromNBT(originalNbt, world);
            if (originalEntity instanceof EntityLivingBase) {
                return replaceEntity(infectedEntity, (EntityLivingBase) originalEntity);
            }
        }

        // 2. 根据反向映射查找原始实体类型
        Map<String, String> reverseMap = buildReverseMapping(SRPConfigSystems.COTHVictimParasite);
        ResourceLocation parasiteKey = EntityList.getKey(infectedEntity);
        if (parasiteKey == null) return null;
        String parasiteRegName = parasiteKey.toString();
        String originalRegName = reverseMap.get(parasiteRegName);
        if (originalRegName == null) return null;

        Entity originalEntity = EntityList.createEntityByIDFromName(new ResourceLocation(originalRegName), world);
        if (originalEntity instanceof EntityLivingBase) {
            EntityLivingBase mob = replaceEntity(infectedEntity, (EntityLivingBase) originalEntity);
            if (mob != null) {
                ProtectedMobHandler.onProtectedSpawn(mob);
            }
            return mob;
        }

        return null;
    }

    /**
     * 执行实体替换：复制位置/旋转/名字等，并添加保护标签
     */
    private static EntityLivingBase replaceEntity(EntityLivingBase oldEntity, EntityLivingBase newEntity) {
        World world = oldEntity.world;
        if (world.isRemote) return null;

        // 复制位置、旋转、运动
        newEntity.setLocationAndAngles(oldEntity.posX, oldEntity.posY, oldEntity.posZ,
                oldEntity.rotationYaw, oldEntity.rotationPitch);
        newEntity.motionX = oldEntity.motionX;
        newEntity.motionY = oldEntity.motionY;
        newEntity.motionZ = oldEntity.motionZ;

        // 复制自定义名称
        if (oldEntity.hasCustomName()) {
            newEntity.setCustomNameTag(oldEntity.getCustomNameTag());
            newEntity.setAlwaysRenderNameTag(oldEntity.getAlwaysRenderNameTag());
        }

        // 添加保护标签
        newEntity.getEntityData().setBoolean("yc_protectcoth", true);

        // 生成新实体
        world.spawnEntity(newEntity);
        // 移除旧实体
        world.removeEntity(oldEntity);

        return newEntity;
    }

    /**
     * 构建反向映射：寄生虫注册名 -> 原始生物注册名
     */
    private static Map<String, String> buildReverseMapping(String[] forwardMapping) {
        Map<String, String> map = new HashMap<>();
        if (forwardMapping == null) return map;
        for (String entry : forwardMapping) {
            String[] parts = entry.split(";");
            if (parts.length == 2) {
                String original = parts[0].trim();
                String parasite = parts[1].trim();
                if (!original.isEmpty() && !parasite.isEmpty()) {
                    map.put(parasite, original);
                }
            }
        }
        return map;
    }
}
