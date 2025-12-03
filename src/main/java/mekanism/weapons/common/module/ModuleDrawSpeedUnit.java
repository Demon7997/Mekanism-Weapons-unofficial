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
import mekanism.weapons.MekanismWeapons;
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

    // ID Statico per il Menu Radiale
    public static final ResourceLocation RADIAL_ID = new ResourceLocation(MekanismWeapons.MODID, "draw_speed");

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
        DrawSpeedLevel current = speedLevelMode.get();
        int newIndex = Math.floorMod(current.ordinal() + shift, module.getInstalledCount() + 1);
        DrawSpeedLevel newMode = DrawSpeedLevel.values()[newIndex];
        
        speedLevelMode.set(newMode);
        
        // Salvataggio dati (importante!)
        if (!player.world.isRemote) {
            player.inventory.markDirty(); 
        }
        
        if (displayChangeMessage) {
            player.sendMessage(new TextComponentString(EnumColor.DARK_GREY + "Mekanism: " + EnumColor.GREY + "Draw Speed: " + EnumColor.INDIGO + LangUtils.localize(newMode.getLangKey())));
        }
    }

    public float getDrawSpeedMultiplier() {
        return speedLevelMode.get().getMultiplier();
    }
    
    @Override
    public void addRadialModes(IModule<ModuleDrawSpeedUnit> module, ItemStack stack, Consumer<NestedRadialMode> adder) {
        if (module.isEnabled()) {
            RadialData<DrawSpeedLevel> radialData = new RadialData<DrawSpeedLevel>(RADIAL_ID) {
                @Override public List<DrawSpeedLevel> getModes() {
                    return Arrays.asList(Arrays.copyOfRange(DrawSpeedLevel.values(), 0, module.getInstalledCount() + 1));
                }
            };
            
            // 1. Creiamo la stringa colorata
            String nomeColorato = EnumColor.YELLOW + LangUtils.localize(module.getData().getTranslationKey());

            // 2. La passiamo dentro TextComponentString
            adder.accept(new NestedRadialMode(radialData, new TextComponentString(nomeColorato), 
                new ResourceLocation("mekaweapons", "textures/gui/radial/damage_low.png"))); 
        }
    }

    @Nullable
    @Override
    public <MODE extends IRadialMode> MODE getMode(IModule<ModuleDrawSpeedUnit> module, ItemStack stack, RadialData<MODE> radialData) {
        // Controllo ID: Obbligatorio perché l'arco ha più moduli
        if (radialData.getIdentifier().equals(RADIAL_ID)) {
            return (MODE) speedLevelMode.get();
        }
        return null;
    }
    
    @Override
    public <MODE extends IRadialMode> boolean setMode(IModule<ModuleDrawSpeedUnit> module, EntityPlayer player, ItemStack stack, RadialData<MODE> radialData, MODE mode) {
        if (mode instanceof DrawSpeedLevel) {
            speedLevelMode.set((DrawSpeedLevel) mode);
            if (!player.world.isRemote) {
                player.inventory.markDirty();
            }
            return true;
        }
        return false;
    }
    
     public enum DrawSpeedLevel implements IRadialMode, IHasTextComponent {
        OFF("mekaweapons.hud.drawspeed.off", EnumColor.WHITE), 
        LEVEL_1("mekaweapons.hud.drawspeed.low", EnumColor.PINK), 
        LEVEL_2("mekaweapons.hud.drawspeed.medium", EnumColor.BRIGHT_GREEN), 
        LEVEL_3("mekaweapons.hud.drawspeed.high", EnumColor.YELLOW);
        
        private final String langKey;
        private final EnumColor color;

        DrawSpeedLevel(String langKey, EnumColor color) {
            this.langKey = langKey;
            this.color = color;
        }

        public String getLangKey() { return langKey; }

        public float getMultiplier() {
            if (this == OFF) return 1.0F;
            return 1.0F + (float)this.ordinal(); 
        }

        @Override
        public ITextComponent getTextComponent() {
            return new TextComponentTranslation(this.langKey);
        }
        
        @Override
        public ITextComponent sliceName() {
            ITextComponent component = new TextComponentTranslation(this.langKey);
            component.getStyle().setColor(this.color.textFormatting);
            return component;
        }
        
        @Override
        public ResourceLocation icon() {
            // Punta al tuo off.png
            if (this == OFF) {
                return new ResourceLocation("mekaweapons", "textures/gui/radial/off.png");
            }
            
            String nomeFile;
            switch (this) {
                case LEVEL_1: 
                    nomeFile = "damage_low.png"; 
                    break;
                case LEVEL_2: 
                    nomeFile = "damage_medium.png"; 
                    break;
                case LEVEL_3: 
                    nomeFile = "damage_high.png"; 
                    break;
                default: 
                    nomeFile = "damage_low.png";
            }
            // CORRETTO: ID "mekaweapons" e percorso con "textures/"
            return new ResourceLocation("mekaweapons", "textures/gui/radial/" + nomeFile); 
        }
    }
}
