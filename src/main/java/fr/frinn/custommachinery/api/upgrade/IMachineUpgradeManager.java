package fr.frinn.custommachinery.api.upgrade;

import com.mojang.datafixers.util.Pair;
import fr.frinn.custommachinery.common.upgrade.MachineUpgrade;

import java.util.List;
import java.util.stream.Stream;

public interface IMachineUpgradeManager {

    /**
     * Mark the cached upgrades list as dirty.
     * Should be called each time the content of an upgrade slot changed.
     */
    void refresh();

    /**
     * Get the upgrades currently applied to the machine.
     * @return A stream of upgrades and how much of each are currently applied.
     */
    Stream<Pair<MachineUpgrade, Integer>> getCurrentUpgrades();

    /**
     * Get the recipe modifiers currently applied to the machine.
     * @return A list of recipe modifiers and how much of each are currently applied.
     */
    List<Pair<IRecipeModifier, Integer>> getRecipeModifiers();
}
