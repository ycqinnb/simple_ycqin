package yc.ycqin.nb.mixins;

import com.dhanantry.scapeandrunparasites.init.SRPPotions;
import com.dhanantry.scapeandrunparasites.potion.SRPEffectBase;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.nbt.NBTTagCompound;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(value = SRPEffectBase.class, remap = false)
public class MixinSRPEffectBase {

    @Inject(method = "func_76394_a", at = @At("HEAD"), cancellable = true, remap = false)
    private void onPerformEffect(EntityLivingBase entity, int amplifier, CallbackInfo ci) {
        // 判断当前效果是否是 COTH_E
        if ((Object) this == SRPPotions.COTH_E) {
            NBTTagCompound data = entity.getEntityData();
            if (data.hasKey("yc_protectcoth")) {
                // 移除效果
                entity.removePotionEffect(SRPPotions.COTH_E);
                // 取消本次效果执行（不再触发转化、传染等逻辑）
                ci.cancel();
            }
        }
    }
}
