package yc.ycqin.nb.common.trait;

import com.google.common.collect.ImmutableList;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityList;
import net.minecraft.entity.EntityLiving;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import slimeknights.tconstruct.library.modifiers.IModifierDisplay;
import slimeknights.tconstruct.library.modifiers.ModifierNBT;
import slimeknights.tconstruct.library.modifiers.ModifierTrait;
import slimeknights.tconstruct.library.traits.AbstractTrait;
import yc.ycqin.nb.common.entity.EntitySummonSlash;
import yc.ycqin.nb.proxy.CommonProxy;

import java.util.List;

public class TraitbeckonStrengthen extends ModifierTrait {
    public TraitbeckonStrengthen() {
        super("trait_beckon_strengthen", 0x7f7f7f,4,16);
    }

    @Override
    public void onHit(ItemStack tool, EntityLivingBase player, EntityLivingBase target,
                      float damage, boolean wasCritical) {
        if (player.world.isRemote) return;
        if (((EntityPlayer)player).getCooledAttackStrength(1.0F) < 0.95F) {
            return;
        }
        ModifierNBT.IntegerNBT data = getData(tool);
        int level = data.level;
        if (level <= 0) return;

        World world = player.world;
        // 目标背后的基准点（面向反方向 1.5 格）
        Vec3d lookVec = target.getLookVec();
        Vec3d behind = target.getPositionVector().subtract(lookVec.scale(1.5));
        behind = new Vec3d(behind.x, target.posY + target.height * 0.5, behind.z);

        float extraDamage = damage * 0.5f;
        double radius = 1.2;          // 环形半径
        double angleStep = 2 * Math.PI / level;  // 每个实体之间的角度差

        for (int i = 0; i < level; i++) {
            // 计算环形位置（以 behind 为中心，半径 radius）
            double angle = i * angleStep;
            double xOffset = Math.cos(angle) * radius;
            double zOffset = Math.sin(angle) * radius;
            Vec3d spawnPos = behind.add(new Vec3d(xOffset, 0, zOffset));

            // 生成 rupter 实体
            Entity rupter = EntityList.createEntityByIDFromName(
                    new ResourceLocation("srparasites", "rupter"), world);
            if (rupter != null) {
                rupter.setPosition(spawnPos.x, spawnPos.y, spawnPos.z);
                if (rupter instanceof EntityLiving) {
                    ((EntityLiving) rupter).setNoAI(true);
                }
                world.spawnEntity(rupter);
            }

            // 生成挥砍实体，传入玩家主手物品用于渲染
            EntitySummonSlash slash = new EntitySummonSlash(world, target, (EntityPlayer) player,
                    rupter, extraDamage, player.getHeldItemMainhand());
            slash.setPosition(spawnPos.x, spawnPos.y, spawnPos.z);
            world.spawnEntity(slash);
        }
    }
}
