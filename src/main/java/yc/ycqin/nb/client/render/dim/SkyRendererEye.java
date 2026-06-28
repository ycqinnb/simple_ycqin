package yc.ycqin.nb.client.render.dim;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.WorldClient;
import net.minecraft.client.renderer.*;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.entity.EntityTracker;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.Vec3d;
import net.minecraftforge.client.IRenderHandler;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import org.lwjgl.opengl.GL11;
import yc.ycqin.nb.client.animation.EyeAnimationHandler;
import yc.ycqin.nb.network.PacketEyeTrigger;
import yc.ycqin.nb.register.NetworkRegister;

@SideOnly(Side.CLIENT)
public class SkyRendererEye extends IRenderHandler {

    public static final SkyRendererEye INSTANCE = new SkyRendererEye();
    private SkyRendererEye() {}

    private static final ResourceLocation EYE_TEX = new ResourceLocation("ycqin", "textures/environment/giant_eye.png");
    private static final double EYE_DISTANCE = 200.0;    // 头顶200格
    private static final float EYE_HALF_SIZE = 15.0F;
    private static final long TRIGGER_COOLDOWN = 3000;
    private long lastTrigger = 0;

    @Override
    public void render(float partialTicks, WorldClient world, Minecraft mc) {
        if (mc.player == null) return;

        // ----- 1. 纯黑夜背景（看不见月亮和星星的关键） -----
        drawNightBackground();

        // ----- 2. 绘制天空巨眼 -----
        drawEyeInSky(mc);

        // ----- 3. 凝视检测 -----
        checkLookAngle(mc);
    }

    private void drawNightBackground() {
        GlStateManager.disableTexture2D();
        GlStateManager.disableFog();
        GlStateManager.depthMask(false);
        GlStateManager.pushMatrix();

        // 天空渲染时模型视图矩阵已经是相机朝向，画一个巨大的暗红四边形覆盖整个天空区域
        Tessellator tess = Tessellator.getInstance();
        BufferBuilder buf = tess.getBuffer();
        buf.begin(GL11.GL_QUADS, DefaultVertexFormats.POSITION_COLOR);
        float far = 500.0F;
        buf.pos(-far, -far, -far).color(0.02F, 0.0F, 0.0F, 1.0F).endVertex();
        buf.pos(-far,  far, -far).color(0.02F, 0.0F, 0.0F, 1.0F).endVertex();
        buf.pos( far,  far, -far).color(0.02F, 0.0F, 0.0F, 1.0F).endVertex();
        buf.pos( far, -far, -far).color(0.02F, 0.0F, 0.0F, 1.0F).endVertex();
        tess.draw();

        GlStateManager.popMatrix();
        GlStateManager.enableFog();
        GlStateManager.enableTexture2D();
        GlStateManager.depthMask(true);
    }

    private void drawEyeInSky(Minecraft mc) {
        // 眼睛固定在世界天顶方向
        double camX = mc.getRenderManager().viewerPosX;
        double camY = mc.getRenderManager().viewerPosY;
        double camZ = mc.getRenderManager().viewerPosZ;

        double eyeX = camX;
        double eyeY = camY + EYE_DISTANCE;
        double eyeZ = camZ;

        GlStateManager.pushMatrix();
        GlStateManager.disableLighting();
        GlStateManager.enableBlend();
        GlStateManager.blendFunc(GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA);
        GlStateManager.depthMask(false);
        GlStateManager.disableCull(); // 保证双面可见

        // 平移到眼睛位置（相对于相机）
        GlStateManager.translate(eyeX - camX, eyeY - camY, eyeZ - camZ);
        // 广告牌旋转：使四边形正对相机
        GlStateManager.rotate(-mc.getRenderManager().playerViewY, 0.0F, 1.0F, 0.0F);
        GlStateManager.rotate(mc.getRenderManager().playerViewX, 1.0F, 0.0F, 0.0F);

        mc.getTextureManager().bindTexture(EYE_TEX);
        Tessellator tess = Tessellator.getInstance();
        BufferBuilder buf = tess.getBuffer();
        buf.begin(GL11.GL_QUADS, DefaultVertexFormats.POSITION_TEX);
        float s = EYE_HALF_SIZE;
        buf.pos(-s, -s, 0.0).tex(0.0, 1.0).endVertex();
        buf.pos(-s,  s, 0.0).tex(0.0, 0.0).endVertex();
        buf.pos( s,  s, 0.0).tex(1.0, 0.0).endVertex();
        buf.pos( s, -s, 0.0).tex(1.0, 1.0).endVertex();
        tess.draw();

        GlStateManager.enableCull();
        GlStateManager.depthMask(true);
        GlStateManager.disableBlend();
        GlStateManager.enableLighting();
        GlStateManager.popMatrix();
    }

    private void checkLookAngle(Minecraft mc) {
        if (EyeAnimationHandler.isAnimating()) return; // 已经在动画中，不重复触发
        long now = System.currentTimeMillis();
        if (now - lastTrigger < TRIGGER_COOLDOWN) return;

        // 检测玩家视线是否接近头顶方向
        Vec3d playerEye = mc.player.getPositionEyes(1.0F);
        Vec3d eyePos = new Vec3d(mc.player.posX, mc.player.posY + EYE_DISTANCE, mc.player.posZ);
        Vec3d toEye = eyePos.subtract(playerEye).normalize();
        Vec3d look = mc.player.getLookVec();
        double dot = look.dotProduct(toEye);
        double angle = Math.toDegrees(Math.acos(dot));

        if (angle < 15.0 && !EyeAnimationHandler.isAnimating()) { // 15°以内视为凝视
            EyeAnimationHandler.startAnimation();  // 启动动画
            lastTrigger = now;
        }
    }
}