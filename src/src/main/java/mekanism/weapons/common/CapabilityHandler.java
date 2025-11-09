package mekanism.weapons.common;

import mekanism.api.energy.IEnergizedItem;
import mekanism.common.content.gear.IModuleContainerItem;
import mekanism.weapons.MekanismWeapons;
import mekanism.weapons.common.item.ItemMekaBow;
import mekanism.weapons.common.item.ItemMekaTana;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumFacing; // <-- IMPORT AGGIUNTO
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.CapabilityInject;
import net.minecraftforge.common.capabilities.ICapabilityProvider;
import net.minecraftforge.event.AttachCapabilitiesEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

import javax.annotation.Nonnull; // <-- IMPORT AGGIUNTO
import javax.annotation.Nullable; // <-- IMPORT AGGIUNTO

@Mod.EventBusSubscriber(modid = MekanismWeapons.MODID)
public class CapabilityHandler {

    @CapabilityInject(IEnergizedItem.class)
    public static Capability<IEnergizedItem> ENERGIZED_ITEM_CAPABILITY = null;
    
    @CapabilityInject(IModuleContainerItem.class)
    public static Capability<IModuleContainerItem> MODULE_CONTAINER_CAPABILITY = null;

    @SubscribeEvent
    public static void attachItemCapabilities(AttachCapabilitiesEvent<ItemStack> event) {
        ItemStack stack = event.getObject();
        if (stack.getItem() instanceof ItemMekaBow || stack.getItem() instanceof ItemMekaTana) {
            event.addCapability(new ResourceLocation(MekanismWeapons.MODID, "data"),
                new ICapabilityProvider() {
                    @Override
                    public boolean hasCapability(@Nonnull Capability<?> capability, @Nullable EnumFacing facing) {
                        return capability == ENERGIZED_ITEM_CAPABILITY || capability == MODULE_CONTAINER_CAPABILITY;
                    }

                    @Nullable
                    @Override
                    public <T> T getCapability(@Nonnull Capability<T> capability, @Nullable EnumFacing facing) {
                        if (capability == ENERGIZED_ITEM_CAPABILITY) {
                            // Se viene richiesta la capacità di energia, esegui il cast dell'item a IEnergizedItem
                            return (T) stack.getItem();
                        }
                        if (capability == MODULE_CONTAINER_CAPABILITY) {
                            // Se viene richiesta la capacità dei moduli, esegui il cast a IModuleContainerItem
                            return (T) stack.getItem();
                        }
                        return null;
                    }
                }
            );
        }
    }
}
