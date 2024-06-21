package fr.frinn.custommachinery.common.integration.kubejs.function;

import dev.latvian.mods.kubejs.level.BlockContainerJS;
import dev.latvian.mods.rhino.Wrapper;
import fr.frinn.custommachinery.common.component.ChunkloadMachineComponent;
import fr.frinn.custommachinery.common.component.EnergyMachineComponent;
import fr.frinn.custommachinery.common.component.FluidMachineComponent;
import fr.frinn.custommachinery.common.component.ItemMachineComponent;
import fr.frinn.custommachinery.common.component.handler.FluidComponentHandler;
import fr.frinn.custommachinery.common.init.CustomMachineTile;
import fr.frinn.custommachinery.common.init.Registration;
import fr.frinn.custommachinery.common.util.TaskDelayer;
import fr.frinn.custommachinery.common.util.Utils;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.neoforged.neoforge.fluids.FluidStack;
import org.jetbrains.annotations.Nullable;

import java.util.UUID;

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

    @Nullable
    public CompoundTag getData() {
        return this.nbt;
    }

    public boolean getPaused() {
        return this.internal.isPaused();
    }

    public void setPaused(boolean paused) {
        this.internal.setPaused(paused);
    }

    /** OWNER STUFF **/

    @Nullable
    public Component getOwnerName() {
        return this.internal.getOwnerName();
    }

    @Nullable
    public UUID getOwnerId() {
        return this.internal.getOwnerId();
    }

    public boolean isOwner(LivingEntity entity) {
        return this.internal.isOwner(entity);
    }

    @Nullable
    public LivingEntity getOwner() {
        return this.internal.getOwner();
    }

    public void setOwner(LivingEntity entity) {
        this.internal.setOwner(entity);
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

    public FluidStack getFluidStored(String tank) {
        return this.internal.getComponentManager().getComponentHandler(Registration.FLUID_MACHINE_COMPONENT.get()).flatMap(handler -> handler.getComponentForID(tank)).map(FluidMachineComponent::getFluidStack).orElse(net.neoforged.neoforge.fluids.FluidStack.EMPTY);
    }

    public void setFluidStored(String tank, FluidStack stack) {
        this.internal.getComponentManager().getComponentHandler(Registration.FLUID_MACHINE_COMPONENT.get()).flatMap(handler -> handler.getComponentForID(tank)).ifPresent(x -> x.setFluidStack(stack));
    }

    public long getFluidCapacity(String tank) {
        return this.internal.getComponentManager().getComponentHandler(Registration.FLUID_MACHINE_COMPONENT.get())
                .flatMap(handler -> handler.getComponentForID(tank))
                .map(FluidMachineComponent::getCapacity)
                .orElse(0L);
    }

    //Return amount of fluid that was NOT added.
    public int addFluid(FluidStack stack, boolean simulate) {
        return this.internal.getComponentManager().getComponentHandler(Registration.FLUID_MACHINE_COMPONENT.get())
                .map(handler -> (FluidComponentHandler)handler)
                .map(handler -> (int)handler.fill(stack, simulate))
                .orElse(stack.getAmount());
    }

    //Return amount of fluid that was NOT added.
    public long addFluidToTank(String tank, FluidStack stack, boolean simulate) {
        return this.internal.getComponentManager().getComponentHandler(Registration.FLUID_MACHINE_COMPONENT.get())
                .flatMap(handler -> handler.getComponentForID(tank))
                .map(component -> (int)component.insert(stack.getFluid(), stack.getAmount(), null, simulate))
                .orElse(stack.getAmount());
    }

    //Return fluid that was successfully removed.
    public FluidStack removeFluid(FluidStack stack, boolean simulate) {
        return this.internal.getComponentManager().getComponentHandler(Registration.FLUID_MACHINE_COMPONENT.get()).map(handler -> (FluidComponentHandler)handler).map(handler -> handler.drain(stack, simulate)).orElse(FluidStack.EMPTY);
    }

    //Return fluid that was successfully removed.
    public FluidStack removeFluidFromTank(String tank, long amount, boolean simulate) {
        return this.internal.getComponentManager().getComponentHandler(Registration.FLUID_MACHINE_COMPONENT.get())
                .flatMap(handler -> handler.getComponentForID(tank))
                .map(component -> component.extract(amount, simulate))
                .orElse(FluidStack.EMPTY);
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
                    int inserted = component.insert(stack.getItem(), stack.getCount(), null, simulate, true);
                    return Utils.makeItemStack(stack.getItem(), stack.getCount() - inserted, null);
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

    public void lockSlot(String slot) {
        this.internal.getComponentManager().getComponentHandler(Registration.ITEM_MACHINE_COMPONENT.get())
                .flatMap(handler -> handler.getComponentForID(slot))
                .ifPresent(component -> component.setLocked(true));
    }

    public void unlockSlot(String slot) {
        this.internal.getComponentManager().getComponentHandler(Registration.ITEM_MACHINE_COMPONENT.get())
                .flatMap(handler -> handler.getComponentForID(slot))
                .ifPresent(component -> component.setLocked(false));
    }

    public boolean isSlotLocked(String slot) {
        return this.internal.getComponentManager().getComponentHandler(Registration.ITEM_MACHINE_COMPONENT.get())
                .flatMap(handler -> handler.getComponentForID(slot))
                .map(ItemMachineComponent::isLocked)
                .orElseThrow(() -> new IllegalArgumentException("Invalid slot id: " + slot));
    }

    /** Chunkload stuff **/

    public void enableChunkload(int radius) {
        this.internal.getComponentManager()
                .getComponent(Registration.CHUNKLOAD_MACHINE_COMPONENT.get())
                .ifPresent(component -> component.setActive((ServerLevel) this.internal.getLevel(), radius));
    }

    public void disableChunkload() {
        this.internal.getComponentManager()
                .getComponent(Registration.CHUNKLOAD_MACHINE_COMPONENT.get())
                .ifPresent(component -> component.setInactive((ServerLevel) this.internal.getLevel()));
    }

    public boolean isChunkloadEnabled() {
        return this.internal.getComponentManager()
                .getComponent(Registration.CHUNKLOAD_MACHINE_COMPONENT.get())
                .map(ChunkloadMachineComponent::isActive)
                .orElse(false);
    }

    public int getChunkloadRadius() {
        return this.internal.getComponentManager()
                .getComponent(Registration.CHUNKLOAD_MACHINE_COMPONENT.get())
                .map(ChunkloadMachineComponent::getRadius)
                .orElse(0);
    }
}
