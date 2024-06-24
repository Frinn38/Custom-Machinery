package fr.frinn.custommachinery.client.model;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonObject;
import fr.frinn.custommachinery.api.machine.MachineStatus;
import fr.frinn.custommachinery.client.model.CustomMachineModelLoader.CustomMachineModelGeometry;
import net.minecraft.client.renderer.block.model.ItemOverrides;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.client.resources.model.Material;
import net.minecraft.client.resources.model.ModelBaker;
import net.minecraft.client.resources.model.ModelState;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.neoforge.client.model.geometry.IGeometryBakingContext;
import net.neoforged.neoforge.client.model.geometry.IGeometryLoader;
import net.neoforged.neoforge.client.model.geometry.IUnbakedGeometry;

import java.util.Arrays;
import java.util.EnumMap;
import java.util.Locale;
import java.util.Map;
import java.util.function.Function;

public class CustomMachineModelLoader implements IGeometryLoader<CustomMachineModelGeometry> {

    public static final CustomMachineModelLoader INSTANCE = new CustomMachineModelLoader();

    @Override
    public CustomMachineModelGeometry read(JsonObject json, JsonDeserializationContext deserializationContext) {
        Map<MachineStatus, ResourceLocation> defaults = new EnumMap<>(MachineStatus.class);
        if(json.has("defaults") && json.get("defaults").isJsonObject()) {
            JsonObject defaultsJson = json.getAsJsonObject("defaults");
            if(defaultsJson.get("").isJsonPrimitive() && defaultsJson.get("").getAsJsonPrimitive().isString()) {
                ResourceLocation location = ResourceLocation.tryParse(defaultsJson.get("").getAsString());
                Arrays.stream(MachineStatus.values()).forEach(status -> defaults.put(status, location));
            }
            for (MachineStatus status : MachineStatus.values()) {
                String key = status.name().toLowerCase(Locale.ROOT);
                if(defaultsJson.has(key) && defaultsJson.get(key).isJsonPrimitive() && defaultsJson.get(key).getAsJsonPrimitive().isString())
                    defaults.put(status, ResourceLocation.tryParse(defaultsJson.get(key).getAsString()));
            }
        }
        return new CustomMachineModelGeometry(defaults);
    }

    public static class CustomMachineModelGeometry implements IUnbakedGeometry<CustomMachineModelGeometry> {

        private final Map<MachineStatus, ResourceLocation> defaults;

        public CustomMachineModelGeometry(Map<MachineStatus, ResourceLocation> defaults) {
            this.defaults = defaults;
        }

        @Override
        public BakedModel bake(IGeometryBakingContext iGeometryBakingContext, ModelBaker arg, Function<Material, TextureAtlasSprite> function, ModelState arg2, ItemOverrides arg3) {
            return new CustomMachineBakedModel(this.defaults);
        }
    }
}
