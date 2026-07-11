package mekanism.weapons.common.network;

import io.netty.buffer.ByteBuf;
import mekanism.common.item.interfaces.IModeItem;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.EnumHand;
import net.minecraft.world.WorldServer;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;

public class PacketModuleWeaponData implements IMessage {

    public EnumHand hand;
    public boolean handleModeValue;

    public PacketModuleWeaponData() {}

    public PacketModuleWeaponData(EnumHand hand, boolean handleMode) {
        this.hand = hand;
        this.handleModeValue = handleMode;
    }

    @Override
    public void toBytes(ByteBuf buf) {
        PacketBuffer buffer = new PacketBuffer(buf);
        buffer.writeEnumValue(this.hand);
        buffer.writeBoolean(this.handleModeValue);
    }

    @Override
    public void fromBytes(ByteBuf buf) {
        PacketBuffer buffer = new PacketBuffer(buf);
        this.hand = buffer.readEnumValue(EnumHand.class);
        this.handleModeValue = buffer.readBoolean();
    }
    
    public static class Handler implements IMessageHandler<PacketModuleWeaponData, IMessage> {
        @Override
        public IMessage onMessage(PacketModuleWeaponData message, MessageContext ctx) {
            final EntityPlayer player = ctx.getServerHandler().player;
            final WorldServer serverWorld = (WorldServer) player.world;
            
            serverWorld.addScheduledTask(new Runnable() {
                @Override
                public void run() {
                    ItemStack stack = player.getHeldItem(message.hand);
                    
                    if (stack.getItem() instanceof IModeItem) {
                    }
                }
            });
            return null;
        }
    }
}
