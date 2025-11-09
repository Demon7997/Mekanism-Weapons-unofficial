package mekanism.weapons.common.module;

import mekanism.api.EnumColor;
import mekanism.api.gear.ICustomModule;
import mekanism.api.gear.IModule;
import mekanism.api.gear.config.IModuleConfigItem;
import mekanism.api.gear.config.ModuleConfigItemCreator;
import mekanism.api.gear.config.ModuleEnumData;
import mekanism.api.radial.RadialData;
import mekanism.api.radial.mode.IRadialMode;
import mekanism.api.radial.mode.NestedRadialMode;
import mekanism.api.text.IHasTextComponent;
import mekanism.common.util.LangUtils;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TextComponentString;
import net.minecraft.util.text.TextComponentTranslation;
import org.jetbrains.annotations.Nullable;

import java.util.Arrays;
import java.util.List;
import java.util.function.Consumer;

public class ModuleDrawSpeedUnit implements ICustomModule<ModuleDrawSpeedUnit> {

    private IModuleConfigItem<DrawSpeedLevel> speedLevelMode;

    @Override
    public void init(IModule<ModuleDrawSpeedUnit> module, ModuleConfigItemCreator configItemCreator) {
        int selectableCount = module.getInstalledCount() + 1; // +1 per includere OFF
        DrawSpeedLevel defaultMode = selectableCount > 1 ? DrawSpeedLevel.LEVEL_1 : DrawSpeedLevel.OFF;
        ModuleEnumData<DrawSpeedLevel> speedData = new ModuleEnumData<>(defaultMode, selectableCount);
        
        speedLevelMode = configItemCreator.createConfigItem("draw_speed_level", () -> "Draw Speed", speedData);
    }
    
    // Aggiunge la scritta "Draw Speed: LOW" etc. all'HUD (ora sempre INDIGO)
    @Override
    public void addHUDStrings(IModule<ModuleDrawSpeedUnit> module, EntityPlayer player, Consumer<String> hudStringAdder) {
        if (module.isEnabled()) {
            String name = LangUtils.localize("module.draw_speed_unit.name");
            DrawSpeedLevel level = speedLevelMode.get();
            String modeText = LangUtils.localize(level.getLangKey());
            
            // Richiesta #1: Colore sempre Indaco
            hudStringAdder.accept(EnumColor.DARK_GREY + name + ": " + EnumColor.INDIGO + modeText);
        }
    }

    // Richiesta #2: Aggiungiamo il metodo per far funzionare Shift+Rotellina
    @Override
    public void changeMode(IModule<ModuleDrawSpeedUnit> module, EntityPlayer player, ItemStack stack, int shift, boolean displayChangeMessage) {
        // Logica standard per cambiare modalità in un enum
        DrawSpeedLevel current = speedLevelMode.get();
        // Calcola il nuovo livello, assicurandosi che rimanga nei limiti dei livelli installati
        int newIndex = Math.floorMod(current.ordinal() + shift, module.getInstalledCount() + 1);
        DrawSpeedLevel newMode = DrawSpeedLevel.values()[newIndex];
        speedLevelMode.set(newMode);
        
        // Mostra il messaggio di cambio al giocatore
        if (displayChangeMessage) {
            String message = LangUtils.localize("module.draw_speed_unit.name") + ": " + LangUtils.localize(newMode.getLangKey());
            player.sendMessage(new TextComponentString(EnumColor.DARK_GREY + "Mekanism: " + EnumColor.GREY + message));
        }
    }

    // --- Metodi Helper ---
    public int getDrawTicks() {
        return speedLevelMode.get().getDrawTicks();
    }
    
    // ====================================================================
    // ==> I PEZZI CHE MANCAVANO SONO QUI <==
    // ====================================================================
    
    // --- METODI PER IL MENÙ RADIALE ---

    @Override
    public void addRadialModes(IModule<ModuleDrawSpeedUnit> module, ItemStack stack, Consumer<NestedRadialMode> adder) {
        if (module.isEnabled()) {
            // Creiamo un set di dati per il menù radiale, dandogli un nome unico
            RadialData<DrawSpeedLevel> radialData = new RadialData<DrawSpeedLevel>(new ResourceLocation("mekanismweapons", "draw_speed")) {
                @Override public List<DrawSpeedLevel> getModes() {
                    // Mostra solo i livelli che il giocatore ha installato
                    return Arrays.asList(Arrays.copyOfRange(DrawSpeedLevel.values(), 0, module.getInstalledCount() + 1));
                }
            };
            // Aggiungiamo il nostro sottomenù al menù radiale principale
            adder.accept(new NestedRadialMode(radialData, new TextComponentTranslation(module.getData().getTranslationKey()), new ResourceLocation("mekanism", "textures/gui/modes/scroll.png")));
        }
    }

    @Nullable
    @Override
    public <MODE extends IRadialMode> MODE getMode(IModule<ModuleDrawSpeedUnit> module, ItemStack stack, RadialData<MODE> radialData) {
        // Quando il menù radiale chiede "qual è la modalità attuale?", noi rispondiamo.
        if (radialData.getIdentifier().equals(new ResourceLocation("mekanismweapons", "draw_speed"))) {
            return (MODE) speedLevelMode.get();
        }
        return null;
    }

    @Override
    public <MODE extends IRadialMode> boolean setMode(IModule<ModuleDrawSpeedUnit> module, EntityPlayer player, ItemStack stack, RadialData<MODE> radialData, MODE mode) {
        // Quando il giocatore clicca una modalità nel menù radiale, noi la impostiamo.
        if (mode instanceof DrawSpeedLevel) {
            speedLevelMode.set((DrawSpeedLevel) mode);
            return true;
        }
        return false;
    }
    
    // --- Enum Modificata (rimane uguale a prima) ---
     public enum DrawSpeedLevel implements IRadialMode, IHasTextComponent {
        OFF("mekaweapons.hud.off", EnumColor.WHITE), 
        LEVEL_1("mekaweapons.hud.low", EnumColor.PINK), 
        LEVEL_2("mekaweapons.hud.medium", EnumColor.BRIGHT_GREEN), 
        LEVEL_3("mekaweapons.hud.high", EnumColor.YELLOW);
        
        private final String langKey;
        private final EnumColor color;

        DrawSpeedLevel(String langKey, EnumColor color) {
            this.langKey = langKey;
            this.color = color;
        }

        public String getLangKey() {
            return langKey;
        }

        public EnumColor getColor() {
            return color;
        }

        public int getDrawTicks() {
            if (this == OFF) return 20;
            return 20 - (this.ordinal() * 5);
        }

        // Questo metodo definisce il testo nel Module Tweaker (es. "0", "1", "2")
        @Override
        public ITextComponent getTextComponent() {
            return new TextComponentString(Integer.toString(this.ordinal()));
        }
        
        // QUESTO METODO È STATO MODIFICATO
        // Ora definisce il testo colorato per il Menù Radiale (es. "Off", "Low", "Medium")
        @Override
        public ITextComponent sliceName() {
            // Prendiamo il testo dalla traduzione (es. "Low")
            ITextComponent component = new TextComponentTranslation(this.langKey);
            // Applichiamo il colore corrispondente (es. Rosa per "Low")
            component.getStyle().setColor(this.color.textFormatting);
            return component;
        }
        
        // Questo metodo definisce l'icona per il Menù Radiale
        @Override
        public ResourceLocation icon() {
            if (this == OFF) {
                return new ResourceLocation("mekanism", "textures/gui/disabled.png");
            }
            return new ResourceLocation("mekanism", "textures/gui/modes/scroll.png"); 
        }
    }
}
