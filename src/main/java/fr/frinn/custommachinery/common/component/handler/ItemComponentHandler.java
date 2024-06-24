package fr.frinn.custommachinery.common.component.handler;

import com.google.common.collect.Maps;
import fr.frinn.custommachinery.api.component.IDumpComponent;
import fr.frinn.custommachinery.api.component.IMachineComponentManager;
import fr.frinn.custommachinery.api.component.ISerializableComponent;
import fr.frinn.custommachinery.api.component.ITickableComponent;
import fr.frinn.custommachinery.api.component.MachineComponentType;
import fr.frinn.custommachinery.api.component.variant.ITickableComponentVariant;
import fr.frinn.custommachinery.api.network.ISyncable;
import fr.frinn.custommachinery.api.network.ISyncableStuff;
import fr.frinn.custommachinery.common.component.ItemMachineComponent;
import fr.frinn.custommachinery.common.component.variant.item.FilterItemComponentVariant;
import fr.frinn.custommachinery.common.init.Registration;
import fr.frinn.custommachinery.common.util.transfer.SidedItemHandler;
import fr.frinn.custommachinery.impl.component.AbstractComponentHandler;
import fr.frinn.custommachinery.impl.component.config.RelativeSide;
import fr.frinn.custommachinery.impl.component.config.SideMode;
import net.minecraft.core.Direction;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.neoforged.neoforge.capabilities.BlockCapabilityCache;
import net.neoforged.neoforge.capabilities.Capabilities.ItemHandler;
import net.neoforged.neoforge.items.IItemHandler;
import net.neoforged.neoforge.items.IItemHandlerModifiable;
import net.neoforged.neoforge.items.ItemHandlerHelper;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Consumer;
import java.util.function.Predicate;

public class ItemComponentHandler extends AbstractComponentHandler<ItemMachineComponent> implements ISerializableComponent, ITickableComponent, ISyncableStuff, IDumpComponent, IItemHandlerModifiable {

    private final List<ItemMachineComponent> tickableVariants;
    private final Map<Direction, SidedItemHandler> sidedHandlers = Maps.newEnumMap(Direction.class);
    private final Map<Direction, BlockCapabilityCache<IItemHandler, Direction>> neighbourStorages = Maps.newEnumMap(Direction.class);

    public ItemComponentHandler(IMachineComponentManager manager, List<ItemMachineComponent> components) {
        super(manager, components);
        components.forEach(component -> {
            component.getConfig().setCallback(this::configChanged);
            if(component.getVariant() != FilterItemComponentVariant.INSTANCE) {
                if(component.getMode().isInput())
                    this.inputs.add(component);
                if(component.getMode().isOutput())
                    this.outputs.add(component);
            }
        });
        this.tickableVariants = components.stream().filter(component -> component.getVariant() instanceof ITickableComponentVariant).toList();
        for(Direction direction : Direction.values())
            this.sidedHandlers.put(direction, new SidedItemHandler(direction, this));
    }

    @Nullable
    public IItemHandler getItemHandlerForSide(@Nullable Direction side) {
        if(side == null)
            return this;
        if(this.getComponents().stream().anyMatch(component -> !component.getConfig().getSideMode(side).isNone()))
            return this.sidedHandlers.get(side);
        return null;
    }

    public void configChanged(RelativeSide side, SideMode oldMode, SideMode newMode) {
        if(oldMode.isNone() != newMode.isNone())
            this.getManager().getTile().invalidateCapabilities();
    }

    @Override
    public MachineComponentType<ItemMachineComponent> getType() {
        return Registration.ITEM_MACHINE_COMPONENT.get();
    }

    @Override
    public Optional<ItemMachineComponent> getComponentForID(String id) {
        return this.getComponents().stream().filter(component -> component.getId().equals(id)).findFirst();
    }

    @Override
    public void serialize(CompoundTag nbt, HolderLookup.Provider registries) {
        ListTag components = new ListTag();
        this.getComponents().forEach(component -> {
            CompoundTag componentNBT = new CompoundTag();
            component.serialize(componentNBT, registries);
            componentNBT.putString("slotID", component.getId());
            components.add(componentNBT);
        });
        nbt.put("items", components);
    }

    @Override
    public void deserialize(CompoundTag nbt, HolderLookup.Provider registries) {
        if(nbt.contains("items", Tag.TAG_LIST)) {
            ListTag components = nbt.getList("items", Tag.TAG_COMPOUND);
            components.forEach(inbt -> {
                if (inbt instanceof CompoundTag componentNBT) {
                    if(componentNBT.contains("slotID", Tag.TAG_STRING)) {
                        this.getComponents().stream().filter(component -> component.getId().equals(componentNBT.getString("slotID"))).findFirst().ifPresent(component -> component.deserialize(componentNBT, registries));
                    }
                }
            });
        }
    }

