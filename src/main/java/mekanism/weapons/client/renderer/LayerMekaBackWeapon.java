package mekanism.weapons.client.renderer;

import mekanism.weapons.common.item.ItemMagnetizer;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.block.model.ItemCameraTransforms;
import net.minecraft.client.renderer.entity.layers.LayerRenderer;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import baubles.api.BaublesApi;

public class LayerMekaBackWeapon implements LayerRenderer<EntityPlayer> {

    @Override
    public void doRenderLayer(EntityPlayer player, float limbSwing, float limbSwingAmount, float partialTicks, float ageInTicks, float netHeadYaw, float headPitch, float scale) {
        if (player.isInvisible()) return;
        if (player == Minecraft.getMinecraft().player && Minecraft.getMinecraft().gameSettings.thirdPersonView == 0) return;

        ItemStack magnetizer = findMagnetizerEverywhere(player);
        if (magnetizer.isEmpty() || !(magnetizer.getItem() instanceof ItemMagnetizer)) return;
        ItemMagnetizer itemMag = (ItemMagnetizer) magnetizer.getItem();

        boolean isHoldingTana = isWeaponInHand(player, "meka_tana");
        boolean isHoldingBow = isWeaponInHand(player, "meka_bow");

        boolean tanaRendered = false;
        boolean bowRendered = false;

        for (int i = 0; i < player.inventory.getSizeInventory(); i++) {
            ItemStack stack = player.inventory.getStackInSlot(i);
            if (stack.isEmpty()) continue;

            String name = stack.getItem().getRegistryName().toString();

            if (name.contains("meka_tana") && !tanaRendered && !isHoldingTana) {
                if (itemMag.isRenderEnabled(magnetizer, "tana")) {
                    //between 22.0 && 25.0
                    renderOnBack(player, stack, 0.05, 0.20, -0.10, 25.0f);
                    tanaRendered = true;
                }
            }
            else if (name.contains("meka_bow") && !bowRendered && !isHoldingBow) {
                if (itemMag.isRenderEnabled(magnetizer, "bow")) {
                    renderOnBack(player, stack, 0.10, 0.42, -0.15, 180.0f);
                    bowRendered = true;
                }
            }
        }
    }

    private boolean isWeaponInHand(EntityPlayer player, String weaponName) {
        ItemStack main = player.getHeldItemMainhand();
        ItemStack off = player.getHeldItemOffhand();
        boolean inMain = !main.isEmpty() && main.getItem().getRegistryName().toString().contains(weaponName);
        boolean inOff = !off.isEmpty() && off.getItem().getRegistryName().toString().contains(weaponName);
        return inMain || inOff;
    }

    private void renderOnBack(EntityPlayer player, ItemStack stack, double x, double y, double z, float angle) {
        GlStateManager.pushMatrix();
        if (player.isSneaking()) {
            GlStateManager.rotate(28.0F, 1.0F, 0.0F, 0.0F);
            GlStateManager.translate(0.0D, 0.15D, 0.0D);
        }
        GlStateManager.rotate(180, 0, 1, 0);
        GlStateManager.translate(x, y, z);
        GlStateManager.rotate(angle, 0, 0, 1);
        GlStateManager.scale(0.8, 0.8, 0.8);
        Minecraft.getMinecraft().getRenderItem().renderItem(stack, ItemCameraTransforms.TransformType.FIXED);
        GlStateManager.popMatrix();
    }

    private ItemStack findMagnetizerEverywhere(EntityPlayer player) {
        if (player.getHeldItemMainhand().getItem() instanceof ItemMagnetizer) return player.getHeldItemMainhand();
        if (player.getHeldItemOffhand().getItem() instanceof ItemMagnetizer) return player.getHeldItemOffhand();
        
        for (ItemStack stack : player.inventory.mainInventory) {
            if (!stack.isEmpty() && stack.getItem() instanceof ItemMagnetizer) return stack;
        }
        
        try {
            for (int i = 0; i < BaublesApi.getBaublesHandler(player).getSlots(); i++) {
                ItemStack bauble = BaublesApi.getBaublesHandler(player).getStackInSlot(i);
                if (!bauble.isEmpty() && bauble.getItem() instanceof ItemMagnetizer) return bauble;
            }
        } catch (Exception ignored) {}
        
        return ItemStack.EMPTY;
    }

    @Override
    public boolean shouldCombineTextures() { return false; }
}
