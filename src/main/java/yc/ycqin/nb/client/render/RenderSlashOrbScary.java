package yc.ycqin.nb.client.render;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.OpenGlHelper;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.culling.ICamera;
import net.minecraft.client.renderer.entity.Render;
import net.minecraft.client.renderer.entity.RenderManager;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.entity.Entity;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.text.TextFormatting;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import org.lwjgl.opengl.GL11;
import yc.ycqin.nb.common.entity.EntitySlashOrbBoom;

import java.util.HashMap;
import java.util.Map;

import static yc.ycqin.nb.ycqin.MODID;

@SideOnly(Side.CLIENT)
public class RenderSlashOrbScary extends Render<EntitySlashOrbBoom> {

    // 纹理资源（请替换为您的实际 modid）
    public static final ResourceLocation TEXTURES = new ResourceLocation("ycqin", "textures/entity/orbscary.png");
    public static final ResourceLocation LIGHTNING_TEX = new ResourceLocation("ycqin", "textures/entity/orbscary_armor.png");

    private static final float SPHERE_RADIUS = 0.317F;
    private static final int SPHERE_STACKS = 18;
    private static final int SPHERE_SLICES = 18;

    private final Map<Integer, Float> scaleSmooth = new HashMap<>();

    public RenderSlashOrbScary(RenderManager renderManager) {
        super(renderManager);
    }

    @Override
    protected ResourceLocation getEntityTexture(EntitySlashOrbBoom entity) {
        return TEXTURES;
    }

    @Override
    public boolean shouldRender(EntitySlashOrbBoom livingEntity, ICamera camera, double camX, double camY, double camZ) {
        return true;
    }

    @Override
    public void doRender(EntitySlashOrbBoom entity, double x, double y, double z, float entityYaw, float partialTicks) {
        super.doRender(entity, x, y, z, entityYaw, partialTicks);
        this.doRenderCosmical(entity, x, y, z, entityYaw, partialTicks);
    }

    public void doRenderCosmical(EntitySlashOrbBoom entity, double x, double y, double z, float entityYaw, float partialTicks) {
        GlStateManager.pushMatrix();
        GlStateManager.disableLighting();

        float f = this.interpolateRotation(entity.prevRenderYawOffset, entity.renderYawOffset, partialTicks);
        float f1 = this.interpolateRotation(entity.prevRotationYawHead, entity.rotationYawHead, partialTicks);
        float f7 = entity.prevRotationPitch + (entity.rotationPitch - entity.prevRotationPitch) * partialTicks;

        this.renderLivingAt(entity, x, y, z);
        float ageInTicks = this.handleRotationFloat(entity, partialTicks);
        this.applyRotations(entity, ageInTicks, f, partialTicks);
        this.prepareScaleCosmical(entity, partialTicks); // 内部调用缩放

        this.renderModelCosmical(entity, partialTicks);

        GlStateManager.enableLighting();
        GlStateManager.popMatrix();
    }

    protected void renderModelCosmical(EntitySlashOrbBoom e, float partialTicks) {
        boolean flag = !e.isInvisibleToPlayer(Minecraft.getMinecraft().player);
        if (flag) {
            if (!this.bindEntityTextureCosmical(e)) return;

            GlStateManager.enableBlend();
            GlStateManager.blendFunc(GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA);
            GlStateManager.alphaFunc(516, 0.003921569F);

            // 不写入深度（但保留深度测试）
            GlStateManager.depthMask(false);

            // 渲染主球体（半透明）
            GlStateManager.color(1.0F, 1.0F, 1.0F, 0.35F); // 使用同步后的 alpha
            this.renderTexturedSphere(SPHERE_RADIUS, SPHERE_STACKS, SPHERE_SLICES);

            // 渲染外壳（加法混合）
            GlStateManager.blendFunc(GlStateManager.SourceFactor.ONE, GlStateManager.DestFactor.ONE);
            this.renderChargedAura(e, partialTicks);

            GlStateManager.disableBlend();
            GlStateManager.alphaFunc(516, 0.1F);
            GlStateManager.depthMask(true); // 恢复深度写入
            GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
        }
    }

    private boolean isCameraInsideOrb(EntitySlashOrbBoom e, float partialTicks) {
        Entity view = Minecraft.getMinecraft().getRenderViewEntity();
        if (view == null) return false;
        Vec3d eye = view.getPositionEyes(partialTicks);
        AxisAlignedBB bb = e.getEntityBoundingBox().grow(1.25);
        return bb.contains(eye);
    }

