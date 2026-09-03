package fr.frinn.custommachinery.api.crafting;

import fr.frinn.custommachinery.api.machine.MachineTile;
import net.minecraft.nbt.CompoundTag;

/**
 * Base interface for a crafting processor.
 * A machine can have only one processor, responsible for handling recipes processing.
 */
public interface IProcessor {

    /**
     * @return A registered {@link ProcessorType} associated with this processor.
     */
    ProcessorType<? extends IProcessor> getType();

    /**
     * @return The {@link MachineTile} which use this processor.
     */
    MachineTile tile();

    /**
     * Will be called by the {@link MachineTile} once per tick only if the machine status is not PAUSED.
     * This method should be responsible for finding a recipe to process and/or process it.
     */
    void tick();

    /**
     * Notify this processor that the current recipe process (if there is one) should be aborted.
     */
    void reset();

    /**
     * Notify this processor that the machine's inventory changed, this processor can rely on this to check inventory requirements of recipes.
     */
    void setMachineInventoryChanged();

    /**
     * Notify this processor that it should try finding a recipe to process immediately instead of waiting (for example an inventory change).
     */
    default void setSearchImmediately() {}

    /**
     * Will be called when the {@link MachineTile} is written to disk, when the world is saved.
     * @return A {@link CompoundTag} holding all the data relative to this processor, like the current recipe, error message, progress...
     */
    CompoundTag serialize();

    /**
     * Will be called when the {@link MachineTile} is read from disk, when the world is loaded.
     * @param nbt A {@link CompoundTag} holding all the data relative to this processor, like the current recipe, error message, progress...
     */
    void deserialize(CompoundTag nbt);
}
