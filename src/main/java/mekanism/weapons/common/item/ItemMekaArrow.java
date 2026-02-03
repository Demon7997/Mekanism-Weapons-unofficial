package mekanism.weapons.common.item;

import mekanism.weapons.common.entity.EntityMekaArrow;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.projectile.EntityArrow;
import net.minecraft.item.ItemArrow;
import net.minecraft.item.ItemStack;
import net.minecraft.world.World;

public class ItemMekaArrow extends ItemArrow {
    
    public ItemMekaArrow() {
        super();
    }

    @Override
    public EntityArrow createArrow(World worldIn, ItemStack stack, EntityLivingBase shooter) {
        EntityMekaArrow arrow = new EntityMekaArrow(worldIn, shooter);
        // Essendo HDPE + Obsidian, diamo un danno fisso elevato (es. 9.0)
        arrow.setDamage(9.0D);
        return arrow;
    }
}
