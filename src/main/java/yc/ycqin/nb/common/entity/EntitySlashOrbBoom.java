package yc.ycqin.nb.common.entity;

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
import net.minecraft.world.World;
import net.minecraftforge.fml.common.registry.ForgeRegistries;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import java.util.List;
import java.util.UUID;

public class EntitySlashOrbBoom extends Entity {
    private PotionEffect effectToApply;
    // ========== 数据同步参数 ==========
    private static final DataParameter<Integer> SELFE = EntityDataManager.createKey(EntitySlashOrbBoom.class, DataSerializers.VARINT);
    private static final DataParameter<Integer> FUSE = EntityDataManager.createKey(EntitySlashOrbBoom.class, DataSerializers.VARINT);
    private static final DataParameter<Integer> WAITSTART = EntityDataManager.createKey(EntitySlashOrbBoom.class, DataSerializers.VARINT);
    // 在类开头添加常量
    private static final float GROWTH_PER_TICK = 0.1F;  // 每 tick 增长量（使最终宽度 ≈ 4.5，视觉直径 ≈ 6）
    private static final float INITIAL_SIZE = 0.5F;
    // ========== 非同步字段 ==========
    private EntityPlayer owner;                 // 攻击者（玩家）
    private ItemStack bladeStack;                // 使用的拔刀剑（用于获取杀敌数、更新杀敌数）
    private double poosX, poosY, poosZ;          // 初始记录位置
    public float alpha = 1.0F;                   // 透明度（客户端渲染用）
    protected int lastActiveTime;                 // 用于客户端闪烁计算（可保留）
    protected int timeSinceIgnited;               // 引信已过时间
    protected int timerDDD;                        // 爆炸阶段计时器

