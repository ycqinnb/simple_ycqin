package yc.ycqin.nb.gui;

import net.minecraft.client.Minecraft;
import net.minecraftforge.client.event.RenderGameOverlayEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

public class EvolutionHUD {
    public static final EvolutionHUD INSTANCE = new EvolutionHUD();
    private final EvolutionStatBar evolutionBar;

    private EvolutionHUD() {
        this.evolutionBar = new EvolutionStatBar();
    }

    // 监听HUD渲染事件
    @SubscribeEvent
    public void onRenderHUD(RenderGameOverlayEvent.Post event) {
        // 只在文本渲染阶段绘制（确保层级正确）
        if (event.getType() != RenderGameOverlayEvent.ElementType.TEXT) {
            return;
        }

        Minecraft mc = Minecraft.getMinecraft();
        // 玩家不在游戏中时不渲染
        if (mc.player == null || mc.world == null) {
            return;
        }

        // 渲染演化进度条
        if (evolutionBar.shouldBeDisplayed()) {
            evolutionBar.render();
        }
    }

    // Getter
    public EvolutionStatBar getEvolutionBar() {
        return evolutionBar;
    }
}