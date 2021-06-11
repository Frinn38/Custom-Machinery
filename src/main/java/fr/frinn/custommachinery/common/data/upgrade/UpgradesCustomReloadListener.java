package fr.frinn.custommachinery.common.data.upgrade;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.google.gson.JsonParseException;
import com.mojang.serialization.JsonOps;
import fr.frinn.custommachinery.CustomMachinery;
import fr.frinn.custommachinery.common.network.NetworkManager;
import fr.frinn.custommachinery.common.network.SUpdateMachinesPacket;
import net.minecraft.client.resources.JsonReloadListener;
import net.minecraft.item.Items;
import net.minecraft.profiler.IProfiler;
import net.minecraft.resources.IResourceManager;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.network.PacketDistributor;
import net.minecraftforge.fml.server.ServerLifecycleHooks;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import javax.annotation.ParametersAreNonnullByDefault;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class UpgradesCustomReloadListener extends JsonReloadListener {

    public static final Logger LOGGER = LogManager.getLogger("CustomMachinery/UpgradeLoader");
    public static final Gson GSON = (new GsonBuilder()).create();

    public UpgradesCustomReloadListener() {
        super(GSON, "upgrades");
    }

    @ParametersAreNonnullByDefault
    @Override
    protected void apply(Map<ResourceLocation, JsonElement> map, IResourceManager resourceManager, IProfiler profiler) {
        LOGGER.info("Reading Custom Machinery upgrades...");

        CustomMachinery.UPGRADES.clear();

        map.forEach((id, json) -> {
            LOGGER.info("Parsing: " + id);

            if(!json.isJsonObject()) {
                LOGGER.error("Bad upgrade json in: " + id);
                return;
            }

            MachineUpgrade upgrade = MachineUpgrade.CODEC.decode(JsonOps.INSTANCE, json).resultOrPartial(LOGGER::error).orElseThrow(() -> new JsonParseException("Error while reading custom machine upgrade: " + id)).getFirst();

            if(upgrade.getItem() == Items.AIR) {
                LOGGER.error("Bad item defined for custom machine upgrade: " + id);
                return;
            }

            for(MachineUpgrade check : CustomMachinery.UPGRADES) {
                if(check.getItem() == upgrade.getItem()) {
                    List<ResourceLocation> commonMachines = check.getMachines().stream().filter(upgrade.getMachines()::contains).collect(Collectors.toList());
                    if(!commonMachines.isEmpty()) {
                        LOGGER.error("Already existing custom machine upgrade for item: " + check.getItem().getRegistryName() + " and machines: " + commonMachines);
                        return;
                    }
                }
            }

            CustomMachinery.UPGRADES.add(upgrade);
        });

        LOGGER.info("Finished creating custom machine upgrades.");

        if(ServerLifecycleHooks.getCurrentServer() != null)
            NetworkManager.CHANNEL.send(PacketDistributor.ALL.noArg(), new SUpdateMachinesPacket(CustomMachinery.MACHINES));
    }
}