    // ========== 新增字段：用于渲染动画（兼容寄生虫渲染器） ==========
    public float prevRenderYawOffset;
    public float renderYawOffset;
    public float prevRotationYawHead;
    public float rotationYawHead;
    public float prevLimbSwingAmount;
    public float limbSwingAmount;
    public float limbSwing;
    public int hurtTime;
    public int deathTime;   // 用于死亡旋转效果
    public void setEffectToApply(PotionEffect effect) {
        this.effectToApply = effect;
    }
    // ========== 构造方法 ==========
    public EntitySlashOrbBoom(World worldIn) {
        super(worldIn);
        this.setSize(INITIAL_SIZE, INITIAL_SIZE); // 初始 0.5
        this.isImmuneToFire = true;               // 免疫火焰
        this.noClip = true;                        // 无碰撞
        this.lastActiveTime = 0;
        this.timeSinceIgnited = 0;
        this.timerDDD = 0;
        this.setFuseState(40);                     // 默认引信 40 ticks
        this.setStartState(20);                     // 默认等待 20 ticks

        // 初始化新增字段（避免使用默认值造成未知问题）
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

    /**
     * 供拔刀剑 SA 调用的构造方法
     * @param worldIn     世界
     * @param owner       攻击者玩家
     * @param blade       使用的拔刀剑物品栈
     * @param fuse        从激活到爆炸的 ticks（建议 30~60）
     * @param waitStart   生成后等待 ticks 才开始激活（建议 0~20，0 则立刻激活）
     */
    public EntitySlashOrbBoom(World worldIn, EntityPlayer owner, ItemStack blade, int fuse, int waitStart) {
        this(worldIn);
        this.owner = owner;
        // 复制一份物品栈，避免外部修改影响内部数据（杀敌数等）
        this.bladeStack = blade;
        this.setFuseState(fuse);
        this.setStartState(waitStart);
    }

    // ========== 数据管理器初始化 ==========
    @Override
    protected void entityInit() {
        this.dataManager.register(SELFE, -1);
        this.dataManager.register(FUSE, 40);
        this.dataManager.register(WAITSTART, 20);
    }

    // ========== 状态读写 ==========
    public int getSelfeState() {
        return this.dataManager.get(SELFE);
    }
    public void setSelfeState(int state) {
        this.dataManager.set(SELFE, state);
    }

    public int getFuseState() {
        return this.dataManager.get(FUSE);
    }
    public void setFuseState(int state) {
        this.dataManager.set(FUSE, state);
    }

    public int getStartState() {
        return this.dataManager.get(WAITSTART);
    }
    public void setStartState(int state) {
        this.dataManager.set(WAITSTART, state);
    }

    // ========== 每 tick 更新 ==========
    @Override
    public void onUpdate() {
        super.onUpdate();

        if (this.world.isRemote) {
            // 客户端：生成特效
            this.spawnOrbEffects(4);
            // 可选：更新 owner 动画（如果需要球体有旋转效果，可以复制 owner 的旋转，但通常不需要）
            return;
        }

        // 服务端逻辑
        if (this.ticksExisted > this.getStartState()) {
            // 激活阶段
            this.setSelfeState(1);
            this.orbDoing();                // 周期性伤害
            this.dyingBurst(true, 1);        // 引信计时，并扩大尺寸

            // 位置固定在记录点（poosX/Y/Z 在等待阶段已记录）
            this.setPosition(this.poosX, this.poosY - this.rand.nextDouble() * 0.1, this.poosZ);
        } else {
            // 等待阶段：记录初始位置
            this.poosX = this.posX;
            this.poosY = this.posY;
            this.poosZ = this.posZ;
        }
    }

    // ========== 周期性伤害 ==========
    private void orbDoing() {
        // 每 10 tick 造成一次伤害（系数 1.0）
        if (this.ticksExisted % 10 == 0) {
            this.doOrbDamage(1.0F);
        }
    }

    // ========== 引信计时 & 尺寸扩大 ==========
    protected void dyingBurst(boolean fromDeath, int value) {
        int i = this.getSelfeState();
        this.timeSinceIgnited += i * value;
        if (this.timeSinceIgnited < 0) {
            this.timeSinceIgnited = 0;
        }

        if (this.timeSinceIgnited >= this.getFuseState()) {
            this.timeSinceIgnited = this.getFuseState();
            this.selfExplode();                     // 达到引信，触发爆炸
        } else {
            // 逐渐扩大尺寸
            this.setSize(this.width + GROWTH_PER_TICK, this.height + GROWTH_PER_TICK);
        }
    }

    // ========== 爆炸阶段 ==========
    protected void selfExplode() {
        this.setSelfeState(2);
        if (this.getSelfeState() == 2) {
            ++this.timerDDD;
            if (this.timerDDD > 1) {
                this.alpha = Math.max(this.alpha - 0.2F, 0.0F);
                this.doOrbDamage(5.0F);

                // 爆炸粒子（服务端和客户端都执行，但粒子只在客户端显示）
                if (this.world.isRemote) {
                    // 生成多个爆炸粒子，模拟大爆炸
                    for (int i = 0; i < 8; i++) {
                        double offsetX = (this.rand.nextDouble() - 0.5) * 2.0;
                        double offsetY = (this.rand.nextDouble() - 0.5) * 2.0;
                        double offsetZ = (this.rand.nextDouble() - 0.5) * 2.0;
                        this.world.spawnParticle(EnumParticleTypes.EXPLOSION_LARGE,
                                this.posX + offsetX, this.posY + offsetY, this.posZ + offsetZ,
                                0.0D, 0.0D, 0.0D);
                    }
                    this.world.spawnParticle(EnumParticleTypes.EXPLOSION_HUGE,
                            this.posX, this.posY, this.posZ, 0.0D, 0.0D, 0.0D);
                }
                if (this.timerDDD > 5) {
                    this.setDead();
                }
            }
        }
    }

    private void doOrbDamage(float multiplier) {
        if (this.owner == null || this.bladeStack == null || this.bladeStack.isEmpty()) return;
        if (this.world.isRemote) return;

        // 从拔刀剑获取杀敌数
        NBTTagCompound tag = ItemSlashBlade.getItemTagCompound(this.bladeStack);
        int killCount = ItemSlashBlade.KillCount.get(tag);
        float baseDamage = (killCount / 10.0f) * multiplier;

        float range = 6.0F; // 攻击半径（可调整）
        AxisAlignedBB bb = new AxisAlignedBB(
                this.posX - range, this.posY - range, this.posZ - range,
                this.posX + range, this.posY + range, this.posZ + range
        );

        // 获取范围内所有实体（排除主人自己）
        List<Entity> allEntities = this.world.getEntitiesWithinAABBExcludingEntity(this.owner, bb);
        for (Entity entity : allEntities) {
            if (!(entity instanceof EntityLivingBase)) continue;
            EntityLivingBase target = (EntityLivingBase) entity;
            if (!target.isEntityAlive()) continue;

            // 判断是否为敌对生物（使用原选择器）
            boolean isHostile = EntitySelectorAttackable.getInstance().apply(target);

            // 施加效果给所有非主人生物（无论是否敌对）
            if (target != this.owner && this.effectToApply != null) {
                target.addPotionEffect(new PotionEffect(this.effectToApply));
            }

            // 仅对敌对生物造成伤害
            if (isHostile) {
                float actualDamage = baseDamage;
                // 保底伤害：至少为目标当前生命值的1/5（根据您原来的逻辑）
                if (target.getHealth() / 5 > actualDamage) {
                    actualDamage = target.getHealth() / 5;
                }

                Potion viralPotion = ForgeRegistries.POTIONS.getValue(new ResourceLocation("srparasites", "viral"));
                if (viralPotion != null) {
                    PotionEffect effect = target.getActivePotionEffect(viralPotion);
                    if (effect != null) {
                        int amplifier = effect.getAmplifier(); // 0-based
                        int virusLevel = amplifier + 1;
                        float multipl = 1 + virusLevel; // 例如病毒等级4 => 倍数5
                        actualDamage = actualDamage * multiplier;
                    }
                }

                float newHealth = target.getHealth() - actualDamage;
                boolean willDie = newHealth <= 0;

                if (willDie) {
                    target.setHealth(0);
                    ItemSlashBlade.updateKillCount(this.bladeStack, target, this.owner);
                    DamageSource source = DamageSource.causePlayerDamage(this.owner);
                    target.onDeath(source);
                } else {
                    target.setHealth(newHealth);
                }
            }
        }
    }

    // ========== NBT 读写（用于保存 owner 和 bladeStack） ==========
    @Override
    protected void readEntityFromNBT(NBTTagCompound compound) {
        // 读取 owner UUID
        if (compound.hasKey("OwnerUUID")) {
            UUID uuid = UUID.fromString(compound.getString("OwnerUUID"));
            this.owner = this.world.getPlayerEntityByUUID(uuid);
        }
        // 读取 bladeStack
        NBTTagCompound bladeTag = compound.getCompoundTag("BladeStack");
        this.bladeStack = new ItemStack(bladeTag);
    }

    @Override
    protected void writeEntityToNBT(NBTTagCompound compound) {
        // 写入 owner UUID
        if (this.owner != null) {
            compound.setString("OwnerUUID", this.owner.getUniqueID().toString());
        }
        // 写入 bladeStack
        if (this.bladeStack != null && !this.bladeStack.isEmpty()) {
            NBTTagCompound bladeTag = new NBTTagCompound();
            this.bladeStack.writeToNBT(bladeTag);
            compound.setTag("BladeStack", bladeTag);
        }
    }

    // ========== 碰撞相关（无碰撞） ==========
    @Override
    public void applyEntityCollision(Entity entityIn) {}

    @Override
    public AxisAlignedBB getCollisionBoundingBox() {
        return new AxisAlignedBB(0.0D, 0.0D, 0.0D, 0.0D, 0.0D, 0.0D);
    }

    @Override
    public boolean canBeCollidedWith() {
        return false;
    }

    // ========== 客户端特效 ==========
    @SideOnly(Side.CLIENT)
    public void spawnOrbEffects(int cap1) {

    }

    @SideOnly(Side.CLIENT)
    public float getSelfeFlashIntensity(float partialTicks) {
        // 用于渲染时闪烁计算（保留原逻辑）
        return ((float)this.lastActiveTime + (float)(this.timeSinceIgnited - this.lastActiveTime) * partialTicks * 5.0F) / (float)(this.getFuseState() - 2);
    }

    // 可选：重写渲染距离
    @Override
    @SideOnly(Side.CLIENT)
    public boolean isInRangeToRenderDist(double distance) {
        return distance < 65536.0D;
    }
}