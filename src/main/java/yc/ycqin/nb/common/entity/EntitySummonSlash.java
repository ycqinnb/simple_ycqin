package yc.ycqin.nb.common.entity;

import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.DamageSource;
import net.minecraft.world.World;

public class EntitySummonSlash extends Entity {
    private EntityLivingBase target;
    public EntityPlayer owner;
    private Entity rupter;            // 对应的 rupter 实体
    private int life = 5;
    private float damage;
    private ItemStack renderItem; // 要渲染的物品

    public EntitySummonSlash(World world) {
        super(world);
    }

    public EntitySummonSlash(World world, EntityLivingBase target, EntityPlayer owner, Entity rupter, float damage,ItemStack renderItem) {
        super(world);
        this.target = target;
        this.owner = owner;
        this.rupter = rupter;
        this.damage = damage;
        this.renderItem = renderItem;
        this.setPosition(target.posX, target.posY + target.height / 2, target.posZ);
    }
    public ItemStack getRenderItem() {
        return renderItem;
    }
    @Override
    public void onUpdate() {
        super.onUpdate();
        if (world.isRemote) return;
        life--;
        if (life <= 0) {
            // 先杀死 rupter
            if (rupter != null && !rupter.isDead) {
                rupter.setDead();
            }
            // 再造成伤害
            if (target != null && !target.isDead && owner != null) {
                int oldInvul = target.hurtResistantTime;
                target.hurtResistantTime = 0;
                target.attackEntityFrom(DamageSource.causePlayerDamage(owner), damage);
                target.hurtResistantTime = oldInvul;
            }
            this.setDead();
        }
    }

    @Override
    protected void entityInit() {}
    @Override
    protected void readEntityFromNBT(NBTTagCompound compound) {}
    @Override
    protected void writeEntityToNBT(NBTTagCompound compound) {}
}