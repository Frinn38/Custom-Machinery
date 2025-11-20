package fr.frinn.custommachinery.common.integration.kubejs;

import dev.latvian.mods.kubejs.KubeJS;
import dev.latvian.mods.kubejs.event.EventResult;
import dev.latvian.mods.kubejs.script.ScriptType;
import dev.latvian.mods.kubejs.script.data.KubeFileResourcePack;
import dev.latvian.mods.kubejs.script.data.VirtualDataPack;
import fr.frinn.custommachinery.api.crafting.CraftingResult;
import fr.frinn.custommachinery.api.crafting.ICraftingContext;
import fr.frinn.custommachinery.common.integration.kubejs.CustomMachineUpgradeJSBuilder.UpgradeKubeEvent;
import fr.frinn.custommachinery.common.integration.kubejs.function.FunctionKubeEvent;
import fr.frinn.custommachinery.common.machine.MachineLocation;
import fr.frinn.custommachinery.common.upgrade.MachineUpgrade;
import fr.frinn.custommachinery.common.upgrade.UpgradeLocation;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.packs.PackResources;
import net.minecraft.server.packs.resources.Resource;
import net.neoforged.neoforge.server.ServerLifecycleHooks;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.attribute.BasicFileAttributeView;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class KubeJSIntegration {

    public static MachineLocation getMachineLocation(Resource resource, String packName, ResourceLocation id) {
        try(PackResources pack = resource.source()) {
            if(pack instanceof KubeFileResourcePack) {
                if(ServerLifecycleHooks.getCurrentServer() instanceof MinecraftServer server) {
                    File file = MachineLocation.getFile(server, id, MachineLocation.Loader.KUBEJS, packName);
                    if(file != null && file.exists()) {
                        try {
                            BasicFileAttributes attributes = Files.getFileAttributeView(file.toPath(), BasicFileAttributeView.class).readAttributes();
                            return MachineLocation.fromKubeJS(id, packName, attributes.creationTime(), attributes.lastModifiedTime());
                        } catch (IOException ignored) {

                        }
                    }
                }
                return MachineLocation.fromKubeJS(id, packName, null, null);
            }
            else if(pack instanceof VirtualDataPack)
                return MachineLocation.fromKubeJSScript(id, packName);
            else
                return MachineLocation.fromDefault(id, packName);
        }
    }

    public static UpgradeLocation getUpgradeLocation(Resource resource, String packName, ResourceLocation id) {
        try(PackResources pack = resource.source()) {
            if(pack instanceof KubeFileResourcePack) {
                if(ServerLifecycleHooks.getCurrentServer() instanceof MinecraftServer server) {
                    File file = UpgradeLocation.getFile(server, id, MachineLocation.Loader.KUBEJS, packName);
                    if(file != null && file.exists()) {
                        try {
                            BasicFileAttributes attributes = Files.getFileAttributeView(file.toPath(), BasicFileAttributeView.class).readAttributes();
                            return UpgradeLocation.fromKubeJS(id, packName, attributes.creationTime(), attributes.lastModifiedTime());
                        } catch (IOException ignored) {

                        }
                    }
                }
                return UpgradeLocation.fromKubeJS(id, packName, null, null);
            }
            else if(pack instanceof VirtualDataPack)
                return UpgradeLocation.fromKubeJSScript(id, packName);
            else
                return UpgradeLocation.fromDefault(id, packName);
        }
    }

    public static Map<UpgradeLocation, MachineUpgrade> collectMachineUpgrades() {
        ScriptType.SERVER.console.info("Collecting Custom Machine upgrades from JS scripts.");

        UpgradeKubeEvent event = new UpgradeKubeEvent();
        CustomMachineryKubeJSPlugin.UPGRADES.post(ScriptType.SERVER, event);

        List<MachineUpgrade> upgrades = new ArrayList<>();

        try {
            upgrades = event.getBuilders().stream().map(CustomMachineUpgradeJSBuilder::build).toList();
        } catch (Exception e) {
            ScriptType.SERVER.console.warn("Couldn't build machine upgrade", e);
        }

        ScriptType.SERVER.console.infof("Successfully added %s Custom Machine upgrades", event.getBuilders().size());

        Map<UpgradeLocation, MachineUpgrade> upgradeMap = new HashMap<>();
        upgrades.stream().collect(Collectors.groupingBy(MachineUpgrade::item)).forEach((item, upgradeList) -> {
            ResourceLocation itemId = BuiltInRegistries.ITEM.getKey(item);
            if(upgradeList.size() == 1)
                upgradeMap.put(UpgradeLocation.fromKubeJSScript(ResourceLocation.fromNamespaceAndPath(KubeJS.MOD_ID, itemId.getNamespace() + "/" + itemId.getPath()), ""), upgradeList.getFirst());
            else
                upgradeList.forEach(u -> upgradeMap.put(UpgradeLocation.fromKubeJSScript(ResourceLocation.fromNamespaceAndPath(KubeJS.MOD_ID, itemId.getNamespace() + "/" + itemId.getPath() + "/" + upgradeList.indexOf(u)), ""), u));
        });
        return upgradeMap;
    }

    public static CraftingResult sendFunctionRequirementEvent(String id, ICraftingContext context) {
        if(!CustomMachineryKubeJSPlugin.FUNCTIONS.hasListeners(id))
            return CraftingResult.error(Component.translatable("custommachinery.requirements.function.no_listener", id));
        EventResult result = CustomMachineryKubeJSPlugin.FUNCTIONS.post(new FunctionKubeEvent(context), id);
        if(result.interruptTrue() || result.interruptDefault() || result.pass())
            return CraftingResult.success();
        else if(result.value() instanceof Component error)
            return CraftingResult.error(error);
        else if(result.value() instanceof CharSequence charSequence)
            return CraftingResult.error(Component.literal(charSequence.toString()));
        else
            return CraftingResult.error(Component.translatable("custommachinery.requirements.function.interrupt"));
    }

    public static void logError(Throwable error) {
        ScriptType.SERVER.console.error("Error while processing function requirement: ", error);
    }
}
