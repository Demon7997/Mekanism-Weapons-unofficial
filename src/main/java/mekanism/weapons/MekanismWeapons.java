package mekanism.weapons;

import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.SidedProxy;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import mekanism.weapons.config.MekanismWeaponsConfig;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;

@Mod(modid = MekanismWeapons.MODID, name = "Mekanism: Weapons", version = "3.0.0", dependencies = "required-after:mekanism")
public class MekanismWeapons {

    public static final String MODID = "mekaweapons";

    @Mod.Instance(MODID)
    public static MekanismWeapons instance;

    @SidedProxy(clientSide = "mekanism.weapons.client.MekanismWeaponsClientProxy", serverSide = "mekanism.weapons.common.MekanismWeaponsCommonProxy")
    public static mekanism.weapons.common.MekanismWeaponsCommonProxy proxy;

    @Mod.EventHandler
    public void preInit(FMLPreInitializationEvent event) {
        MekanismWeaponsConfig.load(event);
        
        // v-- AGGIUNGI QUESTA RIGA --v
        MekanismWeaponsEntities.registerEntities();
        // ^-- FINE DELLA RIGA DA AGGIUNGERE --^
        
        MekanismWeaponsModules.registerModules();
        proxy.preInit();
    }

     @Mod.EventHandler
    public void init(FMLInitializationEvent event) {
        MekanismWeaponsModules.finalizeSetup();
    }
}
