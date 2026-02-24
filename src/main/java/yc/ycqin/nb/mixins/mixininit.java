package yc.ycqin.nb.mixins;

import org.spongepowered.asm.mixin.Mixins;
import zone.rong.mixinbooter.MixinLoader;

@MixinLoader
public class mixininit {
    public mixininit(){
        Mixins.addConfiguration("mixins.ycqin.late.json");//把parasitic_technology_mixin_affiliation换成你的modID
    }
}
