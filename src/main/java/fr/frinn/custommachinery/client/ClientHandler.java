package fr.frinn.custommachinery.client;

import fr.frinn.custommachinery.CustomMachinery;
import fr.frinn.custommachinery.client.render.CustomMachineRenderer;
import fr.frinn.custommachinery.client.screen.CustomMachineScreen;
import fr.frinn.custommachinery.client.screen.MachineLoadingScreen;
import fr.frinn.custommachinery.common.data.MachineAppearance;
import fr.frinn.custommachinery.common.init.CustomMachineTile;
import fr.frinn.custommachinery.common.init.Registration;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.ScreenManager;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.model.data.EmptyModelData;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.client.registry.ClientRegistry;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;

import javax.annotation.Nonnull;

@Mod.EventBusSubscriber(value = Dist.CLIENT, modid = CustomMachinery.MODID, bus = Mod.EventBusSubscriber.Bus.MOD)
public class ClientHandler {

    @SubscribeEvent
    public static void clientSetup(final FMLClientSetupEvent event) {
        event.enqueueWork(() -> {
            ClientRegistry.bindTileEntityRenderer(Registration.CUSTOM_MACHINE_TILE.get(), CustomMachineRenderer::new);

            ScreenManager.registerFactory(Registration.CUSTOM_MACHINE_CONTAINER.get(), CustomMachineScreen::new);
        });
    }

    public static void openMachineLoadingScreen() {
        Minecraft.getInstance().displayGuiScreen(MachineLoadingScreen.INSTANCE);
    }

    @Nonnull
    public static CustomMachineTile getClientSideCustomMachineTile(BlockPos pos) {
        if(Minecraft.getInstance().world != null) {
            TileEntity tile = Minecraft.getInstance().world.getTileEntity(pos);
            if(tile instanceof CustomMachineTile)
                return (CustomMachineTile)tile;
        }
        throw new IllegalStateException("Trying to open a Custom Machine container without clicking on a Custom Machine block");
    }

    public static void setParticleTexture(MachineAppearance appearance) {
        net.minecraft.client.renderer.texture.TextureAtlasSprite particleTexture;
        if(appearance.getType() == MachineAppearance.AppearanceType.BLOCKSTATE)
            particleTexture = Minecraft.getInstance().getModelManager().getModel(appearance.getBlockstate()).getParticleTexture(EmptyModelData.INSTANCE);
        else if(appearance.getType() == MachineAppearance.AppearanceType.BLOCK)
            particleTexture = Minecraft.getInstance().getBlockRendererDispatcher().getModelForState(appearance.getBlock().getDefaultState()).getParticleTexture(EmptyModelData.INSTANCE);
        else if(appearance.getType() == MachineAppearance.AppearanceType.MODEL)
            particleTexture = new ModelHandle(appearance.getModel()).getParticleTexture();
        else
            particleTexture = Minecraft.getInstance().getModelManager().getMissingModel().getParticleTexture(EmptyModelData.INSTANCE);
        net.minecraft.client.renderer.model.IBakedModel dummyModel = new DummyBakedModel(particleTexture);
        Registration.CUSTOM_MACHINE_BLOCK.get().getStateContainer().getValidStates().forEach(state ->
                Minecraft.getInstance().getModelManager().getBlockModelShapes().bakedModelStore.replace(state, dummyModel)
        );
    }
}
