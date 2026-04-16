package yc.ycqin.nb.gui;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.gui.ScaledResolution;
import net.minecraft.util.ResourceLocation;
import yc.ycqin.nb.config.ModConfig;
import yc.ycqin.nb.srpcore.EvolutionPhase;

public class EvolutionStatBar extends Gui {
    private int parasitePoints = 0;
    private int parasiteNextPoints = 0;
    private int displayPhase = 0; // 用于纹理的阶段

    public EvolutionStatBar() {
        this.displayPhase = 0;
    }

    public void updateParasiteData(int phase, int points, int nextPoints) {
        this.parasitePoints = points;
        this.parasiteNextPoints = nextPoints;
        this.displayPhase = phase; // 记录用于纹理的阶段

    }

    public int getMovingBarWidth() {
        if (parasiteNextPoints <= 0) return 0;
        double ratio = (double) parasitePoints / parasiteNextPoints;
        if (ratio > 1) ratio = 1;
        return (int) (ratio * ModConfig.evolutionBarWidth);
    }

    private String getPointsText() {
        return parasitePoints + "P";
    }

    public void render() {
        if (!ModConfig.evolutionBarEnabled || Minecraft.getMinecraft().gameSettings.showDebugInfo) return;

        Minecraft mc = Minecraft.getMinecraft();
        ScaledResolution scaled = new ScaledResolution(mc);
        int screenHeight = scaled.getScaledHeight();

        int x = ModConfig.evolutionBarPosX;
        int y = ModConfig.evolutionBarPosY;
        if (y == -1) {
            y = screenHeight - ModConfig.evolutionBarHeight - 20; // 底部留20像素边距
        }

        int phaseForTexture = displayPhase;
        EvolutionPhase phaseEnum = EvolutionPhase.getByNumber(phaseForTexture);
        ResourceLocation texture = new ResourceLocation(phaseEnum.getTexturePath());

        mc.getTextureManager().bindTexture(texture);

        // 绘制背景条
        drawTexturedModalRect(x, y, 0, 0, ModConfig.evolutionBarWidth, ModConfig.evolutionBarHeight);

        // 绘制移动条
        int movingWidth = getMovingBarWidth();
        if (movingWidth > 0) {
            drawTexturedModalRect(x + ModConfig.evolutionBarMovingOffsetX,
                    y + ModConfig.evolutionBarMovingOffsetY,
                    ModConfig.evolutionBarMovingU,
                    ModConfig.evolutionBarMovingV,
                    movingWidth,
                    ModConfig.evolutionBarHeight);
        }

        // 绘制右侧点数
        String pointsText = getPointsText();
        int textX = x + ModConfig.evolutionBarWidth + 5;
        int textY = y + (ModConfig.evolutionBarHeight / 2) - 2;
        mc.fontRenderer.drawStringWithShadow(pointsText, textX, textY, 0xFFFFFF);
    }

    public boolean shouldBeDisplayed() { return ModConfig.evolutionBarEnabled; }
}