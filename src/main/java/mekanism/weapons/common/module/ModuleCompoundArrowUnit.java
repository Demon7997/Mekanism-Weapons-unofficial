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
import mekanism.api.text.ILangEntry;
import mekanism.common.util.LangUtils;
import mekanism.weapons.config.MekanismWeaponsConfig;
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

public class ModuleCompoundArrowUnit implements ICustomModule<ModuleCompoundArrowUnit> {

    // *** MODIFICA IMPORTANTE: ID ALLINEATO ALLA CARTELLA 'mekaweapons' ***
    public static final ResourceLocation RADIAL_ID = new ResourceLocation("mekaweapons", "compound_arrow");

    private IModuleConfigItem<ArrowLevel> arrowLevelMode;

    @Override
    public void init(IModule<ModuleCompoundArrowUnit> module, ModuleConfigItemCreator configItemCreator) {
        int selectableCount = module.getInstalledCount() + 1;
        ModuleEnumData<ArrowLevel> arrowData = new ModuleEnumData<>(ArrowLevel.OFF, selectableCount);
        ILangEntry description = () -> "Arrow Count";
        arrowLevelMode = configItemCreator.createConfigItem("arrow_level", description, arrowData);
    }

    @Override
    public void addHUDStrings(IModule<ModuleCompoundArrowUnit> module, EntityPlayer player, Consumer<String> hudStringAdder) {
        if (module.isEnabled()) {
            ArrowLevel mode = arrowLevelMode.get();
            if (mode != null) {
                hudStringAdder.accept(EnumColor.DARK_GREY + "Arrow Count: " + EnumColor.INDIGO + LangUtils.localize(mode.getLangKey()));
            }
        }
    }

    @Override
    public void changeMode(IModule<ModuleCompoundArrowUnit> module, EntityPlayer player, ItemStack stack, int shift, boolean displayChangeMessage) {
        ArrowLevel current = arrowLevelMode.get();
        int newIndex = Math.floorMod(current.ordinal() + shift, module.getInstalledCount() + 1);
        ArrowLevel newMode = ArrowLevel.values()[newIndex];
        
        arrowLevelMode.set(newMode);
        
        if (!player.world.isRemote) {
            player.inventory.markDirty();
        }
        
        if (displayChangeMessage) {
            player.sendMessage(new TextComponentString(EnumColor.DARK_GREY + "Mekanism: " + EnumColor.GREY + "Arrow Count: " + newMode.color + LangUtils.localize(newMode.getLangKey())));
        }
    }

    @Override
    public void addRadialModes(IModule<ModuleCompoundArrowUnit> module, ItemStack stack, Consumer<NestedRadialMode> adder) {
        if (module.isEnabled()) {
            RadialData<ArrowLevel> radialData = new RadialData<ArrowLevel>(RADIAL_ID) {
                @Override public List<ArrowLevel> getModes() {
                    return Arrays.asList(Arrays.copyOfRange(ArrowLevel.values(), 0, module.getInstalledCount() + 1));
                }
            };
            
            // 1. Creiamo la stringa colorata
            String nomeColorato = EnumColor.ORANGE + LangUtils.localize(module.getData().getTranslationKey());

            // 2. La passiamo dentro TextComponentString
            adder.accept(new NestedRadialMode(radialData, new TextComponentString(nomeColorato), 
                new ResourceLocation("mekaweapons", "textures/gui/radial/damage_high.png")));
        }
    }

    @Nullable
    @Override
    public <MODE extends IRadialMode> MODE getMode(IModule<ModuleCompoundArrowUnit> module, ItemStack stack, RadialData<MODE> radialData) {
        if (radialData.getIdentifier().equals(RADIAL_ID)) {
            return (MODE) arrowLevelMode.get();
        }
        return null;
    }
    
    @Override
    public <MODE extends IRadialMode> boolean setMode(IModule<ModuleCompoundArrowUnit> module, EntityPlayer player, ItemStack stack, RadialData<MODE> radialData, MODE mode) {
        if (mode instanceof ArrowLevel) {
            arrowLevelMode.set((ArrowLevel) mode);
            if (!player.world.isRemote) {
                player.inventory.markDirty();
            }
            return true;
        }
        return false;
    }

    public int getArrowCount() {
        return arrowLevelMode.get().getArrowCount();
    }
    
    public double getEnergyCost() {
        int extraArrows = getArrowCount() - 1;
        if (extraArrows > 0) {
            return extraArrows * MekanismWeaponsConfig.compoundArrowEnergyUsage;
        }
        return 0;
    }
    
    public enum ArrowLevel implements IRadialMode, IHasTextComponent {
        OFF("mekaweapons.hud.compoundarrow.off", 1, EnumColor.WHITE),
        LOW("mekaweapons.hud.compoundarrow.low", 2, EnumColor.PINK),
        MEDIUM("mekaweapons.hud.compoundarrow.medium", 4, EnumColor.BRIGHT_GREEN),
        HIGH("mekaweapons.hud.compoundarrow.high", 6, EnumColor.YELLOW),
        SUPER_HIGH("mekaweapons.hud.compoundarrow.superhigh", 8, EnumColor.ORANGE); 
        
        private final String langKey;
        private final int arrowCount;
        final EnumColor color; 

        ArrowLevel(String langKey, int count, EnumColor color) { 
            this.langKey = langKey;
            this.arrowCount = count; 
            this.color = color;
        }

        public String getLangKey() { return langKey; }

        @Override 
        public ITextComponent getTextComponent() { 
            return new TextComponentTranslation(langKey); 
        }

        public int getArrowCount() { return arrowCount; }

        @Override 
        public ITextComponent sliceName() { 
            ITextComponent component = new TextComponentTranslation(langKey);
            component.getStyle().setColor(this.color.textFormatting);
            return component; 
        }

        @Override 
        public ResourceLocation icon() {
            String nomeFile;
            
            if (this == OFF) {
                // Assicurati che il file si chiami off.png
                nomeFile = "off.png"; 
            } else {
                switch (this) {
                    case LOW:        
                        nomeFile = "damage_low.png"; 
                        break;
                    case MEDIUM:     
                        nomeFile = "damage_medium.png"; 
                        break;
                    case HIGH:       
                        nomeFile = "damage_high.png"; 
                        break;
                    case SUPER_HIGH: 
                        nomeFile = "damage_super.png"; 
                        break;
                    default:         
                        nomeFile = "off.png";
                }
            }
            
            // *** CORREZIONE QUI SOTTO: HO AGGIUNTO "textures/" ***
            return new ResourceLocation("mekaweapons", "textures/gui/radial/" + nomeFile);
        }
    }
}
