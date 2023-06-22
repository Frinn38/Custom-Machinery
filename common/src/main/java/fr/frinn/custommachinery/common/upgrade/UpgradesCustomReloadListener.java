package fr.frinn.custommachinery.common.upgrade;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.JsonOps;
import dev.architectury.platform.Platform;
import fr.frinn.custommachinery.CustomMachinery;
import fr.frinn.custommachinery.api.ICustomMachineryAPI;
import fr.frinn.custommachinery.common.integration.kubejs.KubeJSIntegration;
import fr.frinn.custommachinery.common.util.CustomJsonReloadListener;
import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.util.profiling.ProfilerFiller;
import net.minecraft.world.item.Items;
import org.apache.logging.log4j.Logger;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class UpgradesCustomReloadListener extends CustomJsonReloadListener {

    private static final Gson GSON = new GsonBuilder().create();
    private static final String MAIN_PACKNAME = "main";

    public UpgradesCustomReloadListener() {
        super("upgrades");
    }

    @Override
    protected void apply(Map<ResourceLocation, JsonElement> map, ResourceManager resourceManager, ProfilerFiller profiler) {
        final Logger logger = ICustomMachineryAPI.INSTANCE.logger();

        List<MachineUpgrade> upgrades = new ArrayList<>();

        logger.info("Reading Custom Machinery Upgrades json");

        map.forEach((id, json) -> {
            String packName;
            try {
                packName = resourceManager.getResourceOrThrow(new ResourceLocation(id.getNamespace(), "upgrades/" + id.getPath() + ".json")).sourcePackId();
            } catch (IOException e) {
                packName = MAIN_PACKNAME;
            }
            logger.info("Parsing upgrade json: {} in datapack: {}", id, packName);

            if(!json.isJsonObject()) {
                logger.error("Bad upgrade JSON: {} must be a json object and not an array or primitive, skipping...", id);
                return;
            }

            DataResult<MachineUpgrade> result = MachineUpgrade.CODEC.read(JsonOps.INSTANCE, json);
            if(result.result().isPresent()) {
                MachineUpgrade upgrade = result.result().get();
                if(upgrade.getItem() == Items.AIR) {
                    logger.error("Invalid item: {}, defined for upgrade: {}", Registry.ITEM.getKey(upgrade.getItem()), id);
                    return;
                }
                logger.info("Successfully parsed upgrade json: {}", id);
                upgrades.add(upgrade);
                return;
            } else if(result.error().isPresent()) {
                logger.error("Error while parsing upgrade json: {}, skipping...\n{}", id, result.error().get().message());
                return;
            }
            throw new IllegalStateException("No success nor error when parsing machine json: " + id + ". This can't happen.");
        });

        if(upgrades.size() != 0)
            logger.info("Successfully parsed {} upgrade json.", upgrades.size());
        else
            logger.info("No machine upgrade json found.");

        if(Platform.isModLoaded("kubejs")) {
            logger.info("Collecting machine upgrades with kubeJS.");
            List<MachineUpgrade> kubejsUpgrades = KubeJSIntegration.collectMachineUpgrades();
            if(kubejsUpgrades.size() != 0)
                logger.info("Successfully added {} machine upgrades with kubejs", kubejsUpgrades.size());
            else
                logger.info("No machine upgrades found with kubejs");
            upgrades.addAll(kubejsUpgrades);
        }

        logger.info("Finished creating custom machine upgrades.");

        CustomMachinery.UPGRADES.refresh(upgrades);
    }
}