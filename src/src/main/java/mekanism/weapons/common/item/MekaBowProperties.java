package mekanism.weapons.common.item;

import net.minecraft.entity.EntityLivingBase;
import net.minecraft.item.IItemPropertyGetter;
import net.minecraft.item.ItemStack;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import javax.annotation.Nullable;

@SideOnly(Side.CLIENT)
public class MekaBowProperties {

    public static final IItemPropertyGetter PULL_GETTER = (stack, world, entity) -> {
        if (entity == null || !entity.isHandActive() || entity.getActiveItemStack() != stack) {
            return 0.0F;
        }

        // ======================= LA PROVA DEL NOVE =======================
        // Invece di provare a leggere qualsiasi cosa, forziamo l'animazione
        // ad essere SEMPRE super veloce.
        float drawTimeNeeded = 5.0F; // VALORE DI TEST FISSO
        // ===============================================================

        float charge = (float) (stack.getMaxItemUseDuration() - entity.getItemInUseCount());
        
        return Math.min(1.0F, charge / drawTimeNeeded);
    };

    public static final IItemPropertyGetter PULLING_GETTER = (stack, world, entity) -> {
        return entity != null && entity.isHandActive() && entity.getActiveItemStack() == stack ? 1.0F : 0.0F;
    };
}
