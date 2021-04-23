package fr.frinn.custommachinery.common.util;

import net.minecraft.block.BlockState;
import net.minecraft.client.renderer.model.BakedQuad;
import net.minecraft.client.renderer.model.IBakedModel;
import net.minecraft.client.renderer.model.ItemOverrideList;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.util.Direction;
import net.minecraftforge.client.extensions.IForgeBakedModel;

import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class DummyBakedModel implements IBakedModel, IForgeBakedModel {

    private TextureAtlasSprite particle;

    public DummyBakedModel(TextureAtlasSprite particle) {
        this.particle = particle;
    }

    @ParametersAreNonnullByDefault
    @Override
    public List<BakedQuad> getQuads(@Nullable BlockState state, @Nullable Direction side, Random rand) {
        return new ArrayList<>();
    }

    @Override
    public boolean isAmbientOcclusion() {
        return false;
    }

    @Override
    public boolean isGui3d() {
        return false;
    }

    @Override
    public boolean isSideLit() {
        return false;
    }

    @Override
    public boolean isBuiltInRenderer() {
        return false;
    }

    @Override
    public TextureAtlasSprite getParticleTexture() {
        return this.particle;
    }

    @Override
    public ItemOverrideList getOverrides() {
        return null;
    }
}
