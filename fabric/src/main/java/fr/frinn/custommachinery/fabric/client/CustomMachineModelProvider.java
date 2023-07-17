package fr.frinn.custommachinery.fabric.client;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import fr.frinn.custommachinery.CustomMachinery;
import fr.frinn.custommachinery.api.machine.MachineStatus;
import fr.frinn.custommachinery.common.init.CustomMachineBlock;
import fr.frinn.custommachinery.common.init.CustomMachineItem;
import fr.frinn.custommachinery.common.init.Registration;
import net.fabricmc.fabric.api.client.model.ModelProviderContext;
import net.fabricmc.fabric.api.client.model.ModelResourceProvider;
import net.minecraft.client.Minecraft;
import net.minecraft.client.resources.model.UnbakedModel;
import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.Nullable;

import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Arrays;
import java.util.EnumMap;
import java.util.Locale;
import java.util.Map;

public class CustomMachineModelProvider implements ModelResourceProvider {

    public static final CustomMachineModelProvider INSTANCE = new CustomMachineModelProvider();
    private static final Gson GSON = new Gson();

    @Nullable
    @Override
    public UnbakedModel loadModelResource(ResourceLocation loc, ModelProviderContext context) {
        if(loc.equals(new ResourceLocation(CustomMachinery.MODID, "block/custom_machine_block")) ||
            loc.equals(new ResourceLocation(CustomMachinery.MODID, "item/custom_machine_item")) ||
            isCustomMachine(loc)) {
            try {
                ResourceLocation filePath = new ResourceLocation(loc.getNamespace(), "models/" + loc.getPath() + ".json");
                JsonObject json = GSON.fromJson(new InputStreamReader(Minecraft.getInstance().getResourceManager().open(filePath)), JsonObject.class);
                return parse(json);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
        return null;
    }

    private UnbakedModel parse(JsonObject json) {
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
        return new CustomMachineUnbakedModel(defaults);
    }

    private boolean isCustomMachine(ResourceLocation loc) {
        String[] split = loc.getPath().split("/");
        ResourceLocation id = new ResourceLocation(loc.getNamespace(), split[split.length -1]);
        return Registration.REGISTRIES.get(Registry.BLOCK_REGISTRY).get(id) instanceof CustomMachineBlock ||
                Registration.REGISTRIES.get(Registry.ITEM_REGISTRY).get(id) instanceof CustomMachineItem;
    }
}
