package yc.ycqin.nb.event.dim;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiDownloadTerrain;
import net.minecraft.client.gui.GuiScreenWorking;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraftforge.client.event.GuiOpenEvent;
import net.minecraftforge.client.event.InputUpdateEvent;
import net.minecraftforge.client.event.RenderGameOverlayEvent;
import net.minecraftforge.fml.common.eventhandler.EventPriority;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import org.lwjgl.opengl.GL11;
import yc.ycqin.nb.client.animation.EyeAnimationHandler;
import yc.ycqin.nb.gui.GuiMirageBlack;


@SideOnly(Side.CLIENT)
public class EyeRenderHandler {

    @SubscribeEvent
    public void onClientTick(TickEvent.ClientTickEvent event) {
        if (event.phase == TickEvent.Phase.START) {
            // 动画期间锁定视角
            if (EyeAnimationHandler.isAnimating()) {
                Minecraft mc = Minecraft.getMinecraft();
                if (mc.player != null) {
                    mc.player.rotationYaw = mc.player.prevRotationYaw;
                    mc.player.rotationPitch = mc.player.prevRotationPitch;
                }
            }
        }
    }

    @SubscribeEvent
    public void onInput(InputUpdateEvent event) {
        if (EyeAnimationHandler.isAnimating()) {
            // 禁止移动和视角改变
            event.getMovementInput().moveForward = 0;
            event.getMovementInput().moveStrafe = 0;
            event.getMovementInput().jump = false;
            event.getMovementInput().sneak = false;
        }
    }


    //遮挡加载动画

    private static boolean hideLoadingScreen = false;

    /**
     * 标记开始遮盖加载界面（由 EyeAnimationHandler 在进入 HOLD_BLACK 时调用）
     */
    public static void startHidingLoadingScreen() {
        hideLoadingScreen = true;
    }

    /**
     * 标记结束遮盖（由 PacketEndBlackScreen 触发后调用）
     */
    public static void stopHidingLoadingScreen() {
        hideLoadingScreen = false;
    }

    @SubscribeEvent(priority = EventPriority.HIGHEST)
    public void onGuiOpen(GuiOpenEvent event) {
        if (!hideLoadingScreen) return;
        if (event.getGui() instanceof GuiDownloadTerrain) {
            event.setCanceled(true);
            Minecraft.getMinecraft().displayGuiScreen(new GuiMirageBlack());
        }
    }

    // 绘制全屏黑色覆盖（当 hideLoadingScreen 为 true 时）
    @SubscribeEvent
    public void onRenderOverlay(RenderGameOverlayEvent.Pre event) {
        if (!hideLoadingScreen) return;
        Minecraft mc = Minecraft.getMinecraft();
        int width = mc.displayWidth;
        int height = mc.displayHeight;

        GlStateManager.pushMatrix();
        GlStateManager.disableLighting();
        GlStateManager.disableTexture2D();
        GlStateManager.enableBlend();
        GlStateManager.depthMask(false);

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
        buf.begin(GL11.GL_QUADS, DefaultVertexFormats.POSITION_COLOR);
        buf.pos(0, 0, 0).color(0, 0, 0, 1).endVertex();
        buf.pos(0, height, 0).color(0, 0, 0, 1).endVertex();
        buf.pos(width, height, 0).color(0, 0, 0, 1).endVertex();
        buf.pos(width, 0, 0).color(0, 0, 0, 1).endVertex();
        tess.draw();

        GlStateManager.matrixMode(GL11.GL_PROJECTION);
        GlStateManager.popMatrix();
        GlStateManager.matrixMode(GL11.GL_MODELVIEW);
        GlStateManager.popMatrix();

        GlStateManager.depthMask(true);
        GlStateManager.enableTexture2D();
        GlStateManager.disableBlend();
        GlStateManager.enableLighting();
        GlStateManager.popMatrix();
    }

    public static boolean isHiding() {
        return hideLoadingScreen;
    }
}