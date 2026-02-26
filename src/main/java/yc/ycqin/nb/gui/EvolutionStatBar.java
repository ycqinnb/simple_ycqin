package yc.ycqin.nb.gui;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.gui.ScaledResolution;
import net.minecraft.util.ResourceLocation;
import yc.ycqin.nb.config.ModConfig;
import yc.ycqin.nb.srpcore.EvolutionBarConfig;
import yc.ycqin.nb.srpcore.EvolutionPhase;

public class EvolutionStatBar extends Gui {
    private EvolutionPhase currentPhase;
    private int currentValue;
    private int maxValue;
    private boolean shouldDisplay;

    // 寄生虫数据
    private boolean useParasiteData = false;
    private int parasitePhase = 0;
    private int parasitePoints = 0;
    private int parasiteNextPoints = 0;
    private int displayPhase = 0; // 用于纹理的阶段

    public EvolutionStatBar() {
        this.currentPhase = EvolutionPhase.PHASE_0;
        this.currentValue = 0;
        this.maxValue = EvolutionBarConfig.PHASE_MAX_VALUES[0];
        this.shouldDisplay = ModConfig.evolutionBarEnabled;
    }

    public void updateData(int phaseNum, int currentValue) {
        this.useParasiteData = false;
        this.currentPhase = EvolutionPhase.getByNumber(phaseNum);
        this.currentValue = currentValue;
        int phaseIndex = phaseNum < 0 ? 0 : Math.min(phaseNum, EvolutionBarConfig.PHASE_MAX_VALUES.length - 1);
        this.maxValue = EvolutionBarConfig.PHASE_MAX_VALUES[phaseIndex];
        checkDisplayCondition();
    }

    public void updateParasiteData(int phase, int points, int nextPoints) {
        this.useParasiteData = true;
        this.parasitePhase = phase;
        this.parasitePoints = points;
        this.parasiteNextPoints = nextPoints;
        this.displayPhase = phase; // 记录用于纹理的阶段
        checkDisplayCondition();
    }

    private void checkDisplayCondition() {
        Minecraft mc = Minecraft.getMinecraft();
        if (!EvolutionBarConfig.SHOW_EVOLUTION_BAR) {
            shouldDisplay = false;
            return;
        }
        if (EvolutionBarConfig.REQUIRE_DIRTY_CLOCK && mc.player != null) {
            shouldDisplay = mc.player.getHeldItemMainhand().getItem().getRegistryName().toString()
                    .equals(EvolutionBarConfig.DIRTY_CLOCK_ITEM_ID);
        } else {
            shouldDisplay = true;
        }
    }

    public int getMovingBarWidth() {
        if (useParasiteData) {
            if (parasiteNextPoints <= 0) return 0;
            double ratio = (double) parasitePoints / parasiteNextPoints;
            if (ratio > 1) ratio = 1;
            return (int) (ratio * EvolutionBarConfig.BAR_WIDTH);
        } else {
            if (maxValue <= 0) return 0;
            double ratio = (double) currentValue / maxValue;
            return (int) (ratio * EvolutionBarConfig.BAR_WIDTH);
        }
    }

    private String getPointsText() {
        if (useParasiteData) {
            return parasitePoints + "P";
        } else {
            return currentValue + "P";
        }
    }

    public void render() {
        if (!shouldDisplay || Minecraft.getMinecraft().gameSettings.showDebugInfo) return;

        Minecraft mc = Minecraft.getMinecraft();
        ScaledResolution scaled = new ScaledResolution(mc);
        int screenHeight = scaled.getScaledHeight();

        int x = ModConfig.evolutionBarPosX;
        int y = ModConfig.evolutionBarPosY;
        if (y == -1) {
            y = screenHeight - ModConfig.evolutionBarHeight - 20; // 底部留20像素边距
        }

        // 确定纹理阶段
        int phaseForTexture = useParasiteData ? displayPhase : currentPhase.getPhaseNum();
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
        int textY = y + (ModConfig.evolutionBarHeight / 2) - 4;
        mc.fontRenderer.drawStringWithShadow(pointsText, textX, textY, 0xFFFFFF);
    }

    public boolean shouldBeDisplayed() { return shouldDisplay; }
    public EvolutionPhase getCurrentPhase() { return currentPhase; }
    public int getCurrentValue() { return currentValue; }
    public int getMaxValue() { return maxValue; }
}