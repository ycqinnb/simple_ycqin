package yc.ycqin.nb.register;

import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.common.registry.EntityRegistry;
import yc.ycqin.nb.common.entity.EntitySlashOrbBoom;
import yc.ycqin.nb.common.entity.EntitySlashOrbVoid;
import yc.ycqin.nb.proxy.CommonProxy;
import yc.ycqin.nb.ycqin;

import static yc.ycqin.nb.ycqin.MODID;

public class EntityRegister {
    public EntityRegister(){
        if (CommonProxy.isSlashBladeLoaded){
            EntityRegistry.registerModEntity(
                    new ResourceLocation(MODID, "textures/entity/orbscary.png"),   // 注册名（资源位置）
                    EntitySlashOrbBoom.class,                        // 实体类
                    "orb_scary",                                 // 实体名称（用于网络协议）
                    114514,                                       // 网络ID（唯一整数）
                    ycqin.instance,                                             // Mod 实例
                    64,                                               // 跟踪范围（单位：区块？通常是半径，建议 64）
                    1,                                                // 更新频率（tick，建议 1）
                    true                                              // 是否发送速度更新
            );
            EntityRegistry.registerModEntity(
                    new ResourceLocation("ycqin", "textures/entity/orbvoid.png"),
                    EntitySlashOrbVoid.class,
                    "orb_void",
                    114515,
                    ycqin.instance,
                    64,
                    1,
                    true
            );
        }
    }
}
