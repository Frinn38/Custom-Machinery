package fr.frinn.custommachinery.common.data;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.JsonOps;
import fr.frinn.custommachinery.CustomMachinery;
import fr.frinn.custommachinery.api.CustomMachineryAPI;
import net.minecraft.client.resources.JsonReloadListener;
import net.minecraft.profiler.IProfiler;
import net.minecraft.resources.IResourceManager;
import net.minecraft.util.ResourceLocation;

import javax.annotation.ParametersAreNonnullByDefault;
import java.io.IOException;
import java.util.Map;

public class CustomMachineJsonReloadListener extends JsonReloadListener {

    private static final Gson GSON = (new GsonBuilder()).create();
    private static final String MAIN_PACKNAME = "main";

    public CustomMachineJsonReloadListener() {
        super(GSON, "machines");
    }

    @ParametersAreNonnullByDefault
    @Override
    protected void apply(Map<ResourceLocation, JsonElement> map, IResourceManager resourceManager, IProfiler profiler) {
        CustomMachineryAPI.getLogger().info("Reading Custom Machinery Machines...");

        CustomMachinery.MACHINES.clear();

        map.forEach((id, json) -> {
            String packName;
            try {
                packName = resourceManager.getResource(new ResourceLocation(id.getNamespace(), "machines/" + id.getPath() + ".json")).getPackName();
            } catch (IOException e) {
                packName = MAIN_PACKNAME;
            }
            CustomMachineryAPI.getLogger().info("Parsing machine json: %s in datapack: %s", id, packName);

            if(!json.isJsonObject()) {
                CustomMachineryAPI.getLogger().error("Bad machine JSON: %s must be a json object and not an array or primitive, skipping...", id);
                return;
            }

            if(CustomMachinery.MACHINES.containsKey(id)) {
                CustomMachineryAPI.getLogger().error("A machine with id: %s already exists, skipping...", id);
                return;
            }

            DataResult<CustomMachine> result = CustomMachine.CODEC.parse(JsonOps.INSTANCE, json);
            if(result.result().isPresent()) {
                CustomMachine machine = result.result().get();
                if(packName.equals(MAIN_PACKNAME))
                    machine.setLocation(MachineLocation.fromDefault(id));
                else
                    machine.setLocation(MachineLocation.fromDatapack(id, packName));
                CustomMachinery.MACHINES.put(id, machine);
                CustomMachineryAPI.getLogger().info("Successfully parsed machine json: %s", id);
                return;
            } else if(result.error().isPresent()) {
                CustomMachineryAPI.getLogger().error("Error while parsing machine json: %s, skipping...%n%s", id, result.error().get().message());
                return;
            }
            throw new IllegalStateException("No success nor error when parsing machine json: " + id + ". This can't happen.");
        });
        CustomMachineryAPI.getLogger().info("Finished creating custom machines.");
    }
}