    @SuppressWarnings("unchecked")
    @Override
    public void serverTick() {
        for(Direction side : Direction.values()) {
            if(this.getComponents().stream().allMatch(component -> component.getConfig().getSideMode(side) == SideMode.NONE))
                continue;

            IItemHandler neighbour;

            if(this.neighbourStorages.get(side) == null || this.neighbourStorages.get(side).getCapability() == null) {
                this.neighbourStorages.put(side, BlockCapabilityCache.create(ItemHandler.BLOCK, (ServerLevel) this.getManager().getLevel(), this.getManager().getTile().getBlockPos().relative(side), side.getOpposite(), () -> !this.getManager().getTile().isRemoved(), () -> this.neighbourStorages.remove(side)));
                if(this.neighbourStorages.get(side) != null)
                    neighbour = this.neighbourStorages.get(side).getCapability();
                else
                    continue;
            }
            else
                neighbour = this.neighbourStorages.get(side).getCapability();

            if(neighbour == null)
                continue;

            this.sidedHandlers.get(side).getHandler().getComponents().forEach(component -> {
                if(component.getConfig().isAutoInput() && component.getConfig().getSideMode(side).isInput() && component.getItemStack().getCount() < component.getCapacity())
                    moveStacks(neighbour, component, Integer.MAX_VALUE);

                if(component.getConfig().isAutoOutput() && component.getConfig().getSideMode(side).isOutput() && !component.getItemStack().isEmpty())
                    moveStacks(component, neighbour, Integer.MAX_VALUE);
            });
        }
        this.tickableVariants.forEach(component -> ((ITickableComponentVariant<ItemMachineComponent>)component.getVariant()).tick(component));
    }

    @Override
    public void getStuffToSync(Consumer<ISyncable<?, ?>> container) {
        this.getComponents().forEach(component -> component.getStuffToSync(container));
    }

    @Override
    public void dump(List<String> ids) {
        this.getComponents().stream()
                .filter(component -> ids.contains(component.getId()))
                .forEach(component -> component.setItemStack(ItemStack.EMPTY));
    }

    /** RECIPE STUFF **/

    private final List<ItemMachineComponent> inputs = new ArrayList<>();
    private final List<ItemMachineComponent> outputs = new ArrayList<>();

    public int getItemAmount(String slot, ItemStack stack) {
        Predicate<ItemMachineComponent> slotPredicate = component -> slot.isEmpty() || component.getId().equals(slot);
        return this.inputs.stream().filter(component -> ItemStack.isSameItemSameComponents(component.getItemStack(), stack) && slotPredicate.test(component))
                .mapToInt(component -> component.getItemStack().getCount())
                .sum();
    }

    public int getDurabilityAmount(String slot, ItemStack stack) {
        Predicate<ItemMachineComponent> slotPredicate = component -> slot.isEmpty() || component.getId().equals(slot);
        return this.inputs.stream().filter(component -> ItemStack.isSameItemSameComponents(component.getItemStack(), stack) && component.getItemStack().isDamageableItem() && slotPredicate.test(component))
                .mapToInt(component -> component.getItemStack().getMaxDamage() - component.getItemStack().getDamageValue())
                .sum();
    }

    public int getSpaceForItem(String slot, ItemStack stack) {
        return this.outputs.stream().filter(component -> canPlaceOutput(component, slot, stack))
                .mapToInt(component -> {
                    if(component.getItemStack().isEmpty())
                        return Math.min(component.getCapacity(), stack.getMaxStackSize());
                    else
                        return Math.min(component.getCapacity() - component.getItemStack().getCount(), stack.getMaxStackSize() - component.getItemStack().getCount());
                })
                .sum();
    }

    private boolean canPlaceOutput(ItemMachineComponent component, @Nullable String slot, ItemStack stack) {
        //Not the specified slot
        if(slot != null && !slot.isEmpty() && !component.getId().equals(slot))
            return false;

        //Check component filter and variant
        if(!component.isItemValid(stack))
            return false;

        //If the slot is empty, any item can go inside
        if(component.getItemStack().isEmpty())
            return true;

        //If the item present in the slot in not the same item, they won't stack
        if(!ItemStack.isSameItemSameComponents(component.getItemStack(), stack))
            return false;

        //Check if the stack present in the slot can accept more items
        return component.getItemStack().getCount() < Math.min(stack.getMaxStackSize(), component.getCapacity());
    }

    public int getSpaceForDurability(String slot, ItemStack stack) {
        Predicate<ItemMachineComponent> slotPredicate = component -> slot.isEmpty() || component.getId().equals(slot);
        return this.inputs.stream().filter(component -> ItemStack.isSameItemSameComponents(component.getItemStack(), stack) && component.getItemStack().isDamageableItem() && slotPredicate.test(component))
                .mapToInt(component -> component.getItemStack().getDamageValue())
                .sum();
    }

