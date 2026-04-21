package yc.ycqin.nb.mixins.baubles;

import baubles.api.BaublesApi;
import baubles.api.cap.IBaublesItemHandler;
import com.dhanantry.scapeandrunparasites.entity.monster.primitive.EntityRanrac;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraftforge.fml.common.Optional;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;
import yc.ycqin.nb.common.item.ItemPullImmunityCharm;

@Mixin(value = EntityRanrac.class,remap = false)
public abstract class MixinEntityRanrac {

    @Shadow
    private int pulling;
    @Shadow private boolean canPull;
    @Shadow private boolean skillpulling;
    @Shadow private EntityLivingBase targetedEntity;
    /**
     * 重定向 getTargetedEntity() 调用，当目标是佩戴免疫饰品的玩家时返回 null
     */
    @Optional.Method(
            modid = "baubles"
    )
    @Redirect(
            method = "func_70636_d",  // update
            at = @At(value = "INVOKE", target = "Lcom/dhanantry/scapeandrunparasites/entity/monster/primitive/EntityRanrac;getTargetedEntity()Lnet/minecraft/entity/EntityLivingBase;"),
            remap = false

    )
    private EntityLivingBase onGetTargetedEntity(EntityRanrac self) {
        EntityLivingBase target = targetedEntity; // 调用原方法获取目标
        if (target instanceof EntityPlayer) {
            EntityPlayer player = (EntityPlayer) target;
            IBaublesItemHandler handler = BaublesApi.getBaublesHandler(player);
            if (handler != null) {
                for (int i = 0; i < handler.getSlots(); i++) {
                    ItemStack stack = handler.getStackInSlot(i);
                    if (!stack.isEmpty() && stack.getItem() instanceof ItemPullImmunityCharm) {
                        pulling = 200;      // 普通蛛形兽超过 200 会重置
                        canPull = true;     // 确保后续可以再次使用技能
                        skillpulling = true; // 标记技能已完成
                        return null; // 佩戴了免疫饰品，不拉拽
                    }
                }
            }
        }
        return target;
    }
}
