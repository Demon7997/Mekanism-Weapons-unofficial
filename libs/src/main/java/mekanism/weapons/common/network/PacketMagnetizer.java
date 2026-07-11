package mekanism.weapons.common.network;

import io.netty.buffer.ByteBuf;
import mekanism.common.Mekanism;
import mekanism.common.PacketHandler;
import mekanism.common.frequency.FrequencyManager;
import mekanism.common.content.entangloporter.InventoryFrequency;
import mekanism.weapons.common.item.ItemMagnetizer;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;

public class PacketMagnetizer implements IMessage {
    public int action;
    public String name;
    public boolean isPublic;
    public String weaponType;
    public boolean toggleState;

    public PacketMagnetizer() {}

    public PacketMagnetizer(int action, String name, boolean isPublic) {
        this.action = action;
        this.name = name;
        this.isPublic = isPublic;
    }

    public PacketMagnetizer(String weaponType, boolean state) {
        this.action = 2;
        this.weaponType = weaponType;
        this.toggleState = state;
    }

    @Override
    public void toBytes(ByteBuf buf) {
        buf.writeInt(action);
        PacketHandler.writeString(buf, name != null ? name : "");
        buf.writeBoolean(isPublic);
        PacketHandler.writeString(buf, weaponType != null ? weaponType : "");
        buf.writeBoolean(toggleState);
    }

    @Override
    public void fromBytes(ByteBuf buf) {
        action = buf.readInt();
        name = PacketHandler.readString(buf);
        isPublic = buf.readBoolean();
        weaponType = PacketHandler.readString(buf);
        toggleState = buf.readBoolean();
    }

    public static class Handler implements IMessageHandler<PacketMagnetizer, IMessage> {
        @Override
    public IMessage onMessage(PacketMagnetizer message, MessageContext ctx) {
        EntityPlayer player = Mekanism.proxy.getPlayer(ctx);
        if (player == null) return null;

        net.minecraft.world.WorldServer mainThread = (net.minecraft.world.WorldServer) player.world;
        mainThread.addScheduledTask(() -> {
            ItemStack stack = player.getHeldItemMainhand();
            if (stack.isEmpty() || !(stack.getItem() instanceof ItemMagnetizer)) {
                stack = player.getHeldItemOffhand();
            }

            if (!stack.isEmpty() && stack.getItem() instanceof ItemMagnetizer) {
                ItemMagnetizer item = (ItemMagnetizer) stack.getItem();

                if (message.action == 0) {
                    FrequencyManager manager = message.isPublic ? Mekanism.publicEntangloporters : Mekanism.privateEntangloporters.get(player.getUniqueID());
                    if (manager != null) {
                        if (!manager.containsFrequency(message.name)) {
                            InventoryFrequency newFreq = new InventoryFrequency(message.name, player.getUniqueID());
                            newFreq.publicFreq = message.isPublic;
                            manager.addFrequency(newFreq);
                        }
                        item.setFrequencyCustom(stack, message.name, message.isPublic);
                    }
                } 
                else if (message.action == 1) {
                    FrequencyManager manager = message.isPublic ? Mekanism.publicEntangloporters : Mekanism.privateEntangloporters.get(player.getUniqueID());
                    if (manager != null) {
                        manager.remove(message.name, player.getUniqueID());
                        item.setFrequencyCustom(stack, "", true);
                    }
                } 
                else if (message.action == 2) {
                    item.setRenderEnabled(stack, message.weaponType, message.toggleState);
                }
            }
        });
            return null;
        }
    }
}
