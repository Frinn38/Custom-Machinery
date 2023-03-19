package fr.frinn.custommachinery.impl.util;

import com.mojang.serialization.DataResult;
import fr.frinn.custommachinery.api.codec.NamedCodec;
import net.minecraft.ResourceLocationException;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.Nullable;

public class ModelLocation {

    public static final NamedCodec<ModelLocation> CODEC = NamedCodec.STRING.comapFlatMap(s -> {
        try {
            return DataResult.success(ModelLocation.of(s));
        } catch (ResourceLocationException e) {
            return DataResult.error(e.getMessage());
        }
    }, ModelLocation::toString, "Model location");

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
        if(this.properties != null)
            return this.loc.toString() + "#" + this.properties;
        return this.loc.toString();
    }
}
