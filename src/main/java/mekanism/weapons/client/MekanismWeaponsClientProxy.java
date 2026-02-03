package mekanism.weapons.client;

import mekanism.weapons.MekanismWeaponsItems;
import mekanism.weapons.common.MekanismWeaponsCommonProxy;
import mekanism.weapons.common.entity.EntityMekaArrow; // Assicurati che l'import sia giusto
import mekanism.weapons.client.renderer.entity.RenderMekaArrow; // Importa il tuo renderer
import net.minecraftforge.client.event.ModelRegistryEvent;
import net.minecraftforge.fml.client.registry.RenderingRegistry; // FONDAMENTALE
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent; // Serve questo evento
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.relauncher.Side;

@Mod.EventBusSubscriber(Side.CLIENT)
public class MekanismWeaponsClientProxy extends MekanismWeaponsCommonProxy {

    // -----------------------------------------------------------------------
    // QUESTA E' LA PARTE CHE MANCAVA
    // Le entità vanno registrate nel PreInit, non nel ModelRegistryEvent
    // -----------------------------------------------------------------------
    @Override
    public void preInit(FMLPreInitializationEvent event) { // <--- Aggiungi il parametro qui
        super.preInit(event); // <--- Passalo anche al super
    
        // Registrazione renderer
        RenderingRegistry.registerEntityRenderingHandler(EntityMekaArrow.class, manager -> new RenderMekaArrow(manager));
}

    // -----------------------------------------------------------------------

    @SubscribeEvent
    public static void registerModels(ModelRegistryEvent event) {
        // Questo registra solo l'item nell'inventario, non la freccia che vola
        MekanismWeaponsItems.registerModels();
    }
}
