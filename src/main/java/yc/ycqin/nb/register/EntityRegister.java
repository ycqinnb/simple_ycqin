package yc.ycqin.nb.register;

import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.common.registry.EntityRegistry;
import net.minecraftforge.fml.common.registry.GameRegistry;
import yc.ycqin.nb.common.entity.EntitySlashOrbBoom;
import yc.ycqin.nb.common.entity.EntitySlashOrbVoid;
import yc.ycqin.nb.common.entity.EntitySummonSlash;
import yc.ycqin.nb.common.entity.tileentity.TileEntityEnergyConverter;
import yc.ycqin.nb.common.entity.tileentity.TileEntityLureActivator;
import yc.ycqin.nb.common.entity.tileentity.TileEntityParasiteCore;
import yc.ycqin.nb.common.entity.tileentity.TileEntityReducerCore;
import yc.ycqin.nb.proxy.CommonProxy;
import yc.ycqin.nb.ycqin;

import static yc.ycqin.nb.ycqin.MODID;

public class EntityRegister {
    public EntityRegister(){
        if (CommonProxy.isSlashBladeLoaded){
            EntityRegistry.registerModEntity(
                    new ResourceLocation(MODID, "orbscary"),   // 注册名（资源位置）
                    EntitySlashOrbBoom.class,                        // 实体类
                    "orb_scary",                                 // 实体名称（用于网络协议）
                    114514,                                       // 网络ID（唯一整数）
                    ycqin.instance,                                             // Mod 实例
                    64,                                               // 跟踪范围（单位：区块？通常是半径，建议 64）
                    1,                                                // 更新频率（tick，建议 1）
                    true                                              // 是否发送速度更新
            );
            EntityRegistry.registerModEntity(
                    new ResourceLocation("ycqin", "orbvoid"),
                    EntitySlashOrbVoid.class,
                    "orb_void",
                    114515,
                    ycqin.instance,
                    64,
                    1,
                    true
            );
        }
        GameRegistry.registerTileEntity(TileEntityParasiteCore.class, new ResourceLocation(ycqin.MODID, "parasite_core"));
        GameRegistry.registerTileEntity(TileEntityLureActivator.class, new ResourceLocation(ycqin.MODID, "lure_activator"));
        GameRegistry.registerTileEntity(TileEntityEnergyConverter.class, new ResourceLocation(ycqin.MODID, "energy_converter"));
        GameRegistry.registerTileEntity(TileEntityReducerCore.class,new ResourceLocation(MODID,"reducer_core"));
        if (CommonProxy.isTCLoaded) {
            EntityRegistry.registerModEntity(
                    new ResourceLocation("ycqin","slashAttack"),
                    EntitySummonSlash.class,
                    "slashAttack",
                    14221,
                    ycqin.instance,
                    64,
                    1,
                    true
            );
        }

    }
}
