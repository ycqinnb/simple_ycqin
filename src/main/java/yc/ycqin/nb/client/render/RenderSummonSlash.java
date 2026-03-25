package yc.ycqin.nb.client.render;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.entity.Render;
import net.minecraft.client.renderer.entity.RenderManager;
import net.minecraft.client.renderer.texture.TextureMap;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
import yc.ycqin.nb.common.entity.EntitySummonSlash;

public class RenderSummonSlash extends Render<EntitySummonSlash> {

    public RenderSummonSlash(RenderManager renderManager) {
        super(renderManager);
    }

    @Override
    public void doRender(EntitySummonSlash entity, double x, double y, double z, float entityYaw, float partialTicks) {
        ItemStack stack = entity.getRenderItem(); // 或 entity.getWeapon()
        if (stack == null || stack.isEmpty()) {
                stack = new ItemStack(net.minecraft.init.Items.DIAMOND_SWORD);
        }

        GlStateManager.pushMatrix();
        GlStateManager.translate(x, y, z);
        int age = entity.ticksExisted;
        float progress = (age + partialTicks) / 5f;
        if (progress > 1) progress = 1;
        float angle = -90 + 180 * progress;
        GlStateManager.rotate(angle, 1, 0, 0);
        GlStateManager.translate(progress * 0.5, 0, 0);
        Minecraft.getMinecraft().getRenderItem().renderItem(stack,
                net.minecraft.client.renderer.block.model.ItemCameraTransforms.TransformType.FIXED);
        GlStateManager.popMatrix();
    }

    private ItemStack getItemStackToRender(EntitySummonSlash entity) {
        return entity.getRenderItem();
    }

    @Override
    protected ResourceLocation getEntityTexture(EntitySummonSlash entity) {
        return null; // 不需要纹理
    }
}