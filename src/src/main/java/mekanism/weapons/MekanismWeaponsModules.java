package mekanism.weapons;

import mekanism.api.gear.ModuleData;
import mekanism.common.MekanismModules;
import mekanism.common.content.gear.ModuleHelper;
import mekanism.weapons.common.module.*;
import net.minecraft.item.EnumRarity;

public class MekanismWeaponsModules {

    public static ModuleData<ModuleWeaponAttackAmplificationUnit> WEAPON_ATTACK_AMPLIFICATION_UNIT;
    public static ModuleData<ModuleArrowVelocityUnit> ARROW_VELOCITY_UNIT;
    public static ModuleData<ModuleAutoFireUnit> AUTO_FIRE_UNIT;
    public static ModuleData<ModuleCompoundArrowUnit> COMPOUND_ARROW_UNIT;
    public static ModuleData<ModuleDrawSpeedUnit> DRAW_SPEED_UNIT;
    public static ModuleData<ModuleEnergyArrowsUnit> ENERGY_ARROWS_UNIT;
    public static ModuleData<ModuleGravityDampenerUnit> GRAVITY_DAMPENER_UNIT;

    // Chiamato in preInit
    public static void registerModules() {
        // --- Moduli Configurabili (con cambio modalità e HUD) ---
        WEAPON_ATTACK_AMPLIFICATION_UNIT = ModuleHelper.register("weapon_attack_amplification_unit", ModuleWeaponAttackAmplificationUnit::new, 
            builder -> builder.rarity(EnumRarity.UNCOMMON).maxStackSize(4).rendersHUD().handlesModeChange());
        
        ARROW_VELOCITY_UNIT = ModuleHelper.register("arrow_velocity_unit", ModuleArrowVelocityUnit::new, 
            builder -> builder.rarity(EnumRarity.RARE).maxStackSize(8).rendersHUD().handlesModeChange());
            
        COMPOUND_ARROW_UNIT = ModuleHelper.register("compound_arrow_unit", ModuleCompoundArrowUnit::new, 
            builder -> builder.rarity(EnumRarity.RARE).maxStackSize(4).rendersHUD().handlesModeChange()); // maxStackSize 4 corrisponde a 5 livelli (0-4)
            
        DRAW_SPEED_UNIT = ModuleHelper.register("draw_speed_unit", ModuleDrawSpeedUnit::new, 
            builder -> builder.rarity(EnumRarity.RARE).maxStackSize(3).rendersHUD().handlesModeChange());

        // --- Moduli Semplici (On/Off) ---
        AUTO_FIRE_UNIT = ModuleHelper.register("auto_fire_unit", ModuleAutoFireUnit::new, 
            builder -> builder.rarity(EnumRarity.RARE).rendersHUD()); // RendersHUD per mostrare lo stato On/Off
            
        ENERGY_ARROWS_UNIT = ModuleHelper.register("energy_arrows_unit", ModuleEnergyArrowsUnit::new, 
            builder -> builder.rarity(EnumRarity.RARE).rendersHUD()); // RendersHUD per mostrare lo stato On/Off
            
        GRAVITY_DAMPENER_UNIT = ModuleHelper.register("gravity_dampener_unit", ModuleGravityDampenerUnit::new, 
            builder -> builder.rarity(EnumRarity.EPIC)); // Non ha bisogno di HUD o cambio modalità
    }
    
    // Chiamato in init
    public static void finalizeSetup() {
        // ... (questa parte è già corretta e non va cambiata) ...
         ModuleHelper.get().setSupported(MekanismWeaponsItems.meka_bow, // <-- CORRETTO
            MekanismModules.ENERGY_UNIT,
            WEAPON_ATTACK_AMPLIFICATION_UNIT,
            ARROW_VELOCITY_UNIT,
            AUTO_FIRE_UNIT,
            COMPOUND_ARROW_UNIT,
            DRAW_SPEED_UNIT,
            ENERGY_ARROWS_UNIT,
            GRAVITY_DAMPENER_UNIT
        );
          ModuleHelper.get().setSupported(MekanismWeaponsItems.meka_tana, // <-- CORRETTO
            MekanismModules.ENERGY_UNIT,
            MekanismModules.TELEPORTATION_UNIT,
            WEAPON_ATTACK_AMPLIFICATION_UNIT
        );
    }
}
