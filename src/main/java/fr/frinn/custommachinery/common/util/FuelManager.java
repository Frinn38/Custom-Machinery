package fr.frinn.custommachinery.common.util;

import fr.frinn.custommachinery.api.network.ISyncable;
import fr.frinn.custommachinery.common.init.CustomMachineTile;
import fr.frinn.custommachinery.common.network.sync.IntegerSyncable;
import net.minecraft.nbt.CompoundNBT;
import net.minecraftforge.common.util.Constants;
import net.minecraftforge.common.util.INBTSerializable;

import java.util.function.Consumer;

public class FuelManager implements INBTSerializable<CompoundNBT> {

    private CustomMachineTile tile;
    private int fuel;
    private int maxFuel;

    public FuelManager(CustomMachineTile tile) {
        this.tile = tile;
    }

    public int getFuel() {
        return this.fuel;
    }

    public int getMaxFuel() {
        return this.maxFuel;
    }

    public void addFuel(int fuel) {
        this.fuel += fuel;
        this.maxFuel = fuel;
        this.tile.markDirty();
    }

    public void tick() {
        if(this.fuel > 0 && !this.tile.isPaused()) {
            this.fuel--;
            this.tile.markDirty();
        }
    }

    @Override
    public CompoundNBT serializeNBT() {
        CompoundNBT nbt = new CompoundNBT();
        nbt.putInt("fuel", this.fuel);
        nbt.putInt("maxFuel", this.maxFuel);
        return nbt;
    }

    @Override
    public void deserializeNBT(CompoundNBT nbt) {
        if(nbt.contains("fuel", Constants.NBT.TAG_INT))
            this.fuel = nbt.getInt("fuel");
        if(nbt.contains("maxFuel", Constants.NBT.TAG_INT))
            this.maxFuel = nbt.getInt("maxFuel");
    }

    public void getStuffToSync(Consumer<ISyncable<?, ?>> container) {
        container.accept(IntegerSyncable.create(() -> this.fuel, fuel -> this.fuel = fuel));
        container.accept(IntegerSyncable.create(() -> this.maxFuel, maxFuel -> this.maxFuel = maxFuel));
    }
}
