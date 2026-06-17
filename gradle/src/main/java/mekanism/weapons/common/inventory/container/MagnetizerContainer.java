package mekanism.weapons.common.inventory.container;

import mekanism.common.inventory.container.MekanismContainer;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.inventory.Slot;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumHand;

import javax.annotation.Nonnull;

public class MagnetizerContainer extends MekanismContainer {

    private final EnumHand hand;
    private final ItemStack stack;

    public MagnetizerContainer(InventoryPlayer inventory, EnumHand hand) {
        super(inventory);
        this.hand = hand;
        this.stack = inventory.player.getHeldItem(hand);

    }

    @Override
    protected Slot addSlotToContainer(Slot slot) {
        super.addSlotToContainer(slot);
        if (this.inventoryItemStacks.size() < this.inventorySlots.size()) {
            this.inventoryItemStacks.add(ItemStack.EMPTY);
        }
        return slot;
    }

    @Override
    public boolean canInteractWith(@Nonnull EntityPlayer player) {
        ItemStack held = player.getHeldItem(hand);
        return !held.isEmpty() && held.getItem() == stack.getItem();
    }

    @Override
    protected int getInventoryYOffset() {
        return 158; 
    }

    public ItemStack getStack() {
        return stack;
    }

    public EnumHand getHand() {
        return hand;
    }
}
