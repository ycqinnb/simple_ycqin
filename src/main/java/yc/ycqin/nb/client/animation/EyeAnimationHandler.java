package yc.ycqin.nb.client.animation;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.client.event.RenderGameOverlayEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import org.lwjgl.opengl.GL11;
import yc.ycqin.nb.event.dim.EyeRenderHandler;
import yc.ycqin.nb.network.PacketEyeTrigger;
import yc.ycqin.nb.register.NetworkRegister;

@SideOnly(Side.CLIENT)
public class EyeAnimationHandler {

    public enum Stage {
        IDLE,
        INITIAL_HOLD,   // 红底 + 全屏眼睛，极短停留
        ZOOM_PUPIL,     // 瞳孔放大
        HOLD_BLACK,     // 纯黑停留，等待传送完成
        DONE
    }

    private static Stage currentStage = Stage.IDLE;
    private static long stageStartTime = 0L;

    private static final long INITIAL_HOLD_MS = 100;
    private static final long ZOOM_DURATION_MS = 1200;

    private static final ResourceLocation EYE_TEX = new ResourceLocation("ycqin", "textures/environment/giant_eye.png");

    // 由外部（如凝视触发）调用
    public static void startAnimation() {
        if (currentStage != Stage.IDLE) return;
        currentStage = Stage.INITIAL_HOLD;
        stageStartTime = System.currentTimeMillis();
    }

    public static boolean isAnimating() {
        return currentStage != Stage.IDLE && currentStage != Stage.DONE;
    }

    // 由服务端数据包 PacketEndBlackScreen 触发，直接结束黑屏
    public static void endAnimation() {
        currentStage = Stage.DONE;
        EyeRenderHandler.stopHidingLoadingScreen();
    }

    @SubscribeEvent
    public void onClientTick(TickEvent.ClientTickEvent event) {
        if (event.phase != TickEvent.Phase.START) return;
        if (currentStage == Stage.IDLE || currentStage == Stage.DONE) return;

        long elapsed = System.currentTimeMillis() - stageStartTime;

        switch (currentStage) {
            case INITIAL_HOLD:
                if (elapsed >= INITIAL_HOLD_MS) {
                    currentStage = Stage.ZOOM_PUPIL;
                    stageStartTime = System.currentTimeMillis();
                }
                break;

            case ZOOM_PUPIL:
                if (elapsed >= ZOOM_DURATION_MS) {
                    currentStage = Stage.HOLD_BLACK;
                    stageStartTime = System.currentTimeMillis();
                    EyeRenderHandler.startHidingLoadingScreen();
                    // 瞳孔放大结束 → 发送传送请求
                    NetworkRegister.NETWORK.sendToServer(new PacketEyeTrigger());
                    // 开始遮盖加载界面

                }
                break;

            // HOLD_BLACK 阶段不自动切换，等待 endAnimation()
        }
    }

    @SubscribeEvent
    public void onRenderOverlay(RenderGameOverlayEvent.Post event) {
        if (event.getType() != RenderGameOverlayEvent.ElementType.ALL) return;
        if (currentStage == Stage.IDLE || currentStage == Stage.DONE) return;

        Minecraft mc = Minecraft.getMinecraft();
        int width = mc.displayWidth;
        int height = mc.displayHeight;

        GlStateManager.pushMatrix();
        GlStateManager.disableLighting();
        GlStateManager.enableBlend();
        GlStateManager.blendFunc(GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA);
        GlStateManager.disableDepth();
        GlStateManager.disableCull();

        GlStateManager.matrixMode(GL11.GL_PROJECTION);
        GlStateManager.pushMatrix();
        GlStateManager.loadIdentity();
        GlStateManager.ortho(0.0D, width, height, 0.0D, 1000.0D, 3000.0D);
        GlStateManager.matrixMode(GL11.GL_MODELVIEW);
        GlStateManager.pushMatrix();
        GlStateManager.loadIdentity();
        GlStateManager.translate(0.0F, 0.0F, -2000.0F);

        Tessellator tess = Tessellator.getInstance();
        BufferBuilder buf = tess.getBuffer();

        switch (currentStage) {
            case INITIAL_HOLD:
                drawBackground(buf, width, height, 0.35F, 0.0F, 0.0F);
                mc.getTextureManager().bindTexture(EYE_TEX);
                drawFullTex(buf, width, height, 0.0F, 0.0F, 1.0F, 1.0F);
                break;

            case ZOOM_PUPIL:
                drawBackground(buf, width, height, 0.35F, 0.0F, 0.0F);
                long elapsed = System.currentTimeMillis() - stageStartTime;
                float t = Math.min(1.0F, (float) elapsed / ZOOM_DURATION_MS);
                t = t * t; // 加速放大
                float cx = 0.5F, cy = 0.45F; // 瞳孔位置（可依贴图调整）
                float half = 0.5F * (1.0F - t);
                float u0 = cx - half, u1 = cx + half;
                float v0 = cy - half, v1 = cy + half;
                mc.getTextureManager().bindTexture(EYE_TEX);
                drawFullTex(buf, width, height, u0, v0, u1, v1);
                break;

            case HOLD_BLACK:
                // 全屏黑色，遮盖一切
                drawBackground(buf, width, height, 0.0F, 0.0F, 0.0F);
                break;
        }

        GlStateManager.matrixMode(GL11.GL_PROJECTION);
        GlStateManager.popMatrix();
        GlStateManager.matrixMode(GL11.GL_MODELVIEW);
        GlStateManager.popMatrix();

        GlStateManager.enableDepth();
        GlStateManager.disableBlend();
        GlStateManager.enableLighting();
        GlStateManager.popMatrix();
    }

    private void drawBackground(BufferBuilder buf, int w, int h, float r, float g, float b) {
        GlStateManager.disableTexture2D();
        buf.begin(GL11.GL_QUADS, DefaultVertexFormats.POSITION_COLOR);
        buf.pos(0, 0, 0).color(r, g, b, 1).endVertex();
        buf.pos(0, h, 0).color(r, g, b, 1).endVertex();
        buf.pos(w, h, 0).color(r, g, b, 1).endVertex();
        buf.pos(w, 0, 0).color(r, g, b, 1).endVertex();
        Tessellator.getInstance().draw();
        GlStateManager.enableTexture2D();
    }

    private void drawFullTex(BufferBuilder buf, int w, int h, float u0, float v0, float u1, float v1) {
        buf.begin(GL11.GL_QUADS, DefaultVertexFormats.POSITION_TEX);
        buf.pos(0, 0, 0).tex(u0, v1).endVertex();
        buf.pos(0, h, 0).tex(u0, v0).endVertex();
        buf.pos(w, h, 0).tex(u1, v0).endVertex();
        buf.pos(w, 0, 0).tex(u1, v1).endVertex();
        Tessellator.getInstance().draw();
    }
}