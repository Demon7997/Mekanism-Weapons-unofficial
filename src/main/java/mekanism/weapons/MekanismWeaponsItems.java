package mekanism.weapons;

import mekanism.common.item.ItemModule;
import mekanism.weapons.common.item.ItemMekaBow;
import mekanism.weapons.common.item.ItemMekaTana;
import net.minecraft.client.renderer.block.model.ModelResourceLocation;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.client.model.ModelLoader;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import net.minecraftforge.registries.IForgeRegistry;
import net.minecraftforge.fml.common.registry.GameRegistry.ObjectHolder;
import net.minecraft.client.renderer.block.model.ModelBakery;

public class MekanismWeaponsItems {

    // --- Oggetti (verranno riempiti da Forge) ---
    @ObjectHolder(MekanismWeapons.MODID + ":meka_tana")
    public static final ItemMekaTana meka_tana = null;
    @ObjectHolder(MekanismWeapons.MODID + ":meka_bow")
    public static final ItemMekaBow meka_bow = null;
    @ObjectHolder(MekanismWeapons.MODID + ":katana_blade")
    public static final Item katana_blade = null;
    @ObjectHolder(MekanismWeapons.MODID + ":bow_limb")
    public static final Item bow_limb = null;
    @ObjectHolder(MekanismWeapons.MODID + ":bow_riser")
    public static final Item bow_riser = null;
    @ObjectHolder(MekanismWeapons.MODID + ":module_attackamplification_unit")
    public static final ItemModule module_attackamplification_unit = null;
    @ObjectHolder(MekanismWeapons.MODID + ":module_arrowvelocity_unit")
    public static final ItemModule module_arrowvelocity_unit = null;
    @ObjectHolder(MekanismWeapons.MODID + ":module_autofire_unit")
    public static final ItemModule module_autofire_unit = null;
    @ObjectHolder(MekanismWeapons.MODID + ":module_compoundarrow_unit")
    public static final ItemModule module_compoundarrow_unit = null;
    @ObjectHolder(MekanismWeapons.MODID + ":module_drawspeed_unit")
    public static final ItemModule module_drawspeed_unit = null;
    @ObjectHolder(MekanismWeapons.MODID + ":module_arrowenergy_unit")
    public static final ItemModule module_arrowenergy_unit = null;
    @ObjectHolder(MekanismWeapons.MODID + ":module_gravitydampener_unit")
    public static final ItemModule module_gravitydampener_unit = null;
    @ObjectHolder(MekanismWeapons.MODID + ":meka_arrow")
    public static final Item meka_arrow = null;

    public static final CreativeTabs tabMekanismWeapons = new CreativeTabs(MekanismWeapons.MODID) {
        @Override
        public ItemStack createIcon() {
            return new ItemStack(meka_bow);
        }
    };
    
    public static void registerItems(IForgeRegistry<Item> registry) {
        registry.register(init(new ItemMekaTana(), "meka_tana"));
        registry.register(init(new ItemMekaBow(), "meka_bow"));
        registry.register(init(new Item(), "katana_blade"));
        registry.register(init(new Item(), "bow_limb"));
        registry.register(init(new Item(), "bow_riser"));
        registry.register(init(new Item(), "meka_arrow"));
        registry.register(init(new ItemModule(MekanismWeaponsModules.WEAPON_ATTACK_AMPLIFICATION_UNIT), "module_attackamplification_unit"));
        registry.register(init(new ItemModule(MekanismWeaponsModules.ARROW_VELOCITY_UNIT), "module_arrowvelocity_unit"));
        registry.register(init(new ItemModule(MekanismWeaponsModules.AUTO_FIRE_UNIT), "module_autofire_unit"));
        registry.register(init(new ItemModule(MekanismWeaponsModules.COMPOUND_ARROW_UNIT), "module_compoundarrow_unit"));
        registry.register(init(new ItemModule(MekanismWeaponsModules.DRAW_SPEED_UNIT), "module_drawspeed_unit"));
        registry.register(init(new ItemModule(MekanismWeaponsModules.ENERGY_ARROWS_UNIT), "module_arrowenergy_unit"));
        registry.register(init(new ItemModule(MekanismWeaponsModules.GRAVITY_DAMPENER_UNIT), "module_gravitydampener_unit"));
    }

    @SideOnly(Side.CLIENT)
    public static void registerModels() {
        registerModel(meka_tana);
        registerModel(meka_bow);
        registerModel(katana_blade);
        registerModel(bow_limb);
        registerModel(bow_riser);
        registerModel(module_attackamplification_unit);
        registerModel(module_arrowvelocity_unit);
        registerModel(module_autofire_unit);
        registerModel(module_compoundarrow_unit);
        registerModel(module_drawspeed_unit);
        registerModel(module_arrowenergy_unit);
        registerModel(module_gravitydampener_unit);
        
        // #############################################################
        // #### ECCO DOVE VA IL TUO CODICE ####
        // #############################################################
        // CANCELLA la vecchia riga "registerModel(meka_arrow);"
        // e INCOLLA questo al suo posto.
        if (meka_arrow != null) {
            ModelLoader.setCustomMeshDefinition(meka_arrow, stack -> 
                new ModelResourceLocation(meka_arrow.getRegistryName(), "inventory"));
            ModelBakery.registerItemVariants(meka_arrow, meka_arrow.getRegistryName());
        }
    }

    // NUOVO CODICE (CORRETTO)
private static <T extends Item> T init(T item, String name) {
    item.setRegistryName(new ResourceLocation(MekanismWeapons.MODID, name));
    item.setTranslationKey(MekanismWeapons.MODID + "." + name); // <-- CORRETTO
    item.setCreativeTab(tabMekanismWeapons);
    return item;
}

    @SideOnly(Side.CLIENT)
    private static void registerModel(Item item) {
        if (item != null) {
            ModelLoader.setCustomModelResourceLocation(item, 0, new ModelResourceLocation(item.getRegistryName(), "inventory"));
        }
    }
}
