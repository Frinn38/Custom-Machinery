package fr.frinn.custommachinery.forge.client;

import dev.architectury.platform.Platform;
import fr.frinn.custommachinery.CustomMachinery;
import fr.frinn.custommachinery.common.integration.config.CMConfig;
import me.shedaniel.autoconfig.AutoConfig;
import net.minecraft.client.Minecraft;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.ConfigScreenHandler;
import net.minecraftforge.client.ConfigScreenHandler.ConfigScreenFactory;
import net.minecraftforge.client.event.ModelEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(value = Dist.CLIENT, modid = CustomMachinery.MODID, bus = Mod.EventBusSubscriber.Bus.MOD)
public class ClientHandler {

    @SubscribeEvent
    public static void registerModelLoader(final ModelEvent.RegisterGeometryLoaders event) {
        event.register("custom_machine", CustomMachineModelLoader.INSTANCE);
    }

    @SubscribeEvent
    public static void registerAdditionalModels(final ModelEvent.RegisterAdditional event) {
        event.register(new ResourceLocation(CustomMachinery.MODID, "block/nope"));
        event.register(new ResourceLocation(CustomMachinery.MODID, "default/custom_machine_default"));
        for(String folder : CMConfig.get().modelFolders) {
            Minecraft.getInstance().getResourceManager().listResources("models/" + folder, s -> s.getPath().endsWith(".json")).forEach((rl, resource) -> {
                ResourceLocation modelRL = new ResourceLocation(rl.getNamespace(), rl.getPath().substring(7).replace(".json", ""));
                event.register(modelRL);
            });
        }
    }
    public static void setupConfig() {
        if(Platform.isModLoaded("cloth_config"))
            ModLoadingContext.get().registerExtensionPoint(ConfigScreenFactory.class, () ->
                new ConfigScreenHandler.ConfigScreenFactory((client, parent) -> AutoConfig.getConfigScreen(CMConfig.class, parent).get()));
    }
}
