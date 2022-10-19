package fr.frinn.custommachinery.api.utils;

import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import net.minecraft.ResourceLocationException;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.Nullable;

public class ModelLocation {

    public static final Codec<ModelLocation> CODEC = Codec.STRING.comapFlatMap(s -> {
        try {
            return DataResult.success(ModelLocation.of(s));
        } catch (ResourceLocationException e) {
            return DataResult.error(e.getMessage());
        }
    }, ModelLocation::toString);

    private final ResourceLocation loc;
    @Nullable
    private final String properties;

    public static ModelLocation of(String s) {
        if(!s.contains("#"))
            return new ModelLocation(new ResourceLocation(s), null);

        return new ModelLocation(new ResourceLocation(s.substring(0, s.indexOf("#"))), s.substring(s.indexOf("#")));
    }

    public static ModelLocation of(ResourceLocation rl) {
        return new ModelLocation(rl, null);
    }

    public static ModelLocation of(ResourceLocation rl, String properties) {
        return new ModelLocation(rl, properties);
    }

    private ModelLocation(ResourceLocation loc, @Nullable String properties) {
        this.loc = loc;
        this.properties = properties;
    }

    public ResourceLocation getLoc() {
        return this.loc;
    }

    public String getProperties() {
        return this.properties;
    }

    @Override
    public String toString() {
        return this.loc.toString() + "#" + this.properties;
    }
}
