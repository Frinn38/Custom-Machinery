package fr.frinn.custommachinery.common.upgrade;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

public class Upgrades {

    private List<MachineUpgrade> upgrades = Collections.emptyList();
    private Map<Item, List<MachineUpgrade>> upgradesByItem = Collections.emptyMap();
    private Map<ResourceLocation, List<MachineUpgrade>> upgradesByMachine = Collections.emptyMap();

    public void refresh(List<MachineUpgrade> upgrades) {
        this.upgrades = Collections.unmodifiableList(upgrades);
        this.upgradesByItem = upgrades.stream().collect(Collectors.groupingBy(MachineUpgrade::getItem));
        this.upgradesByMachine = upgrades.stream().flatMap(upgrade -> upgrade.getMachines().stream()).distinct()
                .collect(Collectors.toMap(Function.identity(), id -> upgrades.stream().filter(upgrade -> upgrade.getMachines().contains(id)).toList()));
    }

    public void addUpgrade(MachineUpgrade upgrade) {
        List<MachineUpgrade> upgrades = new ArrayList<>(this.upgrades);
        upgrades.add(upgrade);
        refresh(upgrades);
    }

    public List<MachineUpgrade> getAllUpgrades() {
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
