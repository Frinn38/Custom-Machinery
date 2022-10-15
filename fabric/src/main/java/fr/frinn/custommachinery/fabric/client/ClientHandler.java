package fr.frinn.custommachinery.fabric.client;

import fr.frinn.custommachinery.CustomMachinery;
import fr.frinn.custommachinery.client.render.BoxCreatorRenderer;
import fr.frinn.custommachinery.client.render.StructureCreatorRenderer;
import fr.frinn.custommachinery.common.init.Registration;
import net.fabricmc.fabric.api.client.model.ModelLoadingRegistry;
import net.fabricmc.fabric.api.client.render.fluid.v1.FluidRenderHandlerRegistry;
import net.fabricmc.fabric.api.client.rendering.v1.WorldRenderContext;
import net.fabricmc.fabric.api.client.rendering.v1.WorldRenderEvents;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.ResourceManager;

import java.util.function.Consumer;

public class ClientHandler {

    public static void init() {
        ModelLoadingRegistry.INSTANCE.registerModelProvider(ClientHandler::provideModels);
        ModelLoadingRegistry.INSTANCE.registerResourceProvider(manager -> CustomMachineModelProvider.INSTANCE);
        WorldRenderEvents.LAST.register(ClientHandler::renderLevelLast);
        FluidRenderHandlerRegistry.INSTANCE.setBlockTransparency(Registration.CUSTOM_MACHINE_BLOCK.get(), true);
    }

    private static void provideModels(ResourceManager manager, Consumer<ResourceLocation> out) {
        out.accept(new ResourceLocation(CustomMachinery.MODID, "block/nope"));
        out.accept(new ResourceLocation(CustomMachinery.MODID, "default/custom_machine_default"));
        manager.listResources("models/machine", s -> s.endsWith(".json")).forEach(rl -> {
            ResourceLocation modelRL = new ResourceLocation(rl.getNamespace(), rl.getPath().substring(7).replace(".json", ""));
            out.accept(modelRL);
        });
    }

    private static void renderLevelLast(WorldRenderContext context) {
        BoxCreatorRenderer.renderSelectedBlocks(context.matrixStack());
        StructureCreatorRenderer.renderSelectedBlocks(context.matrixStack());
    }
}
