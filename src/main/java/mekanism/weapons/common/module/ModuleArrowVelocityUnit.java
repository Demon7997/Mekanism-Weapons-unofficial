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

public class ModuleArrowVelocityUnit implements ICustomModule<ModuleArrowVelocityUnit> {

    // ID STATICO MINUSCOLO PER IL RADIAL MENU (FONDAMENTALE)
    public static final ResourceLocation VELOCITY_ID = new ResourceLocation("mekanismweapons", "arrow_velocity");

    private IModuleConfigItem<VelocityMode> velocityMode;

    @Override
    public void init(IModule<ModuleArrowVelocityUnit> module, ModuleConfigItemCreator configItemCreator) {
        int selectableCount = module.getInstalledCount() + 1; 
        ModuleEnumData<VelocityMode> velocityData = new ModuleEnumData<>(VelocityMode.OFF, selectableCount);
        ILangEntry description = () -> "Velocity";
        velocityMode = configItemCreator.createConfigItem("velocity_level", description, velocityData);
    }

    @Override
    public void addHUDStrings(IModule<ModuleArrowVelocityUnit> module, EntityPlayer player, Consumer<String> hudStringAdder) {
        if (module.isEnabled()) {
            VelocityMode mode = velocityMode.get();
            // HUD coerente con gli altri moduli
            hudStringAdder.accept(EnumColor.DARK_GREY + "Velocity: " + EnumColor.INDIGO + LangUtils.localize(mode.getLangKey()));
        }
    }

    @Override
    public void changeMode(IModule<ModuleArrowVelocityUnit> module, EntityPlayer player, ItemStack stack, int shift, boolean displayChangeMessage) {
        VelocityMode current = velocityMode.get();
        int newIndex = Math.floorMod(current.ordinal() + shift, module.getInstalledCount() + 1);
        VelocityMode newMode = VelocityMode.values()[newIndex];
        
        velocityMode.set(newMode);
        
        if (!player.world.isRemote) {
            player.inventory.markDirty();
        }
        
        if (displayChangeMessage) {
            // Messaggio chat coerente
            player.sendMessage(new TextComponentString(EnumColor.DARK_GREY + "Mekanism: " + EnumColor.GREY + "Velocity: " + newMode.color + LangUtils.localize(newMode.getLangKey())));
        }
    }

    @Override
    public void addRadialModes(IModule<ModuleArrowVelocityUnit> module, ItemStack stack, Consumer<NestedRadialMode> adder) {
        if (module.isEnabled()) {
            RadialData<VelocityMode> radialData = new RadialData<VelocityMode>(VELOCITY_ID) {
                @Override public List<VelocityMode> getModes() {
                    return Arrays.asList(Arrays.copyOfRange(VelocityMode.values(), 0, module.getInstalledCount() + 1));
                }
            };

            // 1. Creiamo la stringa colorata
            String nomeColorato = EnumColor.PINK + LangUtils.localize(module.getData().getTranslationKey());

            // 2. La passiamo dentro TextComponentString
            adder.accept(new NestedRadialMode(radialData, new TextComponentString(nomeColorato), 
                new ResourceLocation("mekaweapons", "textures/gui/radial/damage_super.png")));
        }
    }
    
    @Nullable
    @Override
    public <MODE extends IRadialMode> MODE getMode(IModule<ModuleArrowVelocityUnit> module, ItemStack stack, RadialData<MODE> radialData) {
        // CONTROLLO BLINDATO DELL'ID
        if (VELOCITY_ID.equals(radialData.getIdentifier())) {
            return (MODE) velocityMode.get();
        }
        return null;
    }

    @Override
    public <MODE extends IRadialMode> boolean setMode(IModule<ModuleArrowVelocityUnit> module, EntityPlayer player, ItemStack stack, RadialData<MODE> radialData, MODE mode) {
        if (mode instanceof VelocityMode) {
            velocityMode.set((VelocityMode) mode);
            if (!player.world.isRemote) {
                player.inventory.markDirty();
            }
            return true;
        }
        return false;
    }
    
    public float getVelocityMultiplier(IModule<ModuleArrowVelocityUnit> module) {
        if (!module.isEnabled()) return 1.0F;
        return velocityMode.get().getMultiplier();
    }
    
    public double getEnergyCost(IModule<ModuleArrowVelocityUnit> module) {
        if (!module.isEnabled() || velocityMode.get() == VelocityMode.OFF) return 0;
        int level = velocityMode.get().ordinal();
        return level * MekanismWeaponsConfig.arrowVelocityEnergyUsage;
    }
    
    public enum VelocityMode implements IRadialMode, IHasTextComponent {
        // Ecco la scala da 1 a 8 con nomi sensati
        OFF(1.0F, "mekaweapons.hud.velocity.off", EnumColor.WHITE),
        LEVEL_1(1.2F, "mekaweapons.hud.velocity.low", EnumColor.PINK),          // Low
        LEVEL_2(1.4F, "mekaweapons.hud.velocity.medium", EnumColor.BRIGHT_GREEN), // Medium
        LEVEL_3(1.6F, "mekaweapons.hud.velocity.high", EnumColor.YELLOW),        // High
        LEVEL_4(1.8F, "mekaweapons.hud.velocity.veryhigh", EnumColor.ORANGE),    // Very High
        LEVEL_5(2.0F, "mekaweapons.hud.velocity.super", EnumColor.RED),          // Super
        LEVEL_6(2.2F, "mekaweapons.hud.velocity.ultra", EnumColor.DARK_RED),     // Ultra
        LEVEL_7(2.4F, "mekaweapons.hud.velocity.extreme", EnumColor.PURPLE),     // Extreme
        LEVEL_8(2.6F, "mekaweapons.hud.velocity.max", EnumColor.INDIGO);         // Max
        
        private final float multiplier;
        private final String langKey;
        final EnumColor color;

        VelocityMode(float m, String l, EnumColor c) { 
            multiplier = m; 
            langKey = l; 
            color = c;
        }
        
        public String getLangKey() { return langKey; }
        public float getMultiplier() { return multiplier; }

        @Override 
        public ITextComponent getTextComponent() { 
            return new TextComponentTranslation(langKey); 
        }

        @Override
        public ITextComponent sliceName() {
            ITextComponent component = new TextComponentTranslation(langKey);
            component.getStyle().setColor(this.color.textFormatting);
            return component;
        }

        @Override 
        public ResourceLocation icon() {
            // Se hai messo il file "off.png" nella tua cartella, usa "mekaweapons"
            if (this == OFF) {
                return new ResourceLocation("mekaweapons", "textures/gui/radial/off.png");
            }
            
            String nomeFile;
            
            switch (this) {
                case LEVEL_1:
                case LEVEL_2:
                    nomeFile = "damage_low.png"; 
                    break;
                    
                case LEVEL_3:
                case LEVEL_4:
                    nomeFile = "damage_medium.png"; 
                    break;
                    
                case LEVEL_5:
                case LEVEL_6:
                    nomeFile = "damage_high.png"; 
                    break;
                    
                case LEVEL_7:
                    nomeFile = "damage_super.png"; 
                    break;
                    
                case LEVEL_8:
                    nomeFile = "damage_extreme.png"; 
                    break;
                    
                default:
                    nomeFile = "damage_low.png";
            }
            
            // CORRETTO: ID "mekaweapons" e percorso con "textures/"
            return new ResourceLocation("mekaweapons", "textures/gui/radial/" + nomeFile);
        }
    }
}
