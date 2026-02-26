package yc.ycqin.nb.mixins;
import com.brandon3055.draconicevolution.blocks.tileentity.TileGrinder; // 直接导入，没问题了！
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.util.DamageSource;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;
import yc.ycqin.nb.config.ModConfig;

@Mixin(value = TileGrinder.class, remap = false) // 直接使用类引用，清晰明了
public class MixinTileGrinder {

    @Redirect(
            method = "updateGrinding",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/entity/EntityLivingBase;func_70097_a(Lnet/minecraft/util/DamageSource;F)Z"
            ),
            remap = false // 原版方法需要重映射
    )
    private boolean redirectGrinderAttack(EntityLivingBase target, DamageSource source, float damage) {
      if (ModConfig.fixEnabled) {
          target.setDead();
          return true;
      }
        return target.attackEntityFrom(source, damage);
    }
}