package fr.frinn.custommachinery.client.render;

import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.vertex.IVertexBuilder;
import fr.frinn.custommachinery.CustomMachinery;
import fr.frinn.custommachinery.client.ModelHandle;
import fr.frinn.custommachinery.common.data.MachineAppearance;
import fr.frinn.custommachinery.common.init.CustomMachineTile;
import net.minecraft.block.BlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.IRenderTypeBuffer;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.model.IBakedModel;
import net.minecraft.client.renderer.model.ModelResourceLocation;
import net.minecraft.client.renderer.tileentity.TileEntityRenderer;
import net.minecraft.client.renderer.tileentity.TileEntityRendererDispatcher;
import net.minecraft.state.properties.BlockStateProperties;
import net.minecraft.util.Direction;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.vector.Vector3f;
import net.minecraftforge.client.model.data.EmptyModelData;

import javax.annotation.ParametersAreNonnullByDefault;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;
import java.util.stream.Stream;

public class CustomMachineRenderer extends TileEntityRenderer<CustomMachineTile> {

    private static final ResourceLocation DEFAULT_MODEL = new ResourceLocation(CustomMachinery.MODID, "block/custom_machine_block");
    private static final Map<ResourceLocation, ModelHandle> MODELS = new HashMap<>();
    private static final Random RAND = new Random();

    public CustomMachineRenderer(TileEntityRendererDispatcher dispatcher) {
        super(dispatcher);
    }

    @ParametersAreNonnullByDefault
    @Override
    public void render(CustomMachineTile tile, float partialTicks, MatrixStack matrix, IRenderTypeBuffer buffer, int combinedLight, int combinedOverlay) {
        MachineAppearance appearance = tile.getMachine().getAppearance();
        matrix.push();
        matrix.translate(0.5F, 0, 0.5F);
        matrix.rotate(Vector3f.YN.rotationDegrees(tile.getBlockState().get(BlockStateProperties.HORIZONTAL_FACING).getHorizontalAngle()));
        matrix.translate(-0.5F, 0, -0.5F);
        renderMachineAppearance(appearance, partialTicks, matrix, buffer, combinedLight, combinedOverlay);
        matrix.pop();
    }

    public static void renderMachineAppearance(MachineAppearance appearance, float partialTicks, MatrixStack matrix, IRenderTypeBuffer buffer, int combinedLight, int combinedOverlay) {
        switch (appearance.getType()) {
            case BLOCKSTATE:
                ModelResourceLocation modelLocation = appearance.getBlockstate();
                renderModel(modelLocation, matrix, buffer, combinedLight, combinedOverlay);
                break;
            case BLOCK:
                BlockState blockState = appearance.getBlock().getDefaultState();
                renderBlockState(blockState, matrix, buffer, combinedLight, combinedOverlay);
                break;
            case MODEL:
                ResourceLocation modelFile = appearance.getModel();
                renderModel(modelFile, matrix, buffer, combinedLight, combinedOverlay);
                break;
            default:
                renderDefaultModel(matrix, buffer, combinedLight, combinedOverlay);
        }
    }

    private static void renderBlockState(BlockState state, MatrixStack matrix, IRenderTypeBuffer buffer, int combinedLight, int combinedOverlay) {
        Minecraft.getInstance().getBlockRendererDispatcher().renderBlock(state, matrix, buffer, combinedLight, combinedOverlay, EmptyModelData.INSTANCE);
    }

    private static void renderModel(ResourceLocation modelFile, MatrixStack matrix, IRenderTypeBuffer buffer, int combinedLight, int combinedOverlay) {
        IBakedModel model = Minecraft.getInstance().getModelManager().getModel(modelFile);
        if(model != Minecraft.getInstance().getModelManager().getMissingModel()) {
            IVertexBuilder builder = buffer.getBuffer(RenderType.getSolid());
            model.getQuads(null, null, RAND, EmptyModelData.INSTANCE).forEach(bakedQuad -> builder.addQuad(matrix.getLast(), bakedQuad, 1.0F, 1.0F, 1.0F, combinedLight, combinedOverlay));
            Stream.of(Direction.values()).forEach(side -> model.getQuads(null, side, RAND, EmptyModelData.INSTANCE).forEach(quad -> builder.addQuad(matrix.getLast(), quad, 1.0F, 1.0F, 1.0F, combinedLight, combinedOverlay)));
        }
        else if(!MODELS.containsKey(modelFile))
            MODELS.put(modelFile, new ModelHandle(modelFile));
        else
            MODELS.get(modelFile).render(buffer, RenderType.getSolid(), matrix, combinedLight, combinedOverlay, 0xFFFFFFFF);
    }

    private static void renderDefaultModel(MatrixStack matrix, IRenderTypeBuffer buffer, int combinedLight, int combinedOverlay) {
        IVertexBuilder builder = buffer.getBuffer(RenderType.getSolid());
        //Stream.of(Direction.values()).forEach(side -> Minecraft.getInstance().getModelManager().getModel(DEFAULT_MODEL).getQuads(null, side, RAND, EmptyModelData.INSTANCE).forEach(quad -> builder.addQuad(matrix.getLast(), quad, 1.0F, 1.0F, 1.0F, combinedLight, combinedOverlay)));
        Stream.of(Direction.values()).forEach(side -> Minecraft.getInstance().getModelManager().getMissingModel().getQuads(null, side, RAND, EmptyModelData.INSTANCE).forEach(quad -> builder.addQuad(matrix.getLast(), quad, 1.0F, 1.0F, 1.0F, combinedLight, combinedOverlay)));
    }
}
