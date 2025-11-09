package mekanism.weapons.config;

import net.minecraftforge.common.config.Configuration;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import java.io.File;

public class MekanismWeaponsConfig {

    public static Configuration config;

    // --- Meka-Tana Settings ---
    public static float mekaTanaAttackSpeed;
    public static int mekaTanaBaseDamage;
    public static double mekaTanaEnergyUsage;
    //public static double mekaTanaSweepingEnergyUsage;
    public static double mekaTanaTeleportEnergyUsage;
    public static int mekaTanaMaxTeleportReach;
    //public static double mekaTanaLootingEnergyUsage;
    public static double mekaTanaBaseEnergyCapacity;
    public static double mekaTanaBaseChargeRate;
    public static boolean mekaTanaEnchantments;

    // --- Meka-Bow Settings ---
    public static int mekaBowBaseDamage;
    public static double mekaBowEnergyUsage;
    public static double mekaBowEnergyArrowUsage;
    public static double mekaBowAutofireEnergyUsage;
    public static double mekaBowDrawSpeedUsage;
    public static double mekaBowGravityDampenerUsage;
    //public static double mekaBowLootingEnergyUsage;
    public static double mekaBowBaseEnergyCapacity;
    public static double mekaBowBaseChargeRate;
    public static boolean mekaBowEnchantments;
    
    // --- Module Specific Settings ---
    public static double attackAmplificationEnergyUsage;
    public static double arrowVelocityEnergyUsage;
    public static double compoundArrowEnergyUsage;
    

    public static void load(FMLPreInitializationEvent event) {
    // Costruiamo il percorso: cartella config + "mekanism" + "mekaweapons.cfg"
    config = new Configuration(new File(event.getModConfigurationDirectory() + "/mekanism/", "mekaweapons.cfg"));
    syncConfig();
    }

    public static void syncConfig() {
        String category = "weapons.meka_tana";
        mekaTanaBaseDamage = config.getInt("base_damage", category, 50, 1, Integer.MAX_VALUE, "Base damage of Meka-Tana");
        mekaTanaAttackSpeed = config.getFloat("attack_speed", category, -2.4F, -4.0F, 100.0F, "Attack speed of Meka-Tana.");
        mekaTanaEnergyUsage = config.get("energy_usage", category, 625000, "Cost in Joules of using Meka-Tana to deal damage.").getDouble();
        //mekaTanaSweepingEnergyUsage = config.get("sweeping_attack_energy_usage", category, 125000, "Additional cost in Joules of using Meka-Tana to perform a sweeping attack.").getDouble();
        mekaTanaTeleportEnergyUsage = config.get("teleport_energy_usage", category, 5000, "Cost in Joules of using Meka-Tana to teleport 10 blocks.").getDouble();
        mekaTanaMaxTeleportReach = config.getInt("max_teleport_reach", category, 100, 3, 1024, "Maximum distance a player can teleport with Meka-Tana.");
        //mekaTanaLootingEnergyUsage = config.get("looting_energy_usage", category, 125000, "Cost in Joules of using Meka-Tana to apply Looting effect to a mob.").getDouble();
        mekaTanaBaseEnergyCapacity = config.get("base_energy_capacity", category, 16000000, "Base energy capacity of Meka-Tana.").getDouble();
        mekaTanaBaseChargeRate = config.get("base_charge_rate", category, 350000, "Base charge rate of Meka-Tana.").getDouble();
        mekaTanaEnchantments = config.getBoolean("enchantments", category, false, "Whether Meka-Tana can be enchanted. False by default. Use at your own risk.");

        category = "weapons.meka_bow";
        mekaBowBaseDamage = config.getInt("base_damage", category, 50, 1, Integer.MAX_VALUE, "Base damage of Meka-Bow.");
        mekaBowEnergyUsage = config.get("energy_usage", category, 625000, "Cost in Joules of using Meka-Bow.").getDouble();
        mekaBowEnergyArrowUsage = config.get("energy_arrow_usage", category, 625000, "Cost in Joules of using Meka-Bow with Energy Arrow Unit active, per shot.").getDouble();
        mekaBowAutofireEnergyUsage = config.get("autofire_energy_usage", category, 125000, "Cost in Joules of using Meka-Bow with Auto-Fire active, per shot.").getDouble();
        mekaBowDrawSpeedUsage = config.get("draw_speed_usage", category, 125000, "Cost in Joules of using Meka-Bow with Draw Speed Unit active, multiplied by unit amount, per shot.").getDouble();
        mekaBowGravityDampenerUsage = config.get("gravity_dampener_usage", category, 125000, "Cost in Joules of using Meka-Bow with Gravity Dampener Unit active, per shot.").getDouble();
        //mekaBowLootingEnergyUsage = config.get("looting_energy_usage", category, 125000, "Cost in Joules of using Meka-Bow to apply Looting effect to a mob.").getDouble();
        mekaBowBaseEnergyCapacity = config.get("base_energy_capacity", category, 16000000, "Base energy capacity of Meka-Bow.").getDouble();
        mekaBowBaseChargeRate = config.get("base_charge_rate", category, 350000, "Base charge rate of Meka-Bow.").getDouble();
        mekaBowEnchantments = config.getBoolean("enchantments", category, false, "Whether Meka-Bow can be enchanted. False by default. Use at your own risk.");
        
        category = "modules";
        attackAmplificationEnergyUsage = config.get("attack_amplification_usage", category, 2500, "Additional energy cost per level of the Attack Amplification Unit.").getDouble();
        arrowVelocityEnergyUsage = config.get("arrow_velocity_usage", category, 500, "Energy cost per level of the Arrow Velocity Unit.").getDouble();
        compoundArrowEnergyUsage = config.get("compound_arrow_usage", category, 2000, "Additional energy cost per arrow fired by the Compound Arrow Unit.").getDouble();
        
        if (config.hasChanged()) {
            config.save();
        }
    }
}
