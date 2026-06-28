package yc.ycqin.nb.common.potion;

import net.minecraft.potion.Potion;

public class PotionConfusion extends Potion {
    public PotionConfusion() {
        super(false, 0x800080); // 紫色，负面效果
        setRegistryName("ycqin:confusion");
        setPotionName("effect.ycqin.confusion");
    }

}