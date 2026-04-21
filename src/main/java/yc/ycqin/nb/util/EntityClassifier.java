package yc.ycqin.nb.util;

import com.dhanantry.scapeandrunparasites.entity.ai.misc.EntityPInfected;
import com.dhanantry.scapeandrunparasites.entity.ai.misc.EntityParasiteBase;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.World;
import net.minecraftforge.fml.common.registry.EntityEntry;
import net.minecraftforge.fml.common.registry.EntityRegistry;
import net.minecraftforge.fml.common.registry.ForgeRegistries;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import yc.ycqin.nb.config.ModConfig;

import java.util.UUID;

public class EntityClassifier {
    private static final String[] REVERTIBLE_CLASSES = {
            "com.dhanantry.scapeandrunparasites.entity.EntityInhooM",
            "com.dhanantry.scapeandrunparasites.entity.EntityInhooS"
    };
    private static final Logger log = LogManager.getLogger(EntityClassifier.class);

    public static boolean isRevertible(EntityLivingBase entity) {
        String className = entity.getClass().getName();
        for (String revertClass : REVERTIBLE_CLASSES) {
            if (className.equals(revertClass)) {
                return true;
            }
        }
        return entity instanceof EntityPInfected;
    }

    public static String getEntityRegistryName(Entity entity) {
        ResourceLocation key = EntityRegistry.getEntry(entity.getClass()).getRegistryName();
        return key == null ? null : key.toString();
    }

    public static int getSpawnCountForType(EntityLivingBase entity) {
        String name = getEntityRegistryName(entity);
        if (name == null) return 1;

        // 衍生种
        if (name.equals("srparasites:kirin") || name.equals("srparasites:draconite"))
            return ModConfig.reducerSpawnCount_Derivative;
        // 远古种
        if (name.equals("srparasites:anc_dreadnaut") || name.equals("srparasites:anc_overlord"))
            return ModConfig.reducerSpawnCount_Ancient;
        // 诡化种
        if (name.startsWith("srparasites:mar_"))
            return ModConfig.reducerSpawnCount_Deceptive;
        // 憎恶种
        if (name.equals("srparasites:abo_bodies") || name.equals("srparasites:abo_head"))
            return ModConfig.reducerSpawnCount_Abomination;
        // 卓越种
        if (name.equals("srparasites:bomber_heavy") || name.equals("srparasites:wraith") ||
                name.equals("srparasites:bogle") || name.equals("srparasites:haunter") ||
                name.equals("srparasites:carrier_colony") || name.equals("srparasites:succor") ||
                name.equals("srparasites:seeker") || name.equals("srparasites:architect"))
            return ModConfig.reducerSpawnCount_Excellent;
        // 纯粹种
        if (name.equals("srparasites:vigilante") || name.equals("srparasites:warden") ||
                name.equals("srparasites:overseer") || name.equals("srparasites:bomber_light") ||
                name.equals("srparasites:marauder") || name.equals("srparasites:grunt") ||
                name.equals("srparasites:monarch"))
            return ModConfig.reducerSpawnCount_Pure;
        // 适应种
        if (name.startsWith("srparasites:ada_"))
            return ModConfig.reducerSpawnCount_Adapted;
        // 原始种
        if (name.startsWith("srparasites:pri_"))
            return ModConfig.reducerSpawnCount_Primitive;
        // 狂化种
        if (name.startsWith("srparasites:fer_"))
            return ModConfig.reducerSpawnCount_Feral;
        // 劫持种
        if (name.startsWith("srparasites:hi_"))
            return ModConfig.reducerSpawnCount_Hijacked;
        // 同化种 & 先天种（按你的需求，同化种和先天种使用同一个配置项）
        if (name.equals("srparasites:incompleteform_medium") ||
                name.equals("srparasites:incompleteform_small") ||
                name.equals("srparasites:worker") || name.equals("srparasites:gnat") ||
                name.equals("srparasites:lice") || name.equals("srparasites:buglin") ||
                name.equals("srparasites:rupter") || name.equals("srparasites:mangler") ||
                name.equals("srparasites:carrier_light") || name.equals("srparasites:carrier_heavy") ||
                name.equals("srparasites:carrier_flying"))
            return ModConfig.reducerSpawnCount_Congenital;
        // 威慑种
        if (name.equals("srparasites:kyphosis") || name.equals("srparasites:sentry") ||
                name.equals("srparasites:seizer") || name.equals("srparasites:dispatcherten") ||
                name.equals("srparasites:worm"))
            return ModConfig.reducerSpawnCount_Deterrent;
        // 连结种
        if (name.startsWith("srparasites:beckon_") || name.startsWith("srparasites:dispatcher_") ||
                name.startsWith("srparasites:rooter_") || name.equals("srparasites:rooterball"))
            return ModConfig.reducerSpawnCount_Connective;
        // 粗制种
        if (name.equals("srparasites:movingflesh") || name.equals("srparasites:host") ||
                name.equals("srparasites:crux") || name.equals("srparasites:heed") ||
                name.equals("srparasites:hostii") || name.equals("srparasites:thrall"))
            return ModConfig.reducerSpawnCount_Crude;

        return 1;
    }

    /**
     * 生成强化生物
     */
    public static EntityLivingBase spawnProtectedMob(World world, double x, double y, double z, String mobName) {
        try {
            ResourceLocation location = new ResourceLocation(mobName);
            EntityEntry entry = ForgeRegistries.ENTITIES.getValue(location);
            if (entry != null) {
                Entity entity = entry.newInstance(world);
                if (entity instanceof EntityLivingBase) {
                    entity.setPosition(x, y, z);
                    entity.getEntityData().setBoolean("yc_protectcoth", true);
                    world.spawnEntity(entity);
                    return (EntityLivingBase) entity;
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    public static boolean isTargetParasite(EntityLivingBase living) {
        return living instanceof EntityParasiteBase;
    }
}
