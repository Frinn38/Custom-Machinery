package fr.frinn.custommachinery.api.crafting;

import fr.frinn.custommachinery.api.machine.MachineTile;
import net.minecraft.nbt.CompoundTag;
import org.jetbrains.annotations.Nullable;

public interface IProcessor {

    @Nullable
    ICraftingContext getCurrentContext();

    ProcessorType<? extends IProcessor> getType();

    MachineTile getTile();

    double getRecipeProgressTime();

    void tick();

    void reset();

    void setMachineInventoryChanged();

    default void setSearchImmediately() {

    }

    CompoundTag serialize();

    void deserialize(CompoundTag nbt);
}
