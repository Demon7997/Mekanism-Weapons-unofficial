package mekanism.weapons.client;

import mekanism.weapons.MekanismWeaponsItems;
import mekanism.weapons.common.MekanismWeaponsCommonProxy;
import mekanism.weapons.common.entity.EntityMekaArrow;
import mekanism.weapons.client.renderer.entity.RenderMekaArrow;
import mekanism.weapons.client.renderer.LayerMekaBackWeapon;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.entity.RenderPlayer;
import net.minecraftforge.client.event.ModelRegistryEvent;
import net.minecraftforge.fml.client.registry.RenderingRegistry;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.relauncher.Side;

@Mod.EventBusSubscriber(Side.CLIENT)
public class MekanismWeaponsClientProxy extends MekanismWeaponsCommonProxy {

    @Override
    public void preInit(FMLPreInitializationEvent event) {
        super.preInit(event);
        RenderingRegistry.registerEntityRenderingHandler(EntityMekaArrow.class, RenderMekaArrow::new);
    }

    @Override
    public void init(FMLInitializationEvent event) {
        super.init(event);
        registerRenderLayers();
    }

    private void registerRenderLayers() {
        for (RenderPlayer render : Minecraft.getMinecraft().getRenderManager().getSkinMap().values()) {
            render.addLayer(new LayerMekaBackWeapon());
        }
    }

    @SubscribeEvent
    public static void registerModels(ModelRegistryEvent event) {
        MekanismWeaponsItems.registerModels();
    }
}
