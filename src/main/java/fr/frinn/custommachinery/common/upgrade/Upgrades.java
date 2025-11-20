package fr.frinn.custommachinery.common.upgrade;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

public class Upgrades {

    private Map<UpgradeLocation, MachineUpgrade> upgrades;
    private Map<Item, List<MachineUpgrade>> upgradesByItem = Collections.emptyMap();
    private Map<ResourceLocation, List<MachineUpgrade>> upgradesByMachine = Collections.emptyMap();

    public void refresh(Map<UpgradeLocation, MachineUpgrade> upgrades) {
        this.upgrades = Collections.unmodifiableMap(upgrades);
        this.upgradesByItem = upgrades.values().stream().collect(Collectors.groupingBy(MachineUpgrade::item));
        this.upgradesByMachine = upgrades.values().stream().flatMap(upgrade -> upgrade.machines().stream()).distinct()
                .collect(Collectors.toMap(Function.identity(), id -> upgrades.values().stream().filter(upgrade -> upgrade.machines().contains(id)).toList()));
    }

    public void addUpgrade(UpgradeLocation location, MachineUpgrade upgrade) {
        Map<UpgradeLocation, MachineUpgrade> upgrades = new HashMap<>(this.upgrades);
        upgrades.put(location, upgrade);
        refresh(upgrades);
    }

    public void removeUpgrade(ResourceLocation id) {
        UpgradeLocation location = this.upgrades.keySet().stream().filter(loc -> loc.id().equals(id)).findFirst().orElse(null);
        if(location != null) {
            Map<UpgradeLocation, MachineUpgrade> upgrades = new HashMap<>(this.upgrades);
            upgrades.remove(location);
            refresh(upgrades);
        }
    }

    public Map<UpgradeLocation, MachineUpgrade> getAllUpgrades() {
        return this.upgrades;
    }

    public List<MachineUpgrade> getUpgradesForItem(Item item) {
        return this.upgradesByItem.getOrDefault(item, Collections.emptyList());
    }

    public List<MachineUpgrade> getUpgradesForMachine(ResourceLocation machineID) {
        return this.upgradesByMachine.getOrDefault(machineID, Collections.emptyList());
    }

    public List<MachineUpgrade> getUpgradesForItemAndMachine(Item item, ResourceLocation machineID) {
        return getUpgradesForItem(item).stream().filter(upgrade -> getUpgradesForMachine(machineID).contains(upgrade)).toList();
    }
}
