package fr.frinn.custommachinery.common.data.component;

import fr.frinn.custommachinery.api.components.*;
import fr.frinn.custommachinery.api.machine.MachineStatus;
import fr.frinn.custommachinery.api.network.ISyncable;
import fr.frinn.custommachinery.api.network.ISyncableStuff;
import fr.frinn.custommachinery.common.init.Registration;
import fr.frinn.custommachinery.common.network.sync.IntegerSyncable;
import net.minecraft.item.crafting.IRecipeType;
import net.minecraft.nbt.CompoundNBT;
import net.minecraftforge.common.ForgeHooks;
import net.minecraftforge.common.util.Constants;

import java.util.function.Consumer;

public class FuelMachineComponent extends AbstractMachineComponent implements ISerializableComponent, ITickableComponent, ISyncableStuff {

    private int fuel;
    private int maxFuel;

    public FuelMachineComponent(IMachineComponentManager manager) {
        super(manager, ComponentIOMode.NONE);
    }

    @Override
    public MachineComponentType<FuelMachineComponent> getType() {
        return Registration.FUEL_MACHINE_COMPONENT.get();
    }

    @Override
    public void serialize(CompoundNBT nbt) {
        nbt.putInt("fuel", this.fuel);
        nbt.putInt("maxFuel", this.maxFuel);
    }

    @Override
    public void deserialize(CompoundNBT nbt) {
        if(nbt.contains("fuel", Constants.NBT.TAG_INT))
            this.fuel = nbt.getInt("fuel");
        if(nbt.contains("maxFuel", Constants.NBT.TAG_INT))
            this.maxFuel = nbt.getInt("maxFuel");
    }

    @Override
    public void serverTick() {
        if(this.fuel > 0 && getManager().getTile().getStatus() != MachineStatus.RUNNING) {
            this.fuel--;
            getManager().markDirty();
        }
    }

    @Override
    public void getStuffToSync(Consumer<ISyncable<?, ?>> container) {
        container.accept(IntegerSyncable.create(() -> this.fuel, fuel -> this.fuel = fuel));
        container.accept(IntegerSyncable.create(() -> this.maxFuel, maxFuel -> this.maxFuel = maxFuel));
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
        getManager().getTile().markDirty();
    }

    public boolean burn(int amount) {
        this.fuel -= amount;
        getManager().markDirty();
        if(getFuel() > 0)
            return true;
        getManager().getComponentHandler(Registration.ITEM_MACHINE_COMPONENT.get()).flatMap(handler ->
            handler.getComponents().stream()
                    .filter(component -> component.getVariant() == ItemComponentVariant.FUEL && !component.getItemStack().isEmpty())
                    .findFirst()
        ).ifPresent(component -> {
            int fuel = ForgeHooks.getBurnTime(component.getItemStack(), IRecipeType.SMELTING);
            this.addFuel(fuel);
            component.extract(1);
        });
        return getFuel() > 0;
    }
}
