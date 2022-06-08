package fr.frinn.custommachinery.common.integration.crafttweaker.function;

import com.blamejared.crafttweaker.api.annotation.ZenRegister;
import com.blamejared.crafttweaker.api.fluid.IFluidStack;
import com.blamejared.crafttweaker.api.fluid.MCFluidStack;
import com.blamejared.crafttweaker.api.item.IItemStack;
import com.blamejared.crafttweaker.api.item.MCItemStack;
import fr.frinn.custommachinery.common.component.EnergyMachineComponent;
import fr.frinn.custommachinery.common.component.FluidMachineComponent;
import fr.frinn.custommachinery.common.component.ItemMachineComponent;
import fr.frinn.custommachinery.common.component.handler.FluidComponentHandler;
import fr.frinn.custommachinery.common.init.CustomMachineTile;
import fr.frinn.custommachinery.common.init.Registration;
import fr.frinn.custommachinery.common.util.Utils;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.capability.IFluidHandler;
import org.openzen.zencode.java.ZenCodeType.Getter;
import org.openzen.zencode.java.ZenCodeType.Method;
import org.openzen.zencode.java.ZenCodeType.Name;

@ZenRegister
@Name("mods.custommachinery.Machine")
public class MachineCT {

    private static final MCFluidStack FLUID_EMPTY = new MCFluidStack(FluidStack.EMPTY);
    private static final MCItemStack ITEM_EMPTY = new MCItemStack(ItemStack.EMPTY);

    private final CustomMachineTile internal;

    protected MachineCT(CustomMachineTile internal) {
        this.internal = internal;
    }

    /** ENERGY STUFF **/

    @Getter("energyStored")
    @Method
    public long getEnergyStored() {
        return this.internal.componentManager.getComponent(Registration.ENERGY_MACHINE_COMPONENT.get()).map(EnergyMachineComponent::getEnergy).orElse(0L);
    }

    @Getter("energyCapacity")
    @Method
    public long getEnergyCapacity() {
        return this.internal.componentManager.getComponent(Registration.ENERGY_MACHINE_COMPONENT.get()).map(EnergyMachineComponent::getCapacity).orElse(0L);
    }

    //Return amount of energy added.
    @Method
    public int addEnergy(int toAdd, boolean simulate) {
        return this.internal.componentManager.getComponent(Registration.ENERGY_MACHINE_COMPONENT.get()).map(component -> component.receiveRecipeEnergy(toAdd, simulate)).orElse(0);
    }

    //Return amount of energy removed.
    @Method
    public int removeEnergy(int toRemove, boolean simulate) {
        return this.internal.componentManager.getComponent(Registration.ENERGY_MACHINE_COMPONENT.get()).map(component -> component.extractRecipeEnergy(toRemove, simulate)).orElse(0);
    }

    /** FLUID STUFF **/

    @Method
    public IFluidStack getFluidStored(String tank) {
        return this.internal.componentManager.getComponentHandler(Registration.FLUID_MACHINE_COMPONENT.get()).flatMap(handler -> handler.getComponentForID(tank)).map(component -> {
            FluidStack stack = component.getFluidStack();
            return new MCFluidStack(stack);
        }).orElse(FLUID_EMPTY);
    }

    @Method
    public int getFluidCapacity(String tank) {
        return this.internal.componentManager.getComponentHandler(Registration.FLUID_MACHINE_COMPONENT.get()).flatMap(handler -> handler.getComponentForID(tank)).map(FluidMachineComponent::getCapacity).orElse(0);
    }

    //Return amount of fluid that was NOT added.
    @Method
    public int addFluid(IFluidStack stackCT, boolean simulate) {
        FluidStack stack = stackCT.getInternal();
        return this.internal.componentManager.getComponentHandler(Registration.FLUID_MACHINE_COMPONENT.get()).map(handler -> (FluidComponentHandler)handler).map(handler -> handler.getFluidHandler().fill(stack, simulate ? IFluidHandler.FluidAction.SIMULATE : IFluidHandler.FluidAction.EXECUTE)).orElse(stack.getAmount());
    }

