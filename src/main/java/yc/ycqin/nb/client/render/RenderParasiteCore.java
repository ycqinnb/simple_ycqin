package yc.ycqin.nb.client.render;

import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.tileentity.TileEntitySpecialRenderer;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import org.lwjgl.opengl.GL11;
import yc.ycqin.nb.common.entity.tileentity.TileEntityParasiteCore;

public class RenderParasiteCore extends TileEntitySpecialRenderer<TileEntityParasiteCore> {

    @Override
    public void render(TileEntityParasiteCore te, double x, double y, double z, float partialTicks, int destroyStage, float alpha) {
        // 如果结构无效或被红石信号关闭，则不渲染屏障
        if (!te.isStructureValid() || te.isPowered()) return;

        GlStateManager.pushMatrix();
        GlStateManager.translate(x, y, z); // 确保本地坐标正确

        GlStateManager.disableTexture2D();
        GlStateManager.disableLighting();   // 禁用光照，保持颜色纯正
        GlStateManager.enableBlend();
        GlStateManager.blendFunc(GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA);
        GlStateManager.disableCull();

        Tessellator tessellator = Tessellator.getInstance();
        BufferBuilder buffer = tessellator.getBuffer();
        buffer.begin(GL11.GL_QUADS, DefaultVertexFormats.POSITION_COLOR);

        // 屏障范围：8个区块（半径64格）
        double localMinX = -64.0;
        double localMaxX = 65.0;
        double localMinZ = -64.0;
        double localMaxZ = 65.0;
        int coreY = te.getPos().getY();
        double localMinY = -coreY;
        double localMaxY = 255 - coreY;

        // 纯蓝色，半透明 (R,G,B,A) = (0,0,1,0.3)
        float r = 0.0f;
        float g = 0.0f;
        float b = 1.0f;
        float a = 0.3f;

        // 北面 (Z = localMinZ)
        buffer.pos(localMinX, localMinY, localMinZ).color(r, g, b, a).endVertex();
        buffer.pos(localMaxX, localMinY, localMinZ).color(r, g, b, a).endVertex();
        buffer.pos(localMaxX, localMaxY, localMinZ).color(r, g, b, a).endVertex();
        buffer.pos(localMinX, localMaxY, localMinZ).color(r, g, b, a).endVertex();

        // 南面 (Z = localMaxZ)
        buffer.pos(localMinX, localMinY, localMaxZ).color(r, g, b, a).endVertex();
        buffer.pos(localMinX, localMaxY, localMaxZ).color(r, g, b, a).endVertex();
        buffer.pos(localMaxX, localMaxY, localMaxZ).color(r, g, b, a).endVertex();
        buffer.pos(localMaxX, localMinY, localMaxZ).color(r, g, b, a).endVertex();

        // 西面 (X = localMinX)
        buffer.pos(localMinX, localMinY, localMinZ).color(r, g, b, a).endVertex();
        buffer.pos(localMinX, localMaxY, localMinZ).color(r, g, b, a).endVertex();
        buffer.pos(localMinX, localMaxY, localMaxZ).color(r, g, b, a).endVertex();
        buffer.pos(localMinX, localMinY, localMaxZ).color(r, g, b, a).endVertex();

        // 东面 (X = localMaxX)
        buffer.pos(localMaxX, localMinY, localMinZ).color(r, g, b, a).endVertex();
        buffer.pos(localMaxX, localMinY, localMaxZ).color(r, g, b, a).endVertex();
        buffer.pos(localMaxX, localMaxY, localMaxZ).color(r, g, b, a).endVertex();
        buffer.pos(localMaxX, localMaxY, localMinZ).color(r, g, b, a).endVertex();

        tessellator.draw();

        GlStateManager.enableCull();
        GlStateManager.disableBlend();
        GlStateManager.enableLighting();
        GlStateManager.enableTexture2D();
        GlStateManager.popMatrix();
    }
}