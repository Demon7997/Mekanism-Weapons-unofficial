package mekanism.weapons.common.item;

import net.minecraft.item.IItemPropertyGetter;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

@SideOnly(Side.CLIENT)
public class MekaBowProperties {

    public static final IItemPropertyGetter PULL_GETTER = (stack, world, entity) -> {
        if (entity == null || !entity.isHandActive() || entity.getActiveItemStack() != stack) {
            return 0.0F;
        }
        float drawTimeNeeded = 20.0F;
        if (stack.hasTagCompound()) {
            NBTTagCompound rootTag = stack.getTagCompound();
            if (rootTag.hasKey("mekData", 10)) {
                NBTTagCompound mekData = rootTag.getCompoundTag("mekData");
                if (mekData.hasKey("modules", 10)) {
                    NBTTagCompound modules = mekData.getCompoundTag("modules");
                    if (modules.hasKey("draw_speed_unit", 10)) {
                        NBTTagCompound drawSpeedData = modules.getCompoundTag("draw_speed_unit");
                        if (drawSpeedData.getBoolean("enabled")) {
                            int levelOrdinal = drawSpeedData.getInteger("draw_speed_level");
                            if (levelOrdinal > 0) {
                                drawTimeNeeded = 20 - (levelOrdinal * 5);
                            }
                        }
                    }
                }
            }
        }
        float charge = (float) (stack.getMaxItemUseDuration() - entity.getItemInUseCount());
        return drawTimeNeeded > 0 ? Math.min(1.0F, charge / drawTimeNeeded) : 0.0F;
    };

    public static final IItemPropertyGetter PULLING_GETTER = (stack, world, entity) -> {
        return entity != null && entity.isHandActive() && entity.getActiveItemStack() == stack ? 1.0F : 0.0F;
    };
}
