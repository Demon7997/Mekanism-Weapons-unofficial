package mekanism.weapons.common;

import java.util.function.BooleanSupplier;
import com.google.gson.JsonObject;
import net.minecraft.init.Items;
import net.minecraft.item.Item;
import net.minecraft.util.JsonUtils;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.common.crafting.IConditionFactory;
import net.minecraftforge.common.crafting.JsonContext;
import net.minecraftforge.fml.common.Loader;
import net.minecraftforge.oredict.OreDictionary;
import mekanism.weapons.config.MekanismWeaponsConfig;

public class MekaSweepingCondition implements IConditionFactory {
    @Override
    public BooleanSupplier parse(JsonContext context, JsonObject json) {
        registerNetheriteSword();
        
        boolean expectedValue = JsonUtils.getBoolean(json, "value", true);
        
        return () -> {
            boolean isModPresent = Loader.isModLoaded("futuremc") || 
                                  Loader.isModLoaded("upnetherbackport") || 
                                  Loader.isModLoaded("upvnether") || 
                                  Loader.isModLoaded("nb");
            
            boolean isConfigEnabled = MekanismWeaponsConfig.netheriteRecipes;

            boolean finalResult = isModPresent && isConfigEnabled;
            
            return finalResult == expectedValue;
        };
    }

    private void registerNetheriteSword() {
        String[] mods = {"futuremc", "upvnether", "upnetherbackport", "nb"};
        for (String mod : mods) {
            Item item = Item.REGISTRY.getObject(new ResourceLocation(mod, "netherite_sword"));
            if (item != null && item != Items.AIR) {
                if (OreDictionary.getOres("swordNetherite").isEmpty()) {
                    OreDictionary.registerOre("swordNetherite", item);
                }
            }
        }
    }
}
