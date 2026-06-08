package fr.frinn.custommachinery.impl.util;

import com.mojang.datafixers.util.Either;
import fr.frinn.custommachinery.api.codec.NamedCodec;
import fr.frinn.custommachinery.impl.codec.DefaultCodecs;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.fml.loading.FMLLoader;

import java.util.Optional;
import java.util.function.Function;

public record TextureInfo(ResourceLocation texture, int u, int v) {

    public static final NamedCodec<TextureInfo> COMPLETE_CODEC = NamedCodec.record(textureInfoInstance ->
            textureInfoInstance.group(
                    DefaultCodecs.RESOURCE_LOCATION.fieldOf("texture").forGetter(TextureInfo::texture),
                    NamedCodec.intRange(0, Integer.MAX_VALUE).optionalFieldOf("u", 0).forGetter(TextureInfo::u),
                    NamedCodec.intRange(0, Integer.MAX_VALUE).optionalFieldOf("v", 0).forGetter(TextureInfo::v)
            ).apply(textureInfoInstance, TextureInfo::new), "Texture");

    public static final NamedCodec<TextureInfo> CODEC = NamedCodec.either(DefaultCodecs.RESOURCE_LOCATION, COMPLETE_CODEC)
            .xmap(either -> either.map(TextureInfo::new, Function.identity()), Either::right, "Texture");

    public TextureInfo(ResourceLocation texture) {
        this(texture, 0, 0);
    }

    public int width() {
        if(FMLLoader.getDist() == Dist.CLIENT)
            return TextureSizeHelper.getTextureWidth(this.texture);
        throw new IllegalStateException("Can't get width of texture on the server");
    }

    public int height() {
        if(FMLLoader.getDist() == Dist.CLIENT)
            return TextureSizeHelper.getTextureHeight(this.texture);
        throw new IllegalStateException("Can't get height of texture on the server");
    }
}
