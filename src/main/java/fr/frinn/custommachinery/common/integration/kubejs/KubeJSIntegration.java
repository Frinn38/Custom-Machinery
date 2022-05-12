package fr.frinn.custommachinery.common.integration.kubejs;

import dev.latvian.mods.kubejs.script.ScriptType;
import fr.frinn.custommachinery.common.data.upgrade.MachineUpgrade;

import java.util.ArrayList;
import java.util.List;

public class KubeJSIntegration {

    public static List<MachineUpgrade> collectMachineUpgrades() {
        ScriptType.SERVER.console.info("Collecting Custom Machine upgrades from JS scripts.");

        CustomMachineJSUpgradeBuilder.UpgradeEvent event = new CustomMachineJSUpgradeBuilder.UpgradeEvent();
        event.post(ScriptType.SERVER, "cm_upgrades");

        List<MachineUpgrade> upgrades = new ArrayList<>();

        try {
            upgrades = event.getBuilders().stream().map(CustomMachineJSUpgradeBuilder::build).toList();
        } catch (Exception e) {
            ScriptType.SERVER.console.warn("Couldn't build machine upgrade", e);
        }

        ScriptType.SERVER.console.infof("Successfully added %s Custom Machine upgrades", event.getBuilders().size());
        return upgrades;
    }

}
