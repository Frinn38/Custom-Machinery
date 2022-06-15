package fr.frinn.custommachinery.common.integration.kubejs.function;

import dev.latvian.mods.kubejs.fluid.EmptyFluidStackJS;
import dev.latvian.mods.kubejs.fluid.FluidStackJS;
import dev.latvian.mods.kubejs.item.ItemStackJS;
import dev.latvian.mods.kubejs.level.BlockContainerJS;
import dev.latvian.mods.rhino.Wrapper;
import fr.frinn.custommachinery.common.component.EnergyMachineComponent;
import fr.frinn.custommachinery.common.component.FluidMachineComponent;
import fr.frinn.custommachinery.common.component.ItemMachineComponent;
import fr.frinn.custommachinery.common.component.handler.FluidComponentHandler;
import fr.frinn.custommachinery.common.init.CustomMachineTile;
import fr.frinn.custommachinery.common.init.Registration;
import fr.frinn.custommachinery.common.util.Utils;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.capability.IFluidHandler;

public class MachineJS {

    private final CustomMachineTile internal;

    protected MachineJS(CustomMachineTile internal) {
        this.internal = internal;
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

    /** ENERGY STUFF **/

    public long getEnergyStored() {
        return this.internal.componentManager.getComponent(Registration.ENERGY_MACHINE_COMPONENT.get()).map(EnergyMachineComponent::getEnergy).orElse(0L);
    }

    public void setEnergyStored(long energy) {
        this.internal.componentManager.getComponent(Registration.ENERGY_MACHINE_COMPONENT.get()).ifPresent(component -> component.setEnergy(energy));
    }

    public long getEnergyCapacity() {
        return this.internal.componentManager.getComponent(Registration.ENERGY_MACHINE_COMPONENT.get()).map(EnergyMachineComponent::getCapacity).orElse(0L);
    }

    //Return amount of energy added.
    public int addEnergy(int toAdd, boolean simulate) {
        return this.internal.componentManager.getComponent(Registration.ENERGY_MACHINE_COMPONENT.get()).map(component -> component.receiveRecipeEnergy(toAdd, simulate)).orElse(0);
    }

    //Return amount of energy removed.
    public int removeEnergy(int toRemove, boolean simulate) {
        return this.internal.componentManager.getComponent(Registration.ENERGY_MACHINE_COMPONENT.get()).map(component -> component.extractRecipeEnergy(toRemove, simulate)).orElse(0);
    }

    /** FLUID STUFF **/

    public FluidStackJS getFluidStored(String tank) {
        return this.internal.componentManager.getComponentHandler(Registration.FLUID_MACHINE_COMPONENT.get()).flatMap(handler -> handler.getComponentForID(tank)).map(component -> {
            FluidStack stack = component.getFluidStack();
            return FluidStackJS.of(stack.getFluid(), stack.getAmount(), stack.getTag());
        }).orElse(EmptyFluidStackJS.INSTANCE);
    }

    public void setFluidStored(String tank, FluidStackJS stackJS) {
        FluidStack stack = new FluidStack(stackJS.getFluid(), (int)stackJS.getAmount(), stackJS.getNbt());
        this.internal.componentManager.getComponentHandler(Registration.FLUID_MACHINE_COMPONENT.get()).flatMap(handler -> handler.getComponentForID(tank)).ifPresent(x -> x.setFluidStack(stack));
    }

    public int getFluidCapacity(String tank) {
        return this.internal.componentManager.getComponentHandler(Registration.FLUID_MACHINE_COMPONENT.get()).flatMap(handler -> handler.getComponentForID(tank)).map(FluidMachineComponent::getCapacity).orElse(0);
    }

    //Return amount of fluid that was NOT added.
    public int addFluid(FluidStackJS stackJS, boolean simulate) {
        FluidStack stack = new FluidStack(stackJS.getFluid(), (int)stackJS.getAmount(), stackJS.getNbt());
        return this.internal.componentManager.getComponentHandler(Registration.FLUID_MACHINE_COMPONENT.get()).map(handler -> (FluidComponentHandler)handler).map(handler -> handler.getFluidHandler().fill(stack, simulate ? IFluidHandler.FluidAction.SIMULATE : IFluidHandler.FluidAction.EXECUTE)).orElse(stack.getAmount());
    }

    //Return amount of fluid that was NOT added.
    public int addFluidToTank(String tank, FluidStackJS stackJS, boolean simulate) {
        return this.internal.componentManager.getComponentHandler(Registration.FLUID_MACHINE_COMPONENT.get())
                .flatMap(handler -> handler.getComponentForID(tank))
                .map(component -> component.insert(stackJS.getFluid(), (int)stackJS.getAmount(), stackJS.getNbt(), simulate ? IFluidHandler.FluidAction.SIMULATE : IFluidHandler.FluidAction.EXECUTE))
                .orElse((int)stackJS.getAmount());
    }

    //Return fluid that was successfully removed.
    public FluidStackJS removeFluid(FluidStackJS stackJS, boolean simulate) {
        FluidStack stack = new FluidStack(stackJS.getFluid(), (int)stackJS.getAmount(), stackJS.getNbt());
        return this.internal.componentManager.getComponentHandler(Registration.FLUID_MACHINE_COMPONENT.get()).map(handler -> (FluidComponentHandler)handler).map(handler -> {
            FluidStack removed = handler.getFluidHandler().drain(stack, simulate ? IFluidHandler.FluidAction.SIMULATE : IFluidHandler.FluidAction.EXECUTE);
            return FluidStackJS.of(removed.getFluid(), removed.getAmount(), removed.getTag());
        }).orElse(EmptyFluidStackJS.INSTANCE);
    }

    //Return fluid that was successfully removed.
    public FluidStackJS removeFluidFromTank(String tank, int amount, boolean simulate) {
        return this.internal.componentManager.getComponentHandler(Registration.FLUID_MACHINE_COMPONENT.get())
                .flatMap(handler -> handler.getComponentForID(tank))
                .map(component -> {
                    FluidStack stack = component.extract(amount, simulate ? IFluidHandler.FluidAction.SIMULATE : IFluidHandler.FluidAction.EXECUTE);
                    return FluidStackJS.of(stack.getFluid(), stack.getAmount(), stack.getTag());
                })
                .orElse(EmptyFluidStackJS.INSTANCE);
    }

    /** ITEM STUFF **/

    public ItemStackJS getItemStored(String slot) {
        return this.internal.componentManager.getComponentHandler(Registration.ITEM_MACHINE_COMPONENT.get())
                .flatMap(handler -> handler.getComponentForID(slot))
                .map(component -> ItemStackJS.of(component.getItemStack()))
                .orElse(ItemStackJS.EMPTY);
    }

    public void setItemStored(String slot, ItemStackJS stackJS) {
        this.internal.componentManager.getComponentHandler(Registration.ITEM_MACHINE_COMPONENT.get())
                .flatMap(handler -> handler.getComponentForID(slot))
                .ifPresent(component -> {
                    component.setItemStack(stackJS.getItemStack());
                });
    }

    public int getItemCapacity(String slot) {
        return this.internal.componentManager.getComponentHandler(Registration.ITEM_MACHINE_COMPONENT.get())
                .flatMap(handler -> handler.getComponentForID(slot))
                .map(ItemMachineComponent::getCapacity)
                .orElse(0);
    }

    //Return items that couldn't be added.
    public ItemStackJS addItemToSlot(String slot, ItemStackJS stackJS, boolean simulate) {
        return this.internal.componentManager.getComponentHandler(Registration.ITEM_MACHINE_COMPONENT.get())
                .flatMap(handler -> handler.getComponentForID(slot))
                .map(component -> {
                    ItemStack stack = stackJS.getItemStack();
                    int maxInsert = component.getSpaceForItem(stack);
                    if(maxInsert <= 0)
                        return stackJS;
                    if(!simulate)
                        component.insert(stack);
                    if(maxInsert >= stack.getCount())
                        return ItemStackJS.EMPTY;
                    else
                        return ItemStackJS.of(Utils.makeItemStack(stack.getItem(), stack.getCount() - maxInsert, stack.getTag()));
                })
                .orElse(stackJS);
    }

    //Return items that were successfully removed from the slot.
    public ItemStackJS removeItemFromSlot(String slot, int toRemove, boolean simulate) {
        return this.internal.componentManager.getComponentHandler(Registration.ITEM_MACHINE_COMPONENT.get())
                .flatMap(handler -> handler.getComponentForID(slot))
                .map(component -> {
                    if(component.getItemStack().isEmpty())
                        return ItemStackJS.EMPTY;
                    int maxRemove = Math.min(toRemove, component.getItemStack().getCount());
                    ItemStack extracted = component.getItemStack().copy();
                    extracted.setCount(component.getItemStack().getCount() - maxRemove);
                    if(!simulate)
                        component.extract(maxRemove);
                    return ItemStackJS.of(extracted);
                })
                .orElse(ItemStackJS.EMPTY);
    }
}
