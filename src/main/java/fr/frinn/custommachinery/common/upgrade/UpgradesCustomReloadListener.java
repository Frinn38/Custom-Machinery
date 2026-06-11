package fr.frinn.custommachinery.common.upgrade;

import com.google.gson.JsonElement;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.JsonOps;
import fr.frinn.custommachinery.CustomMachinery;
import fr.frinn.custommachinery.api.ICustomMachineryAPI;
import fr.frinn.custommachinery.common.integration.kubejs.KubeJSIntegration;
import fr.frinn.custommachinery.common.machine.MachineLocation;
import fr.frinn.custommachinery.common.util.CustomJsonReloadListener;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.packs.FilePackResources;
import net.minecraft.server.packs.PackResources;
import net.minecraft.server.packs.PathPackResources;
import net.minecraft.server.packs.resources.Resource;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.util.profiling.ProfilerFiller;
import net.minecraft.world.item.Items;
import net.neoforged.fml.ModList;
import net.neoforged.neoforge.server.ServerLifecycleHooks;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.Marker;
import org.apache.logging.log4j.MarkerManager;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.attribute.BasicFileAttributeView;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.HashMap;
import java.util.Map;

public class UpgradesCustomReloadListener extends CustomJsonReloadListener {

    private static final String MAIN_PACKNAME = "main";

    public UpgradesCustomReloadListener() {
        super("upgrade", "upgrades");
    }

    @Override
    protected void apply(Map<ResourceLocation, JsonElement> map, ResourceManager resourceManager, ProfilerFiller profiler) {
        Logger logger = ICustomMachineryAPI.INSTANCE.logger();
        Marker marker = MarkerManager.getMarker("UpgradeLoader");

        Map<UpgradeLocation, MachineUpgrade> upgrades = new HashMap<>();

        logger.info(marker, "Reading Custom Machinery Upgrades json");

        map.forEach((id, json) -> {
            String packName;
            try {
                packName = resourceManager.getResourceOrThrow(ResourceLocation.fromNamespaceAndPath(id.getNamespace(), "upgrades/" + id.getPath() + ".json")).sourcePackId();
            } catch (IOException e) {
                packName = MAIN_PACKNAME;
            }
            logger.info(marker, "Parsing upgrade json: {} in datapack: {}", id, packName);

            if(!json.isJsonObject()) {
                logger.error(marker, "Bad upgrade JSON: {} must be a json object and not an array or primitive, skipping...", id);
                return;
            }

            DataResult<MachineUpgrade> result = MachineUpgrade.CODEC.read(JsonOps.INSTANCE, json);
            if(result.result().isPresent()) {
                MachineUpgrade upgrade = result.result().get();
                if(upgrade.item() == Items.AIR) {
                    logger.error(marker, "Invalid item: {}, defined for upgrade: {}", BuiltInRegistries.ITEM.getKey(upgrade.item()), id);
                    return;
                }
                logger.info(marker, "Successfully parsed upgrade json: {}", id);
                upgrades.put(this.getUpgradeLocation(resourceManager, id), upgrade);
                return;
            } else if(result.error().isPresent()) {
                logger.error(marker, "Error while parsing upgrade json: {}, skipping...\n{}", id, result.error().get().message());
                return;
            }
            throw new IllegalStateException("No success nor error when parsing machine json: " + id + ". This can't happen.");
        });

        if(!upgrades.isEmpty())
            logger.info(marker, "Successfully parsed {} upgrade json.", upgrades.size());
        else
            logger.info(marker, "No machine upgrade json found.");

        if(ModList.get().isLoaded("kubejs")) {
            logger.info(marker, "Collecting machine upgrades with kubeJS.");
            Map<UpgradeLocation, MachineUpgrade> kubejsUpgrades = KubeJSIntegration.collectMachineUpgrades();
            if(!kubejsUpgrades.isEmpty())
                logger.info(marker, "Successfully added {} machine upgrades with kubejs", kubejsUpgrades.size());
            else
                logger.info(marker, "No machine upgrades found with kubejs");
            upgrades.putAll(kubejsUpgrades);
        }

        logger.info(marker, "Finished creating custom machine upgrades.");

        CustomMachinery.UPGRADES.refresh(upgrades);
    }

    private UpgradeLocation getUpgradeLocation(ResourceManager resourceManager, ResourceLocation id) {
        ResourceLocation path = ResourceLocation.fromNamespaceAndPath(id.getNamespace(), "upgrade/" + id.getPath() + ".json");
        try {
            Resource res = resourceManager.getResourceOrThrow(path);
            String packName = res.sourcePackId();
            if(packName.equals(MAIN_PACKNAME))
                return UpgradeLocation.fromDefault(id, packName);
            else if(packName.contains("KubeJS") && ModList.get().isLoaded("kubejs"))
                return KubeJSIntegration.getUpgradeLocation(res, packName, id);
            else {
                try(PackResources pack = res.source()) {
                    if(pack instanceof FilePackResources) {
                        if(ServerLifecycleHooks.getCurrentServer() instanceof MinecraftServer server) {
                            File file = UpgradeLocation.getFile(server, id, MachineLocation.Loader.DATAPACK_ZIP, packName);
                            if(file != null && file.exists()) {
                                try {
                                    BasicFileAttributes attributes = Files.getFileAttributeView(file.toPath(), BasicFileAttributeView.class).readAttributes();
                                    return UpgradeLocation.fromDatapackZip(id, packName, attributes.creationTime(), attributes.lastModifiedTime());
                                } catch (IOException ignored) {

                                }
                            }
                        }
                        return UpgradeLocation.fromDatapackZip(id, packName, null, null);
                    }
                    else if(pack instanceof PathPackResources) {
                        if(ServerLifecycleHooks.getCurrentServer() instanceof MinecraftServer server) {
                            File file = UpgradeLocation.getFile(server, id, MachineLocation.Loader.DATAPACK, packName);
                            if(file != null && file.exists()) {
                                try {
                                    BasicFileAttributes attributes = Files.getFileAttributeView(file.toPath(), BasicFileAttributeView.class).readAttributes();
                                    return UpgradeLocation.fromDatapack(id, packName, attributes.creationTime(), attributes.lastModifiedTime());
                                } catch (IOException ignored) {

                                }
                            }
                        }
                        return UpgradeLocation.fromDatapack(id, packName, null, null);
                    }
                }
            }
        } catch (IOException ignored) {

        }
        return UpgradeLocation.fromDefault(id, MAIN_PACKNAME);
    }
}