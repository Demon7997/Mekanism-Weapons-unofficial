package mekanism.weapons.common;

import com.google.common.collect.Multimap;
import mekanism.api.EnumColor;
import mekanism.weapons.common.item.ItemMekaBow;
import mekanism.weapons.common.item.ItemMekaTana;
import net.minecraft.entity.SharedMonsterAttributes;
import net.minecraft.entity.ai.attributes.AttributeModifier;
import net.minecraft.init.Enchantments;
import net.minecraft.inventory.EntityEquipmentSlot;
import net.minecraft.item.ItemStack;
import net.minecraftforge.event.entity.player.ItemTooltipEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.relauncher.Side;

import java.util.Collection;
import java.util.List;

@Mod.EventBusSubscriber(modid = "mekaweapons", value = Side.CLIENT)
public class MekaWeaponsEvents {

    @SubscribeEvent
    public static void onTooltip(ItemTooltipEvent event) {
        ItemStack stack = event.getItemStack();
        if (stack.isEmpty()) return;

        if (stack.getItem() instanceof ItemMekaTana || stack.getItem() instanceof ItemMekaBow) {
            List<String> tooltip = event.getToolTip();

            for (int i = 1; i <= 5; i++) {
                tooltip.remove(Enchantments.LOOTING.getTranslatedName(i));
                tooltip.remove(Enchantments.SWEEPING.getTranslatedName(i));
            }

            if (stack.getItem() instanceof ItemMekaTana) {
                Multimap<String, AttributeModifier> modifiers = stack.getAttributeModifiers(EntityEquipmentSlot.MAINHAND);
                Collection<AttributeModifier> damageMods = modifiers.get(SharedMonsterAttributes.ATTACK_DAMAGE.getName());
                
                double damageValue = 0;
                for (AttributeModifier mod : damageMods) {
                    damageValue += mod.getAmount();
                }

                int finalValue = (int) damageValue + 1;

                if (finalValue > 1) {
                    tooltip.add(EnumColor.INDIGO + "+" + finalValue + " Sweeping Damage Ratio");
                }
            }
        }
    }
}
