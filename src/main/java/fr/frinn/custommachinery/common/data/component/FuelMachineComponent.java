package fr.frinn.custommachinery.common.data.component;

import fr.frinn.custommachinery.api.component.*;
import fr.frinn.custommachinery.api.machine.MachineStatus;
import fr.frinn.custommachinery.api.network.ISyncable;
import fr.frinn.custommachinery.api.network.ISyncableStuff;
import fr.frinn.custommachinery.apiimpl.component.AbstractMachineComponent;
import fr.frinn.custommachinery.apiimpl.network.syncable.IntegerSyncable;
import fr.frinn.custommachinery.common.data.component.variant.item.FuelItemComponentVariant;
import fr.frinn.custommachinery.common.init.Registration;
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

    //Return true if the component successfully burned the required fuel amount
    public boolean burn(int amount) {
        //If the machine have sufficient fuel, just burn it and return true
        if(this.fuel >= amount) {
            this.fuel -= amount;
            getManager().markDirty();
            return true;
        }

        //Else we try to burn a fuel item to add some fuel
        tryBurnItem();

        //Then we check again
        if(this.fuel >= amount) {
            this.fuel -= amount;
            getManager().markDirty();
            return true;
        }

        //If the machine still don't have the required fuel amount return false, the fuel requirement will error
        return false;
    }

    private void tryBurnItem() {
        getManager().getComponentHandler(Registration.ITEM_MACHINE_COMPONENT.get()).flatMap(handler ->
                handler.getComponents().stream()
                        .filter(component -> component.getVariant() == FuelItemComponentVariant.INSTANCE && !component.getItemStack().isEmpty())
                        .findFirst()
        ).ifPresent(component -> {
            int fuel = ForgeHooks.getBurnTime(component.getItemStack(), IRecipeType.SMELTING);
            this.addFuel(fuel);
            component.extract(1);
        });
    }
}
