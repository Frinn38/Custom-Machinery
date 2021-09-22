package fr.frinn.custommachinery.common.data.component.handler;

import fr.frinn.custommachinery.api.components.*;
import fr.frinn.custommachinery.api.network.ISyncable;
import fr.frinn.custommachinery.api.network.ISyncableStuff;
import fr.frinn.custommachinery.common.data.component.ItemMachineComponent;
import fr.frinn.custommachinery.common.init.Registration;
import fr.frinn.custommachinery.common.util.Utils;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.nbt.ListNBT;
import net.minecraft.util.Direction;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.util.Constants;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.items.CapabilityItemHandler;
import net.minecraftforge.items.IItemHandler;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Random;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Consumer;
import java.util.function.Predicate;

public class ItemComponentHandler extends AbstractComponentHandler<ItemMachineComponent> implements IItemHandler, ICapabilityComponent, ISerializableComponent, ITickableComponent, ISyncableStuff {

    private LazyOptional<IItemHandler> capability = LazyOptional.of(() -> this);
    private final Random rand = new Random();

    public ItemComponentHandler(IMachineComponentManager manager) {
        super(manager);
    }

    @Override
    public MachineComponentType<ItemMachineComponent> getType() {
        return Registration.ITEM_MACHINE_COMPONENT.get();
    }

    @Override
    public void putComponent(ItemMachineComponent component) {
        super.putComponent(component);
        if(component.getMode().isInput())
            this.inputs.add(component);
        if(component.getMode().isOutput())
            this.outputs.add(component);
    }

    @Override
    public <T> LazyOptional<T> getCapability(@Nonnull Capability<T> cap, @Nullable Direction side) {
        if(cap == CapabilityItemHandler.ITEM_HANDLER_CAPABILITY)
            return this.capability.cast();
        return LazyOptional.empty();
    }

    @Override
    public void invalidateCapability() {
        this.capability.invalidate();
    }

    @Override
    public Optional<ItemMachineComponent> getComponentForID(String id) {
        return this.getComponents().stream().filter(component -> component.getId().equals(id)).findFirst();
    }

    @Override
    public void serialize(CompoundNBT nbt) {
        ListNBT components = new ListNBT();
        this.getComponents().forEach(component -> {
            CompoundNBT componentNBT = new CompoundNBT();
            component.serialize(componentNBT);
            components.add(componentNBT);
        });
        nbt.put("items", components);
    }

    @Override
    public void deserialize(CompoundNBT nbt) {
        if(nbt.contains("items", Constants.NBT.TAG_LIST)) {
            ListNBT components = nbt.getList("items", Constants.NBT.TAG_COMPOUND);
            components.forEach(inbt -> {
                if (inbt instanceof CompoundNBT) {
                    CompoundNBT componentNBT = (CompoundNBT)inbt;
                    if(componentNBT.contains("slotID", Constants.NBT.TAG_STRING)) {
                        this.getComponents().stream().filter(component -> component.getId().equals(componentNBT.getString("slotID"))).findFirst().ifPresent(component -> component.deserialize(componentNBT));
                    }
                }
            });
        }
    }

    @Override
    public void serverTick() {
        this.getComponents().forEach(component -> component.getVariant().tick(getManager()));
    }

    @Override
    public void getStuffToSync(Consumer<ISyncable<?, ?>> container) {
        this.getComponents().forEach(component -> component.getStuffToSync(container));
    }

    /** ITEM HANDLER STUFF **/

    @Override
    public int getSlots() {
        return this.getComponents().size();
    }

    @Nonnull
    @Override
    public ItemStack getStackInSlot(int index) {
        return this.getComponents().get(index).getItemStack();
    }

    @Nonnull
    @Override
    public ItemStack insertItem(int index, @Nonnull ItemStack stack, boolean simulate) {
        ItemMachineComponent component = this.getComponents().get(index);
        if(!component.getMode().isInput())
            return stack;
        int maxInsert = component.getSpaceForItem(stack);
        int toInsert = Math.min(maxInsert, stack.getCount());
        if(!simulate) {
            ItemStack stackIn = stack.copy();
            stackIn.setCount(toInsert);
            component.insert(stackIn);
            getManager().markDirty();
        }
        ItemStack stackRemaining = stack.copy();
        stackRemaining.shrink(toInsert);
        return stackRemaining;
    }

