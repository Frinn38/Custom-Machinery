package fr.frinn.custommachinery.client.render;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonObject;
import com.mojang.datafixers.util.Pair;
import fr.frinn.custommachinery.api.machine.MachineStatus;
import fr.frinn.custommachinery.common.util.Utils;
import mcp.MethodsReturnNonnullByDefault;
import net.minecraft.client.renderer.model.*;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.resources.IResourceManager;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.client.model.IModelBuilder;
import net.minecraftforge.client.model.IModelConfiguration;
import net.minecraftforge.client.model.IModelLoader;
import net.minecraftforge.client.model.ModelLoader;
import net.minecraftforge.client.model.geometry.IModelGeometry;
import net.minecraftforge.client.model.geometry.ISimpleModelGeometry;

import javax.annotation.ParametersAreNonnullByDefault;
import java.util.*;
import java.util.function.Function;

@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public class CustomMachineModelLoader implements IModelLoader<CustomMachineModelLoader.CustomMachineModelGeometry> {

    public static final CustomMachineModelLoader INSTANCE = new CustomMachineModelLoader();

    @Override
    public void onResourceManagerReload(IResourceManager resourceManager) {

    }

    @Override
    public CustomMachineModelGeometry read(JsonDeserializationContext deserializationContext, JsonObject json) {
        Map<MachineStatus, ResourceLocation> defaults = new EnumMap<>(MachineStatus.class);
        if(json.has("defaults") && json.get("defaults").isJsonObject()) {
            JsonObject defaultsJson = json.getAsJsonObject("defaults");
            if(defaultsJson.get("").isJsonPrimitive() && defaultsJson.get("").getAsJsonPrimitive().isString()) {
                ResourceLocation location = ResourceLocation.tryCreate(defaultsJson.get("").getAsString());
                Arrays.stream(MachineStatus.values()).forEach(status -> defaults.put(status, location));
            }
            for (MachineStatus status : MachineStatus.values()) {
                String key = status.name().toLowerCase(Locale.ROOT);
                if(defaultsJson.has(key) && defaultsJson.get(key).isJsonPrimitive() && defaultsJson.get(key).getAsJsonPrimitive().isString())
                    defaults.put(status, ResourceLocation.tryCreate(defaultsJson.get(key).getAsString()));
            }
        }
        defaults.forEach((status, loc) -> ModelLoader.addSpecialModel(loc));
        return new CustomMachineModelGeometry(defaults);
    }

    public static class CustomMachineModelGeometry implements IModelGeometry<CustomMachineModelGeometry> {

        private final Map<MachineStatus, ResourceLocation> defaults;

        public CustomMachineModelGeometry(Map<MachineStatus, ResourceLocation> defaults) {
            this.defaults = defaults;
        }

        @Override
        public Collection<RenderMaterial> getTextures(IModelConfiguration owner, Function<ResourceLocation, IUnbakedModel> modelGetter, Set<Pair<String, String>> missingTextureErrors) {
            return Collections.emptyList();
        }

        @Override
        public IBakedModel bake(IModelConfiguration owner, ModelBakery bakery, Function<RenderMaterial, TextureAtlasSprite> spriteGetter, IModelTransform modelTransform, ItemOverrideList overrides, ResourceLocation modelLocation) {
            return new CustomMachineBakedModel(this.defaults);
        }
    }
}
