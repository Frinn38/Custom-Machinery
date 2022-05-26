package fr.frinn.custommachinery.client.render;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonObject;
import com.mojang.datafixers.util.Pair;
import fr.frinn.custommachinery.api.machine.MachineStatus;
import net.minecraft.client.renderer.block.model.ItemOverrides;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.client.resources.model.Material;
import net.minecraft.client.resources.model.ModelBakery;
import net.minecraft.client.resources.model.ModelState;
import net.minecraft.client.resources.model.UnbakedModel;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraftforge.client.model.ForgeModelBakery;
import net.minecraftforge.client.model.IModelConfiguration;
import net.minecraftforge.client.model.IModelLoader;
import net.minecraftforge.client.model.geometry.IModelGeometry;

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.EnumMap;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;

public class CustomMachineModelLoader implements IModelLoader<CustomMachineModelLoader.CustomMachineModelGeometry> {

    public static final CustomMachineModelLoader INSTANCE = new CustomMachineModelLoader();

    @Override
    public void onResourceManagerReload(ResourceManager resourceManager) {

    }

    @Override
    public CustomMachineModelGeometry read(JsonDeserializationContext deserializationContext, JsonObject json) {
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
        defaults.forEach((status, loc) -> ForgeModelBakery.addSpecialModel(loc));
        return new CustomMachineModelGeometry(defaults);
    }

    public static class CustomMachineModelGeometry implements IModelGeometry<CustomMachineModelGeometry> {

        private final Map<MachineStatus, ResourceLocation> defaults;

        public CustomMachineModelGeometry(Map<MachineStatus, ResourceLocation> defaults) {
            this.defaults = defaults;
        }

        @Override
        public Collection<Material> getTextures(IModelConfiguration owner, Function<ResourceLocation, UnbakedModel> modelGetter, Set<Pair<String, String>> missingTextureErrors) {
            return Collections.emptyList();
        }

        @Override
        public BakedModel bake(IModelConfiguration owner, ModelBakery bakery, Function<Material, TextureAtlasSprite> spriteGetter, ModelState modelTransform, ItemOverrides overrides, ResourceLocation modelLocation) {
            return new CustomMachineBakedModel(this.defaults);
        }
    }
}