    @Nonnull
    @Override
    public ItemStack extractItem(int index, int amount, boolean simulate) {
        ItemMachineComponent component = this.getComponents().get(index);
        if(!component.getMode().isOutput() || component.getItemStack().isEmpty())
            return ItemStack.EMPTY;
        ItemStack stack = component.getItemStack().copy();
        stack.setCount(Math.min(component.getItemStack().getCount(), amount));
        if(!simulate) {
            component.extract(stack.getCount());
            getManager().markDirty();
        }
        return stack;
    }

    @Override
    public int getSlotLimit(int index) {
        return this.getComponents().get(index).getCapacity();
    }

    @Override
    public boolean isItemValid(int index, @Nonnull ItemStack stack) {
        return this.getComponents().get(index).isItemValid(stack);
    }

    /** RECIPE STUFF **/

    private List<ItemMachineComponent> inputs = new ArrayList<>();
    private List<ItemMachineComponent> outputs = new ArrayList<>();

    public int getItemAmount(String slot, Item item, @Nullable CompoundNBT nbt) {
        Predicate<ItemMachineComponent> nbtPredicate = component -> nbt == null || nbt.isEmpty() || (component.getItemStack().getTag() != null && Utils.testNBT(component.getItemStack().getTag(), nbt));
        Predicate<ItemMachineComponent> slotPredicate = component -> slot.isEmpty() || component.getId().equals(slot);
        return this.inputs.stream().filter(component -> component.getItemStack().getItem() == item && nbtPredicate.test(component) && slotPredicate.test(component))
                .mapToInt(component -> component.getItemStack().getCount())
                .sum();
    }

    public int getDurabilityAmount(String slot, Item item, @Nullable CompoundNBT nbt) {
        Predicate<ItemMachineComponent> nbtPredicate = component -> nbt == null || nbt.isEmpty() || (component.getItemStack().getTag() != null && Utils.testNBT(component.getItemStack().getTag(), nbt));
        Predicate<ItemMachineComponent> slotPredicate = component -> slot.isEmpty() || component.getId().equals(slot);
        return this.inputs.stream().filter(component -> component.getItemStack().getItem() == item && component.getItemStack().isDamageable() && nbtPredicate.test(component) && slotPredicate.test(component))
                .mapToInt(component -> component.getItemStack().getMaxDamage() - component.getItemStack().getDamage())
                .sum();
    }

    public int getSpaceForItem(String slot, Item item, @Nullable CompoundNBT nbt) {
        ItemStack stack = item.getDefaultInstance();
        stack.setTag(nbt == null ? null : nbt.copy());
        int maxStackSize = stack.getMaxStackSize();
        Predicate<ItemMachineComponent> nbtPredicate = component -> nbt == null || nbt.isEmpty() || (component.getItemStack().getTag() != null && Utils.testNBT(component.getItemStack().getTag(), nbt));
        Predicate<ItemMachineComponent> slotPredicate = component -> slot.isEmpty() || component.getId().equals(slot);
        return this.outputs.stream().filter(component -> component.getItemStack().isEmpty() || (component.getItemStack().getItem() == item && component.getItemStack().getCount() < Math.min(maxStackSize, component.getCapacity()) && nbtPredicate.test(component) && slotPredicate.test(component)))
                .mapToInt(component -> {
                    if(component.getItemStack().isEmpty())
                        return Math.min(component.getCapacity(), maxStackSize);
                    else
                        return Math.min(component.getCapacity() - component.getItemStack().getCount(), maxStackSize - component.getItemStack().getCount());
                })
                .sum();
    }

    public int getSpaceForDurability(String slot, Item item, @Nullable CompoundNBT nbt) {
        Predicate<ItemMachineComponent> nbtPredicate = component -> nbt == null || nbt.isEmpty() || (component.getItemStack().getTag() != null && Utils.testNBT(component.getItemStack().getTag(), nbt));
        Predicate<ItemMachineComponent> slotPredicate = component -> slot.isEmpty() || component.getId().equals(slot);
        return this.inputs.stream().filter(component -> component.getItemStack().getItem() == item && component.getItemStack().isDamageable() && nbtPredicate.test(component) && slotPredicate.test(component))
                .mapToInt(component -> component.getItemStack().getDamage())
                .sum();
    }

