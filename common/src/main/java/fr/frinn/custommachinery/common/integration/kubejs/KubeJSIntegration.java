package fr.frinn.custommachinery.common.integration.kubejs;

import dev.latvian.mods.kubejs.KubeJSPaths;
import dev.latvian.mods.kubejs.script.ScriptType;
import dev.latvian.mods.kubejs.script.data.VirtualKubeJSDataPack;
import dev.latvian.mods.kubejs.server.GeneratedServerResourcePack;
import fr.frinn.custommachinery.common.machine.MachineLocation;
import fr.frinn.custommachinery.common.upgrade.MachineUpgrade;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.PackResources;
import net.minecraft.server.packs.PackType;
import net.minecraft.server.packs.resources.Resource;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.Nullable;

import java.nio.file.Path;
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

    @Nullable
    public static CompoundTag nbtFromStack(ItemStack stack) {
        CompoundTag nbt = stack.getTag();
        if(nbt == null || nbt.isEmpty())
            return null;
        if(nbt.contains("Damage", Tag.TAG_INT) && nbt.getInt("Damage") == 0)
            nbt.remove("Damage");
        if(nbt.isEmpty())
            return null;
        return nbt;
    }


    public static Path getMachineJsonPath(ResourceLocation location) {
        return KubeJSPaths.DIRECTORY.resolve(String.format("%s/%s/%s", PackType.SERVER_DATA.getDirectory(), location.getNamespace(), location.getPath()));
    }
}
