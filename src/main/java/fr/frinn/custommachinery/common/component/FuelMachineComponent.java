package fr.frinn.custommachinery.common.component;

import fr.frinn.custommachinery.api.component.ComponentIOMode;
import fr.frinn.custommachinery.api.component.IMachineComponentManager;
import fr.frinn.custommachinery.api.component.ISerializableComponent;
import fr.frinn.custommachinery.api.component.ITickableComponent;
import fr.frinn.custommachinery.api.component.MachineComponentType;
import fr.frinn.custommachinery.api.machine.MachineStatus;
import fr.frinn.custommachinery.api.network.ISyncable;
import fr.frinn.custommachinery.api.network.ISyncableStuff;
import fr.frinn.custommachinery.common.init.CMRegistration;
import fr.frinn.custommachinery.common.network.syncable.IntegerSyncable;
import fr.frinn.custommachinery.impl.component.AbstractMachineComponent;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import net.neoforged.neoforge.transfer.item.ItemResource;

import java.util.function.Consumer;

public class FuelMachineComponent extends AbstractMachineComponent implements ISerializableComponent, ITickableComponent, ISyncableStuff {

    private int fuel;
    private int maxFuel;

    public FuelMachineComponent(IMachineComponentManager manager) {
        super(manager, ComponentIOMode.NONE);
    }

    @Override
    public MachineComponentType<FuelMachineComponent> getType() {
        return CMRegistration.FUEL_MACHINE_COMPONENT.get();
    }

    @Override
    public void serialize(ValueOutput output) {
        output.putInt("fuel", this.fuel);
        output.putInt("maxFuel", this.maxFuel);
    }

    @Override
    public void deserialize(ValueInput input) {
        input.getInt("fuel").ifPresent(fuel -> this.fuel = fuel);
        input.getInt("maxFuel").ifPresent(maxFuel -> this.maxFuel = maxFuel);
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
        getManager().markDirty();
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

    public boolean canStartRecipe(int amount) {
        if(this.fuel >= amount)
            return true;
        return getManager().getComponentHandler(CMRegistration.ITEM_MACHINE_COMPONENT.get()).flatMap(handler ->
                    handler.getComponents().stream()
                        .filter(component -> component.getType() == CMRegistration.ITEM_FUEL_MACHINE_COMPONENT.get() && component.getItemStack().getBurnTime(RecipeType.SMELTING, getManager().getLevel().fuelValues()) > 0)
                        .findFirst()
                ).isPresent();
    }

    private void tryBurnItem() {
        getManager().getComponentHandler(CMRegistration.ITEM_MACHINE_COMPONENT.get()).flatMap(handler ->
                handler.getComponents().stream()
                        .filter(component -> component.getType() == CMRegistration.ITEM_FUEL_MACHINE_COMPONENT.get() && !component.getItemStack().isEmpty())
                        .findFirst()
        ).ifPresent(component -> {
            int fuel = component.getItemStack().getBurnTime(RecipeType.SMELTING, getManager().getLevel().fuelValues());
            this.addFuel(fuel);
            ItemStack stack = component.getItemStack();
            if(stack.getCraftingRemainder() != null)
                component.setItemStack(stack.getCraftingRemainder().create());
            else
                component.setItemStack(stack.copyWithCount(stack.count() - 1));
        });
    }
}