    public void removeFromInputs(String slot, ItemStack stack, int amount) {
        AtomicInteger toRemove = new AtomicInteger(amount);
        Predicate<ItemMachineComponent> slotPredicate = component -> slot.isEmpty() || component.getId().equals(slot);
        this.inputs.stream().filter(component -> ItemStack.isSameItemSameComponents(component.getItemStack(), stack) && slotPredicate.test(component)).forEach(component -> {
            int maxExtract = Math.min(component.getItemStack().getCount(), toRemove.get());
            toRemove.addAndGet(-maxExtract);
            component.getItemStack().shrink(maxExtract);
        });
        getManager().markDirty();
    }

    public void removeDurability(String slot, ItemStack input, int amount, boolean canBreak) {
        AtomicInteger toRemove = new AtomicInteger(amount);
        Predicate<ItemMachineComponent> slotPredicate = component -> slot.isEmpty() || component.getId().equals(slot);
        this.inputs.stream().filter(component -> ItemStack.isSameItemSameComponents(component.getItemStack(), input) && component.getItemStack().isDamageableItem() && slotPredicate.test(component)).forEach(component -> {
            int maxRemove = Math.min(component.getItemStack().getMaxDamage() - component.getItemStack().getDamageValue(), toRemove.get());
            ItemStack stack = component.getItemStack();
            maxRemove = stack.getItem().damageItem(stack, maxRemove, null, s -> {});
            if (maxRemove > 0) {
                maxRemove = EnchantmentHelper.processDurabilityChange((ServerLevel)this.getManager().getLevel(), stack, maxRemove);
                if (maxRemove <= 0) {
                    return;
                }
            }
            toRemove.addAndGet(-maxRemove);
            stack.setDamageValue(stack.getDamageValue() + maxRemove);
            if(stack.getDamageValue() >= stack.getMaxDamage() && canBreak)
                stack.shrink(1);
        });
        getManager().markDirty();
    }

    public void addToOutputs(String slot, ItemStack stack, int amount) {
        AtomicInteger toAdd = new AtomicInteger(amount);
        this.outputs.stream().filter(component -> canPlaceOutput(component, slot, stack)).forEach(component -> {
            int maxInsert = toAdd.get() - component.insertItemBypassLimit(stack, true).getCount();
            toAdd.addAndGet(-maxInsert);
            component.insertItemBypassLimit(stack.copyWithCount(maxInsert), false);
        });
        getManager().markDirty();
    }

    public void repairItem(String slot, ItemStack stack, int amount) {
        AtomicInteger toRepair = new AtomicInteger(amount);
        Predicate<ItemMachineComponent> slotPredicate = component -> slot.isEmpty() || component.getId().equals(slot);
        this.inputs.stream().filter(component -> ItemStack.isSameItemSameComponents(component.getItemStack(), stack) && component.getItemStack().isDamageableItem() && slotPredicate.test(component)).forEach(component -> {
            int maxRepair = Math.min(component.getItemStack().getDamageValue(), toRepair.get());
            toRepair.addAndGet(-maxRepair);
            component.getItemStack().setDamageValue(component.getItemStack().getDamageValue() - maxRepair);
        });
        getManager().markDirty();
    }

    /** IItemHandler stuff **/

    @Override
    public int getSlots() {
        return this.getComponents().size();
    }

    @Override
    public ItemStack getStackInSlot(int slot) {
        validateSlotIndex(slot);
        return this.getComponents().get(slot).getStackInSlot(0);
    }

    @Override
    public void setStackInSlot(int slot, ItemStack stack) {
        validateSlotIndex(slot);
        this.getComponents().get(slot).setStackInSlot(0, stack);
    }

    @Override
    public ItemStack insertItem(int slot, ItemStack stack, boolean simulate) {
        validateSlotIndex(slot);
        return this.getComponents().get(slot).insertItem(0, stack, simulate);
    }

    @Override
    public ItemStack extractItem(int slot, int amount, boolean simulate) {
        validateSlotIndex(slot);
        return this.getComponents().get(slot).extractItem(0, amount, simulate);
    }

    @Override
    public int getSlotLimit(int slot) {
        validateSlotIndex(slot);
        return this.getComponents().get(slot).getSlotLimit(0);
    }

    @Override
    public boolean isItemValid(int slot, ItemStack stack) {
        return this.getComponents().get(slot).isItemValid(0, stack);
    }

    protected void validateSlotIndex(int slot) {
        if (slot < 0 || slot >= this.getSlots())
            throw new RuntimeException("Slot " + slot + " not in valid range - [0," + this.getSlots() + ")");
    }

    private void moveStacks(IItemHandler from, IItemHandler to, int maxAmount) {
        for(int i = 0; i < from.getSlots(); i++) {
            ItemStack canExtract = from.extractItem(i, maxAmount, true);
            if(canExtract.isEmpty())
                continue;

            ItemStack canInsert = ItemHandlerHelper.insertItemStacked(to, canExtract, false);
            if(canInsert.isEmpty())
                from.extractItem(i, maxAmount, false);
            else
                from.extractItem(i, canExtract.getCount() - canInsert.getCount(), false);
        }
    }
}