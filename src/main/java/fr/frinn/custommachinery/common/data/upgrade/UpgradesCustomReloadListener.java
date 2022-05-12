package fr.frinn.custommachinery.common.data.upgrade;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.JsonOps;
import fr.frinn.custommachinery.CustomMachinery;
import fr.frinn.custommachinery.api.ICustomMachineryAPI;
import fr.frinn.custommachinery.common.integration.kubejs.KubeJSIntegration;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.server.packs.resources.SimpleJsonResourceReloadListener;
import net.minecraft.util.profiling.ProfilerFiller;
import net.minecraft.world.item.Items;
import net.minecraftforge.fml.ModList;

import javax.annotation.ParametersAreNonnullByDefault;
import java.io.IOException;
import java.util.Map;

public class UpgradesCustomReloadListener extends SimpleJsonResourceReloadListener {

    private static final Gson GSON = (new GsonBuilder()).create();
    private static final String MAIN_PACKNAME = "main";

    public UpgradesCustomReloadListener() {
        super(GSON, "upgrades");
    }

    @ParametersAreNonnullByDefault
    @Override
    protected void apply(Map<ResourceLocation, JsonElement> map, ResourceManager resourceManager, ProfilerFiller profiler) {
        ICustomMachineryAPI.INSTANCE.logger().info("Reading Custom Machinery Upgrades...");

        CustomMachinery.UPGRADES.clear();

        map.forEach((id, json) -> {
            String packName;
            try {
                packName = resourceManager.getResource(new ResourceLocation(id.getNamespace(), "machines/" + id.getPath() + ".json")).getSourceName();
            } catch (IOException e) {
                packName = MAIN_PACKNAME;
            }
            ICustomMachineryAPI.INSTANCE.logger().info("Parsing upgrade json: {} in datapack: {}", id, packName);

            if(!json.isJsonObject()) {
                ICustomMachineryAPI.INSTANCE.logger().error("Bad upgrade JSON: {} must be a json object and not an array or primitive, skipping...", id);
                return;
            }

            DataResult<MachineUpgrade> result = MachineUpgrade.CODEC.parse(JsonOps.INSTANCE, json);
            if(result.result().isPresent()) {
                MachineUpgrade upgrade = result.result().get();
                if(upgrade.getItem() == Items.AIR) {
                    ICustomMachineryAPI.INSTANCE.logger().error("Invalid item: {}, defined for upgrade: {}", upgrade.getItem().getRegistryName(), id);
                    return;
                }
                ICustomMachineryAPI.INSTANCE.logger().info("Successfully parsed upgrade json: {}", id);
                CustomMachinery.UPGRADES.add(upgrade);
                return;
            } else if(result.error().isPresent()) {
                ICustomMachineryAPI.INSTANCE.logger().error("Error while parsing upgrade json: {}, skipping...\n{}", id, result.error().get().message());
                return;
            }
            throw new IllegalStateException("No success nor error when parsing machine json: " + id + ". This can't happen.");
        });

        ICustomMachineryAPI.INSTANCE.logger().info("Finished creating custom machine upgrades.");

        if(ModList.get().isLoaded("kubejs"))
            CustomMachinery.UPGRADES.addAll(KubeJSIntegration.collectMachineUpgrades());
    }
}