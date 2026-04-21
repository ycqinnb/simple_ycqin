package yc.ycqin.nb.srpcore;

import net.minecraftforge.fml.relauncher.IFMLLoadingPlugin;

import javax.annotation.Nullable;
import java.util.Map;

@IFMLLoadingPlugin.MCVersion("1.12.2")
@IFMLLoadingPlugin.Name("ycqincore")
@IFMLLoadingPlugin.SortingIndex(1145)
public class ProtectCorePlugin implements IFMLLoadingPlugin {
    public ProtectCorePlugin(){}
    @Override
    public String[] getASMTransformerClass() {
        return new String[]{"yc.ycqin.nb.srpcore.ProtectClassTransformer"};
    }
    @Override public String getModContainerClass() { return null; }
    @Nullable
    @Override public String getSetupClass() { return null; }

    @Override
    public void injectData(Map<String, Object> map) {

    }

    @Override public String getAccessTransformerClass() { return null; }
}