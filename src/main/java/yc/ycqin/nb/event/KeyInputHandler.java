package yc.ycqin.nb.event;

import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.InputEvent;
import yc.ycqin.nb.client.keyBoard.KeyBindings;
import yc.ycqin.nb.config.ModConfig;

public class KeyInputHandler {
    @SubscribeEvent
    public void onKeyInput(InputEvent.KeyInputEvent event) {
        // 检查我们自定义的按键是否被按下

        if (KeyBindings.isShowEvoBar.isPressed()) {
            ModConfig.evolutionBarEnabled = !ModConfig.evolutionBarEnabled;
        }
    }
}
