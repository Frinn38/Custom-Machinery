package fr.frinn.custommachinery.client;

import net.minecraft.block.BlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.model.BakedQuad;
import net.minecraft.client.renderer.model.IBakedModel;
import net.minecraft.client.renderer.model.ItemOverrideList;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.util.Direction;
import net.minecraftforge.client.extensions.IForgeBakedModel;
import net.minecraftforge.client.model.data.IModelData;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.List;
import java.util.Random;

public class WrappedBakedModel implements IBakedModel, IForgeBakedModel {

    private IBakedModel model;

    public WrappedBakedModel(IBakedModel model) {
        this.model = model;
    }

    @Override
    public List<BakedQuad> getQuads(@Nullable BlockState state, @Nullable Direction side, Random rand) {
        return this.model.getQuads(state, side, rand);
    }

    @Override
    public boolean isAmbientOcclusion() {
        return this.model.isAmbientOcclusion();
    }

    @Override
    public boolean isGui3d() {
        return this.model.isGui3d();
    }

    @Override
    public boolean isSideLit() {
        return this.model.isSideLit();
    }

    @Override
    public boolean isBuiltInRenderer() {
        return this.model.isBuiltInRenderer();
    }

    @Override
    public TextureAtlasSprite getParticleTexture(@Nonnull IModelData data) {
        if(data.hasProperty(DummyBakedModel.PARTICLE_TEXTURE))
            return data.getData(DummyBakedModel.PARTICLE_TEXTURE);
        return Minecraft.getInstance().getModelManager().getMissingModel().getParticleTexture(data);
    }

    @Override
    public TextureAtlasSprite getParticleTexture() {
        return this.model.getParticleTexture();
    }

    @Override
    public ItemOverrideList getOverrides() {
        return this.model.getOverrides();
    }
}