    //Return amount of fluid that was NOT added.
    @Method
    public int addFluidToTank(String tank, IFluidStack stackCT, boolean simulate) {
        return this.internal.componentManager.getComponentHandler(Registration.FLUID_MACHINE_COMPONENT.get())
                .flatMap(handler -> handler.getComponentForID(tank))
                .map(component -> component.insert(stackCT.getFluid(), stackCT.getAmount(), stackCT.getInternal().getTag(), simulate ? IFluidHandler.FluidAction.SIMULATE : IFluidHandler.FluidAction.EXECUTE))
                .orElse(stackCT.getAmount());
    }

    //Return fluid that was successfully removed.
    @Method
    public IFluidStack removeFluid(IFluidStack stackJS, boolean simulate) {
        FluidStack stack = stackJS.getInternal();
        return this.internal.componentManager.getComponentHandler(Registration.FLUID_MACHINE_COMPONENT.get()).map(handler -> (FluidComponentHandler)handler).map(handler -> {
            FluidStack removed = handler.getFluidHandler().drain(stack, simulate ? IFluidHandler.FluidAction.SIMULATE : IFluidHandler.FluidAction.EXECUTE);
            return new MCFluidStack(removed);
        }).orElse(FLUID_EMPTY);
    }

    //Return fluid that was successfully removed.
    @Method
    public IFluidStack removeFluidFromTank(String tank, int amount, boolean simulate) {
        return this.internal.componentManager.getComponentHandler(Registration.FLUID_MACHINE_COMPONENT.get())
                .flatMap(handler -> handler.getComponentForID(tank))
                .map(component -> {
                    FluidStack stack = component.extract(amount, simulate ? IFluidHandler.FluidAction.SIMULATE : IFluidHandler.FluidAction.EXECUTE);
                    return new MCFluidStack(stack);
                })
                .orElse(FLUID_EMPTY);
    }

    /** ITEM STUFF **/

    @Method
    public IItemStack getItemStored(String slot) {
        return this.internal.componentManager.getComponentHandler(Registration.ITEM_MACHINE_COMPONENT.get())
                .flatMap(handler -> handler.getComponentForID(slot))
                .map(component -> new MCItemStack(component.getItemStack()))
                .orElse(ITEM_EMPTY);
    }

    @Method
    public int getItemCapacity(String slot) {
        return this.internal.componentManager.getComponentHandler(Registration.ITEM_MACHINE_COMPONENT.get())
                .flatMap(handler -> handler.getComponentForID(slot))
                .map(ItemMachineComponent::getCapacity)
                .orElse(0);
    }

    //Return items that couldn't be added.
    @Method
    public IItemStack addItemToSlot(String slot, IItemStack stackCT, boolean simulate) {
        return this.internal.componentManager.getComponentHandler(Registration.ITEM_MACHINE_COMPONENT.get())
                .flatMap(handler -> handler.getComponentForID(slot))
                .map(component -> {
                    ItemStack stack = stackCT.getInternal();
                    int maxInsert = component.getSpaceForItem(stack);
                    if(maxInsert <= 0)
                        return stackCT;
                    if(!simulate)
                        component.insert(stack);
                    if(maxInsert >= stack.getCount())
                        return ITEM_EMPTY;
                    else
                        return new MCItemStack(Utils.makeItemStack(stack.getItem(), stack.getCount() - maxInsert, stack.getTag()));
                })
                .orElse(stackCT);
    }

    //Return items that were successfully removed from the slot.
    @Method
    public IItemStack removeItemFromSlot(String slot, int toRemove, boolean simulate) {
        return this.internal.componentManager.getComponentHandler(Registration.ITEM_MACHINE_COMPONENT.get())
                .flatMap(handler -> handler.getComponentForID(slot))
                .map(component -> {
                    if(component.getItemStack().isEmpty())
                        return ITEM_EMPTY;
                    int maxRemove = Math.min(toRemove, component.getItemStack().getCount());
                    ItemStack extracted = component.getItemStack().copy();
                    extracted.setCount(component.getItemStack().getCount() - maxRemove);
                    if(!simulate)
                        component.extract(maxRemove);
                    return new MCItemStack(extracted);
                })
                .orElse(ITEM_EMPTY);
    }
}
