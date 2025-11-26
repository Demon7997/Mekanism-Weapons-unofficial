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
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TextComponentString;
import net.minecraft.util.text.TextComponentTranslation;
import org.jetbrains.annotations.Nullable;
import mekanism.common.util.LangUtils;

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
            // MODIFICA: Ora usa LangUtils.localize per scrivere "Low", "High" ecc. invece del numero
            hudStringAdder.accept(EnumColor.DARK_GREY + "Draw Speed: " + EnumColor.INDIGO + LangUtils.localize(level.getLangKey()));
        }
    }

    @Override
    public void changeMode(IModule<ModuleDrawSpeedUnit> module, EntityPlayer player, ItemStack stack, int shift, boolean displayChangeMessage) {
        DrawSpeedLevel current = speedLevelMode.get();
        int newIndex = Math.floorMod(current.ordinal() + shift, module.getInstalledCount() + 1);
        DrawSpeedLevel newMode = DrawSpeedLevel.values()[newIndex];
        
        speedLevelMode.set(newMode);
        
        if (!player.world.isRemote) {
            player.inventory.markDirty(); 
        }
        
        if (displayChangeMessage) {
            // MODIFICA: Usa LangUtils per scrivere OFF/Low/Medium/High in chat
            player.sendMessage(new TextComponentString(EnumColor.DARK_GREY + "Mekanism: " + EnumColor.GREY + "Draw Speed: " + EnumColor.INDIGO + LangUtils.localize(newMode.getLangKey())));
        }
    }

    // *** MODIFICA IMPORTANTE ***
    // Non restituiamo più i tick (int), ma il moltiplicatore (float)
    // Insanium usava un moltiplicatore float diretto.
    public float getDrawSpeedMultiplier() {
        return speedLevelMode.get().getMultiplier();
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
            speedLevelMode.set((DrawSpeedLevel) mode);
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

        public float getMultiplier() {
            if (this == OFF) return 1.0F;
            return 1.0F + (float)this.ordinal(); 
        }

        @Override
        public ITextComponent getTextComponent() {
            // Restituisce "OFF", "Low", "Medium", "High"
            return new TextComponentTranslation(this.langKey);
        }
        
        @Override
        public ITextComponent sliceName() {
            // Restituisce il nome tradotto (OFF, Low, Medium...) colorato
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
