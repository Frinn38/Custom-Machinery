package fr.frinn.custommachinery.common.integration.crafttweaker.function;

import com.blamejared.crafttweaker.api.annotation.ZenRegister;
import com.blamejared.crafttweaker.api.data.IData;
import com.blamejared.crafttweaker.api.data.MapData;
import com.blamejared.crafttweaker.api.item.IItemStack;
import com.blamejared.crafttweaker.platform.Services;
import dev.architectury.fluid.FluidStack;
import fr.frinn.custommachinery.common.component.EnergyMachineComponent;
import fr.frinn.custommachinery.common.component.FluidMachineComponent;
import fr.frinn.custommachinery.common.component.ItemMachineComponent;
import fr.frinn.custommachinery.common.component.handler.FluidComponentHandler;
import fr.frinn.custommachinery.common.init.CustomMachineTile;
import fr.frinn.custommachinery.common.init.Registration;
import fr.frinn.custommachinery.common.util.Utils;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.material.Fluid;
import org.jetbrains.annotations.Nullable;
import org.openzen.zencode.java.ZenCodeType.Getter;
import org.openzen.zencode.java.ZenCodeType.Method;
import org.openzen.zencode.java.ZenCodeType.Name;
import org.openzen.zencode.java.ZenCodeType.Optional;
import org.openzen.zencode.java.ZenCodeType.Setter;

import java.util.UUID;

@ZenRegister
@Name("mods.custommachinery.Machine")
public class MachineCT {

    private final CustomMachineTile internal;
    private final MapData data;

    protected MachineCT(CustomMachineTile internal) {
        this.internal = internal;
        this.data = new MapData(this.internal.getComponentManager().getComponent(Registration.DATA_MACHINE_COMPONENT.get()).orElseThrow().getData());
    }

    @Getter("id")
    @Method
    public String getId() {
        return this.internal.getId().toString();
    }

    @Getter("data")
    @Method
    public MapData getData() {
        return this.data;
    }

    @Getter("paused")
    @Method
    public boolean getPaused() {
        return this.internal.isPaused();
    }

    @Setter("paused")
    @Method
    public void setPaused(boolean paused) {
        this.internal.setPaused(paused);
    }

    /** OWNER STUFF **/

    @Nullable
    @Getter("ownerName")
    @Method
    public Component getOwnerName() {
        return this.internal.getOwnerName();
    }

    @Nullable
    @Getter("ownerId")
    @Method
    public UUID getOwnerId() {
        return this.internal.getOwnerId();
    }

    @Method
    public boolean isOwner(LivingEntity entity) {
        return this.internal.isOwner(entity);
    }

    @Nullable
    @Getter("owner")
    @Method
    public LivingEntity getOwner() {
        return this.internal.getOwner();
    }

    @Setter("owner")
    @Method
    public void setOwner(LivingEntity entity) {
        this.internal.setOwner(entity);
    }

    /** ENERGY STUFF **/

    @Getter("energyStored")
    @Method
    public long getEnergyStored() {
        return this.internal.getComponentManager().getComponent(Registration.ENERGY_MACHINE_COMPONENT.get()).map(EnergyMachineComponent::getEnergy).orElse(0L);
    }

    @Setter
    @Method
    public void setEnergyStored(long energy) {
        this.internal.getComponentManager().getComponent(Registration.ENERGY_MACHINE_COMPONENT.get()).ifPresent(component -> component.setEnergy(energy));
    }

    @Getter("energyCapacity")
    @Method
    public long getEnergyCapacity() {
        return this.internal.getComponentManager().getComponent(Registration.ENERGY_MACHINE_COMPONENT.get()).map(EnergyMachineComponent::getCapacity).orElse(0L);
    }

    //Return amount of energy added.
    @Method
    public int addEnergy(int toAdd, boolean simulate) {
        return this.internal.getComponentManager().getComponent(Registration.ENERGY_MACHINE_COMPONENT.get()).map(component -> component.receiveRecipeEnergy(toAdd, simulate)).orElse(0);
    }

    //Return amount of energy removed.
    @Method
    public int removeEnergy(int toRemove, boolean simulate) {
        return this.internal.getComponentManager().getComponent(Registration.ENERGY_MACHINE_COMPONENT.get()).map(component -> component.extractRecipeEnergy(toRemove, simulate)).orElse(0);
    }

    /** FLUID STUFF **/

    @Method
    public FluidStack getFluidStored(String tank) {
        return this.internal.getComponentManager().getComponentHandler(Registration.FLUID_MACHINE_COMPONENT.get())
                .flatMap(handler -> handler.getComponentForID(tank))
                .map(FluidMachineComponent::getFluidStack)
                .orElse(FluidStack.empty());
    }

    @Method
    public void setFluidStored(String tank, Fluid fluid, long amount, @Optional IData data) {
        this.internal.getComponentManager().getComponentHandler(Registration.FLUID_MACHINE_COMPONENT.get())
                .flatMap(handler -> handler.getComponentForID(tank))
                .ifPresent(x -> x.setFluidStack(FluidStack.create(fluid, amount, data == null ? null : (CompoundTag) data.getInternal())));
    }

    @Method
    public long getFluidCapacity(String tank) {
        return this.internal.getComponentManager().getComponentHandler(Registration.FLUID_MACHINE_COMPONENT.get())
                .flatMap(handler -> handler.getComponentForID(tank))
                .map(FluidMachineComponent::getCapacity)
                .orElse(0L);
    }

