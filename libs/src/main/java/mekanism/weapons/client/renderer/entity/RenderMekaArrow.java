package mekanism.weapons.client.renderer.entity;

import mekanism.weapons.MekanismWeaponsItems;
import mekanism.weapons.common.entity.EntityMekaArrow;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.block.model.ItemCameraTransforms;
import net.minecraft.client.renderer.entity.Render;
import net.minecraft.client.renderer.entity.RenderManager;
import net.minecraft.client.renderer.texture.TextureMap;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

@SideOnly(Side.CLIENT)
public class RenderMekaArrow extends Render<EntityMekaArrow> {

    public RenderMekaArrow(RenderManager renderManager) {
        super(renderManager);
    }

    @Override
    public void doRender(EntityMekaArrow entity, double x, double y, double z, float entityYaw, float partialTicks) {
        GlStateManager.pushMatrix();
        
        GlStateManager.translate((float)x, (float)y, (float)z);
        
        GlStateManager.rotate(entity.prevRotationYaw + (entity.rotationYaw - entity.prevRotationYaw) * partialTicks - 90.0F, 0.0F, 1.0F, 0.0F);
        GlStateManager.rotate(entity.prevRotationPitch + (entity.rotationPitch - entity.prevRotationPitch) * partialTicks, 0.0F, 0.0F, 1.0F);
        
        this.bindTexture(TextureMap.LOCATION_BLOCKS_TEXTURE);

        GlStateManager.disableLighting();
        GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
        
        ItemStack stack = new ItemStack(MekanismWeaponsItems.meka_arrow);
        
        if (!stack.isEmpty()) {
            Minecraft.getMinecraft().getRenderItem().renderItem(stack, ItemCameraTransforms.TransformType.NONE);
        }

        GlStateManager.enableLighting();
        GlStateManager.popMatrix();
        
        super.doRender(entity, x, y, z, entityYaw, partialTicks);
    }

    @Override
    protected ResourceLocation getEntityTexture(EntityMekaArrow entity) {
        return TextureMap.LOCATION_BLOCKS_TEXTURE;
    }
}
