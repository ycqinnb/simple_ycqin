package yc.ycqin.nb.mixins;

import zone.rong.mixinbooter.ILateMixinLoader; // 注意导包路径
import java.util.Collections;
import java.util.List;

public class YourLateMixinLoader implements ILateMixinLoader {

    @Override
    public List<String> getMixinConfigs() {
        // 返回一个或多个需要延迟加载的 Mixin 配置文件名
        // 这里对应稍后要创建的 mixins.yourmod.late.json
        return Collections.singletonList("mixins.ycqin.late.json");
    }

    // 可以重写 shouldMixinConfigQueue 方法，在加载前做更精细的判断
    // 比如在这里检查 DE 是否真的存在，如果不存在就不加载配置，避免日志报错
    @Override
    public boolean shouldMixinConfigQueue(String mixinConfig) {
        try {
            // 尝试加载 DE 的一个特征类，如果加载成功说明 DE 存在
            Class.forName("com.brandon3055.draconicevolution.DraconicEvolution");
            return true;
        } catch (ClassNotFoundException e) {
            return false; // DE 没装，就不加载这个配置
        }
    }
}