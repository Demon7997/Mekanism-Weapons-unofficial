package mekanism.weapons.common.network;

import io.netty.buffer.ByteBuf;
import mekanism.common.item.interfaces.IModeItem;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.EnumHand;
import net.minecraft.world.WorldServer; // Import necessario
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;

public class PacketModuleWeaponData implements IMessage {

    public EnumHand hand;
    public boolean handleModeValue;
    // Puoi aggiungere altri campi qui in futuro per inviare dati diversi

    // Costruttore vuoto necessario per Forge
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
    
    // --- L'HANDLER ---
    
    public static class Handler implements IMessageHandler<PacketModuleWeaponData, IMessage> {
        @Override
        public IMessage onMessage(PacketModuleWeaponData message, MessageContext ctx) {
            // Otteniamo il player e il mondo dal contesto del server
            final EntityPlayer player = ctx.getServerHandler().player;
            final WorldServer serverWorld = (WorldServer) player.world;
            
            // Usiamo un Runnable per eseguire il codice nel thread principale del server
            serverWorld.addScheduledTask(new Runnable() {
                @Override
                public void run() {
                    // La nostra logica viene eseguita qui, in modo sicuro
                    ItemStack stack = player.getHeldItem(message.hand);
                    
                    if (stack.getItem() instanceof IModeItem) {
                        // Quando avrai un modulo che ha bisogno di inviare dati
                        // dal client al server, qui chiamerai un metodo
                        // sul tuo item per applicare la modifica.
                        
                        // Esempio futuro:
                        // ((ItemMekaBow) stack.getItem()).handlePacketData(stack, message.handleModeValue);
                    }
                }
            });
            return null; // Nessuna risposta al client
        }
    }
}
