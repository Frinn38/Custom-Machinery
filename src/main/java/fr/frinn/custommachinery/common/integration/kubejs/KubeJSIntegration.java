package fr.frinn.custommachinery.common.integration.kubejs;

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
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.PackResources;
import net.minecraft.server.packs.resources.Resource;

import java.util.ArrayList;
import java.util.List;

public class KubeJSIntegration {

    public static MachineLocation getMachineLocation(Resource resource, String packName, ResourceLocation id) {
        try(PackResources pack = resource.source()) {
            if(pack instanceof KubeFileResourcePack)
                return MachineLocation.fromKubeJS(id, packName);
            else if(pack instanceof VirtualDataPack)
                return MachineLocation.fromKubeJSScript(id, packName);
            return MachineLocation.fromDefault(id, packName);
        }
    }

    public static List<MachineUpgrade> collectMachineUpgrades() {
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
        return upgrades;
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
