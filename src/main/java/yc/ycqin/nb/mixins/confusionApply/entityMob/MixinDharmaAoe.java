package yc.ycqin.nb.mixins.confusionApply.entityMob;

import com.dhanantry.scapeandrunparasites.entity.EntityDamage;
import com.dhanantry.scapeandrunparasites.entity.ai.misc.EntityParasiteBase;
import com.dhanantry.scapeandrunparasites.entity.monster.ancient.EntityDharma;
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

@Mixin(value = EntityDharma.class,remap = false)
public abstract class MixinDharmaAoe {

    @Inject(method = "attackEntityAsMobAOE", at = @At("HEAD"), cancellable = true)
    private void onAttackEntityAsMobAOE(Entity entityIn, CallbackInfoReturnable<Boolean> cir) {
        EntityDharma self = (EntityDharma) (Object) this;
        boolean confused = self.isPotionActive(PotionsRegister.CONFUSION);
        if (!confused) return;
        // 初始范围：以目标为中心，半径5（X/Z方向），高度2
        AxisAlignedBB aabb = new AxisAlignedBB(
                entityIn.posX, entityIn.posY, entityIn.posZ,
                entityIn.posX + 1.0, entityIn.posY + 1.0, entityIn.posZ + 1.0
        ).grow(5.0, 2.0, 5.0);
        List<EntityLivingBase> moblist = self.world.getEntitiesWithinAABB(EntityLivingBase.class, aabb);

        if (moblist.size() > 4) {
            // 如果目标多，扩大范围为以自身为中心，半径5（X/Z方向），高度3
            aabb = new AxisAlignedBB(
                    self.posX, self.posY, self.posZ,
                    self.posX + 1.0, self.posY + 1.0, self.posZ + 1.0
            ).grow(5.0, 3.0, 5.0);
            moblist = self.world.getEntitiesWithinAABB(EntityLivingBase.class, aabb);
            float luck = (float) (SRPAttributes.TERLA_ATTACK_DAMAGE * 2.0F);
            for (EntityLivingBase mob : moblist) {
                if (mob != null && mob != self && (confused || !(mob instanceof EntityParasiteBase))) {
                    // 生成伤害实体（相当于AOE冲击波）
                    EntityDamage damage = new EntityDamage(
                            self.world, mob.posX, mob.posY, mob.posZ,
                            0.0F, self, luck, false, 3.0F
                    );
                    self.world.spawnEntity(damage);
                }
            }
            cir.setReturnValue(true);
            return;
        } else {
            // 目标较少时，逐个近战攻击
            for (EntityLivingBase mob : moblist) {
                if (mob != null && mob != self && (confused || !(mob instanceof EntityParasiteBase))) {
                    self.attackEntityAsMob(mob);
                }
            }
            cir.setReturnValue(!moblist.isEmpty());
            return;
        }
    }
}