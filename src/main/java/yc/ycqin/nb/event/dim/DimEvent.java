package yc.ycqin.nb.event.dim;

import net.minecraft.entity.item.EntityItem;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.Teleporter;
import net.minecraft.world.WorldServer;
import net.minecraftforge.event.entity.EntityJoinWorldEvent;
import net.minecraftforge.event.entity.EntityTravelToDimensionEvent;
import net.minecraftforge.event.entity.living.LivingDropsEvent;
import net.minecraftforge.event.entity.player.EntityItemPickupEvent;
import net.minecraftforge.event.world.BlockEvent;
import net.minecraftforge.event.world.ExplosionEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.PlayerEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;
import yc.ycqin.nb.common.dim.MirageManager;
import yc.ycqin.nb.common.dim.MirageTags;
import yc.ycqin.nb.common.dim.TeleporterDirect;
import yc.ycqin.nb.config.ModConfig;
import yc.ycqin.nb.register.DimRegister;

public class DimEvent {
    private static final int SCAN_INTERVAL = 20; // 每20 tick检查一次

    @SubscribeEvent
    public void onTravelToDimension(EntityTravelToDimensionEvent event) {
        if (event.getEntity() instanceof EntityPlayerMP) {
            EntityPlayerMP player = (EntityPlayerMP) event.getEntity();
            if (!ModConfig.mirageEnabled) return;
            if (!player.getEntityData().getBoolean("inMirage")) return;

            int targetDim = event.getDimension();
            if (targetDim == DimRegister.ycdim.getId()) {
                player.getEntityData().removeTag("inMirage");
                return;
            }

            if (!MirageManager.isMirageDimension(targetDim)) {
                int mirageTarget = MirageManager.getMirageDim(targetDim);
                if (MirageManager.isProcessing(player)) return;

                event.setCanceled(true);
                MirageManager.ensureMirageWorld(player.mcServer, targetDim);
                WorldServer mirageWorld = player.mcServer.getWorld(mirageTarget);
                if (mirageWorld == null) return;

                int originalDim = MirageManager.getOriginalDim(mirageTarget);
                BlockPos destination = (originalDim == 1) ? new BlockPos(100, 49, 0) : player.getPosition();
                try {
                    MirageManager.setProcessing(player, true);
                    player.changeDimension(mirageTarget, new TeleporterDirect(mirageWorld, destination, originalDim));
                } finally {
                    MirageManager.setProcessing(player, false);
                }
            }
        }
    }

    // 2. 生物死亡掉落（包括玩家击杀）
    @SubscribeEvent
    public void onLivingDrops(LivingDropsEvent event) {
        if (!ModConfig.mirageCleanItems) return;
        // 在幻境中，所有掉落物直接打标签
        if (event.getEntity().world.isRemote) return;
        boolean isMirage = MirageManager.isMirageDimension(event.getEntity().dimension);
        if (isMirage) {
            for (EntityItem item : event.getDrops()) {
                ItemStack stack = item.getItem();
                if (!stack.isEmpty() && !MirageTags.isMirageItem(stack.getTagCompound())) {
                    stack.setTagCompound(MirageTags.markMirage(stack.getTagCompound()));
                }
            }
        }
    }

    // 3. 方块破坏掉落（HarvestDropsEvent 发生于服务端）
    @SubscribeEvent
    public void onBlockHarvest(BlockEvent.HarvestDropsEvent event) {
        if (!ModConfig.mirageCleanItems) return;
        if (event.getWorld().isRemote) return;
        boolean isMirage = MirageManager.isMirageDimension(event.getWorld().provider.getDimension());
        if (isMirage) {
            for (ItemStack stack : event.getDrops()) {
                if (!stack.isEmpty() && !MirageTags.isMirageItem(stack.getTagCompound())) {
                    stack.setTagCompound(MirageTags.markMirage(stack.getTagCompound()));
                }
            }
        }
    }

