// File: mekanism/weapons/MekanismWeaponsEntities.java (NUOVO FILE)

package mekanism.weapons;

import mekanism.weapons.common.entity.EntityMekaArrow;
import net.minecraft.entity.Entity;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.common.registry.EntityRegistry;

public class MekanismWeaponsEntities {

    private static int entityId = 0;

    public static void registerEntities() {
        // Registra la nostra freccia personalizzata
        registerEntity("meka_arrow", EntityMekaArrow.class, 64, 1, true);
    }

    private static void registerEntity(String name, Class<? extends Entity> clazz, int trackingRange, int updateFrequency, boolean sendsVelocityUpdates) {
        ResourceLocation registryName = new ResourceLocation(MekanismWeapons.MODID, name);
        EntityRegistry.registerModEntity(registryName, clazz, name, entityId++, MekanismWeapons.instance, trackingRange, updateFrequency, sendsVelocityUpdates);
    }
}
