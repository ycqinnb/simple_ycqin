package yc.ycqin.nb.common.entity.ai;

import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.ai.EntityAITarget;
import net.minecraft.entity.monster.EntityMob;

public class EntityAIHurtByTargetParasiteOnly extends EntityAITarget {
    private final EntityMob entity;

    public EntityAIHurtByTargetParasiteOnly(EntityMob entity) {
        super(entity, false, false);
        this.entity = entity;
        this.setMutexBits(1);
    }

    @Override
    public boolean shouldExecute() {
        if (!entity.getEntityData().hasKey("yc_protectcoth")) return false;
        EntityLivingBase attacker = entity.getRevengeTarget();
        return attacker != null && attacker.isEntityAlive();
    }

    @Override
    public void startExecuting() {
        entity.setAttackTarget(entity.getRevengeTarget());
        super.startExecuting();
    }

    @Override
    public boolean shouldContinueExecuting() {
        return entity.getAttackTarget() != null && entity.getAttackTarget().isEntityAlive();
    }
}