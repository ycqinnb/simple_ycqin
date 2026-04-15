package yc.ycqin.nb.mixins;

import baubles.api.BaublesApi;
import baubles.api.cap.IBaublesItemHandler;
import com.dhanantry.scapeandrunparasites.entity.ai.misc.EntityPMalleable;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraftforge.fml.common.Loader;
import net.minecraftforge.fml.common.Optional;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyArg;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import yc.ycqin.nb.common.item.ItemCooldownAmulet;
import yc.ycqin.nb.config.ModConfig;

@Mixin(EntityPMalleable.class)
public abstract class MixinEntityPMalleable {

    // 存储当前玩家的冷却缩减比例（使用 ThreadLocal 避免并发问题）
    private static final ThreadLocal<Float> COOLDOWN_REDUCTION = new ThreadLocal<>();

    /**
     * 在方法头部检查目标玩家是否佩戴了饰品，并设置缩减比例
     */
    @Optional.Method(
            modid = "baubles"
    )
    @Inject(method = "scaryOrbEffect", at = @At("HEAD"),remap = false)
    private void onScaryOrbEffectHead(EntityLivingBase target, int totalMobs, CallbackInfoReturnable<Boolean> cir) {
        float reduction = 1.0f; // 默认不缩减
        if (target instanceof EntityPlayer && Loader.isModLoaded("baubles")) {
            EntityPlayer player = (EntityPlayer) target;
            IBaublesItemHandler handler = BaublesApi.getBaublesHandler(player);
            if (handler != null) {
                for (int i = 0; i < handler.getSlots(); i++) {
                    ItemStack stack = handler.getStackInSlot(i);
                    if (!stack.isEmpty() && stack.getItem() instanceof ItemCooldownAmulet) {
                        reduction = ModConfig.OrbReduction; // 减少 50% 冷却，可调整
                        break;
                    }
                }
            }
        }
        COOLDOWN_REDUCTION.set(reduction);
    }

    /**
     * 修改 setCooldown 的第二个参数（冷却时间）
     */
    @Optional.Method(
            modid = "baubles"
    )
    @ModifyArg(
            method = "scaryOrbEffect",
            at = @At(value = "INVOKE", target = "Lnet/minecraft/util/CooldownTracker;func_185145_a(Lnet/minecraft/item/Item;I)V"),
            index = 1,
            remap = false
    )
    private int modifyCooldown(int originalCooldown) {
        Float reduction = COOLDOWN_REDUCTION.get();
        if (reduction != null && reduction < 1.0f) {
            int newCooldown = Math.max(1, (int)(originalCooldown * (1 - reduction)));
            return newCooldown;
        }
        return originalCooldown;
    }

    @Inject(method = "scaryOrbEffect", at = @At("RETURN"),remap = false)
    private void onScaryOrbEffectReturn(EntityLivingBase target, int totalMobs, CallbackInfoReturnable<Boolean> cir) {
        COOLDOWN_REDUCTION.remove();
    }
}