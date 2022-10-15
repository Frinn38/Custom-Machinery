package fr.frinn.custommachinery.api.crafting;

public interface IMachineUpgradeManager {

    /**
     * Mark the cached upgrades list as dirty.
     * Should be called each time the content of an upgrade slot changed.
     */
    void markDirty();
}
