package yc.ycqin.nb.common.blade.se;

import mods.flammpfeil.slashblade.specialeffect.ISpecialEffect;
import mods.flammpfeil.slashblade.specialeffect.SpecialEffects;

public class SeLoad {
    public static ISpecialEffect mindag;
    public SeLoad(){
       mindag = SpecialEffects.register(new MinDamageSe());
    }
}
