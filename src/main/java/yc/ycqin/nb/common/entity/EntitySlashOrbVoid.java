package yc.ycqin.nb.common.entity;

import java.util.List;
import java.util.UUID;

import mods.flammpfeil.slashblade.entity.selector.EntitySelectorAttackable;
import mods.flammpfeil.slashblade.item.ItemSlashBlade;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.SoundEvents;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.datasync.DataParameter;
import net.minecraft.network.datasync.DataSerializers;
import net.minecraft.network.datasync.EntityDataManager;
import net.minecraft.potion.Potion;
import net.minecraft.potion.PotionEffect;
import net.minecraft.util.DamageSource;
import net.minecraft.util.EnumParticleTypes;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import net.minecraftforge.fml.common.registry.ForgeRegistries;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

public class EntitySlashOrbVoid extends Entity {

    // ========== 数据同步参数 ==========
    private static final DataParameter<Integer> SELFE = EntityDataManager.createKey(EntitySlashOrbVoid.class, DataSerializers.VARINT);
    private static final DataParameter<Integer> FUSE = EntityDataManager.createKey(EntitySlashOrbVoid.class, DataSerializers.VARINT);
    private static final DataParameter<Integer> WAITSTART = EntityDataManager.createKey(EntitySlashOrbVoid.class, DataSerializers.VARINT);
    // 新增常量
    private static final float MAX_SIZE = 3.0F;           // 最终直径（3格）
    private static final float ATTRACT_RANGE = 48.0F;     // 吸引范围（32格）
    private double pullStrength = 0.2;                     // 吸引强度（与原版一致）
    // ========== 非同步字段 ==========
    private EntityPlayer owner;                 // 攻击者（玩家）
    private ItemStack bladeStack;                // 使用的拔刀剑
    private double poosX, poosY, poosZ;          // 初始记录位置
    public float alpha = 1.0F;                   // 透明度（客户端渲染用）
    protected int lastActiveTime;                 // 用于客户端闪烁
    protected int timeSinceIgnited;               // 引信已过时间
    protected int timerDDD;                        // 爆炸阶段计时器

    // 渲染动画字段（供渲染器使用）
    public float prevRenderYawOffset;
    public float renderYawOffset;
    public float prevRotationYawHead;
    public float rotationYawHead;
    public float prevLimbSwingAmount;
    public float limbSwingAmount;
    public float limbSwing;
    public int hurtTime;
    public int deathTime;

    // ========== 构造方法 ==========
    public EntitySlashOrbVoid(World worldIn) {
        super(worldIn);
        this.setSize(0.5F, 0.5F);
        this.isImmuneToFire = true;
        this.noClip = true;
        this.lastActiveTime = 0;
        this.timeSinceIgnited = 0;
        this.timerDDD = 0;
        this.setFuseState(60);                     // 默认引信 60 ticks
        this.setStartState(10);                     // 默认等待 10 ticks

        // 初始化动画字段
        this.prevRenderYawOffset = 0;
        this.renderYawOffset = 0;
        this.prevRotationYawHead = 0;
        this.rotationYawHead = 0;
        this.prevLimbSwingAmount = 0;
        this.limbSwingAmount = 0;
        this.limbSwing = 0;
        this.hurtTime = 0;
        this.deathTime = 0;
    }

    public EntitySlashOrbVoid(World worldIn, EntityPlayer owner, ItemStack blade, int fuse, int waitStart) {
        this(worldIn);
        this.owner = owner;
        this.bladeStack = blade;
        this.setFuseState(fuse);
        this.setStartState(waitStart);
    }

    // ========== 数据管理器初始化 ==========
    @Override
    protected void entityInit() {
        this.dataManager.register(SELFE, -1);
        this.dataManager.register(FUSE, 60);
        this.dataManager.register(WAITSTART, 10);
    }

    // ========== 状态读写 ==========
    public int getSelfeState() { return this.dataManager.get(SELFE); }
    public void setSelfeState(int state) { this.dataManager.set(SELFE, state); }

    public int getFuseState() { return this.dataManager.get(FUSE); }
    public void setFuseState(int state) { this.dataManager.set(FUSE, state); }

