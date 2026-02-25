package yc.ycqin.nb.mixins;

import net.minecraft.entity.item.EntityEnderCrystal;
import net.minecraft.world.end.DragonFightManager;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import yc.ycqin.nb.common.item.ItemDrafh;

import java.util.List;

@Mixin(DragonFightManager.class)
public abstract class MixinDragonFightManager {
    @Shadow
    protected abstract void respawnDragon(List<EntityEnderCrystal> crystals);

    /**
     * 在公共的 respawnDragon() 方法头部注入
     * 如果 customCrystals 非空，则使用它调用私有方法并取消原方法
     */
    @Inject(method = "respawnDragon()V", at = @At("HEAD"), cancellable = true)
    private void onRespawnDragon(CallbackInfo ci) {
        List<EntityEnderCrystal> customCrystals = ItemDrafh.getAndClearCustomCrystals();
        if (customCrystals != null && !customCrystals.isEmpty()) {
            // 使用自定义列表执行复活流程
            this.respawnDragon(customCrystals);
            // 取消原方法的执行
            ci.cancel();
        }
    }
}