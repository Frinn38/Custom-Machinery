package fr.frinn.custommachinery.client;

import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.vertex.IVertexBuilder;
import net.minecraft.client.renderer.IRenderTypeBuffer;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.model.*;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.util.Direction;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.client.model.ModelLoader;
import net.minecraftforge.client.model.data.EmptyModelData;

import java.util.Random;
import java.util.stream.Stream;

public class ModelHandle {

    private final Random rand = new Random();
    private final IBakedModel model;

    public ModelHandle(ResourceLocation modelLocation)
    {
        IUnbakedModel model = ModelLoader.instance().getUnbakedModel(modelLocation);
        this.model = model.bakeModel(ModelLoader.instance(), ModelLoader.instance().getSpriteMap()::getSprite, ModelRotation.X0_Y0, modelLocation);
    }

    public void render(IRenderTypeBuffer bufferIn, RenderType rt, MatrixStack matrix, int combinedLight, int combinedOverlay, int color)
    {
        float a = ((color >> 24) & 0xFF) / 255.0f;
        float r = ((color >> 16) & 0xFF) / 255.0f;
        float g = ((color >> 8) & 0xFF) / 255.0f;
        float b = ((color >> 0) & 0xFF) / 255.0f;

        IVertexBuilder builder = bufferIn.getBuffer(rt);
        model.getQuads(null, null, rand, EmptyModelData.INSTANCE).forEach(quad -> builder.addQuad(matrix.getLast(), quad, r, g, b, combinedLight, combinedOverlay));
        Stream.of(Direction.values()).forEach(side -> model.getQuads(null, side, rand, EmptyModelData.INSTANCE).forEach(quad -> builder.addQuad(matrix.getLast(), quad, r, g, b, combinedLight, combinedOverlay)));
    }

    public TextureAtlasSprite getParticleTexture() {
        return this.model.getParticleTexture(EmptyModelData.INSTANCE);
    }
}
