package fr.frinn.custommachinery.api.component;

import fr.frinn.custommachinery.api.component.handler.IComponentHandler;
import fr.frinn.custommachinery.api.machine.MachineTile;
import fr.frinn.custommachinery.api.network.ISyncableStuff;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.level.Level;

import java.util.List;
import java.util.Map;
import java.util.Optional;

public interface IMachineComponentManager {

    /**
     * @return A copy of the List of IMachineComponent hold by the manager.
     */
    Map<MachineComponentType<?>, IMachineComponent> getComponents();

    /**
     * @return A List of all components hold by this manager that implements ISerializableComponent.
     */
    List<ISerializableComponent> getSerializableComponents();

    /**
     * @return A List of all components hold by this manager that implements ITickableComponent.
     */
    List<ITickableComponent> getTickableComponents();

    /**
     * @return A List of all components hold by this manager that implements ISyncableStuff.
     */
    List<ISyncableStuff> getSyncableComponents();

    /**
     * @return A List of all components hold by this manager that implements IComparatorInputComponent.
     */
    List<IComparatorInputComponent> getComparatorInputComponents();

    /**
     * @param type The MachineComponentType to search.
     * @param <T> The component.
     * @return An Optional IMachineComponent.
     */
    <T extends IMachineComponent> Optional<T> getComponent(MachineComponentType<T> type);

    /**
     * @param type The MachineComponentType to search.
     * @param <T> The Component.
     * @return An Optional IComponentHandler<T>.
     */
    <T extends IMachineComponent> Optional<IComponentHandler<T>> getComponentHandler(MachineComponentType<T> type);

    /**
     * @param type The MachineComponentType to check.
     * @return true if this manager hold a component for this type, false otherwise.
     */
    boolean hasComponent(MachineComponentType<?> type);

    /**
     * @return The MachineTile that hold this manager.
     */
    MachineTile getTile();

    /**
     * @return The world the machine is in.
     */
    Level getLevel();

    /**
     * @return The server currently running (integrated or dedicated).
     */
    MinecraftServer getServer();

    /**
     * Mark the tile as dirty, meaning some data changed and need to be written on disk on next tile unload.
     */
    void markDirty();
}