    private void renderChargedAura(EntitySlashOrbBoom e, float partialTicks) {
        this.bindTexture(LIGHTNING_TEX);
        GlStateManager.pushMatrix();
        GlStateManager.scale(1.12F, 1.12F, 1.12F);
        GlStateManager.depthMask(false);
        GlStateManager.enableBlend();
        GlStateManager.blendFunc(GlStateManager.SourceFactor.ONE, GlStateManager.DestFactor.ONE);

        float prevBX = OpenGlHelper.lastBrightnessX;
        float prevBY = OpenGlHelper.lastBrightnessY;
        GlStateManager.setActiveTexture(OpenGlHelper.lightmapTexUnit);
        GlStateManager.enableTexture2D();
        OpenGlHelper.setLightmapTextureCoords(OpenGlHelper.lightmapTexUnit, 240.0F, 240.0F);
        GlStateManager.setActiveTexture(OpenGlHelper.defaultTexUnit);

        float t = (float) e.ticksExisted + partialTicks;
        float pulse = 0.25F + 0.15F * MathHelper.sin(t * 0.25F);
        GlStateManager.color(0.6F, 0.85F, 1.0F, pulse);

        float uScroll = t * 0.01F;
        float vScroll = t * 0.015F;
        this.renderSwirlSphere(SPHERE_RADIUS, SPHERE_STACKS, SPHERE_SLICES, uScroll, vScroll);

        GlStateManager.setActiveTexture(OpenGlHelper.lightmapTexUnit);
        GlStateManager.enableTexture2D();
        OpenGlHelper.setLightmapTextureCoords(OpenGlHelper.lightmapTexUnit, prevBX, prevBY);
        GlStateManager.setActiveTexture(OpenGlHelper.defaultTexUnit);

        GlStateManager.disableBlend();
        GlStateManager.depthMask(true);
        GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
        GlStateManager.popMatrix();
    }

    private void renderSwirlSphere(float radius, int stacks, int slices, float uOff, float vOff) {
        Tessellator tess = Tessellator.getInstance();
        BufferBuilder bb = tess.getBuffer();
        for (int i = 0; i < stacks; ++i) {
            float v0 = (float) i / (float) stacks;
            float v1 = (float) (i + 1) / (float) stacks;
            double phi0 = Math.PI * v0;
            double phi1 = Math.PI * v1;
            bb.begin(5, DefaultVertexFormats.POSITION_TEX);
            for (int j = 0; j <= slices; ++j) {
                float u = (float) j / (float) slices;
                double theta = (Math.PI * 2D) * u;
                double x0 = Math.sin(phi0) * Math.cos(theta);
                double y0 = Math.cos(phi0);
                double z0 = Math.sin(phi0) * Math.sin(theta);
                double x1 = Math.sin(phi1) * Math.cos(theta);
                double y1 = Math.cos(phi1);
                double z1 = Math.sin(phi1) * Math.sin(theta);
                float uu = u * 2.0F + uOff;
                float vv0 = (1.0F - v0) * 2.0F + vOff;
                float vv1 = (1.0F - v1) * 2.0F + vOff;
                bb.pos(x0 * radius, y0 * radius, z0 * radius).tex(uu, vv0).endVertex();
                bb.pos(x1 * radius, y1 * radius, z1 * radius).tex(uu, vv1).endVertex();
            }
            tess.draw();
        }
    }

    private void renderTexturedSphere(float radius, int stacks, int slices) {
        Tessellator tess = Tessellator.getInstance();
        BufferBuilder bb = tess.getBuffer();
        for (int i = 0; i < stacks; ++i) {
            float v0 = (float) i / (float) stacks;
            float v1 = (float) (i + 1) / (float) stacks;
            double phi0 = Math.PI * v0;
            double phi1 = Math.PI * v1;
            bb.begin(5, DefaultVertexFormats.POSITION_TEX);
            for (int j = 0; j <= slices; ++j) {
                float u = (float) j / (float) slices;
                double theta = (Math.PI * 2D) * u;
                double x0 = Math.sin(phi0) * Math.cos(theta);
                double y0 = Math.cos(phi0);
                double z0 = Math.sin(phi0) * Math.sin(theta);
                double x1 = Math.sin(phi1) * Math.cos(theta);
                double y1 = Math.cos(phi1);
                double z1 = Math.sin(phi1) * Math.sin(theta);
                bb.pos(x0 * radius, y0 * radius, z0 * radius).tex(u, 1.0F - v0).endVertex();
                bb.pos(x1 * radius, y1 * radius, z1 * radius).tex(u, 1.0F - v1).endVertex();
            }
            tess.draw();
        }
    }

