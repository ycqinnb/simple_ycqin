package yc.ycqin.nb.mixins.confusionApply.entityMob;
import com.dhanantry.scapeandrunparasites.entity.EntityDamage;
import com.dhanantry.scapeandrunparasites.entity.monster.ancient.EntityTerla;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.SharedMonsterAttributes;
import net.minecraft.util.math.AxisAlignedBB;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import yc.ycqin.nb.register.PotionsRegister;

import java.util.List;

@Mixin(value = EntityTerla.class,remap = false)
public abstract class MixinTerlaAOE {

    @Inject(method = "attackEntityAsMobAOE", at = @At("HEAD"), cancellable = true)
    private void onAttackEntityAsMobAOE(Entity entityIn, CallbackInfoReturnable<Boolean> cir) {
        EntityTerla self = (EntityTerla)(Object)this;
        boolean confused = self.isPotionActive(PotionsRegister.CONFUSION);
        if (!confused) {
            return; // 无混乱，走原方法
        }

        // 混乱状态：复制原逻辑，但移除对寄生虫的跳过判断
        AxisAlignedBB aabb = new AxisAlignedBB(entityIn.posX, entityIn.posY, entityIn.posZ,
                entityIn.posX + 1.0, entityIn.posY + 1.0, entityIn.posZ + 1.0).grow(5.0, 2.0, 5.0);
        List<EntityLivingBase> moblist = self.world.getEntitiesWithinAABB(EntityLivingBase.class, aabb);
        if (moblist.size() > 4) {
            aabb = new AxisAlignedBB(self.posX, self.posY, self.posZ,
                    self.posX + 1.0, self.posY + 1.0, self.posZ + 1.0).grow(5.0, 3.0, 5.0);
            moblist = self.world.getEntitiesWithinAABB(EntityLivingBase.class, aabb);
            float luck = (float)(self.getEntityAttribute(SharedMonsterAttributes.ATTACK_DAMAGE).getBaseValue() * 2.0);
            for (EntityLivingBase mob : moblist) {
                if (mob != null && mob != self) {
                    // 混乱时攻击所有生物，不再检查是否寄生虫
                    EntityDamage damage = new EntityDamage(self.world, mob.posX, mob.posY, mob.posZ, 0.0F, self, luck, false, 3.0F);
                    self.world.spawnEntity(damage);
                }
            }
            cir.setReturnValue(true);
        } else {
            for (EntityLivingBase mob : moblist) {
                if (mob != null && mob != self) {
                    // 混乱时攻击所有生物
                    self.attackEntityAsMob(mob);
                }
            }
            cir.setReturnValue(!moblist.isEmpty());
        }
    }
}