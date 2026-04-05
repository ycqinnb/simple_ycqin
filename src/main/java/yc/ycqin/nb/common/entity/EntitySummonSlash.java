package yc.ycqin.nb.common.entity;

import io.netty.buffer.ByteBuf;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.DamageSource;
import net.minecraft.world.World;
import net.minecraftforge.fml.common.registry.IEntityAdditionalSpawnData;

import java.io.IOException;

public class EntitySummonSlash extends Entity implements IEntityAdditionalSpawnData {
    private EntityLivingBase target;
    private EntityPlayer owner;
    private Entity rupter;
    private ItemStack weapon = ItemStack.EMPTY;
    private float damage;
    private int life = 8; // 存活 tick 数（约0.4秒）

    // 客户端反序列化时暂存的ID
    private int targetId;
    private int ownerId;
    private int rupterId;

    public EntitySummonSlash(World world) {
        super(world);
    }

    public EntitySummonSlash(World world, EntityLivingBase target, EntityPlayer owner,
                             Entity rupter, float damage, ItemStack weapon) {
        super(world);
        this.target = target;
        this.owner = owner;
        this.rupter = rupter;
        this.damage = damage;
        this.weapon = weapon.copy();
        this.setPosition(target.posX, target.posY + target.height / 2, target.posZ);
    }

    @Override
    public void onUpdate() {
        super.onUpdate();
        if (world.isRemote) {
            // 客户端只负责渲染，不执行伤害逻辑
            return;
        }

        life--;
        if (life <= 0) {
            if (target != null && !target.isDead && owner != null) {
                int oldInvul = target.hurtResistantTime;
                target.hurtResistantTime = 0; // 无视无敌帧
                target.attackEntityFrom(DamageSource.causePlayerDamage(owner), damage);
                target.hurtResistantTime = oldInvul;
            }
            if (rupter != null && !rupter.isDead) {
                rupter.setDead();
            }
            this.setDead();
        }
    }

    // 同步生成时的数据（服务端 -> 客户端）
    @Override
    public void writeSpawnData(ByteBuf buffer) {
        PacketBuffer buf = new PacketBuffer(buffer);
        buf.writeInt(target == null ? 0 : target.getEntityId());
        buf.writeInt(owner == null ? 0 : owner.getEntityId());
        buf.writeInt(rupter == null ? 0 : rupter.getEntityId());
        buf.writeFloat(damage);
        buf.writeItemStack(weapon);
    }

    // 客户端读取数据
    @Override
    public void readSpawnData(ByteBuf additionalData) {
        PacketBuffer buf = new PacketBuffer(additionalData);
        targetId = buf.readInt();
        ownerId = buf.readInt();
        rupterId = buf.readInt();
        damage = buf.readFloat();
        try {
            weapon = buf.readItemStack();
        } catch (IOException e) {
            System.out.println("readItemStack Error");
        }
    }

    // 客户端实体添加到世界后，通过ID获取实体引用
    @Override
    public void onAddedToWorld() {
        super.onAddedToWorld();
        if (world.isRemote) {
            if (targetId != 0) {
                Entity e = world.getEntityByID(targetId);
                if (e instanceof EntityLivingBase) target = (EntityLivingBase) e;
            }
            if (ownerId != 0) {
                Entity e = world.getEntityByID(ownerId);
                if (e instanceof EntityPlayer) owner = (EntityPlayer) e;
            }
            if (rupterId != 0) {
                Entity e = world.getEntityByID(rupterId);
                if (e != null) rupter = e;
            }
        }
    }

    // 供渲染器获取数据
    public ItemStack getRenderItem() { return weapon; }
    public EntityLivingBase getTarget() { return target; }

    // 必要的NBT方法（无需持久化）
    @Override
    protected void entityInit() {}
    @Override
    protected void readEntityFromNBT(NBTTagCompound compound) {}
    @Override
    protected void writeEntityToNBT(NBTTagCompound compound) {}
}