    public int getStartState() { return this.dataManager.get(WAITSTART); }
    public void setStartState(int state) { this.dataManager.set(WAITSTART, state); }

    // ========== 每 tick 更新 ==========
    @Override
    public void onUpdate() {
        super.onUpdate();

        if (this.world.isRemote) {
            return; // 客户端无额外粒子
        }

        // 服务端逻辑
        if (this.ticksExisted > this.getStartState()) {
            // 激活阶段：立即设置为最大尺寸（仅第一次）
            if (this.width < MAX_SIZE - 0.01F) { // 避免浮点误差反复设置
                this.setSize(MAX_SIZE, MAX_SIZE);
            }

            this.setSelfeState(1);
            this.orbDoing();                // 吸引 + 持续伤害（仅球体内）
            this.dyingBurst(true, 1);        // 引信计时（不再扩大尺寸）

            // 位置固定在记录点
            this.setPosition(this.poosX, this.poosY - this.rand.nextDouble() * 0.1, this.poosZ);
        } else {
            // 等待阶段：记录初始位置，尺寸保持初始 0.5
            this.poosX = this.posX;
            this.poosY = this.posY;
            this.poosZ = this.posZ;
        }
    }

    // ========== 核心行为：吸引 + 持续伤害 ==========
    private void orbDoing() {
        // 使用固定吸引范围 32 格
        AxisAlignedBB bb = new AxisAlignedBB(
                this.posX - ATTRACT_RANGE, this.posY - ATTRACT_RANGE, this.posZ - ATTRACT_RANGE,
                this.posX + ATTRACT_RANGE, this.posY + ATTRACT_RANGE, this.posZ + ATTRACT_RANGE
        );
        List<EntityLivingBase> list = this.world.getEntitiesWithinAABB(EntityLivingBase.class, bb,
                entity -> entity != owner && !entity.isDead && EntitySelectorAttackable.getInstance().apply(entity));

        for (EntityLivingBase target : list) {
            // 吸引逻辑（所有在范围内的生物都被拉向球心）
            pullEntity(target);

            // 只有进入球体内部（距离 < 半径 1.5 格）才造成伤害
            double dist = this.getDistance(target);
            if (dist < this.width / 2.0) { // 半径 = 宽度/2
                doOrbDamage(1.0F, target);
            }
        }
    }

    /**
     * 吸引单个实体（原版虚空球逻辑）
     */
    private void pullEntity(EntityLivingBase target) {
        double dx = this.posX - target.posX;
        double dy = this.posY - target.posY;
        double dz = this.posZ - target.posZ;
        double distance = Math.sqrt(dx * dx + dy * dy + dz * dz);

        if (distance < 0.1) return;

        dx /= distance;
        dy /= distance;
        dz /= distance;

        target.motionX += dx * pullStrength;
        target.motionY += dy * pullStrength;
        target.motionZ += dz * pullStrength;

        // 限制最大速度，防止飞出
        double maxSpeed = 0.5;
        if (target.motionX > maxSpeed) target.motionX = maxSpeed;
        if (target.motionX < -maxSpeed) target.motionX = -maxSpeed;
        if (target.motionY > maxSpeed) target.motionY = maxSpeed;
        if (target.motionY < -maxSpeed) target.motionY = -maxSpeed;
        if (target.motionZ > maxSpeed) target.motionZ = maxSpeed;
        if (target.motionZ < -maxSpeed) target.motionZ = -maxSpeed;

        target.velocityChanged = true;
    }

