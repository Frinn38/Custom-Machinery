package fr.frinn.custommachinery.forge.client;

import dev.architectury.platform.Platform;
import fr.frinn.custommachinery.CustomMachinery;
import fr.frinn.custommachinery.common.integration.config.CMConfig;
import me.shedaniel.autoconfig.AutoConfig;
import net.minecraft.client.Minecraft;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.ConfigGuiHandler;
import net.minecraftforge.client.event.ModelRegistryEvent;
import net.minecraftforge.client.model.ForgeModelBakery;
import net.minecraftforge.client.model.ModelLoaderRegistry;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(value = Dist.CLIENT, modid = CustomMachinery.MODID, bus = Mod.EventBusSubscriber.Bus.MOD)
public class ClientHandler {

    @SubscribeEvent
    public static void modelRegistry(final ModelRegistryEvent event) {
        ModelLoaderRegistry.registerLoader(new ResourceLocation(CustomMachinery.MODID, "custom_machine"), CustomMachineModelLoader.INSTANCE);
        ForgeModelBakery.addSpecialModel(new ResourceLocation(CustomMachinery.MODID, "block/nope"));
        Minecraft.getInstance().getResourceManager().listResources("models/machine", s -> s.endsWith(".json")).forEach(rl -> {
            ResourceLocation modelRL = new ResourceLocation(rl.getNamespace(), rl.getPath().substring(7).replace(".json", ""));
            ForgeModelBakery.addSpecialModel(modelRL);
        });
    }
    public static void setupConfig() {
        if(Platform.isModLoaded("cloth_config"))
            ModLoadingContext.get().registerExtensionPoint(ConfigGuiHandler.ConfigGuiFactory.class, () ->
                new ConfigGuiHandler.ConfigGuiFactory((client, parent) -> AutoConfig.getConfigScreen(CMConfig.class, parent).get()));
    }
}
