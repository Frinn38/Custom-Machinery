package fr.frinn.custommachinery.common.integration.crafttweaker.function;

import com.blamejared.crafttweaker.api.annotation.ZenRegister;
import com.blamejared.crafttweaker.api.data.MapData;
import com.blamejared.crafttweaker.api.fluid.IFluidStack;
import com.blamejared.crafttweaker.api.item.IItemStack;
import fr.frinn.custommachinery.common.component.ChunkloadMachineComponent;
import fr.frinn.custommachinery.common.component.EnergyMachineComponent;
import fr.frinn.custommachinery.common.component.FluidMachineComponent;
import fr.frinn.custommachinery.common.component.ItemMachineComponent;
import fr.frinn.custommachinery.common.component.handler.FluidComponentHandler;
import fr.frinn.custommachinery.common.init.CustomMachineTile;
import fr.frinn.custommachinery.common.init.Registration;
import fr.frinn.custommachinery.common.util.TaskDelayer;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.LivingEntity;
import net.neoforged.neoforge.fluids.FluidStack;
import net.neoforged.neoforge.fluids.capability.IFluidHandler.FluidAction;
import org.jetbrains.annotations.Nullable;
import org.openzen.zencode.java.ZenCodeType.Getter;
import org.openzen.zencode.java.ZenCodeType.Method;
import org.openzen.zencode.java.ZenCodeType.Name;
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

    @Setter("id")
    @Method
    public void setId(String id) {
        ResourceLocation loc = ResourceLocation.tryParse(id);
        if(loc != null) {
            TaskDelayer.enqueue(0, () -> {
                this.internal.resetProcess();
                this.internal.refreshMachine(loc);
            });
        }
        else throw new IllegalArgumentException("Invalid machine ID: " + id);
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
                .map(FluidMachineComponent::getFluid)
                .orElse(FluidStack.EMPTY);
    }

    @Method
    public void setFluidStored(String tank, IFluidStack stack) {
        this.internal.getComponentManager().getComponentHandler(Registration.FLUID_MACHINE_COMPONENT.get())
                .flatMap(handler -> handler.getComponentForID(tank))
                .ifPresent(x -> x.setFluidStack(stack.getImmutableInternal()));
    }

    @Method
    public int getFluidCapacity(String tank) {
        return this.internal.getComponentManager().getComponentHandler(Registration.FLUID_MACHINE_COMPONENT.get())
                .flatMap(handler -> handler.getComponentForID(tank))
                .map(FluidMachineComponent::getCapacity)
                .orElse(0);
    }

    //Return amount of fluid that was added.
    @Method
    public int addFluid(IFluidStack stack, boolean simulate) {
        return this.internal.getComponentManager().getComponentHandler(Registration.FLUID_MACHINE_COMPONENT.get())
                .map(handler -> (FluidComponentHandler)handler)
                .map(handler -> handler.fill(stack.getInternal(), simulate ? FluidAction.SIMULATE : FluidAction.EXECUTE))
                .orElse(0);
    }

    //Return amount of fluid that was added.
    @Method
    public int addFluidToTank(String tank, IFluidStack stack, boolean simulate) {
        return this.internal.getComponentManager().getComponentHandler(Registration.FLUID_MACHINE_COMPONENT.get())
                .flatMap(handler -> handler.getComponentForID(tank))
                .map(component -> component.fillBypassLimit(stack.getInternal(), simulate ? FluidAction.SIMULATE : FluidAction.EXECUTE))
                .orElse(0);
    }

    //Return fluid that was successfully removed.
    @Method
    public IFluidStack removeFluid(IFluidStack stack, boolean simulate) {
        return this.internal.getComponentManager().getComponentHandler(Registration.FLUID_MACHINE_COMPONENT.get())
                .map(handler -> (FluidComponentHandler)handler)
                .map(handler -> IFluidStack.of(handler.drain(stack.getInternal(), simulate ? FluidAction.SIMULATE : FluidAction.EXECUTE)))
                .orElse(IFluidStack.empty());
    }

    //Return fluid that was successfully removed.
    @Method
    public IFluidStack removeFluidFromTank(String tank, int amount, boolean simulate) {
        return this.internal.getComponentManager().getComponentHandler(Registration.FLUID_MACHINE_COMPONENT.get())
                .flatMap(handler -> handler.getComponentForID(tank))
                .map(component -> IFluidStack.of(component.drainBypassLimit(amount, simulate ? FluidAction.SIMULATE : FluidAction.EXECUTE)))
                .orElse(IFluidStack.empty());
    }

    /** ITEM STUFF **/

    @Method
    public IItemStack getItemStored(String slot) {
        return this.internal.getComponentManager().getComponentHandler(Registration.ITEM_MACHINE_COMPONENT.get())
                .flatMap(handler -> handler.getComponentForID(slot))
                .map(component -> IItemStack.of(component.getItemStack()))
                .orElse(IItemStack.empty());
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
                .map(component -> IItemStack.of(component.insertItemBypassLimit(stackCT.getInternal(), simulate)))
                .orElse(stackCT);
    }

    //Return items that were successfully removed from the slot.
    @Method
    public IItemStack removeItemFromSlot(String slot, int toRemove, boolean simulate) {
        return this.internal.getComponentManager().getComponentHandler(Registration.ITEM_MACHINE_COMPONENT.get())
                .flatMap(handler -> handler.getComponentForID(slot))
                .map(component -> IItemStack.of(component.extractItemBypassLimit(toRemove, simulate)))
                .orElse(IItemStack.empty());
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

    /** Chunkload stuff **/

    @Method
    public void enableChunkload(int radius) {
        this.internal.getComponentManager()
                .getComponent(Registration.CHUNKLOAD_MACHINE_COMPONENT.get())
                .ifPresent(component -> component.setActive((ServerLevel) this.internal.getLevel(), radius));
    }

    @Method
    public void disableChunkload() {
        this.internal.getComponentManager()
                .getComponent(Registration.CHUNKLOAD_MACHINE_COMPONENT.get())
                .ifPresent(component -> component.setInactive((ServerLevel) this.internal.getLevel()));
    }

    @Method
    @Getter("chunkloadEnabled")
    public boolean isChunkloadEnabled() {
        return this.internal.getComponentManager()
                .getComponent(Registration.CHUNKLOAD_MACHINE_COMPONENT.get())
                .map(ChunkloadMachineComponent::isActive)
                .orElse(false);
    }

    @Method
    @Getter("chunkloadRadius")
    public int getChunkloadRadius() {
        return this.internal.getComponentManager()
                .getComponent(Registration.CHUNKLOAD_MACHINE_COMPONENT.get())
                .map(ChunkloadMachineComponent::getRadius)
                .orElse(0);
    }
}
