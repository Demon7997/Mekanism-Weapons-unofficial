package mekanism.weapons.common.network;

import mekanism.weapons.MekanismWeapons;
import net.minecraftforge.fml.common.network.NetworkRegistry;
import net.minecraftforge.fml.common.network.simpleimpl.SimpleNetworkWrapper;
import net.minecraftforge.fml.relauncher.Side;

public class MekaWeaponsPacketHandler {
    public static SimpleNetworkWrapper netHandler = NetworkRegistry.INSTANCE.newSimpleChannel(MekanismWeapons.MODID);
    public static void registerMessages() {
        netHandler.registerMessage(PacketMagnetizer.Handler.class, PacketMagnetizer.class, 0, Side.SERVER);
    }
}
