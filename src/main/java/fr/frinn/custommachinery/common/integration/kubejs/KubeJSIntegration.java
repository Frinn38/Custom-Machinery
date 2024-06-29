package fr.frinn.custommachinery.common.integration.kubejs;

import dev.latvian.mods.kubejs.script.ScriptType;
import dev.latvian.mods.kubejs.script.data.VirtualKubeJSDataPack;
import dev.latvian.mods.kubejs.server.GeneratedServerResourcePack;
import fr.frinn.custommachinery.common.machine.MachineLocation;
import fr.frinn.custommachinery.common.upgrade.MachineUpgrade;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.PackResources;
import net.minecraft.server.packs.resources.Resource;

import java.util.ArrayList;
import java.util.List;

public class KubeJSIntegration {

    public static MachineLocation getMachineLocation(Resource resource, String packName, ResourceLocation id) {
        try(PackResources pack = resource.source()) {
            if(pack instanceof GeneratedServerResourcePack)
                return MachineLocation.fromKubeJS(id, packName);
            else if(pack instanceof VirtualKubeJSDataPack)
                return MachineLocation.fromKubeJSScript(id, packName);
            return MachineLocation.fromDefault(id, packName);
        }
    }

    public static List<MachineUpgrade> collectMachineUpgrades() {
        ScriptType.SERVER.console.info("Collecting Custom Machine upgrades from JS scripts.");

        CustomMachineUpgradeJSBuilder.UpgradeEvent event = new CustomMachineUpgradeJSBuilder.UpgradeEvent();
        CustomMachineryKubeJSPlugin.UPGRADES.post(ScriptType.SERVER, event);

        List<MachineUpgrade> upgrades = new ArrayList<>();

        try {
            upgrades = event.getBuilders().stream().map(CustomMachineUpgradeJSBuilder::build).toList();
        } catch (Exception e) {
            ScriptType.SERVER.console.warn("Couldn't build machine upgrade", e);
        }

        ScriptType.SERVER.console.infof("Successfully added %s Custom Machine upgrades", event.getBuilders().size());
        return upgrades;
    }
}
