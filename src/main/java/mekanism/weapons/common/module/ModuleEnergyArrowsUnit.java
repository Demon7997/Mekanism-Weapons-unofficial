package mekanism.weapons.common.module;

import mekanism.api.EnumColor;
import mekanism.api.gear.ICustomModule;
import mekanism.api.gear.IModule;
import mekanism.common.util.LangUtils;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;

import java.util.function.Consumer;

public class ModuleEnergyArrowsUnit implements ICustomModule<ModuleEnergyArrowsUnit> {

    @Override
    public void addHUDStrings(IModule<ModuleEnergyArrowsUnit> module, EntityPlayer player, Consumer<String> hudStringAdder) {
        String name = LangUtils.localize("module.energy_arrows_unit.name");
        
        EnumColor stateColor;
        String stateText;
        
        if (module.isEnabled()) {
            stateColor = EnumColor.DARK_GREEN;
            stateText = LangUtils.localize("on");
        } else {
            stateColor = EnumColor.DARK_RED;
            stateText = LangUtils.localize("off");
        }
        
        hudStringAdder.accept(EnumColor.DARK_GREY + name + ": " + stateColor + stateText);
    }

    @Override
    public void changeMode(IModule<ModuleEnergyArrowsUnit> module, EntityPlayer player, ItemStack stack, int shift, boolean displayChangeMessage) {
        module.toggleEnabled(player, LangUtils.localize("module.energy_arrows_unit.name"));
    }
}
