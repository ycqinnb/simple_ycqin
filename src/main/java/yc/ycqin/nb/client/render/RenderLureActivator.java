package yc.ycqin.nb.client.render;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.block.model.ItemCameraTransforms;
import net.minecraft.client.renderer.tileentity.TileEntitySpecialRenderer;
import net.minecraft.item.ItemStack;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import yc.ycqin.nb.common.entity.tileentity.TileEntityLureActivator;
@SideOnly(Side.CLIENT)
public class RenderLureActivator extends TileEntitySpecialRenderer<TileEntityLureActivator> {
    @Override
    public void render(TileEntityLureActivator te, double x, double y, double z, float partialTicks, int destroyStage, float alpha) {
        ItemStack stack = te.getLure();
        if (!stack.isEmpty()) {
            GlStateManager.pushMatrix();
            GlStateManager.translate(x + 0.5, y + 0.5, z + 0.5);
            long time = te.getWorld().getTotalWorldTime();
            GlStateManager.rotate((time + partialTicks) * 2, 0, 1, 0);
            GlStateManager.scale(0.8f, 0.8f, 0.8f);
            Minecraft.getMinecraft().getRenderItem().renderItem(stack, ItemCameraTransforms.TransformType.GROUND);
            GlStateManager.popMatrix();
        } else {
            System.out.println("empty");
        }
    }
}
