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

public class ModuleCompoundArrowUnit implements ICustomModule<ModuleCompoundArrowUnit> {

    private IModuleConfigItem<ArrowLevel> arrowLevelMode;

    @Override
    public void init(IModule<ModuleCompoundArrowUnit> module, ModuleConfigItemCreator configItemCreator) {
        int selectableCount = module.getInstalledCount() + 1;
        ModuleEnumData<ArrowLevel> arrowData = new ModuleEnumData<>(ArrowLevel.OFF, selectableCount);
        ILangEntry description = () -> "Arrow Count";
        arrowLevelMode = configItemCreator.createConfigItem("arrow_level", description, arrowData);
    }

    @Override
    public void changeMode(IModule<ModuleCompoundArrowUnit> module, EntityPlayer player, ItemStack stack, int shift, boolean displayChangeMessage) {
        ArrowLevel current = arrowLevelMode.get();
        int newIndex = Math.floorMod(current.ordinal() + shift, module.getInstalledCount() + 1);
        ArrowLevel newMode = ArrowLevel.values()[newIndex];
        arrowLevelMode.set(newMode);
        if (displayChangeMessage) {
            player.sendMessage(getModeScrollComponent(module, stack));
        }
    }

    @Nullable
    @Override
    public ITextComponent getModeScrollComponent(IModule<ModuleCompoundArrowUnit> module, ItemStack stack) {
        return new TextComponentTranslation("mekanism.module.mode.text", 
             new TextComponentTranslation(module.getData().getTranslationKey()), 
             arrowLevelMode.get().getTextComponent()
         );
    }

    @Override
    public void addRadialModes(IModule<ModuleCompoundArrowUnit> module, ItemStack stack, Consumer<NestedRadialMode> adder) {
        if (module.isEnabled()) {
            RadialData<ArrowLevel> radialData = new RadialData<ArrowLevel>(new ResourceLocation(MekanismWeapons.MODID, "compound_arrow")) {
                @Override public List<ArrowLevel> getModes() {
                    return Arrays.asList(Arrays.copyOfRange(ArrowLevel.values(), 0, module.getInstalledCount() + 1));
                }
            };
            adder.accept(new NestedRadialMode(radialData, new TextComponentTranslation(module.getData().getTranslationKey()), new ResourceLocation("mekanism", "textures/gui/disabled.png")));
        }
    }

    public int getArrowCount() {
        return arrowLevelMode.get().getArrowCount();
    }
    
    public double getEnergyCost() {
        // Il costo è basato sulle frecce *extra*
        int extraArrows = getArrowCount() - 1;
        if (extraArrows > 0) {
            return extraArrows * MekanismWeaponsConfig.compoundArrowEnergyUsage;
        }
        return 0;
    }
    
    public enum ArrowLevel implements IRadialMode, IHasTextComponent {
        OFF(1), // Livello 0 spara 1 freccia
        LEVEL_1(2), 
        LEVEL_2(4),
        LEVEL_3(6), 
        LEVEL_4(8);
        
        private final int arrowCount;
        ArrowLevel(int count) { arrowCount = count; }
        @Override public ITextComponent getTextComponent() { 
            if (this == OFF) return new TextComponentString("Off");
            return new TextComponentString(arrowCount + "x"); 
        }
        public int getArrowCount() { return arrowCount; }
        @Override public ITextComponent sliceName() { return getTextComponent(); }
        @Override public ResourceLocation icon() {
            if (this == OFF) return new ResourceLocation("mekanism", "textures/gui/disabled.png");
            return new ResourceLocation("mekanism", "textures/gui/modes/scroll.png");
        }
    }
}
