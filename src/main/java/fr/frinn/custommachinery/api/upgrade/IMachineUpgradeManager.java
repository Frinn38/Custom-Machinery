package fr.frinn.custommachinery.api.upgrade;

import com.mojang.datafixers.util.Pair;

import java.util.List;

public interface IMachineUpgradeManager {

    /**
     * Mark the cached upgrades list as dirty.
     * Should be called each time the content of an upgrade slot changed.
     */
    void markDirty();

    List<Pair<IRecipeModifier, Integer>> getAllModifiers();
}
