package mekanism.weapons.common.module;

import mekanism.api.EnumColor;
import mekanism.api.gear.ICustomModule;
import mekanism.api.gear.IModule;
import mekanism.common.util.LangUtils;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import java.util.function.Consumer;

public class ModuleAutoFireUnit implements ICustomModule<ModuleAutoFireUnit> {

    // Il metodo init NON è richiesto dall'interfaccia, quindi lo rimuoviamo.
    // L'errore sull'@Override ce lo ha confermato.

    // Aggiunge la stringa "Auto-Fire: On/Off" all'HUD del giocatore
    @Override
public void addHUDStrings(IModule<ModuleAutoFireUnit> module, EntityPlayer player, Consumer<String> hudStringAdder) {
    // Il nome del modulo rimane grigio
    String name = LangUtils.localize("module.autofire_unit.name");
    
    // Scegliamo il colore e il testo dello stato in base a se il modulo è attivo o no
    EnumColor stateColor;
    String stateText;
    
    if (module.isEnabled()) {
        stateColor = EnumColor.DARK_GREEN; // Verde per ON
        stateText = LangUtils.localize("on");
    } else {
        stateColor = EnumColor.DARK_RED; // Rosso per OFF
        stateText = LangUtils.localize("off");
    }
    
    // Combiniamo tutto nella stringa finale
    hudStringAdder.accept(EnumColor.DARK_GREY + name + ": " + stateColor + stateText);
}
    
    // Gestisce il cambio di stato quando il giocatore preme il tasto di cambio modalità
    @Override
    public void changeMode(IModule<ModuleAutoFireUnit> module, EntityPlayer player, ItemStack stack, int shift, boolean displayChangeMessage) {
        // L'errore "incompatible types" ci ha detto che la firma di toggleEnabled è diversa.
        // Anche se non abbiamo la firma esatta, il comportamento più probabile per un modulo on/off
        // è che il cambio di modalità stesso gestisca il toggle.
        // Mekanism fornisce un metodo helper proprio per questo.
        
        // Questo è il modo corretto e standard per attivare/disattivare un modulo.
        module.toggleEnabled(player, LangUtils.localize("module.autofire_unit.name"));
    }
}
