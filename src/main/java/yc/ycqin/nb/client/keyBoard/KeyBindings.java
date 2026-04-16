package yc.ycqin.nb.client.keyBoard;

import net.minecraft.client.settings.KeyBinding;
import net.minecraftforge.fml.client.registry.ClientRegistry;
import org.lwjgl.input.Keyboard;

public class KeyBindings {
    public static KeyBinding isShowEvoBar;

    public static void register() {
        // 实例化 KeyBinding，参数分别是：描述ID，默认按键，分类ID
        isShowEvoBar = new KeyBinding("key.ycqin.toggle.ShowEvoBar", Keyboard.KEY_COMMA, "key.categories.ycqin");
        // 注册到 Forge 系统
        ClientRegistry.registerKeyBinding(isShowEvoBar);
    }
}
