package yc.ycqin.nb.common.blade.se;


import mods.flammpfeil.slashblade.entity.selector.EntitySelectorAttackable;
import mods.flammpfeil.slashblade.item.ItemSlashBlade;
import mods.flammpfeil.slashblade.specialeffect.ISpecialEffect;
import mods.flammpfeil.slashblade.specialeffect.SpecialEffects;
import mods.flammpfeil.slashblade.util.SlashBladeEvent;
import mods.flammpfeil.slashblade.util.SlashBladeHooks;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.EntityEquipmentSlot;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.DamageSource;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.Style;
import net.minecraft.util.text.TextComponentString;
import net.minecraft.world.World;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import yc.ycqin.nb.common.trait.armorTrait.TraitMinDamageProtect;
import yc.ycqin.nb.proxy.CommonProxy;
import yc.ycqin.nb.register.ModEnchantments;

import java.util.Collections;
import java.util.Iterator;
import java.util.List;


public class MinDamageSe implements ISpecialEffect {
    private static final String KEY = "MinDamageSe";

    @Override
    public int getDefaultRequiredLevel() {
        return 0; // 10级可用
    }

    @Override
    public String getEffectKey() {
        return KEY;
    }

    @Override
    public void register() {
        SlashBladeHooks.EventBus.register(this);
    }

    private void aoeAttack(EntityPlayer player, ItemStack blade) {
        World world = player.world;
        if (world.isRemote) return;

        NBTTagCompound tag = ItemSlashBlade.getItemTagCompound(blade);
        int killCount = ItemSlashBlade.KillCount.get(tag);
        float damage = killCount / 10.0f;

        for (int dist = 2; dist < 14; dist += 2) {
            AxisAlignedBB bb = player.getEntityBoundingBox()
                    .grow(2.0, 0.25, 2.0)
                    .offset(player.getLookVec().normalize().scale(dist));

            for (Entity entity : world.getEntitiesInAABBexcluding(player, bb, EntitySelectorAttackable.getInstance())) {
                if (!(entity instanceof EntityLivingBase)) continue;
                EntityLivingBase target = (EntityLivingBase) entity;
                if (!target.isEntityAlive()) continue;

                if (target instanceof EntityPlayer) {
                    EntityPlayer playerTarget = (EntityPlayer) target;
                    ItemStack chest = playerTarget.getItemStackFromSlot(EntityEquipmentSlot.CHEST);
                    int level;
                    if (CommonProxy.isTCArmorLoaded){
                        level = EnchantmentHelper.getEnchantmentLevel(ModEnchantments.MIN_DAMAGE_PROTECT, chest) + TraitMinDamageProtect.getTotalProtectionLevel(target);
                    } else {
                        level = EnchantmentHelper.getEnchantmentLevel(ModEnchantments.MIN_DAMAGE_PROTECT, chest);
                    }

                    if (level > 0) {
                        damage = Math.max(0, damage - level); // 每级减少1点
                    }
                }

                float newHealth = target.getHealth() - damage;
                boolean willDie = newHealth <= 0;

                if (willDie) {
                    // 1. 使目标死亡
                    target.setHealth(0);

                    // 2. 通知拔刀剑：这把刀击杀了目标（增加杀敌数、耀魂值等）
                    ItemSlashBlade.updateKillCount(blade, target, player);

                    // 3. 触发正常死亡事件（让其他模组也能处理）
                    DamageSource source = DamageSource.causePlayerDamage(player);
                    target.onDeath(source);
                } else {
                    target.setHealth(newHealth);
                }
            }
        }
    }

    @SubscribeEvent
    public void onImpact(SlashBladeEvent.ImpactEffectEvent event) {
        ItemStack blade = event.blade;
        EntityLivingBase user = event.user;
        EntityPlayer player = (EntityPlayer) user;
        aoeAttack(player, blade);
    }

}
