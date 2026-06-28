package yc.ycqin.nb.gui;

import net.minecraft.client.gui.GuiScreen;

public class GuiMirageBlack extends GuiScreen {

    public GuiMirageBlack() {
        this.allowUserInput = false; // 禁止任何按键交互，防止被打断
    }

    @Override
    public void drawScreen(int mouseX, int mouseY, float partialTicks) {
        // 绘制全屏纯黑，遮盖一切内容
        drawRect(0, 0, this.width, this.height, 0xFF000000);
    }

    @Override
    public boolean doesGuiPauseGame() {
        return false; // 不暂停游戏，确保后台加载正常进行
    }
}