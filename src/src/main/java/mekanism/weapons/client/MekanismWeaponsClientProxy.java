package mekanism.weapons.client;

import mekanism.weapons.MekanismWeaponsItems;
import mekanism.weapons.common.MekanismWeaponsCommonProxy;
import net.minecraftforge.client.event.ModelRegistryEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.relauncher.Side;

@Mod.EventBusSubscriber(Side.CLIENT)
public class MekanismWeaponsClientProxy extends MekanismWeaponsCommonProxy {

    @SubscribeEvent
    public static void registerModels(ModelRegistryEvent event) {
        // Ora deleghiamo la registrazione dei modelli alla classe degli item,
        // che è un posto più logico per gestirla.
        MekanismWeaponsItems.registerModels();
    }
}
