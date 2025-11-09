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
import mekanism.common.MekanismLang;
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

public class ModuleWeaponAttackAmplificationUnit implements ICustomModule<ModuleWeaponAttackAmplificationUnit> {

    private IModuleConfigItem<DamageMode> damageMode;

    @Override
    public void init(IModule<ModuleWeaponAttackAmplificationUnit> module, ModuleConfigItemCreator configItemCreator) {
        int selectableCount = module.getInstalledCount() + 2;
        DamageMode preferredDefault = DamageMode.MEDIUM;
        DamageMode actualDefault;
        if (preferredDefault.ordinal() < selectableCount) {
            actualDefault = preferredDefault;
        } else {
            actualDefault = DamageMode.values()[selectableCount - 1];
        }
        ModuleEnumData<DamageMode> damageData = new ModuleEnumData<>(actualDefault, selectableCount);
        damageMode = configItemCreator.createConfigItem("damage_level", MekanismLang.MODULE_ATTACK_DAMAGE, damageData);
    }

    @Override
public void addHUDStrings(IModule<ModuleWeaponAttackAmplificationUnit> module, EntityPlayer player, Consumer<String> hudStringAdder) {
    // La logica è la stessa, ma usa i parametri corretti
    if (module.isEnabled()) {
        DamageMode mode = damageMode.get();
        
        // Usiamo il metodo standard di Minecraft per la traduzione
        String label = new TextComponentTranslation("mekaweapons.hud.damage_amplification").getUnformattedText();
        
        // Calcoliamo il danno
        double damage = MekanismWeaponsConfig.mekaTanaBaseDamage * mode.getMultiplier();
        
        // Costruiamo la stringa con i codici colore corretti
        String hudString = EnumColor.DARK_GREY + label + ": " + EnumColor.INDIGO + (int)damage;
        
        // Invece di "list.add", usiamo "hudStringAdder.accept" come richiesto dall'interfaccia
        hudStringAdder.accept(hudString);
    }
}

    @Override
    public void changeMode(IModule<ModuleWeaponAttackAmplificationUnit> module, EntityPlayer player, ItemStack stack, int shift, boolean displayChangeMessage) {
        DamageMode current = damageMode.get();
        int newIndex = Math.floorMod(current.ordinal() + shift, module.getInstalledCount() + 2);
        DamageMode newMode = DamageMode.byIndex(newIndex);
        damageMode.set(newMode);
        if (displayChangeMessage) {
            player.sendMessage(getModeScrollComponent(module, stack));
        }
    }

    @Nullable
    @Override
    public ITextComponent getModeScrollComponent(IModule<ModuleWeaponAttackAmplificationUnit> module, ItemStack stack) {
        DamageMode mode = damageMode.get();
        ITextComponent text = mode.sliceName();
        ITextComponent openParen = new TextComponentString(" (");
        openParen.getStyle().setColor(EnumColor.INDIGO.textFormatting);
        ITextComponent levelNumber = new TextComponentString(mode.label);
        levelNumber.getStyle().setColor(EnumColor.AQUA.textFormatting);
        ITextComponent closeParen = new TextComponentString(")");
        closeParen.getStyle().setColor(EnumColor.INDIGO.textFormatting);
        text.appendSibling(openParen);
        text.appendSibling(levelNumber);
        text.appendSibling(closeParen);
        return text;
    }

    @Override
    public void addRadialModes(IModule<ModuleWeaponAttackAmplificationUnit> module, ItemStack stack, Consumer<NestedRadialMode> adder) {
        if (module.isEnabled()) {
            RadialData<DamageMode> radialData = new RadialData<DamageMode>(new ResourceLocation(MekanismWeapons.MODID, "damage_amplification")) {
                @Override public List<DamageMode> getModes() {
                    return Arrays.asList(Arrays.copyOfRange(DamageMode.values(), 0, module.getInstalledCount() + 2));
                }
            };
            adder.accept(new NestedRadialMode(radialData, new TextComponentTranslation(module.getData().getTranslationKey()), new ResourceLocation("mekanism", "textures/gui/disabled.png")));
        }
    }

    @Nullable
    @Override
    public <MODE extends IRadialMode> MODE getMode(IModule<ModuleWeaponAttackAmplificationUnit> module, ItemStack stack, RadialData<MODE> radialData) {
        return (MODE) damageMode.get();
    }

    @Override
    public <MODE extends IRadialMode> boolean setMode(IModule<ModuleWeaponAttackAmplificationUnit> module, EntityPlayer player, ItemStack stack, RadialData<MODE> radialData, MODE mode) {
        if (mode instanceof DamageMode) {
            damageMode.set((DamageMode) mode);
            return true;
        }
        return false;
    }

    public float getDamageBonus(IModule<ModuleWeaponAttackAmplificationUnit> module) {
        if (!module.isEnabled()) return 1F;
        return damageMode.get().getMultiplier();
    }

    public double getEnergyCost(IModule<ModuleWeaponAttackAmplificationUnit> module) {
        if (!module.isEnabled() || damageMode.get() == DamageMode.OFF) return 0;
        int level = damageMode.get().ordinal();
        return level * MekanismWeaponsConfig.attackAmplificationEnergyUsage;
    }

    public DamageMode getDamageMode() {
        return damageMode.get();
    }

    public enum DamageMode implements IRadialMode, IHasTextComponent {
        OFF("mekanism.mode.damage.off", "0", 0F, EnumColor.WHITE),
        LOW("mekanism.mode.damage.low", "1", 1F, EnumColor.PINK),
        MEDIUM("mekanism.mode.damage.medium", "2", 2F, EnumColor.BRIGHT_GREEN),
        HIGH("mekanism.mode.damage.high", "3", 3F, EnumColor.YELLOW),
        SUPER_HIGH("mekanism.mode.damage.superhigh", "4", 4F, EnumColor.ORANGE),
        ULTRA_HIGH("mekanism.mode.damage.extreme", "5", 5F, EnumColor.DARK_RED);

        private final String langKey;
        private final String label;
        private final float multiplier;
        private final EnumColor color;

        DamageMode(String lang, String lbl, float mult, EnumColor c) {
            langKey = lang;
            label = lbl;
            multiplier = mult;
            color = c;
        }

        public static DamageMode byIndex(int i) {
            if (i < 0 || i >= values().length) return OFF;
            return values()[i];
        }

        public float getMultiplier() { return multiplier; }
        public EnumColor getColor() { return color; }
        @Override public ITextComponent getTextComponent() { return new TextComponentString(label); }

        @Override
        public ITextComponent sliceName() {
            ITextComponent component = new TextComponentTranslation(langKey);
            component.getStyle().setColor(color.textFormatting);
            return component;
        }

        @Override public ResourceLocation icon() { return new ResourceLocation(MekanismWeapons.MODID, "gui/radial/damage_off.png"); }
    }
}
