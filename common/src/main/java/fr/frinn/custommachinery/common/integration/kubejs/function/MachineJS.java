package fr.frinn.custommachinery.common.integration.kubejs.function;

import dev.architectury.fluid.FluidStack;
import dev.latvian.mods.kubejs.fluid.EmptyFluidStackJS;
import dev.latvian.mods.kubejs.fluid.FluidStackJS;
import dev.latvian.mods.kubejs.level.BlockContainerJS;
import dev.latvian.mods.rhino.Wrapper;
import fr.frinn.custommachinery.common.component.EnergyMachineComponent;
import fr.frinn.custommachinery.common.component.FluidMachineComponent;
import fr.frinn.custommachinery.common.component.ItemMachineComponent;
import fr.frinn.custommachinery.common.component.handler.FluidComponentHandler;
import fr.frinn.custommachinery.common.init.CustomMachineTile;
import fr.frinn.custommachinery.common.init.Registration;
import fr.frinn.custommachinery.common.util.Utils;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntity;
import org.jetbrains.annotations.Nullable;

public class MachineJS {

    private final CustomMachineTile internal;
    private final CompoundTag nbt;

    protected MachineJS(CustomMachineTile internal) {
        this.internal = internal;
        this.nbt = this.internal.getComponentManager().getComponent(Registration.DATA_MACHINE_COMPONENT.get()).orElseThrow().getData();
    }

    public static MachineJS of(Object o) {
        if(o instanceof Wrapper w) {
            o = w.unwrap();
        }

        if(o instanceof BlockEntity blockEntity) {
            if(blockEntity instanceof CustomMachineTile customMachineTile)
                return new MachineJS(customMachineTile);
        }
        if(o instanceof BlockContainerJS blockContainerJS) {
            return of(blockContainerJS.getEntity());
        }

        return null;
    }

    public String getId() {
        return this.internal.getId().toString();
    }

    @Nullable
    public CompoundTag getData() {
        return this.nbt;
    }

    /** ENERGY STUFF **/

    public long getEnergyStored() {
        return this.internal.getComponentManager().getComponent(Registration.ENERGY_MACHINE_COMPONENT.get()).map(EnergyMachineComponent::getEnergy).orElse(0L);
    }

    public void setEnergyStored(long energy) {
        this.internal.getComponentManager().getComponent(Registration.ENERGY_MACHINE_COMPONENT.get()).ifPresent(component -> component.setEnergy(energy));
    }

    public long getEnergyCapacity() {
        return this.internal.getComponentManager().getComponent(Registration.ENERGY_MACHINE_COMPONENT.get()).map(EnergyMachineComponent::getCapacity).orElse(0L);
    }

    //Return amount of energy added.
    public int addEnergy(int toAdd, boolean simulate) {
        return this.internal.getComponentManager().getComponent(Registration.ENERGY_MACHINE_COMPONENT.get()).map(component -> component.receiveRecipeEnergy(toAdd, simulate)).orElse(0);
    }

    //Return amount of energy removed.
    public int removeEnergy(int toRemove, boolean simulate) {
        return this.internal.getComponentManager().getComponent(Registration.ENERGY_MACHINE_COMPONENT.get()).map(component -> component.extractRecipeEnergy(toRemove, simulate)).orElse(0);
    }

    /** FLUID STUFF **/

    public FluidStackJS getFluidStored(String tank) {
        return this.internal.getComponentManager().getComponentHandler(Registration.FLUID_MACHINE_COMPONENT.get()).flatMap(handler -> handler.getComponentForID(tank)).map(component -> {
            FluidStack stack = component.getFluidStack();
            return FluidStackJS.of(stack.getFluid(), stack.getAmount(), stack.getTag());
        }).orElse(EmptyFluidStackJS.INSTANCE);
    }

    public void setFluidStored(String tank, FluidStackJS stackJS) {
        FluidStack stack = stackJS.getFluidStack();
        this.internal.getComponentManager().getComponentHandler(Registration.FLUID_MACHINE_COMPONENT.get()).flatMap(handler -> handler.getComponentForID(tank)).ifPresent(x -> x.setFluidStack(stack));
    }

