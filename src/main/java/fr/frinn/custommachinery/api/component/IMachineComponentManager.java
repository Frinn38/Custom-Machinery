package fr.frinn.custommachinery.api.component;

import fr.frinn.custommachinery.api.component.handler.IComponentHandler;
import fr.frinn.custommachinery.api.machine.MachineTile;
import fr.frinn.custommachinery.api.network.ISyncableStuff;
import net.minecraft.core.Direction;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Consumer;
import java.util.function.Supplier;

public interface IMachineComponentManager {

    /**
     * @return A map of all of {@link IMachineComponent} hold by the manager.
     * @param <C> Class implementing {@link IMachineComponent}.
     * This map includes all single components and all {@link IComponentHandler} but not their subcomponents.
     */
    <C extends IMachineComponent> Map<MachineComponentType<C>, C> getComponents();

    /**
     * @return A map of all of {@link IMachineComponent} hold by the manager.
     * @param <C> Class implementing {@link IMachineComponent}.
     * This map includes all components and all {@link IComponentHandler} including their subcomponents.
     */
    <C extends IMachineComponent> Map<MachineComponentId<C>, C> getAllComponents();

    /**
     * @return A List of all components hold by this manager that implements {@link ISerializableComponent}.
     */
    List<ISerializableComponent> getSerializableComponents();

    /**
     * @return A List of all components hold by this manager that implements {@link ITickableComponent}.
     */
    List<ITickableComponent> getTickableComponents();

    /**
     * @return A List of all components hold by this manager that implements {@link ISyncableStuff}.
     */
    List<ISyncableStuff> getSyncableComponents();

    /**
     * @return A map of all components hold by this manager that implements {@link IComparatorInputComponent}.
     */
    <C extends IComparatorInputComponent> Map<MachineComponentId<C>, C> getComparatorInputComponents();

    /**
     * @return A List of all components hold by this manager that implements {@link IDumpComponent}.
     */
    List<IDumpComponent> getDumpComponents();

    /**
     * @param type The {@link MachineComponentType} to search.
     * @param <C> The class implementing {@link IMachineComponent}.
     * @return An Optional instance of {@link C}.
     */
    <C extends IMachineComponent> Optional<C> getComponent(MachineComponentType<C> type);

    /**
     * Search for a specific {@link IMachineComponent}, identified by its type and optionally its unique id if the component is handled by an {@link IComponentHandler}.
     * @param id The {@link MachineComponentId} to search.
     * @param <C> The class implementing {@link IMachineComponent}.
     * @return An Optional instance of {@link C}.
     */
    <C extends IMachineComponent> Optional<C> getComponent(MachineComponentId<C> id);

    /**
     * @param type The {@link MachineComponentType} to search.
     * @param <C> The class implementing {@link IMachineComponent}.
     * @return An optional {@link IComponentHandler<C>}.
     */
    <C extends IMachineComponent> Optional<IComponentHandler<C>> getComponentHandler(MachineComponentType<C> type);

    /**
     * @param type The {@link MachineComponentType} to check.
     * @param <C> The class implementing {@link IMachineComponent}.
     * @return true if this manager hold a component for this type, false otherwise.
     */
    <C extends IMachineComponent> boolean hasComponent(MachineComponentType<C> type);

    /**
     * Return a side-configurable component (if found).
     * @param id The id of the component to find, syntax is 'type:id' Example: 'fluid:input1'
     * @return A configurable component or nothing.
     */
    Optional<ISideConfigComponent> getConfigComponentById(String id);

    /**
     * @return The {@link MachineTile} that hold this manager.
     */
    MachineTile getTile();

    /**
     * @return The level the machine is in.
     */
    Level getLevel();

    /**
     * @return The server currently running (integrated or dedicated).
     */
    MinecraftServer getServer();

    /**
     * @return the {@link net.minecraft.core.Direction} the machine block is facing.
     */
    Direction facing();

    /**
     * Mark the tile as dirty, meaning some data changed and need to be written on disk on next tile unload.
     */
    void setChanged();

    /**
     * Add a value that can be modified using machine upgrades, example: energy capacity.
     * @param component The {@link IMachineComponent} that will use the value.
     * @param target A marker to know which value should be modified in case the component has several.
     * @param defaultValue The default value (without upgrades).
     * @param min A lower bound for the modified value.
     * @param max An upper bound for the modified value.
     * @param onChange A responder that will be called when the value is changed.
     * @return A supplied that give the modified value (or default value if no upgrades are present).
     */
    Supplier<Double> addUpgradeableComponentValue(IMachineComponent component, @Nullable String target, double defaultValue, double min, double max, Consumer<Double> onChange);
}
