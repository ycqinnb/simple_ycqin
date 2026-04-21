package yc.ycqin.nb.srpcore;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.datasync.DataParameter;

import java.lang.reflect.Field;

public class ProtectHelper {

    private static final float DAMAGE_CAP_RATIO = 0.25f;

    public static float getHealth(EntityLivingBase self) throws IllegalAccessException {
        Field field;
        try{
            field = EntityLivingBase.class.getDeclaredField("field_184632_c");
        } catch (NoSuchFieldException e) {
            try {
                field = EntityLivingBase.class.getDeclaredField("HEALTH");
            } catch (NoSuchFieldException ex) {
                throw new RuntimeException(ex);
            }
        }
        field.setAccessible(true);
        // 获取原版生命值
        float current = self.getDataManager().get((DataParameter<Float>) field.get(null)).floatValue();
        NBTTagCompound data = self.getEntityData();

        if (data.hasKey("yc_protectcoth")) {
            float max = self.getMaxHealth();
            float limit = max * DAMAGE_CAP_RATIO;
            float last = data.getFloat("yc_last_health");
            float diff = last - current;
            if (diff > limit && diff > 0) {
                float corrected = last - limit;
                if (corrected < 0) corrected = 0;
                // 直接设置 DataManager 中的生命值
                self.setHealth(corrected);
                data.setFloat("yc_last_health", corrected);
                return corrected;
            } else {
                data.setFloat("yc_last_health", current);
            }
        }
        return current;
    }
}