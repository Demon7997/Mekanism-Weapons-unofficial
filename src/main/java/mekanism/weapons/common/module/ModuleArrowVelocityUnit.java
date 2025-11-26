package mekanism.weapons.common.module;

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
import mekanism.api.EnumColor;
import mekanism.weapons.MekanismWeapons;
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

    private IModuleConfigItem<VelocityMode> velocityMode;

    @Override
    public void init(IModule<ModuleArrowVelocityUnit> module, ModuleConfigItemCreator configItemCreator) {
        int selectableCount = module.getInstalledCount() + 1; // +1 per includere OFF
        ModuleEnumData<VelocityMode> velocityData = new ModuleEnumData<>(VelocityMode.OFF, selectableCount);
        ILangEntry description = () -> "Velocity";
        velocityMode = configItemCreator.createConfigItem("velocity_level", description, velocityData);
    }

    @Override
    public void addHUDStrings(IModule<ModuleArrowVelocityUnit> module, EntityPlayer player, Consumer<String> hudStringAdder) {
        if (module.isEnabled()) {
            VelocityMode mode = velocityMode.get();
            hudStringAdder.accept(EnumColor.DARK_GREY + "Velocity: " + EnumColor.INDIGO + mode.getTextComponent().getUnformattedComponentText());
        }
    }

    @Override
    public void changeMode(IModule<ModuleArrowVelocityUnit> module, EntityPlayer player, ItemStack stack, int shift, boolean displayChangeMessage) {
        VelocityMode current = velocityMode.get();
        int newIndex = Math.floorMod(current.ordinal() + shift, module.getInstalledCount() + 1);
        VelocityMode newMode = VelocityMode.values()[newIndex];
        
        velocityMode.set(newMode);
        
        if (displayChangeMessage) {
            player.sendMessage(new TextComponentString(EnumColor.DARK_GREY + "Mekanism: " + EnumColor.GREY + "Velocity: " + EnumColor.INDIGO + newMode.getTextComponent().getUnformattedComponentText()));
        }
    }

    @Nullable
    @Override
    public ITextComponent getModeScrollComponent(IModule<ModuleArrowVelocityUnit> module, ItemStack stack) {
        return new TextComponentTranslation("mekanism.module.mode.text", 
             new TextComponentTranslation(module.getData().getTranslationKey()), 
             velocityMode.get().getTextComponent()
         );
    }

    @Override
    public void addRadialModes(IModule<ModuleArrowVelocityUnit> module, ItemStack stack, Consumer<NestedRadialMode> adder) {
        if (module.isEnabled()) {
            RadialData<VelocityMode> radialData = new RadialData<VelocityMode>(new ResourceLocation(MekanismWeapons.MODID, "arrow_velocity")) {
                @Override public List<VelocityMode> getModes() {
                    return Arrays.asList(Arrays.copyOfRange(VelocityMode.values(), 0, module.getInstalledCount() + 1));
                }
            };
            adder.accept(new NestedRadialMode(radialData, new TextComponentTranslation(module.getData().getTranslationKey()), new ResourceLocation("mekanism", "textures/gui/disabled.png")));
        }
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
        OFF(1.0F, "Off"),
        LEVEL_1(1.2F, "1.2x"), 
        LEVEL_2(1.4F, "1.4x"), 
        LEVEL_3(1.6F, "1.6x"),
        LEVEL_4(1.8F, "1.8x"), 
        LEVEL_5(2.0F, "2.0x"), 
        LEVEL_6(2.2F, "2.2x"),
        LEVEL_7(2.4F, "2.4x"), 
        LEVEL_8(2.6F, "2.6x");
        
        private final float multiplier;
        private final String label;
        VelocityMode(float m, String l) { multiplier = m; label = l; }
        @Override public ITextComponent getTextComponent() { return new TextComponentString(label); }
        public float getMultiplier() { return multiplier; }

       @Override
        public ITextComponent sliceName() {
            ITextComponent component = getTextComponent();
            component.getStyle().setColor(EnumColor.INDIGO.textFormatting);
            return component;
        }

        @Override public ResourceLocation icon() {
            if (this == OFF) return new ResourceLocation("mekanism", "textures/gui/disabled.png");
            return new ResourceLocation("mekanism", "textures/gui/modes/scroll.png");
        }
    }
}
