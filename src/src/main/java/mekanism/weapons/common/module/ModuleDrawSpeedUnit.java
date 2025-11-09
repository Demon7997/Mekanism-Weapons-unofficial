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
import mekanism.common.util.ItemDataUtils; // Import necessario
import mekanism.common.util.LangUtils;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound; // Import necessario
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TextComponentString;
import net.minecraft.util.text.TextComponentTranslation;
import org.jetbrains.annotations.Nullable;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;

import java.util.Arrays;
import java.util.List;
import java.util.function.Consumer;

public class ModuleDrawSpeedUnit implements ICustomModule<ModuleDrawSpeedUnit> {

    private IModuleConfigItem<DrawSpeedLevel> speedLevelMode;

    @Override
    public void init(IModule<ModuleDrawSpeedUnit> module, ModuleConfigItemCreator configItemCreator) {
        int selectableCount = module.getInstalledCount() + 1;
        DrawSpeedLevel defaultMode = selectableCount > 1 ? DrawSpeedLevel.LEVEL_1 : DrawSpeedLevel.OFF;
        ModuleEnumData<DrawSpeedLevel> speedData = new ModuleEnumData<>(defaultMode, selectableCount);
        speedLevelMode = configItemCreator.createConfigItem("draw_speed_level", () -> "Draw Speed", speedData);
    }
    
    @Override
    public void addHUDStrings(IModule<ModuleDrawSpeedUnit> module, EntityPlayer player, Consumer<String> hudStringAdder) {
        if (module.isEnabled()) {
            DrawSpeedLevel level = speedLevelMode.get();
            hudStringAdder.accept(EnumColor.DARK_GREY + "Draw Speed: " + EnumColor.INDIGO + LangUtils.localize(level.getLangKey()));
        }
    }

    @Override
public void changeMode(IModule<ModuleDrawSpeedUnit> module, EntityPlayer player, ItemStack stack, int shift, boolean displayChangeMessage) {
    // Calcoliamo la nuova modalità
    DrawSpeedLevel current = speedLevelMode.get();
    int newIndex = Math.floorMod(current.ordinal() + shift, module.getInstalledCount() + 1);
    DrawSpeedLevel newMode = DrawSpeedLevel.values()[newIndex];
    
    // La impostiamo (questo aggiorna l'NBT in memoria)
    speedLevelMode.set(newMode);
    
    // QUESTA È LA RIGA CHE RISOLVE TUTTO.
    // Diciamo all'inventario del giocatore che qualcosa è cambiato, forzando la sincronizzazione.
    if (!player.world.isRemote) {
        player.inventory.markDirty(); 
    }
    
    // Mostriamo il messaggio al giocatore
    if (displayChangeMessage) {
        player.sendMessage(new TextComponentString(EnumColor.DARK_GREY + "Mekanism: " + EnumColor.GREY + "Draw Speed: " + LangUtils.localize(newMode.getLangKey())));
    }
}

    public int getDrawTicks() {
        return speedLevelMode.get().getDrawTicks();
    }
    
    @Override
    public void addRadialModes(IModule<ModuleDrawSpeedUnit> module, ItemStack stack, Consumer<NestedRadialMode> adder) {
        if (module.isEnabled()) {
            RadialData<DrawSpeedLevel> radialData = new RadialData<DrawSpeedLevel>(new ResourceLocation("mekanismweapons", "draw_speed")) {
                @Override public List<DrawSpeedLevel> getModes() {
                    return Arrays.asList(Arrays.copyOfRange(DrawSpeedLevel.values(), 0, module.getInstalledCount() + 1));
                }
            };
            adder.accept(new NestedRadialMode(radialData, new TextComponentTranslation(module.getData().getTranslationKey()), new ResourceLocation("mekanism", "textures/gui/modes/scroll.png")));
        }
    }

    @Nullable
    @Override
    public <MODE extends IRadialMode> MODE getMode(IModule<ModuleDrawSpeedUnit> module, ItemStack stack, RadialData<MODE> radialData) {
        if (radialData.getIdentifier().equals(new ResourceLocation("mekanismweapons", "draw_speed"))) {
            return (MODE) speedLevelMode.get();
        }
        return null;
    }
    
@Override
public <MODE extends IRadialMode> boolean setMode(IModule<ModuleDrawSpeedUnit> module, EntityPlayer player, ItemStack stack, RadialData<MODE> radialData, MODE mode) {
    if (mode instanceof DrawSpeedLevel) {
        // Impostiamo la nuova modalità scelta dal menu radiale
        speedLevelMode.set((DrawSpeedLevel) mode);

        // E ANCHE QUI, FORZIAMO LA SINCRONIZZAZIONE.
        if (!player.world.isRemote) {
            player.inventory.markDirty();
        }
        return true;
    }
    return false;
}
    
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

        public int getDrawTicks() {
            if (this == OFF) return 20;
            return 20 - (this.ordinal() * 5);
        }

        @Override
        public ITextComponent getTextComponent() {
            return new TextComponentString(Integer.toString(this.ordinal()));
        }
        
        @Override
        public ITextComponent sliceName() {
            ITextComponent component = new TextComponentTranslation(this.langKey);
            component.getStyle().setColor(this.color.textFormatting);
            return component;
        }
        
        @Override
        public ResourceLocation icon() {
            if (this == OFF) {
                return new ResourceLocation("mekanism", "textures/gui/disabled.png");
            }
            return new ResourceLocation("mekanism", "textures/gui/modes/scroll.png"); 
        }
    }
}
