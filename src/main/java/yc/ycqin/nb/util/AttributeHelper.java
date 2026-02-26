package yc.ycqin.nb.util;

import net.minecraft.entity.SharedMonsterAttributes;
import net.minecraft.entity.ai.attributes.IAttribute;
import java.util.HashMap;
import java.util.Map;

public class AttributeHelper {
    private static final Map<String, IAttribute> ATTRIBUTE_MAP = new HashMap<>();

    static {
        // 原版属性友好名称（与 SharedMonsterAttributes 字段名不同，但更通用）
        ATTRIBUTE_MAP.put("generic.maxHealth", SharedMonsterAttributes.MAX_HEALTH);
        ATTRIBUTE_MAP.put("generic.followRange", SharedMonsterAttributes.FOLLOW_RANGE);
        ATTRIBUTE_MAP.put("generic.knockbackResistance", SharedMonsterAttributes.KNOCKBACK_RESISTANCE);
        ATTRIBUTE_MAP.put("generic.movementSpeed", SharedMonsterAttributes.MOVEMENT_SPEED);
        ATTRIBUTE_MAP.put("generic.attackDamage", SharedMonsterAttributes.ATTACK_DAMAGE);
        ATTRIBUTE_MAP.put("generic.armor", SharedMonsterAttributes.ARMOR);
        ATTRIBUTE_MAP.put("generic.armorToughness", SharedMonsterAttributes.ARMOR_TOUGHNESS);
        ATTRIBUTE_MAP.put("generic.luck", SharedMonsterAttributes.LUCK);
        ATTRIBUTE_MAP.put("generic.attackSpeed", SharedMonsterAttributes.ATTACK_SPEED);
    }

    /**
     * 注册一个自定义属性（供其他模组或反射调用）
     * @param friendlyName 配置中使用的友好名称（如 "srparasites.parasiteDamage"）
     * @param attribute 属性实例
     */
    public static void registerAttribute(String friendlyName, IAttribute attribute) {
        ATTRIBUTE_MAP.put(friendlyName, attribute);
    }

    /**
     * 根据友好名称获取属性
     */
    public static IAttribute getAttribute(String friendlyName) {
        return ATTRIBUTE_MAP.get(friendlyName);
    }

    /**
     * 获取所有已注册的友好名称（用于调试或提示）
     */
    public static String[] getRegisteredNames() {
        return ATTRIBUTE_MAP.keySet().toArray(new String[0]);
    }
}