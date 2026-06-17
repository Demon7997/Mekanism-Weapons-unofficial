package mekanism.weapons.common;

import mekanism.weapons.client.gui.GuiMagnetizer;
import mekanism.weapons.common.inventory.container.MagnetizerContainer;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.EnumHand;
import net.minecraft.world.World;
import net.minecraftforge.fml.common.network.IGuiHandler;

public class MekaWeaponsGuiHandler implements IGuiHandler {
    @Override
    public Object getServerGuiElement(int ID, EntityPlayer player, World world, int x, int y, int z) {
        if (ID == 100) {
            return new MagnetizerContainer(player.inventory, EnumHand.values()[x]);
        }
        return null;
    }

    @Override
    public Object getClientGuiElement(int ID, EntityPlayer player, World world, int x, int y, int z) {
        if (ID == 100) {
            return new GuiMagnetizer(player.inventory, EnumHand.values()[x]);
        }
        return null;
    }
}
