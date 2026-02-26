package yc.ycqin.nb.event;

import net.minecraft.entity.EntityLiving;
import net.minecraft.entity.SharedMonsterAttributes;
import net.minecraft.entity.ai.attributes.AbstractAttributeMap;
import net.minecraft.entity.ai.attributes.AttributeModifier;
import net.minecraft.entity.ai.attributes.IAttribute;
import net.minecraft.entity.ai.attributes.IAttributeInstance;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraftforge.event.entity.living.LivingSpawnEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import yc.ycqin.nb.config.ModConfig;
import yc.ycqin.nb.register.DimRegister;
import yc.ycqin.nb.util.AttributeHelper;
import yc.ycqin.nb.ycqin;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class DimensionAttributeHandler {

    // 使用固定的UUID，确保每次都能识别并替换旧的修饰符
    private static final UUID DIM_BOOST_UUID = UUID.fromString("ced0fea4-c225-451e-834d-060de5230e08");
    private static final String DIM_BOOST_NAME = "dimension_boost";

    @SubscribeEvent
    public void onLivingSpawn(LivingSpawnEvent event) {
        // 只在服务端处理
        if (!ModConfig.dimBoostEnabled) return;
        if (event.getWorld().isRemote) return;

        // 只处理怪物，不处理玩家
        if (!(event.getEntity() instanceof EntityLiving)) return;
        EntityLiving entity = (EntityLiving) event.getEntity();

        // 检查维度：通过维度ID或WorldProvider类型
        if (event.getWorld().provider.getDimension() != 78) return;
        double multiplier = ModConfig.dimBoostMultiplier;
        double amount = multiplier - 1.0; // 操作类型1需要增加值
        AttributeModifier modifier = new AttributeModifier(DIM_BOOST_UUID, "dim_boost", amount, 1);

        for (String attrName : ModConfig.dimBoostAttributes) {
            IAttribute attr = AttributeHelper.getAttribute(attrName);
            if (attr == null) {
                // 可选：打印一次警告（避免刷屏），提示用户该属性未找到
                continue;
            }
            IAttributeInstance instance = entity.getAttributeMap().getAttributeInstance(attr);
            if (instance == null) {
                continue; // 该生物没有此属性
            }
            instance.removeModifier(DIM_BOOST_UUID);
            instance.applyModifier(modifier);
        }

        // 如果修改了最大生命值，需要同步当前生命值，否则显示可能异常
        IAttributeInstance healthAttr = entity.getEntityAttribute(SharedMonsterAttributes.MAX_HEALTH);
        if (healthAttr != null) {
            double newMaxHealth = healthAttr.getAttributeValue();
            entity.setHealth((float) newMaxHealth); // 直接设为满血，也可按比例设置
        }
    }
}