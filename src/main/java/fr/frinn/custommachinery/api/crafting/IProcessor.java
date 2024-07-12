package fr.frinn.custommachinery.api.crafting;

import fr.frinn.custommachinery.api.machine.MachineTile;
import net.minecraft.nbt.CompoundTag;

public interface IProcessor {

    ProcessorType<? extends IProcessor> getType();

    MachineTile tile();

    void tick();

    void reset();

    void setMachineInventoryChanged();

    default void setSearchImmediately() {

    }

    CompoundTag serialize();

    void deserialize(CompoundTag nbt);
}
