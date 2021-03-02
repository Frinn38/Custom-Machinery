package fr.frinn.custommachinery.client.render;

import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.vertex.IVertexBuilder;
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
import net.minecraft.util.Direction;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.client.model.data.EmptyModelData;

import javax.annotation.ParametersAreNonnullByDefault;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;
import java.util.stream.Stream;

public class CustomMachineRenderer extends TileEntityRenderer<CustomMachineTile> {

    private Map<ResourceLocation, ModelHandle> models = new HashMap<>();
    private Random random = new Random();

    public CustomMachineRenderer(TileEntityRendererDispatcher dispatcher) {
        super(dispatcher);
    }

    @ParametersAreNonnullByDefault
    @Override
    public void render(CustomMachineTile tile, float partialTicks, MatrixStack matrix, IRenderTypeBuffer buffer, int combinedLight, int combinedOverlay) {
        MachineAppearance appearance = tile.getMachine().getAppearance();
        switch (appearance.getType()) {
            case BLOCKSTATE:
                ModelResourceLocation modelLocation = appearance.getBlockstate();
                this.renderModel(modelLocation, matrix, buffer, combinedLight, combinedOverlay);
                break;
            case BLOCK:
                BlockState blockState = appearance.getBlock().getDefaultState();
                this.renderBlockState(blockState, matrix, buffer, combinedLight, combinedOverlay);
                break;
            case MODEL:
                ResourceLocation modelFile = appearance.getModel();
                this.renderModel(modelFile, matrix, buffer, combinedLight, combinedOverlay);
                break;
            default:
                this.renderDefaultModel(matrix, buffer, combinedLight, combinedOverlay);
        }
    }

    private void renderBlockState(BlockState state, MatrixStack matrix, IRenderTypeBuffer buffer, int combinedLight, int combinedOverlay) {
        Minecraft.getInstance().getBlockRendererDispatcher().renderBlock(state, matrix, buffer, combinedLight, combinedOverlay, EmptyModelData.INSTANCE);
    }

    private void renderModel(ResourceLocation modelFile, MatrixStack matrix, IRenderTypeBuffer buffer, int combinedLight, int combinedOverlay) {
        IBakedModel model = Minecraft.getInstance().getModelManager().getModel(modelFile);
        if(model != Minecraft.getInstance().getModelManager().getMissingModel()) {
            IVertexBuilder builder = buffer.getBuffer(RenderType.getSolid());
            model.getQuads(null, null, random, EmptyModelData.INSTANCE).forEach(bakedQuad -> builder.addQuad(matrix.getLast(), bakedQuad, 1.0F, 1.0F, 1.0F, combinedLight, combinedOverlay));
            Stream.of(Direction.values()).forEach(side -> model.getQuads(null, side, random, EmptyModelData.INSTANCE).forEach(quad -> builder.addQuad(matrix.getLast(), quad, 1.0F, 1.0F, 1.0F, combinedLight, combinedOverlay)));
        }
        else if(!this.models.containsKey(modelFile))
            this.models.put(modelFile, new ModelHandle(modelFile));
        else
            this.models.get(modelFile).render(buffer, RenderType.getSolid(), matrix, combinedLight, combinedOverlay, 0xFFFFFFFF);
    }

    private void renderDefaultModel(MatrixStack matrix, IRenderTypeBuffer buffer, int combinedLight, int combinedOverlay) {
        IVertexBuilder builder = buffer.getBuffer(RenderType.getSolid());
        Stream.of(Direction.values()).forEach(side -> Minecraft.getInstance().getModelManager().getMissingModel().getQuads(null, side, random, EmptyModelData.INSTANCE).forEach(quad -> builder.addQuad(matrix.getLast(), quad, 1.0F, 1.0F, 1.0F, combinedLight, combinedOverlay)));
    }
}