    protected boolean bindEntityTextureCosmical(EntitySlashOrbBoom entity) {
        ResourceLocation res = this.getEntityTexture(entity);
        if (res == null) return false;
        this.bindTexture(res);
        return true;
    }

    protected void prepareScaleCosmical(EntitySlashOrbBoom e, float partialTickTime) {
        GlStateManager.enableRescaleNormal();
        GlStateManager.scale(-1.0F, -1.0F, 1.0F);
        this.preRenderCallbackCosmical(e, partialTickTime);
    }

    protected void preRenderCallbackCosmical(EntitySlashOrbBoom e, float partialTickTime) {
        float age = (float) e.ticksExisted + partialTickTime;

        // 基础缩放：基于实体当前大小，乘以一个系数使视觉大小合适
        // 原版乘 0.8，现改为 1.5（可根据需要调整）
        float BASE_FACTOR = 10F;
        float base = Math.max(e.width * 2.0F, e.height * 1.9F) * BASE_FACTOR;

        // 生长因子 (0~1) 平滑过渡
        float g = MathHelper.clamp(age / 35.0F, 0.0F, 1.0F);
        g = g * g * (3.0F - 2.0F * g);

        float normalScale = base * (0.35F + 0.95F * g);

        // 弹跳效果
        float tau = MathHelper.clamp(age / 35.0F, 0.0F, 1.0F);
        float bounce = 0.28F * (float) Math.exp(-5.0F * tau) * MathHelper.sin(10.0F * tau * (float) Math.PI);

        float targetScale = normalScale * (1.0F + bounce);
        if (targetScale < 0.001F) targetScale = 0.001F;

        // 平滑缩放（保持不变）
        int id = e.getEntityId();
        Float prev = this.scaleSmooth.get(id);
        if (prev == null) prev = targetScale;
        float smooth = prev + (targetScale - prev) * 0.2F;
        this.scaleSmooth.put(id, smooth);
        if (e.isDead) this.scaleSmooth.remove(id);

        GlStateManager.scale(smooth, smooth, smooth);

        // 悬浮效果（保持不变）
        float hoverAmp = 0.05F * (0.2F + 0.8F * g);
        float hover = MathHelper.sin(age * 0.1F + (float) id * 0.3F) * hoverAmp;
        GlStateManager.translate(0.0F, hover, 0.0F);

        // 自转（保持不变）
        float yaw = age * 1.4F % 360.0F;
        float pitch = 8.0F * MathHelper.sin(age * 0.07F + (float) id);
        GlStateManager.rotate(yaw, 0.0F, 1.0F, 0.0F);
        GlStateManager.rotate(pitch, 1.0F, 0.0F, 0.0F);
    }

    protected float interpolateRotation(float prevYawOffset, float yawOffset, float partialTicks) {
        float f = yawOffset - prevYawOffset;
        while (f < -180.0F) f += 360.0F;
        while (f >= 180.0F) f -= 360.0F;
        return prevYawOffset + partialTicks * f;
    }

    protected void renderLivingAt(EntitySlashOrbBoom entity, double x, double y, double z) {
        GlStateManager.translate(x, y, z);
    }

    protected float handleRotationFloat(EntitySlashOrbBoom livingBase, float partialTicks) {
        return (float) livingBase.ticksExisted + partialTicks;
    }

    protected void applyRotations(EntitySlashOrbBoom entityLiving, float ageInTicks, float rotationYaw, float partialTicks) {
        GlStateManager.rotate(180.0F - rotationYaw, 0.0F, 1.0F, 0.0F);
        if (entityLiving.deathTime > 0) {
            float f = ((float) entityLiving.deathTime + partialTicks - 1.0F) / 20.0F * 1.6F;
            f = MathHelper.sqrt(f);
            if (f > 1.0F) f = 1.0F;
            GlStateManager.rotate(f * 90.0F, 0.0F, 0.0F, 1.0F);
        } else {
            String s = TextFormatting.getTextWithoutFormattingCodes(entityLiving.getName());
            if (s != null && ("Dinnerbone".equals(s) || "Grumm".equals(s))) {
                GlStateManager.translate(0.0F, entityLiving.height + 0.1F, 0.0F);
                GlStateManager.rotate(180.0F, 0.0F, 0.0F, 1.0F);
            }
        }
    }
}