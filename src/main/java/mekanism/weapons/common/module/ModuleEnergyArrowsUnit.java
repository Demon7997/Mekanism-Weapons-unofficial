package mekanism.weapons.common.module;

import mekanism.api.EnumColor;
import mekanism.api.gear.ICustomModule;
import mekanism.api.gear.IModule;
import mekanism.common.util.LangUtils;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;

import java.util.function.Consumer;

public class ModuleEnergyArrowsUnit implements ICustomModule<ModuleEnergyArrowsUnit> {

    // Aggiunge la stringa "Energy Arrows: On/Off" all'HUD del giocatore
    @Override
    public void addHUDStrings(IModule<ModuleEnergyArrowsUnit> module, EntityPlayer player, Consumer<String> hudStringAdder) {
        // Il nome del modulo rimane grigio
        String name = LangUtils.localize("module.energy_arrows_unit.name");
        
        // Scegliamo il colore e il testo dello stato in base a se il modulo è attivo o no
        EnumColor stateColor;
        String stateText;
        
        if (module.isEnabled()) {
            stateColor = EnumColor.DARK_GREEN; // Verde per ON
            stateText = LangUtils.localize("on"); // Usiamo la traduzione di Mekanism
        } else {
            stateColor = EnumColor.DARK_RED; // Rosso per OFF
            stateText = LangUtils.localize("off"); // Usiamo la traduzione di Mekanism
        }
        
        // Combiniamo tutto nella stringa finale
        hudStringAdder.accept(EnumColor.DARK_GREY + name + ": " + stateColor + stateText);
    }
    
    // Gestisce il cambio di stato quando il giocatore preme il tasto di cambio modalità
    @Override
    public void changeMode(IModule<ModuleEnergyArrowsUnit> module, EntityPlayer player, ItemStack stack, int shift, boolean displayChangeMessage) {
        // Questo è il modo corretto e standard per attivare/disattivare un modulo in questa versione dell'API,
        // e mostra anche il messaggio al giocatore.
        module.toggleEnabled(player, LangUtils.localize("module.energy_arrows_unit.name"));
    }
}
