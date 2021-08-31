package fr.frinn.custommachinery.common.data;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.google.gson.JsonParseException;
import com.mojang.datafixers.util.Pair;
import com.mojang.serialization.JsonOps;
import fr.frinn.custommachinery.CustomMachinery;
import fr.frinn.custommachinery.common.network.NetworkManager;
import fr.frinn.custommachinery.common.network.SLootTablesPacket;
import fr.frinn.custommachinery.common.network.SUpdateMachinesPacket;
import fr.frinn.custommachinery.common.util.LootTableHelper;
import net.minecraft.client.resources.JsonReloadListener;
import net.minecraft.profiler.IProfiler;
import net.minecraft.resources.IResourceManager;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.concurrent.TickDelayedTask;
import net.minecraftforge.fml.network.PacketDistributor;
import net.minecraftforge.fml.server.ServerLifecycleHooks;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import javax.annotation.ParametersAreNonnullByDefault;
import java.io.IOException;
import java.util.Map;

public class CustomMachineJsonReloadListener extends JsonReloadListener {

    public static final Logger LOGGER = LogManager.getLogger("CustomMachinery/MachineLoader");
    public static final Gson GSON = (new GsonBuilder()).create();
    public static final String MAIN_PACKNAME = "main";

    public CustomMachineJsonReloadListener() {
        super(GSON, "machines");
    }

    @ParametersAreNonnullByDefault
    @Override
    protected void apply(Map<ResourceLocation, JsonElement> map, IResourceManager resourceManager, IProfiler profiler) {
        LOGGER.info("Reading Custom Machinery machines...");

        CustomMachinery.MACHINES.clear();
        map.forEach((id, json) -> {
            String packName;
            try {
                packName = resourceManager.getResource(new ResourceLocation(id.getNamespace(), "machines/" + id.getPath() + ".json")).getPackName();
                LOGGER.info("Found Custom Machine Json in datapack: " + packName);
            } catch (IOException e) {
                packName = MAIN_PACKNAME;
            }
            LOGGER.info("Parsing: " + id);

            if(!json.isJsonObject()) {
                LOGGER.warn("Bad Machine JSON in: " + id);
                return;
            }

            if(CustomMachinery.MACHINES.containsKey(id)) {
                LOGGER.warn("A machine with id: " + id + " already exists, skipping...");
                return;
            }

            CustomMachine machine = CustomMachine.CODEC.decode(JsonOps.INSTANCE, json).resultOrPartial(LOGGER::error).map(Pair::getFirst).orElseThrow(() -> new JsonParseException("Error while reading machine: " + id));
            if(packName.equals(MAIN_PACKNAME))
                machine.setLocation(MachineLocation.fromDefault(id));
            else
                machine.setLocation(MachineLocation.fromDatapack(id, packName));
            CustomMachinery.MACHINES.put(id, machine);
        });

        LOGGER.info("Finished creating custom machines.");
    }
}
