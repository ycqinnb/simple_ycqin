package yc.ycqin.nb.util;

import com.dhanantry.scapeandrunparasites.entity.ai.misc.EntityPMalleable;

import java.util.ArrayList;

public class AdaptationHelper {
    /**
     * 以指定概率减少实体所有适应类型的点数。
     * @param entity 目标寄生虫实体
     * @param chance 触发概率（0.0 ~ 1.0）
     */
    public static void reduceAllResistances(EntityPMalleable entity, float chance) {
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
            int newValue = resistanceI.get(i) - 1;
            if (newValue <= 0) {
                resistanceS.remove(i);
                resistanceI.remove(i);
            } else {
                resistanceI.set(i, newValue);
            }
        }
    }
}
