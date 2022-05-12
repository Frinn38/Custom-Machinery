package fr.frinn.custommachinery.common.data;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.JsonOps;
import fr.frinn.custommachinery.CustomMachinery;
import fr.frinn.custommachinery.api.ICustomMachineryAPI;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.server.packs.resources.SimpleJsonResourceReloadListener;
import net.minecraft.util.profiling.ProfilerFiller;

import javax.annotation.ParametersAreNonnullByDefault;
import java.io.IOException;
import java.util.Map;

public class CustomMachineJsonReloadListener extends SimpleJsonResourceReloadListener {

    private static final Gson GSON = (new GsonBuilder()).create();
    private static final String MAIN_PACKNAME = "main";

    public CustomMachineJsonReloadListener() {
        super(GSON, "machines");
    }

    @ParametersAreNonnullByDefault
    @Override
    protected void apply(Map<ResourceLocation, JsonElement> map, ResourceManager resourceManager, ProfilerFiller profiler) {
        ICustomMachineryAPI.INSTANCE.logger().info("Reading Custom Machinery Machines...");

        CustomMachinery.MACHINES.clear();

        map.forEach((id, json) -> {
            String packName;
            try {
                packName = resourceManager.getResource(new ResourceLocation(id.getNamespace(), "machines/" + id.getPath() + ".json")).getSourceName();
            } catch (IOException e) {
                packName = MAIN_PACKNAME;
            }
            ICustomMachineryAPI.INSTANCE.logger().info("Parsing machine json: %s in datapack: %s", id, packName);

            if(!json.isJsonObject()) {
                ICustomMachineryAPI.INSTANCE.logger().error("Bad machine JSON: %s must be a json object and not an array or primitive, skipping...", id);
                return;
            }

            if(CustomMachinery.MACHINES.containsKey(id)) {
                ICustomMachineryAPI.INSTANCE.logger().error("A machine with id: %s already exists, skipping...", id);
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
                ICustomMachineryAPI.INSTANCE.logger().info("Successfully parsed machine json: %s", id);
                return;
            } else if(result.error().isPresent()) {
                ICustomMachineryAPI.INSTANCE.logger().error("Error while parsing machine json: %s, skipping...%n%s", id, result.error().get().message());
                return;
            }
            throw new IllegalStateException("No success nor error when parsing machine json: " + id + ". This can't happen.");
        });
        ICustomMachineryAPI.INSTANCE.logger().info("Finished creating custom machines.");
    }
}