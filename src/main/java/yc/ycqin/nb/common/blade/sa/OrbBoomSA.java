package yc.ycqin.nb.common.blade.sa;

import mods.flammpfeil.slashblade.ability.StylishRankManager;
import mods.flammpfeil.slashblade.entity.selector.EntitySelectorAttackable;
import mods.flammpfeil.slashblade.item.ItemSlashBlade;
import mods.flammpfeil.slashblade.specialattack.IJustSpecialAttack;
import mods.flammpfeil.slashblade.specialattack.ISuperSpecialAttack;
import mods.flammpfeil.slashblade.specialattack.SpecialAttackBase;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.SoundEvents;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.potion.PotionEffect;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import yc.ycqin.nb.common.entity.EntitySlashOrbBoom;
import yc.ycqin.nb.common.entity.EntitySlashOrbVoid;

import java.util.List;

import static com.dhanantry.scapeandrunparasites.init.SRPPotions.COTH_E;

public class OrbBoomSA extends SpecialAttackBase implements ISuperSpecialAttack, IJustSpecialAttack {

    private static final String SA_NAME = "orb_boom";
    private static final int COST = 20; // 消耗的荣耀值
    private static final int DAMAGE_ON_NO_SOUL = 10; // 荣耀值不足时扣除的耐久

    @Override
    public String toString() {
        return SA_NAME;
    }

    @Override
    public void doSuperSpecialAttack(ItemStack stack, EntityPlayer player) {
        World world = player.world;
        NBTTagCompound tag = ItemSlashBlade.getItemTagCompound(stack);

        // 消耗荣耀值或耐久
        if (!ItemSlashBlade.ProudSoul.tryAdd(tag, -COST, false)) {
            ItemSlashBlade.damageItem(stack, DAMAGE_ON_NO_SOUL, player);
        }

        // 设置连击序列
        ItemSlashBlade.setComboSequence(tag, ItemSlashBlade.ComboSequence.SlashDim);

        // 播放音效
        world.playSound(null, player.posX, player.posY, player.posZ,
                SoundEvents.ENTITY_WITHER_SHOOT, SoundCategory.PLAYERS, 0.5F, 1.0F);

        // 只在服务端生成实体
        if (!world.isRemote) {
            // 设置 orb 参数
            int fuse = 100;       // 引信时间（激活到爆炸）
            int waitStart = 10;  // 等待时间

            // 创建 EntitySlashOrbVoid 实体
            EntitySlashOrbVoid orb = new EntitySlashOrbVoid(world, player, stack, fuse, waitStart);

            // 设置生成位置：玩家头顶上方3格
            // 玩家脚部位置 + (0, 玩家身高 + 3, 0)
            double posX = player.posX;
            double posY = player.posY + player.height + 3.0;  // player.height 是玩家站立高度（约1.8）
            double posZ = player.posZ;
            orb.setPosition(posX, posY, posZ);

            // 将实体加入世界
            world.spawnEntity(orb);

            // 增加评级分数
            StylishRankManager.setNextAttackType(player, StylishRankManager.AttackTypes.SlashDim);
        }
    }

    @Override
    public void doJustSpacialAttack(ItemStack stack, EntityPlayer player) {
        doSpacialAttack(stack, player);
    }

    /**
     * 核心攻击逻辑
     */
    public void doSpacialAttack(ItemStack stack, EntityPlayer player) {
        World world = player.world;
        NBTTagCompound tag = ItemSlashBlade.getItemTagCompound(stack);
        if (player.isPotionActive(COTH_E)) {
            player.removePotionEffect(COTH_E);
        }
        // 获取当前锁定的目标
        Entity target = null;
        int entityId = ItemSlashBlade.TargetEntityId.get(tag);
        if (entityId != 0) {
            Entity tmp = world.getEntityByID(entityId);
            if (tmp != null && tmp.getDistance(player) < 30.0F) {
                target = tmp;
            }
        }

        // 如果没有锁定目标，尝试获取最近的可攻击目标
        if (target == null) {
            target = getEntityToWatch(player);
        }

        // 消耗荣耀值或耐久
        if (!ItemSlashBlade.ProudSoul.tryAdd(tag, -COST, false)) {
            ItemSlashBlade.damageItem(stack, DAMAGE_ON_NO_SOUL, player);
        }

        // 设置连击序列（用于动画和评级）
        ItemSlashBlade.setComboSequence(tag, ItemSlashBlade.ComboSequence.SlashDim); // 使用次元斩的序列，可以根据需要修改

        // 播放音效
        world.playSound(null, player.posX, player.posY, player.posZ,
                SoundEvents.ENTITY_WITHER_SHOOT, SoundCategory.PLAYERS, 0.5F, 1.0F);

        // 只在服务端生成实体
        if (!world.isRemote) {
            // 设置 orb 参数：引信时长（从激活到爆炸的 ticks）和等待时长（生成后多久开始激活）
            int fuse = 40;       // 激活后 40 ticks 爆炸
            int waitStart = 10;  // 生成后等待 10 ticks 才开始激活（可以设为 0 让 orb 立即激活）

            // 创建 orb 实体
            EntitySlashOrbBoom orb = new EntitySlashOrbBoom(world, player, stack, fuse, waitStart);
            orb.setEffectToApply(new PotionEffect(COTH_E, 100, 2));
            // 设置 orb 的生成位置
            if (target == null) {
                // 方式1：如果没有目标，在玩家前方 5 格生成
                Vec3d look = player.getLookVec().normalize().scale(5.0);
                Vec3d pos = player.getPositionEyes(1.0F).add(look);
                orb.setPosition(pos.x, pos.y, pos.z);
            } else {
                // 方式2：如果有目标，在目标位置生成（在目标中心）
                orb.setPosition(target.posX, target.posY + target.height / 2.0, target.posZ);
            }

            // 将实体加入世界
            world.spawnEntity(orb);

            // 增加评级分数（可选）
            StylishRankManager.setNextAttackType(player, StylishRankManager.AttackTypes.SlashDim);
        }
    }

    /**
     * 获取玩家正在注视的目标
     */
    private Entity getEntityToWatch(EntityPlayer player) {
        World world = player.world;
        Entity target = null;

        // 从近到远扫描
        for (int dist = 2; dist < 20; dist += 2) {
            AxisAlignedBB bb = player.getEntityBoundingBox();
            Vec3d vec = player.getLookVec();
            vec = vec.normalize();
            bb = bb.grow(2.0F, 0.25F, 2.0F);
            bb = bb.offset(vec.x * dist, vec.y * dist, vec.z * dist);

            List<Entity> list = world.getEntitiesInAABBexcluding(player, bb, EntitySelectorAttackable.getInstance());
            float distance = 30.0F;

            for (Entity curEntity : list) {
                float curDist = curEntity.getDistance(player);
                if (curDist < distance) {
                    target = curEntity;
                    distance = curDist;
                }
            }

            if (target != null) {
                break;
            }
        }

        return target;
    }
}