package fr.frinn.custommachinery.client;

import com.mojang.blaze3d.matrix.MatrixStack;
import fr.frinn.custommachinery.CustomMachinery;
import fr.frinn.custommachinery.client.render.CustomMachineRenderer;
import fr.frinn.custommachinery.client.screen.CustomMachineScreen;
import fr.frinn.custommachinery.client.screen.MachineLoadingScreen;
import fr.frinn.custommachinery.common.data.MachineAppearance;
import fr.frinn.custommachinery.common.init.CustomMachineTile;
import fr.frinn.custommachinery.common.init.Registration;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.gui.ScreenManager;
import net.minecraft.client.renderer.BlockModelShapes;
import net.minecraft.client.renderer.model.ModelResourceLocation;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.ModelBakeEvent;
import net.minecraftforge.client.event.ModelRegistryEvent;
import net.minecraftforge.client.model.ModelLoader;
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

    @SubscribeEvent
    public static void modelRegistry(final ModelRegistryEvent event) {
        ModelLoader.addSpecialModel(new ResourceLocation(CustomMachinery.MODID, "block/custom_machine_block"));
    }

    @SubscribeEvent
    public static void modelBake(final ModelBakeEvent event) {
        Registration.CUSTOM_MACHINE_BLOCK.get().getStateContainer().getValidStates().forEach(state -> {
            ModelResourceLocation modelLocation = BlockModelShapes.getModelLocation(state);
            if(event.getModelRegistry().containsKey(modelLocation))
                event.getModelRegistry().replace(BlockModelShapes.getModelLocation(state), new WrappedBakedModel(event.getModelRegistry().get(modelLocation)));
            event.getModelRegistry().put(modelLocation, new DummyBakedModel());
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

    public static TextureAtlasSprite getParticleTexture(MachineAppearance appearance) {
        switch (appearance.getType()) {
            case DEFAULT:
                return Minecraft.getInstance().getModelManager().getModel(CustomMachineRenderer.DEFAULT_MODEL).getParticleTexture(EmptyModelData.INSTANCE);
            case BLOCK:
                return Minecraft.getInstance().getModelManager().getModel(appearance.getBlock().getRegistryName()).getParticleTexture(EmptyModelData.INSTANCE);
            case BLOCKSTATE:
                return Minecraft.getInstance().getModelManager().getModel(appearance.getBlockstate()).getParticleTexture(EmptyModelData.INSTANCE);
            case MODEL:
                return Minecraft.getInstance().getModelManager().getModel(appearance.getModel()).getParticleTexture(EmptyModelData.INSTANCE);
        }
        return Minecraft.getInstance().getModelManager().getMissingModel().getParticleTexture(EmptyModelData.INSTANCE);
    }

    public static void drawSizedString(FontRenderer font, MatrixStack matrix, String string, int x, int y, int size, float maxScale, int color) {
        float stringSize = font.getStringWidth(string);
        float scale = Math.min(size / stringSize, maxScale);
        matrix.push();
        matrix.scale(scale, scale, 0);
        font.drawString(matrix, string, x / scale, y / scale, color);
        matrix.pop();
    }

    public static void drawCenteredString(FontRenderer font, MatrixStack matrix, String string, int x, int y, int color) {
        int width = font.getStringWidth(string);
        int height = font.FONT_HEIGHT;
        matrix.push();
        matrix.translate(-width / 2.0D, -height / 2.0D, 0);
        font.drawString(matrix, string, x, y, color);
        matrix.pop();
    }
}
