package yc.ycqin.nb.mixins.baubles;

import baubles.api.BaublesApi;
import baubles.api.cap.IBaublesItemHandler;
import com.dhanantry.scapeandrunparasites.entity.monster.adapted.EntityRanracAdapted;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraftforge.fml.common.Optional;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;
import yc.ycqin.nb.common.item.ItemPullImmunityCharm;

@Mixin(value = EntityRanracAdapted.class,remap = false)
public abstract class MixinEntityRanracAdapted {
    @Shadow
    private int pulling;
    @Shadow private boolean canPull;
    @Shadow private boolean skillpulling;
    @Shadow private EntityLivingBase targetedEntity;

    @Optional.Method(
            modid = "baubles"
    )
    @Redirect(
            method = "func_70636_d",
            at = @At(value = "INVOKE", target = "Lcom/dhanantry/scapeandrunparasites/entity/monster/adapted/EntityRanracAdapted;getTargetedEntity()Lnet/minecraft/entity/EntityLivingBase;"),
            remap = false
    )
    private EntityLivingBase onGetTargetedEntity(EntityRanracAdapted self) {
        EntityLivingBase target = targetedEntity;
        if (target instanceof EntityPlayer) {
            EntityPlayer player = (EntityPlayer) target;
            IBaublesItemHandler handler = BaublesApi.getBaublesHandler(player);
            if (handler != null) {
                for (int i = 0; i < handler.getSlots(); i++) {
                    ItemStack stack = handler.getStackInSlot(i);
                    if (!stack.isEmpty() && stack.getItem() instanceof ItemPullImmunityCharm) {
                        pulling = 400;      // 适应体阈值更高
                        canPull = true;
                        skillpulling = true;
                        return null;
                    }
                }
            }
        }
        return target;
    }
}
