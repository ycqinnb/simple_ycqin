package yc.ycqin.nb.mixins.confusionApply.entityMob;
import com.dhanantry.scapeandrunparasites.entity.EntityDamage;
import com.dhanantry.scapeandrunparasites.entity.monster.pure.preeminent.EntityPheon;
import com.dhanantry.scapeandrunparasites.util.SRPAttributes;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.util.math.AxisAlignedBB;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import yc.ycqin.nb.register.PotionsRegister;

import java.util.List;

@Mixin(value = EntityPheon.class,remap = false)
public class MixinEntityPheonAOE {

    @Inject(method = "attackEntityAsMobAOE", at = @At("HEAD"), cancellable = true)
    private void onAttackEntityAsMobAOE(Entity entityIn, CallbackInfoReturnable<Boolean> cir) {
        EntityPheon self = (EntityPheon) (Object) this;
        boolean confused = self.isPotionActive(PotionsRegister.CONFUSION);
        if (!confused) {
            return; // 没有混乱效果，执行原方法
        }

        // 重写混乱状态下的攻击逻辑
        AxisAlignedBB aabb = new AxisAlignedBB(
                entityIn.posX, entityIn.posY, entityIn.posZ,
                entityIn.posX + 1.0, entityIn.posY + 1.0, entityIn.posZ + 1.0
        ).grow(5.0, 2.0, 5.0);
        List<EntityLivingBase> mobs = self.world.getEntitiesWithinAABB(EntityLivingBase.class, aabb);

        if (mobs.size() > 4) {
            aabb = new AxisAlignedBB(
                    self.posX, self.posY, self.posZ,
                    self.posX + 1.0, self.posY + 1.0, self.posZ + 1.0
            ).grow(5.0, 3.0, 5.0);
            mobs = self.world.getEntitiesWithinAABB(EntityLivingBase.class, aabb);
            float damage = (float) (SRPAttributes.TERLA_ATTACK_DAMAGE * 2.0);
            for (EntityLivingBase mob : mobs) {
                if (mob != null && mob != self) {
                    EntityDamage damageEntity = new EntityDamage(self.world, mob.posX, mob.posY, mob.posZ, 0.0F, self, damage, false, 3.0F);
                    self.world.spawnEntity(damageEntity);
                }
            }
            cir.setReturnValue(true);
        } else {
            for (EntityLivingBase mob : mobs) {
                if (mob != null && mob != self) {
                    self.attackEntityAsMob(mob);
                }
            }
            cir.setReturnValue(!mobs.isEmpty());
        }
    }
}