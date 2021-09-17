package fr.frinn.custommachinery.common.data.upgrade;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.JsonOps;
import fr.frinn.custommachinery.CustomMachinery;
import fr.frinn.custommachinery.api.CustomMachineryAPI;
import net.minecraft.client.resources.JsonReloadListener;
import net.minecraft.item.Items;
import net.minecraft.profiler.IProfiler;
import net.minecraft.resources.IResourceManager;
import net.minecraft.util.ResourceLocation;

import javax.annotation.ParametersAreNonnullByDefault;
import java.io.IOException;
import java.util.Map;

public class UpgradesCustomReloadListener extends JsonReloadListener {

    private static final Gson GSON = (new GsonBuilder()).create();
    private static final String MAIN_PACKNAME = "main";

    public UpgradesCustomReloadListener() {
        super(GSON, "upgrades");
    }

    @ParametersAreNonnullByDefault
    @Override
    protected void apply(Map<ResourceLocation, JsonElement> map, IResourceManager resourceManager, IProfiler profiler) {
        CustomMachineryAPI.getLogger().info("Reading Custom Machinery Upgrades...");

        CustomMachinery.UPGRADES.clear();

        map.forEach((id, json) -> {
            String packName;
            try {
                packName = resourceManager.getResource(new ResourceLocation(id.getNamespace(), "machines/" + id.getPath() + ".json")).getPackName();
            } catch (IOException e) {
                packName = MAIN_PACKNAME;
            }
            CustomMachineryAPI.getLogger().info("Parsing upgrade json: %s in datapack: %s", id, packName);

            if(!json.isJsonObject()) {
                CustomMachineryAPI.getLogger().error("Bad upgrade JSON: %s must be a json object and not an array or primitive, skipping..." + id);
                return;
            }

            DataResult<MachineUpgrade> result = MachineUpgrade.CODEC.parse(JsonOps.INSTANCE, json);
            if(result.result().isPresent()) {
                MachineUpgrade upgrade = result.result().get();
                if(upgrade.getItem() == Items.AIR) {
                    CustomMachineryAPI.getLogger().error("Invalid item: %s, defined for upgrade: %s", upgrade.getItem().getRegistryName(), id);
                    return;
                }
                CustomMachineryAPI.getLogger().info("Successfully parsed upgrade json: %s", id);
                CustomMachinery.UPGRADES.add(upgrade);
                return;
            } else if(result.error().isPresent()) {
                CustomMachineryAPI.getLogger().error("Error while parsing upgrade json: %s, skipping...%n%s", id, result.error().get().message());
                return;
            }
            throw new IllegalStateException("No success nor error when parsing machine json: " + id + ". This can't happen.");
        });

        CustomMachineryAPI.getLogger().info("Finished creating custom machine upgrades.");
    }
}