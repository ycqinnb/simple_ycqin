package yc.ycqin.nb.common.entity.ai;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.ai.EntityAITarget;
import net.minecraft.entity.monster.EntityMob;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.math.AxisAlignedBB;
import yc.ycqin.nb.util.EntityClassifier;

public class EntityAIFindParasite extends EntityAITarget {
    private final EntityMob entity;
    private final double range;
    private EntityLivingBase target;
    private int scanCooldown = 0;

    public EntityAIFindParasite(EntityMob entity, double range) {
        super(entity, false, false);
        this.entity = entity;
        this.range = range;
        this.setMutexBits(1);
    }

    @Override
    public boolean shouldExecute() {
        if (!entity.getEntityData().hasKey("yc_protectcoth")) return false;
        // 如果已有攻击目标且存活，不再扫描
        if (entity.getAttackTarget() != null && entity.getAttackTarget().isEntityAlive() && !(entity.getAttackTarget() instanceof EntityPlayer)) {
            return false;
        }
        if (scanCooldown > 0) {
            scanCooldown--;
            return false;
        }
        scanCooldown = 10;

        AxisAlignedBB aabb = entity.getEntityBoundingBox().grow(range);
        for (EntityLivingBase living : entity.world.getEntitiesWithinAABB(EntityLivingBase.class, aabb)) {
            if (living != entity && EntityClassifier.isTargetParasite(living) && entity.canEntityBeSeen(living)) {
                this.target = living;
                return true;
            }
        }
        return false;
    }

    @Override
    public void startExecuting() {
        entity.setAttackTarget(target);
        super.startExecuting();
    }

    @Override
    public boolean shouldContinueExecuting() {
        return target != null && target.isEntityAlive() && entity.getAttackTarget() == target;
    }
}