    /**
     * 对单个目标造成伤害（基于拔刀剑杀敌数）
     */
    private void doOrbDamage(float multiplier, EntityLivingBase target) {
        if (owner == null || bladeStack == null || bladeStack.isEmpty()) return;

        NBTTagCompound tag = ItemSlashBlade.getItemTagCompound(bladeStack);
        int killCount = ItemSlashBlade.KillCount.get(tag);
        float baseDamage = (killCount / 10.0f) * multiplier;
        if (target.getHealth()/5 > baseDamage) baseDamage = target.getHealth()/5;

        Potion viralPotion = ForgeRegistries.POTIONS.getValue(new ResourceLocation("srparasites", "viral"));
        if (viralPotion != null) {
            PotionEffect effect = target.getActivePotionEffect(viralPotion);
            if (effect != null) {
                int amplifier = effect.getAmplifier(); // 0-based
                int virusLevel = amplifier + 1;
                float multipl = 1 + virusLevel; // 例如病毒等级4 => 倍数5
                baseDamage = baseDamage * multiplier;
            }
        }

        float newHealth = target.getHealth() - baseDamage;
        boolean willDie = newHealth <= 0 || Float.isNaN(newHealth);

        if (willDie) {
            target.setHealth(0);
            ItemSlashBlade.updateKillCount(bladeStack, target, owner);
            DamageSource source = DamageSource.causePlayerDamage(owner);
            target.onDeath(source);
        } else {
            target.setHealth(newHealth);
        }
    }

    // ========== 引信计时 & 尺寸扩大（已禁用） ==========
    protected void dyingBurst(boolean fromDeath, int value) {
        int i = this.getSelfeState();
        this.timeSinceIgnited += i * value;
        if (this.timeSinceIgnited < 0) this.timeSinceIgnited = 0;

        if (this.timeSinceIgnited >= this.getFuseState()) {
            this.timeSinceIgnited = this.getFuseState();
            this.selfExplode();
        }
        // 尺寸扩大已被移除，因为球体大小固定为 MAX_SIZE
    }

    // ========== 爆炸阶段 ==========
    protected void selfExplode() {
        this.setSelfeState(2);
        if (this.getSelfeState() == 2) {
            ++this.timerDDD;

            if (this.timerDDD > 1) {
                this.alpha = Math.max(this.alpha - 0.2F, 0.0F);

                // 造成最终爆炸伤害（范围伤害，系数 5.0）
                float range = MAX_SIZE * 1.5F; // 爆炸范围稍大于球体
                AxisAlignedBB bb = new AxisAlignedBB(
                        this.posX - range, this.posY - range, this.posZ - range,
                        this.posX + range, this.posY + range, this.posZ + range
                );
                List<EntityLivingBase> list = this.world.getEntitiesWithinAABB(EntityLivingBase.class, bb,
                        entity -> entity != owner && !entity.isDead && EntitySelectorAttackable.getInstance().apply(entity));
                for (EntityLivingBase target : list) {
                    doOrbDamage(5.0F, target);
                }

                if (this.timerDDD > 5) {
                    this.setDead();
                }
            }
        }
    }

    // ========== NBT 读写 ==========
    @Override
    protected void readEntityFromNBT(NBTTagCompound compound) {
        if (compound.hasKey("OwnerUUID")) {
            UUID uuid = UUID.fromString(compound.getString("OwnerUUID"));
            this.owner = this.world.getPlayerEntityByUUID(uuid);
        }
        NBTTagCompound bladeTag = compound.getCompoundTag("BladeStack");
        this.bladeStack = new ItemStack(bladeTag);
    }

    @Override
    protected void writeEntityToNBT(NBTTagCompound compound) {
        if (this.owner != null) {
            compound.setString("OwnerUUID", this.owner.getUniqueID().toString());
        }
        if (this.bladeStack != null && !this.bladeStack.isEmpty()) {
            NBTTagCompound bladeTag = new NBTTagCompound();
            this.bladeStack.writeToNBT(bladeTag);
            compound.setTag("BladeStack", bladeTag);
        }
    }

    // ========== 碰撞相关 ==========
    @Override
    public void applyEntityCollision(Entity entityIn) {}
    @Override
    public AxisAlignedBB getCollisionBoundingBox() { return null; }
    @Override
    public boolean canBeCollidedWith() { return false; }

    // ========== 客户端辅助 ==========
    @SideOnly(Side.CLIENT)
    public float getSelfeFlashIntensity(float partialTicks) {
        return ((float)this.lastActiveTime + (float)(this.timeSinceIgnited - this.lastActiveTime) * partialTicks * 5.0F) / (float)(this.getFuseState() - 2);
    }

    @Override
    @SideOnly(Side.CLIENT)
    public boolean isInRangeToRenderDist(double distance) {
        return distance < 65536.0D;
    }
}