    public long getFluidCapacity(String tank) {
        return this.internal.getComponentManager().getComponentHandler(Registration.FLUID_MACHINE_COMPONENT.get())
                .flatMap(handler -> handler.getComponentForID(tank))
                .map(FluidMachineComponent::getCapacity)
                .orElse(0L);
    }

    //Return amount of fluid that was NOT added.
    public long addFluid(FluidStackJS stackJS, boolean simulate) {
        FluidStack stack = stackJS.getFluidStack();
        return this.internal.getComponentManager().getComponentHandler(Registration.FLUID_MACHINE_COMPONENT.get())
                .map(handler -> (FluidComponentHandler)handler)
                .map(handler -> handler.fill(stack, simulate))
                .orElse(stack.getAmount());
    }

    //Return amount of fluid that was NOT added.
    public long addFluidToTank(String tank, FluidStackJS stackJS, boolean simulate) {
        return this.internal.getComponentManager().getComponentHandler(Registration.FLUID_MACHINE_COMPONENT.get())
                .flatMap(handler -> handler.getComponentForID(tank))
                .map(component -> component.insert(stackJS.getFluid(), (int)stackJS.getAmount(), stackJS.getNbt(), simulate))
                .orElse(stackJS.getAmount());
    }

    //Return fluid that was successfully removed.
    public FluidStackJS removeFluid(FluidStackJS stackJS, boolean simulate) {
        FluidStack stack = stackJS.getFluidStack();
        return this.internal.getComponentManager().getComponentHandler(Registration.FLUID_MACHINE_COMPONENT.get()).map(handler -> (FluidComponentHandler)handler).map(handler -> {
            FluidStack removed = handler.drain(stack, simulate);
            return FluidStackJS.of(removed);
        }).orElse(EmptyFluidStackJS.INSTANCE);
    }

    //Return fluid that was successfully removed.
    public FluidStackJS removeFluidFromTank(String tank, long amount, boolean simulate) {
        return this.internal.getComponentManager().getComponentHandler(Registration.FLUID_MACHINE_COMPONENT.get())
                .flatMap(handler -> handler.getComponentForID(tank))
                .map(component -> {
                    FluidStack stack = component.extract(amount, simulate);
                    return FluidStackJS.of(stack.getFluid(), stack.getAmount(), stack.getTag());
                })
                .orElse(EmptyFluidStackJS.INSTANCE);
    }

    /** ITEM STUFF **/

    public ItemStack getItemStored(String slot) {
        return this.internal.getComponentManager().getComponentHandler(Registration.ITEM_MACHINE_COMPONENT.get())
                .flatMap(handler -> handler.getComponentForID(slot))
                .map(ItemMachineComponent::getItemStack)
                .orElse(ItemStack.EMPTY);
    }

    public void setItemStored(String slot, ItemStack stack) {
        this.internal.getComponentManager().getComponentHandler(Registration.ITEM_MACHINE_COMPONENT.get())
                .flatMap(handler -> handler.getComponentForID(slot))
                .ifPresent(component -> {
                    component.setItemStack(stack);
                });
    }

    public int getItemCapacity(String slot) {
        return this.internal.getComponentManager().getComponentHandler(Registration.ITEM_MACHINE_COMPONENT.get())
                .flatMap(handler -> handler.getComponentForID(slot))
                .map(ItemMachineComponent::getCapacity)
                .orElse(0);
    }

    //Return items that couldn't be added.
    public ItemStack addItemToSlot(String slot, ItemStack stack, boolean simulate) {
        return this.internal.getComponentManager().getComponentHandler(Registration.ITEM_MACHINE_COMPONENT.get())
                .flatMap(handler -> handler.getComponentForID(slot))
                .map(component -> {
                    int inserted = component.insert(stack.getItem(), stack.getCount(), stack.getTag(), simulate, true);
                    return Utils.makeItemStack(stack.getItem(), stack.getCount() - inserted, stack.getTag());
                })
                .orElse(stack);
    }

    //Return items that were successfully removed from the slot.
    public ItemStack removeItemFromSlot(String slot, int toRemove, boolean simulate) {
        return this.internal.getComponentManager().getComponentHandler(Registration.ITEM_MACHINE_COMPONENT.get())
                .flatMap(handler -> handler.getComponentForID(slot))
                .map(component -> component.extract(toRemove, simulate, true))
                .orElse(ItemStack.EMPTY);
    }
}
