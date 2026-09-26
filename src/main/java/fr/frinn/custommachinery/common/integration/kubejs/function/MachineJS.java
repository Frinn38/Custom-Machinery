package fr.frinn.custommachinery.common.integration.kubejs.function;

import dev.latvian.mods.kubejs.error.KubeRuntimeException;
import dev.latvian.mods.kubejs.level.CachedLevelBlock;
import dev.latvian.mods.rhino.Wrapper;
import fr.frinn.custommachinery.api.machine.MachineStatus;
import fr.frinn.custommachinery.api.machine.MachineTile;
import fr.frinn.custommachinery.common.component.ChunkloadMachineComponent;
import fr.frinn.custommachinery.common.component.EnergyMachineComponent;
import fr.frinn.custommachinery.common.component.FluidMachineComponent;
import fr.frinn.custommachinery.common.component.handler.FluidComponentHandler;
import fr.frinn.custommachinery.common.component.item.ItemMachineComponent;
import fr.frinn.custommachinery.common.init.CMRegistration;
import fr.frinn.custommachinery.common.util.TaskDelayer;
import fr.frinn.custommachinery.impl.component.config.SideConfig;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.neoforged.neoforge.fluids.FluidStack;
import net.neoforged.neoforge.transfer.ResourceHandler;
import net.neoforged.neoforge.transfer.fluid.FluidResource;
import net.neoforged.neoforge.transfer.item.ItemResource;
import net.neoforged.neoforge.transfer.transaction.Transaction;
import org.jetbrains.annotations.Nullable;

import java.util.UUID;

public class MachineJS {

    private final MachineTile internal;
    private final CompoundTag nbt;

    protected MachineJS(MachineTile internal) {
        this.internal = internal;
        this.nbt = this.internal.getComponentManager().getComponent(CMRegistration.DATA_MACHINE_COMPONENT.get()).orElseThrow().getData();
    }

    @Nullable
    public static MachineJS of(@Nullable Object o) {
        if(o == null)
            return null;

        if(o instanceof Wrapper w)
            o = w.unwrap();

        if(o instanceof BlockEntity blockEntity) {
            if(blockEntity instanceof MachineTile machineTile)
                return new MachineJS(machineTile);
        }

        if(o instanceof CachedLevelBlock block)
            return of(block.getEntity());

        return null;
    }

    public String getId() {
        return this.internal.getId().toString();
    }