    // 4. 爆炸产生的掉落物
    @SubscribeEvent
    public void onExplosionDrop(ExplosionEvent.Detonate event) {
        if (!ModConfig.mirageCleanItems) return;
        if (event.getWorld().isRemote) return;
        boolean isMirage = MirageManager.isMirageDimension(event.getWorld().provider.getDimension());
        if (isMirage) {
            for (EntityItem item : event.getAffectedEntities().stream()
                    .filter(e -> e instanceof EntityItem).map(e -> (EntityItem) e).toArray(EntityItem[]::new)) {
                ItemStack stack = item.getItem();
                if (!stack.isEmpty() && !MirageTags.isMirageItem(stack.getTagCompound())) {
                    stack.setTagCompound(MirageTags.markMirage(stack.getTagCompound()));
                }
            }
        }
    }

    // 熔炼产物
    @SubscribeEvent
    public void onSmelt(PlayerEvent.ItemSmeltedEvent event) {
        if (!ModConfig.mirageCleanItems) return;
        if (MirageManager.isMirageDimension(event.player.dimension)) {
            // 原版熔炉只能从单格输入，熔炼产物自动带标签
            MirageTags.markMirage(event.smelting);
        }
    }

    // 合成产物（核心）
    @SubscribeEvent
    public void onCraft(PlayerEvent.ItemCraftedEvent event) {
        if (!ModConfig.mirageCleanItems) return;
        if (MirageManager.isMirageDimension(event.player.dimension)) {
            // 检查合成矩阵中是否有幻境物品
            boolean hasMirage = false;
            for (int i = 0; i < event.craftMatrix.getSizeInventory(); i++) {
                ItemStack ingredient = event.craftMatrix.getStackInSlot(i);
                if (MirageTags.isMirage(ingredient)) {
                    hasMirage = true;
                    break;
                }
            }
            if (hasMirage) {
                MirageTags.markMirage(event.crafting);
            }
        }
    }


    @SubscribeEvent
    public void onPlayerTick(TickEvent.PlayerTickEvent event) {
        if (event.phase != TickEvent.Phase.END || event.player.world.isRemote) return;
        if (!ModConfig.mirageEnabled) return;

        EntityPlayer player = event.player;
        if (player.ticksExisted % SCAN_INTERVAL != 0) return;

        boolean inMirageDim = MirageManager.isMirageDimension(player.dimension);

        // 自动纠正 inMirage 标签：维度与实际标签不符时同步
        boolean hasMirageTag = player.getEntityData().getBoolean("inMirage");
        if (inMirageDim && !hasMirageTag) {
            // 玩家在幻境内但丢失了标签，补回
            player.getEntityData().setBoolean("inMirage", true);
        } else if (!inMirageDim && hasMirageTag) {
            // 玩家已离开幻境但标签残留，清除
            player.getEntityData().removeTag("inMirage");
        }

        // 清理物品（根据配置）
        if (!ModConfig.mirageCleanItems) return;
        if (!inMirageDim) {
            cleanMirageItems(player);
        }
    }

    // 维度变化时立即清理一次
    @SubscribeEvent
    public void onDimensionChange(PlayerEvent.PlayerChangedDimensionEvent event) {
        if (!ModConfig.mirageCleanItems) return;
        EntityPlayer player = event.player;
        if (player.world.isRemote) return;
        if (!MirageManager.isMirageDimension(player.dimension)) {
            cleanMirageItems(player);
        }
    }

    // 玩家登录时也检查（防止异常退出导致物品残留）
    @SubscribeEvent
    public void onPlayerLogin(PlayerEvent.PlayerLoggedInEvent event) {
        if (!ModConfig.mirageCleanItems) return;
        EntityPlayer player = event.player;
        if (player.world.isRemote) return;
        if (!MirageManager.isMirageDimension(player.dimension)) {
            cleanMirageItems(player);
        }
    }

    private void cleanMirageItems(EntityPlayer player) {
        if (!ModConfig.mirageCleanItems) return;
        cleanInventory(player.inventory.mainInventory);
        cleanInventory(player.inventory.armorInventory);
        cleanInventory(player.inventory.offHandInventory);
    }

    private void cleanInventory(java.util.List<ItemStack> inv) {
        for (int i = 0; i < inv.size(); i++) {
            ItemStack stack = inv.get(i);
            if (!stack.isEmpty() && MirageTags.isMirageItem(stack.getTagCompound())) {
                inv.set(i, ItemStack.EMPTY);
            }
        }
    }
}
