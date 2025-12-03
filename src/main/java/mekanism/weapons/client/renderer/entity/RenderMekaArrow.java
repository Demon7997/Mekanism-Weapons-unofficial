package mekanism.weapons.client.renderer.entity;

import mekanism.weapons.MekanismWeaponsItems;
import mekanism.weapons.common.entity.EntityMekaArrow;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.RenderHelper;
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
        
        // 1. Posizione
        GlStateManager.translate((float)x, (float)y, (float)z);
        
        // 2. Rotazione (Standard delle frecce: punta verso dove vola)
        GlStateManager.rotate(entity.prevRotationYaw + (entity.rotationYaw - entity.prevRotationYaw) * partialTicks - 90.0F, 0.0F, 1.0F, 0.0F);
        GlStateManager.rotate(entity.prevRotationPitch + (entity.rotationPitch - entity.prevRotationPitch) * partialTicks, 0.0F, 0.0F, 1.0F);
        
        // 3. Bind Texture (OBBLIGATORIO)
        this.bindTexture(TextureMap.LOCATION_BLOCKS_TEXTURE);

        // 4. FIX DEL BLOCCO BIANCO
        // Disabilitiamo l'illuminazione dinamica che rende tutto bianco/nero sulle entità item
        GlStateManager.disableLighting();
        // Resettiamo il colore a Bianco Puro (1,1,1,1) per disegnare la texture originale
        GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
        
        // 5. Creazione Item
        ItemStack stack = new ItemStack(MekanismWeaponsItems.meka_arrow);
        
        if (!stack.isEmpty()) {
            // Scaliamo un po' se necessario (spesso gli item sono grandi)
            // GlStateManager.scale(1.5F, 1.5F, 1.5F); // Decommenta se è troppo piccola

            // Renderizza l'item. 
            // Usiamo GROUND o NONE. FIXED a volte da problemi se il JSON non è perfetto.
            // NONE dice a Minecraft: "Disegna il modello così com'è nel file JSON".
            Minecraft.getMinecraft().getRenderItem().renderItem(stack, ItemCameraTransforms.TransformType.NONE);
        }

        // 6. Ripristino
        GlStateManager.enableLighting();
        GlStateManager.popMatrix();
        
        super.doRender(entity, x, y, z, entityYaw, partialTicks);
    }

    @Override
    protected ResourceLocation getEntityTexture(EntityMekaArrow entity) {
        return TextureMap.LOCATION_BLOCKS_TEXTURE;
    }
}