    public void setId(String id) {
        Identifier loc = Identifier.tryParse(id);
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

    public SideConfig<?> getComponentConfig(String type) {
        return this.getComponentConfig(type, "");
    }

    public SideConfig<?> getComponentConfig(String type, String id) {
        return this.internal.getComponentManager().getConfigComponentById(type + ":" + id).orElseThrow(() -> new IllegalArgumentException("IO config not found for component with type: " + type + " and id: " + id)).getConfig();
    }

    public MachineTile getBlockEntity() {
        return this.internal;
    }

    public MachineStatus getStatus() {
        return this.internal.getStatus();
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

    public void setOwner(@Nullable LivingEntity entity) {
        if(entity == null)
            throw new KubeRuntimeException("Can't set null owner of custom machine: '" + getId() + "'");
        this.internal.setOwner(entity);
    }

    /** ENERGY STUFF **/

    public long getEnergyStored() {
        return this.internal.getComponentManager().getComponent(CMRegistration.ENERGY_MACHINE_COMPONENT.get()).map(EnergyMachineComponent::getEnergy).orElse(0L);
    }

    public void setEnergyStored(long energy) {
        this.internal.getComponentManager().getComponent(CMRegistration.ENERGY_MACHINE_COMPONENT.get()).ifPresent(component -> component.setEnergy(energy));
    }

    public long getEnergyCapacity() {
        return this.internal.getComponentManager().getComponent(CMRegistration.ENERGY_MACHINE_COMPONENT.get()).map(EnergyMachineComponent::getCapacity).orElse(0L);
    }

    //Return amount of energy added.
    public int addEnergy(int toAdd, boolean simulate) {
        return this.internal.getComponentManager().getComponent(CMRegistration.ENERGY_MACHINE_COMPONENT.get()).map(component -> component.receiveRecipeEnergy(toAdd, simulate)).orElse(0);
    }

    //Return amount of energy removed.
    public int removeEnergy(int toRemove, boolean simulate) {
        return this.internal.getComponentManager().getComponent(CMRegistration.ENERGY_MACHINE_COMPONENT.get()).map(component -> component.extractRecipeEnergy(toRemove, simulate)).orElse(0);
    }

    /** FLUID STUFF **/

    public FluidStack getFluidStored(String tank) {
        return this.internal.getComponentManager().getComponentHandler(CMRegistration.FLUID_MACHINE_COMPONENT.get()).flatMap(handler -> handler.getComponentForID(tank)).map(FluidMachineComponent::getFluid).orElse(FluidStack.EMPTY);
    }

    public void setFluidStored(String tank, FluidStack stack) {
        this.internal.getComponentManager().getComponentHandler(CMRegistration.FLUID_MACHINE_COMPONENT.get()).flatMap(handler -> handler.getComponentForID(tank)).ifPresent(x -> x.setFluidStack(stack));
    }

    public int getFluidCapacity(String tank) {
        return this.internal.getComponentManager().getComponentHandler(CMRegistration.FLUID_MACHINE_COMPONENT.get())
                .flatMap(handler -> handler.getComponentForID(tank))
                .map(FluidMachineComponent::getCapacity)
                .orElse(0);
    }

    //Return amount of fluid that was added.
    public int addFluid(FluidStack stack, boolean simulate) {
        return this.internal.getComponentManager().getComponentHandler(CMRegistration.FLUID_MACHINE_COMPONENT.get())
                .map(handler -> (FluidComponentHandler)handler)
                .map(handler -> {
                    ResourceHandler<FluidResource> fluidHandler = handler.getFluidHandler(null);
                    if(fluidHandler == null)
                        return 0;
                    try(Transaction transaction = Transaction.openRoot()) {
                        int inserted = fluidHandler.insert(FluidResource.of(stack), stack.amount(), transaction);
                        if(!simulate)
                            transaction.commit();
                        return inserted;
                    }
                })
                .orElse(0);
    }

    //Return amount of fluid that was added.
    public int addFluidToTank(String tank, FluidStack stack, boolean simulate) {
        return this.internal.getComponentManager().getComponentHandler(CMRegistration.FLUID_MACHINE_COMPONENT.get())
                .flatMap(handler -> handler.getComponentForID(tank))
                .map(component -> {
                    try(Transaction transaction = Transaction.openRoot()) {
                        int inserted = component.insertBypassLimit(FluidResource.of(stack), stack.amount(), transaction);
                        if(!simulate)
                            transaction.commit();
                        return inserted;
                    }
                })
                .orElse(0);
    }

    //Return fluid that was successfully removed.
    public FluidStack removeFluid(FluidStack stack, boolean simulate) {
        return this.internal.getComponentManager().getComponentHandler(CMRegistration.FLUID_MACHINE_COMPONENT.get())
                .map(handler -> (FluidComponentHandler)handler)
                .map(handler -> {
                    ResourceHandler<FluidResource> fluidHandler = handler.getFluidHandler(null);
                    if(fluidHandler == null)
                        return FluidStack.EMPTY;
                    try(Transaction transaction = Transaction.openRoot()) {
                        int extracted = fluidHandler.extract(FluidResource.of(stack), stack.amount(), transaction);
                        if(!simulate)
                            transaction.commit();
                        return stack.copyWithAmount(extracted);
                    }
                })
                .orElse(FluidStack.EMPTY);
    }

    //Return fluid that was successfully removed.
    public FluidStack removeFluidFromTank(String tank, int amount, boolean simulate) {
        return this.internal.getComponentManager().getComponentHandler(CMRegistration.FLUID_MACHINE_COMPONENT.get())
                .flatMap(handler -> handler.getComponentForID(tank))
                .map(component -> {
                    FluidStack stack = component.getFluid();
                    if(stack.isEmpty())
                        return FluidStack.EMPTY;
                    try(Transaction transaction = Transaction.openRoot()) {
                        int extracted = component.extractBypassLimit(FluidResource.of(stack), stack.amount(), transaction);
                        if(!simulate)
                            transaction.commit();
                        return stack.copyWithAmount(extracted);
                    }
                })
                .orElse(FluidStack.EMPTY);
    }

    /** ITEM STUFF **/

    public ItemStack getItemStored(String slot) {
        return this.internal.getComponentManager().getComponentHandler(CMRegistration.ITEM_MACHINE_COMPONENT.get())
                .flatMap(handler -> handler.getComponentForID(slot))
                .map(ItemMachineComponent::getItemStack)
                .orElse(ItemStack.EMPTY);
    }

    public void setItemStored(String slot, ItemStack stack) {
        this.internal.getComponentManager().getComponentHandler(CMRegistration.ITEM_MACHINE_COMPONENT.get())
                .flatMap(handler -> handler.getComponentForID(slot))
                .ifPresent(component -> component.setItemStack(stack));
    }

    public int getItemCapacity(String slot) {
        return this.internal.getComponentManager().getComponentHandler(CMRegistration.ITEM_MACHINE_COMPONENT.get())
                .flatMap(handler -> handler.getComponentForID(slot))
                .map(ItemMachineComponent::getCapacity)
                .orElse(0);
    }

    //Return items that couldn't be added.
    public int addItemToSlot(String slot, ItemStack stack, boolean simulate) {
        return this.internal.getComponentManager().getComponentHandler(CMRegistration.ITEM_MACHINE_COMPONENT.get())
                .flatMap(handler -> handler.getComponentForID(slot))
                .map(component -> {
                    try(Transaction transaction = Transaction.openRoot()) {
                        int inserted = component.insertBypassLimit(ItemResource.of(stack), stack.count(), transaction);
                        if(!simulate)
                            transaction.commit();
                        return inserted;
                    }
                })
                .orElse(0);
    }

    //Return items that were successfully removed from the slot.
    public ItemStack removeItemFromSlot(String slot, int toRemove, boolean simulate) {
        return this.internal.getComponentManager().getComponentHandler(CMRegistration.ITEM_MACHINE_COMPONENT.get())
                .flatMap(handler -> handler.getComponentForID(slot))
                .map(component -> {
                    ItemStack stack = component.getItemStack();
                    if(stack.isEmpty())
                        return ItemStack.EMPTY;
                    try(Transaction transaction = Transaction.openRoot()) {
                        int extracted = component.extractBypassLimit(ItemResource.of(stack), stack.count(), transaction);
                        if(!simulate)
                            transaction.commit();
                        return stack.copyWithCount(extracted);
                    }
                })
                .orElse(ItemStack.EMPTY);
    }

    public void lockSlot(String slot) {
        this.internal.getComponentManager().getComponentHandler(CMRegistration.ITEM_MACHINE_COMPONENT.get())
                .flatMap(handler -> handler.getComponentForID(slot))
                .ifPresent(component -> component.setLocked(true));
    }

    public void unlockSlot(String slot) {
        this.internal.getComponentManager().getComponentHandler(CMRegistration.ITEM_MACHINE_COMPONENT.get())
                .flatMap(handler -> handler.getComponentForID(slot))
                .ifPresent(component -> component.setLocked(false));
    }

    public boolean isSlotLocked(String slot) {
        return this.internal.getComponentManager().getComponentHandler(CMRegistration.ITEM_MACHINE_COMPONENT.get())
                .flatMap(handler -> handler.getComponentForID(slot))
                .map(ItemMachineComponent::isLocked)
                .orElseThrow(() -> new IllegalArgumentException("Invalid slot id: " + slot));
    }

    /** Chunkload stuff **/

    public void enableChunkload(int radius) {
        this.internal.getComponentManager()
                .getComponent(CMRegistration.CHUNKLOAD_MACHINE_COMPONENT.get())
                .ifPresent(component -> component.setActive(radius));
    }

    public void disableChunkload() {
        this.internal.getComponentManager()
                .getComponent(CMRegistration.CHUNKLOAD_MACHINE_COMPONENT.get())
                .ifPresent(component -> component.setInactive((ServerLevel) this.internal.getLevel()));
    }

    public boolean isChunkloadEnabled() {
        return this.internal.getComponentManager()
                .getComponent(CMRegistration.CHUNKLOAD_MACHINE_COMPONENT.get())
                .map(ChunkloadMachineComponent::isActive)
                .orElse(false);
    }

    public int getChunkloadRadius() {
        return this.internal.getComponentManager()
                .getComponent(CMRegistration.CHUNKLOAD_MACHINE_COMPONENT.get())
                .map(ChunkloadMachineComponent::getRadius)
                .orElse(0);
    }
}