    //Return amount of fluid that was NOT added.
    @Method
    public long addFluid(Fluid fluid, long amount, boolean simulate) {
        return this.internal.getComponentManager().getComponentHandler(Registration.FLUID_MACHINE_COMPONENT.get())
                .map(handler -> (FluidComponentHandler)handler)
                .map(handler -> handler.fill(FluidStack.create(fluid, amount), simulate))
                .orElse(amount);
    }

    //Return amount of fluid that was NOT added.
    @Method
    public long addFluidToTank(String tank, Fluid fluid, long amount, boolean simulate, @Optional IData data) {
        return this.internal.getComponentManager().getComponentHandler(Registration.FLUID_MACHINE_COMPONENT.get())
                .flatMap(handler -> handler.getComponentForID(tank))
                .map(component -> component.insert(fluid, amount, data == null ? null : (CompoundTag) data.getInternal(), simulate))
                .orElse(amount);
    }

    //Return fluid that was successfully removed.
    @Method
    public FluidStack removeFluid(Fluid fluid, int amount, boolean simulate, @Optional IData data) {
        return this.internal.getComponentManager().getComponentHandler(Registration.FLUID_MACHINE_COMPONENT.get())
                .map(handler -> (FluidComponentHandler)handler)
                .map(handler -> handler.drain(FluidStack.create(fluid, amount, data == null ? null : (CompoundTag) data.getInternal()), simulate))
                .orElse(FluidStack.empty());
    }

    //Return fluid that was successfully removed.
    @Method
    public FluidStack removeFluidFromTank(String tank, int amount, boolean simulate) {
        return this.internal.getComponentManager().getComponentHandler(Registration.FLUID_MACHINE_COMPONENT.get())
                .flatMap(handler -> handler.getComponentForID(tank))
                .map(component -> component.extract(amount, simulate))
                .orElse(FluidStack.empty());
    }

    /** ITEM STUFF **/

    @Method
    public IItemStack getItemStored(String slot) {
        return this.internal.getComponentManager().getComponentHandler(Registration.ITEM_MACHINE_COMPONENT.get())
                .flatMap(handler -> handler.getComponentForID(slot))
                .map(component -> Services.PLATFORM.createItemStack(component.getItemStack()))
                .orElse(Services.PLATFORM.getEmptyItemStack());
    }

    @Method
    public void setItemStored(String slot, IItemStack stackCT) {
        this.internal.getComponentManager().getComponentHandler(Registration.ITEM_MACHINE_COMPONENT.get())
                .flatMap(handler -> handler.getComponentForID(slot))
                .ifPresent(component -> {
                    component.setItemStack(stackCT.getInternal());
                });
    }

    @Method
    public int getItemCapacity(String slot) {
        return this.internal.getComponentManager().getComponentHandler(Registration.ITEM_MACHINE_COMPONENT.get())
                .flatMap(handler -> handler.getComponentForID(slot))
                .map(ItemMachineComponent::getCapacity)
                .orElse(0);
    }

    //Return items that couldn't be added.
    @Method
    public IItemStack addItemToSlot(String slot, IItemStack stackCT, boolean simulate) {
        return this.internal.getComponentManager().getComponentHandler(Registration.ITEM_MACHINE_COMPONENT.get())
                .flatMap(handler -> handler.getComponentForID(slot))
                .map(component -> {
                    int inserted = component.insert(stackCT.getDefinition(), stackCT.getAmount(), stackCT.getInternal().getTag(), simulate, true);
                    return Services.PLATFORM.createItemStack(Utils.makeItemStack(stackCT.getDefinition(), stackCT.getAmount() - inserted, stackCT.getInternal().getTag()));
                })
                .orElse(stackCT);
    }

    //Return items that were successfully removed from the slot.
    @Method
    public IItemStack removeItemFromSlot(String slot, int toRemove, boolean simulate) {
        return this.internal.getComponentManager().getComponentHandler(Registration.ITEM_MACHINE_COMPONENT.get())
                .flatMap(handler -> handler.getComponentForID(slot))
                .map(component -> Services.PLATFORM.createItemStack(component.extract(toRemove, simulate, true)))
                .orElse(Services.PLATFORM.getEmptyItemStack());
    }

    @Method
    public void lockSlot(String slot) {
        this.internal.getComponentManager().getComponentHandler(Registration.ITEM_MACHINE_COMPONENT.get())
                .flatMap(handler -> handler.getComponentForID(slot))
                .ifPresent(component -> component.setLocked(true));
    }

    @Method
    public void unlockSlot(String slot) {
        this.internal.getComponentManager().getComponentHandler(Registration.ITEM_MACHINE_COMPONENT.get())
                .flatMap(handler -> handler.getComponentForID(slot))
                .ifPresent(component -> component.setLocked(false));
    }

    @Method
    public boolean isSlotLocked(String slot) {
        return this.internal.getComponentManager().getComponentHandler(Registration.ITEM_MACHINE_COMPONENT.get())
                .flatMap(handler -> handler.getComponentForID(slot))
                .map(ItemMachineComponent::isLocked)
                .orElseThrow(() -> new IllegalArgumentException("Invalid slot id: " + slot));
    }
}
