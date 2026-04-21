package yc.ycqin.nb.mixins;

import com.dhanantry.scapeandrunparasites.entity.ai.misc.EntityParasiteBase;
import com.dhanantry.scapeandrunparasites.init.SRPPotions;
import com.dhanantry.scapeandrunparasites.util.ParasiteEventEntity;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.LocalCapture;

@Mixin(value = ParasiteEventEntity.class, remap = false)
public class MixinParasiteEventEntity {
    @Inject(method = "convertEntity", at = @At("HEAD"), cancellable = true, remap = false)
    private static void onConvertEntity(EntityLivingBase entityin, NBTTagCompound tags,
                                        boolean ignoreKey, String[] list, CallbackInfo ci) {
        if (entityin != null && entityin.getEntityData().hasKey("yc_protectcoth")) {
            if (entityin.isPotionActive(SRPPotions.COTH_E)) {
                entityin.removePotionEffect(SRPPotions.COTH_E);
            }
            ci.cancel();
        }
    }

    @Inject(
            method = "convertEntity",
            at = @At(value = "INVOKE", target = "Lnet/minecraft/world/World;func_72838_d(Lnet/minecraft/entity/Entity;)Z", shift = At.Shift.AFTER),
            locals = LocalCapture.CAPTURE_FAILSOFT,
            remap = false
    )
    private static void saveOriginalEntityDataIfNotParasite(
            EntityLivingBase entityin,
            NBTTagCompound tags,
            boolean ignoreKey,
            String[] list,
            CallbackInfo ci,
            Entity entityout   // spawnEntity 的参数，即新生成的寄生虫实体
    ) {
        // 关键判断：原始实体不是寄生虫时才保存
        if (entityout != null && entityin != null && !(entityin instanceof EntityParasiteBase)) {
            if (!entityout.getEntityData().hasKey("original_entity_nbt")) {
                NBTTagCompound originalNbt = new NBTTagCompound();
                entityin.writeToNBT(originalNbt);
                entityout.getEntityData().setTag("original_entity_nbt", originalNbt);
            }
        }
    }


    @Inject(
            method = "spawnInsider",
            at = @At(value = "INVOKE", target = "Lnet/minecraft/world/World;func_72838_d(Lnet/minecraft/entity/Entity;)Z", shift = At.Shift.AFTER),
            locals = LocalCapture.CAPTURE_FAILSOFT,
            remap = false
    )
    private static void saveOriginalEntityDataIfNotParasite2(
            EntityLivingBase entityin,
            World world,
            NBTTagCompound tags,
            CallbackInfo ci,
            Entity out   // spawnEntity 的参数，即新生成的寄生虫实体
    ) {
        // 关键判断：原始实体不是寄生虫时才保存
        if (out != null && entityin != null && !(entityin instanceof EntityParasiteBase)) {
            if (!out.getEntityData().hasKey("original_entity_nbt")) {
                NBTTagCompound originalNbt = new NBTTagCompound();
                entityin.writeToNBT(originalNbt);
                out.getEntityData().setTag("original_entity_nbt", originalNbt);
            }
        }
    }
}