    public void removeFromInputs(String slot, Item item, int amount, @Nullable CompoundNBT nbt) {
        AtomicInteger toRemove = new AtomicInteger(amount);
        Predicate<ItemMachineComponent> nbtPredicate = component -> nbt == null || nbt.isEmpty() || (component.getItemStack().getTag() != null && Utils.testNBT(component.getItemStack().getTag(), nbt));
        Predicate<ItemMachineComponent> slotPredicate = component -> slot.isEmpty() || component.getId().equals(slot);
        this.inputs.stream().filter(component -> component.getItemStack().getItem() == item && nbtPredicate.test(component) && slotPredicate.test(component)).forEach(component -> {
            int maxExtract = Math.min(component.getItemStack().getCount(), toRemove.get());
            toRemove.addAndGet(-maxExtract);
            component.extract(maxExtract);
        });
        getManager().markDirty();
    }

    public void removeDurability(String slot, Item item, int amount, @Nullable CompoundNBT nbt) {
        AtomicInteger toRemove = new AtomicInteger(amount);
        Predicate<ItemMachineComponent> nbtPredicate = component -> nbt == null || nbt.isEmpty() || (component.getItemStack().getTag() != null && Utils.testNBT(component.getItemStack().getTag(), nbt));
        Predicate<ItemMachineComponent> slotPredicate = component -> slot.isEmpty() || component.getId().equals(slot);
        this.inputs.stream().filter(component -> component.getItemStack().getItem() == item && component.getItemStack().isDamageable() && nbtPredicate.test(component) && slotPredicate.test(component)).forEach(component -> {
            int maxRemove = Math.min(component.getItemStack().getMaxDamage() - component.getItemStack().getDamage(), toRemove.get());
            toRemove.addAndGet(-maxRemove);
            component.getItemStack().attemptDamageItem(maxRemove, rand, null);
        });
    }

    public void addToOutputs(String slot, Item item, int amount, @Nullable CompoundNBT nbt) {
        AtomicInteger toAdd = new AtomicInteger(amount);
        Predicate<ItemMachineComponent> nbtPredicate = component -> nbt == null || nbt.isEmpty() || (component.getItemStack().getTag() != null && Utils.testNBT(component.getItemStack().getTag(), nbt));
        Predicate<ItemMachineComponent> slotPredicate = component -> slot.isEmpty() || component.getId().equals(slot);
        this.outputs.stream().filter(component -> component.getItemStack().isEmpty() || (component.getItemStack().getItem() == item && component.getSpaceForItem(item.getDefaultInstance()) > 0 && nbtPredicate.test(component) && slotPredicate.test(component))).forEach(component -> {
            int maxInsert = Math.min(component.getSpaceForItem(item.getDefaultInstance()), toAdd.get());
            toAdd.addAndGet(-maxInsert);
            ItemStack stack = new ItemStack(item, maxInsert);
            if(nbt != null && !nbt.isEmpty())
                stack.setTag(nbt.copy());
            component.insert(stack);
        });
        getManager().markDirty();
    }

    public void repairItem(String slot, Item item, int amount, @Nullable CompoundNBT nbt) {
        AtomicInteger toRepair = new AtomicInteger(amount);
        Predicate<ItemMachineComponent> nbtPredicate = component -> nbt == null || nbt.isEmpty() || (component.getItemStack().getTag() != null && Utils.testNBT(component.getItemStack().getTag(), nbt));
        Predicate<ItemMachineComponent> slotPredicate = component -> slot.isEmpty() || component.getId().equals(slot);
        this.inputs.stream().filter(component -> component.getItemStack().getItem() == item && component.getItemStack().isDamageable() && nbtPredicate.test(component) && slotPredicate.test(component)).forEach(component -> {
            int maxRepair = Math.min(component.getItemStack().getDamage(), toRepair.get());
            toRepair.addAndGet(-maxRepair);
            component.getItemStack().setDamage(component.getItemStack().getDamage() - maxRepair);
        });
        getManager().markDirty();
    }
}