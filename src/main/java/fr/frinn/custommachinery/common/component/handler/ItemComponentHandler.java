package fr.frinn.custommachinery.common.component.handler;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Maps;
import fr.frinn.custommachinery.api.component.IDumpComponent;
import fr.frinn.custommachinery.api.component.IMachineComponentManager;
import fr.frinn.custommachinery.api.component.ISerializableComponent;
import fr.frinn.custommachinery.api.component.ITickableComponent;
import fr.frinn.custommachinery.api.component.MachineComponentType;
import fr.frinn.custommachinery.api.network.ISyncable;
import fr.frinn.custommachinery.api.network.ISyncableStuff;
import fr.frinn.custommachinery.common.component.item.ItemMachineComponent;
import fr.frinn.custommachinery.common.guielement.SplitButtonGuiElement;
import fr.frinn.custommachinery.common.init.Registration;
import fr.frinn.custommachinery.common.util.transfer.SidedItemHandler;
import fr.frinn.custommachinery.impl.component.AbstractComponentHandler;
import fr.frinn.custommachinery.impl.component.config.IOSideMode;
import fr.frinn.custommachinery.impl.component.config.RelativeSide;
import net.minecraft.core.Direction;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.component.DataComponents;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.StringTag;
import net.minecraft.nbt.Tag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.neoforged.neoforge.capabilities.BlockCapabilityCache;
import net.neoforged.neoforge.capabilities.Capabilities.ItemHandler;
import net.neoforged.neoforge.items.IItemHandler;
import net.neoforged.neoforge.items.IItemHandlerModifiable;
import net.neoforged.neoforge.items.ItemHandlerHelper;
import org.apache.commons.lang3.tuple.Triple;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Consumer;
import java.util.function.Predicate;
import java.util.stream.Collectors;

public class ItemComponentHandler extends AbstractComponentHandler<ItemMachineComponent> implements ISerializableComponent, ITickableComponent, ISyncableStuff, IDumpComponent, IItemHandlerModifiable {

    private final Map<Direction, SidedItemHandler> sidedHandlers = Maps.newEnumMap(Direction.class);
    private final Map<Direction, BlockCapabilityCache<IItemHandler, Direction>> neighbourStorages = Maps.newEnumMap(Direction.class);

    private final Map<String, List<String>> slotSplitters = new HashMap<>();

    public ItemComponentHandler(IMachineComponentManager manager, List<ItemMachineComponent> components) {
        super(manager, components);
        components.forEach(component -> {
            component.getConfig().setCallback(this::configChanged);
            if(component.getMode().isInput())
                this.inputs.add(component);
            if(component.getMode().isOutput())
                this.outputs.add(component);
        });
        for(Direction direction : Direction.values())
            this.sidedHandlers.put(direction, new SidedItemHandler(direction, this));
    }

    @Nullable
    public IItemHandler getItemHandlerForSide(@Nullable Direction side) {
        if(side == null)
            return this;
        if(this.getComponents().stream().anyMatch(component -> !component.getConfig().getDirectionMode(side).isNone()))
            return this.sidedHandlers.get(side);
        return null;
    }

    public void configChanged(RelativeSide side, IOSideMode oldMode, IOSideMode newMode) {
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
        ListTag splitters = new ListTag();
        this.slotSplitters.forEach((id, slots) -> splitters.add(StringTag.valueOf(id)));
        nbt.put("splitters", splitters);
    }

    @Override
    public void deserialize(CompoundTag nbt, HolderLookup.Provider registries) {
        if(nbt.contains("items", Tag.TAG_LIST)) {
            ListTag components = nbt.getList("items", Tag.TAG_COMPOUND);
            components.forEach(tag -> {
                if (tag instanceof CompoundTag componentNBT) {
                    if(componentNBT.contains("slotID", Tag.TAG_STRING)) {
                        this.getComponents().stream().filter(component -> component.getId().equals(componentNBT.getString("slotID"))).findFirst().ifPresent(component -> component.deserialize(componentNBT, registries));
                    }
                }
            });
        }
        if(nbt.contains("splitters", Tag.TAG_LIST)) {
            ListTag splitters = nbt.getList("splitters", Tag.TAG_STRING);
            splitters.forEach(tag -> {
                if(tag instanceof StringTag stringTag) {
                    this.getManager().getTile().getMachine().getGuiElements().stream()
                            .filter(element -> element instanceof SplitButtonGuiElement && element.getId().equals(stringTag.getAsString()))
                            .findFirst()
                            .ifPresent(element -> this.slotSplitters.put(stringTag.getAsString(), ((SplitButtonGuiElement)element).getSlots()));
                }
            });
        }
    }

    @Override
    public void serverTick() {
        //Tick each component
        super.serverTick();

        //Sort slots
        Set<String> sortedSlots = new HashSet<>();
        this.slotSplitters.forEach((id, slots) -> {
            //All slots are already sorted so shortcut
            if(sortedSlots.containsAll(slots))
                return;

            //Use only slot not already sorted
            List<String> toSort = slots.stream().filter(slot -> !sortedSlots.contains(slot)).collect(Collectors.toList());
            sortedSlots.addAll(toSort);
            
            //Get a list of items that are in the slots to sort
            //Triple<Item, Amount, Slots>
            List<Triple<ItemStack, Integer, List<String>>> itemsToSort = new ArrayList<>();
            toSort.forEach(slot -> this.getComponentForID(slot).filter(component -> !component.getItemStack().isEmpty()).ifPresent((component -> {
                Triple<ItemStack, Integer, List<String>> alreadyPresent = itemsToSort.stream().filter(triple -> ItemStack.isSameItemSameComponents(triple.getLeft(), component.getItemStack())).findFirst().orElse(null);
                if(alreadyPresent == null) {
                    itemsToSort.add(Triple.of(component.getItemStack().copy(), component.getItemStack().getCount(), Collections.singletonList(slot)));
                } else {
                    itemsToSort.remove(alreadyPresent);
                    itemsToSort.add(Triple.of(alreadyPresent.getLeft(), alreadyPresent.getMiddle() + component.getItemStack().getCount(), ImmutableList.<String>builder().addAll(alreadyPresent.getRight()).add(slot).build()));
                }
            })));

            //Sort each item
            Iterator<Triple<ItemStack, Integer, List<String>>> iterator = itemsToSort.iterator();
            while(iterator.hasNext()) {
                //If every slot to sort already contains items then sort nothing.
                if(itemsToSort.size() == toSort.size())
                    return;

                //Current item to sort
                Triple<ItemStack, Integer, List<String>> sorting = iterator.next();

                //If slot only contains 1 item don't touch it
                if(sorting.getMiddle() == 1) {
                    toSort.removeAll(sorting.getRight());
                    iterator.remove();
                    return;
                }

                //Gather all slots where this item can go (empty slots + current slot)
                List<String> availableSlots = new ArrayList<>(sorting.getRight());
                toSort.stream().filter(slot -> this.getComponentForID(slot).map(component -> component.getItemStack().isEmpty()).orElse(false)).forEach(availableSlots::add);

                //Amount of items to put in each slots
                int count = sorting.getMiddle() / availableSlots.size();
                //Remaining items
                AtomicInteger remaining = new AtomicInteger(sorting.getMiddle() % availableSlots.size());

                //Place items in slots
                availableSlots.forEach(slot -> this.getComponentForID(slot).ifPresent(component -> {
                    //If there is remaining add 1 extra item and remove it from remaining count
                    if(remaining.getAndAdd(-1) > 0)
                        component.setItemStack(sorting.getLeft().copyWithCount(count + 1));
                    else
                        component.setItemStack(sorting.getLeft().copyWithCount(count));
                }));

                //Remove used slots from toSort list
                toSort.removeAll(availableSlots);
                iterator.remove();
            }
        });

        //Sided auto-I/O
        for(Direction side : Direction.values()) {
            if(this.getComponents().stream().noneMatch(component -> component.getConfig().canAutoIO(side)))
                continue;

            if(this.neighbourStorages.get(side) == null)
                this.neighbourStorages.put(side, BlockCapabilityCache.create(ItemHandler.BLOCK, (ServerLevel) this.getManager().getLevel(), this.getManager().getTile().getBlockPos().relative(side), side.getOpposite(), () -> !this.getManager().getTile().isRemoved(), () -> this.neighbourStorages.remove(side)));

            IItemHandler neighbour = this.neighbourStorages.get(side).getCapability();

            if(neighbour == null)
                continue;

            this.sidedHandlers.get(side).getHandler().getComponents().forEach(component -> {
                if(component.getConfig().isAutoInput() && component.getConfig().getDirectionMode(side).isInput() && component.getItemStack().getCount() < component.getCapacity())
                    moveStacks(neighbour, component, Integer.MAX_VALUE);

                if(component.getConfig().isAutoOutput() && component.getConfig().getDirectionMode(side).isOutput() && !component.getItemStack().isEmpty())
                    moveStacks(component, neighbour, Integer.MAX_VALUE);
            });
        }
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

    /** SPLITTER STUFF **/

    public void addSplitter(String id, List<String> slots) {
        this.slotSplitters.put(id, slots);
    }

    public void removeSplitter(String id) {
        this.slotSplitters.remove(id);
    }

    public Set<String> getSplitters() {
        return this.slotSplitters.keySet();
    }

    /** RECIPE STUFF **/

    private final List<ItemMachineComponent> inputs = new ArrayList<>();
    private final List<ItemMachineComponent> outputs = new ArrayList<>();

    public int getIngredientAmount(String slot, Ingredient ingredient) {
        Predicate<ItemMachineComponent> slotPredicate = component -> slot.isEmpty() || component.getId().equals(slot);
        return this.inputs.stream().filter(component -> ingredient.test(component.getItemStack()) && slotPredicate.test(component))
                .mapToInt(component -> component.getItemStack().getCount())
                .sum();
    }

    public int getDurabilityAmount(String slot, ItemStack stack) {
        Predicate<ItemMachineComponent> slotPredicate = component -> slot.isEmpty() || component.getId().equals(slot);
        return this.inputs.stream().filter(component -> isSameItem(component.getItemStack(), stack) && component.getItemStack().isDamageableItem() && slotPredicate.test(component))
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
        if(!component.isItemValid(0, stack))
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
        return this.inputs.stream().filter(component -> isSameItem(component.getItemStack(), stack) && component.getItemStack().isDamageableItem() && slotPredicate.test(component))
                .mapToInt(component -> component.getItemStack().getDamageValue())
                .sum();
    }

    public void removeFromInputs(String slot, Ingredient ingredient, int amount) {
        AtomicInteger toRemove = new AtomicInteger(amount);
        Predicate<ItemMachineComponent> slotPredicate = component -> slot.isEmpty() || component.getId().equals(slot);
        this.inputs.stream().filter(component -> ingredient.test(component.getItemStack()) && slotPredicate.test(component)).forEach(component -> {
            int maxExtract = Math.min(component.getItemStack().getCount(), toRemove.get());
            toRemove.addAndGet(-maxExtract);
            component.getItemStack().shrink(maxExtract);
        });
        getManager().markDirty();
    }

    public void removeDurability(String slot, ItemStack input, int amount, boolean canBreak) {
        AtomicInteger toRemove = new AtomicInteger(amount);
        Predicate<ItemMachineComponent> slotPredicate = component -> slot.isEmpty() || component.getId().equals(slot);
        this.inputs.stream().filter(component -> isSameItem(component.getItemStack(), input) && component.getItemStack().isDamageableItem() && slotPredicate.test(component)).forEach(component -> {
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
        this.inputs.stream().filter(component -> isSameItem(component.getItemStack(), stack) && component.getItemStack().isDamageableItem() && slotPredicate.test(component)).forEach(component -> {
            int maxRepair = Math.min(component.getItemStack().getDamageValue(), toRepair.get());
            toRepair.addAndGet(-maxRepair);
            component.getItemStack().setDamageValue(component.getItemStack().getDamageValue() - maxRepair);
        });
        getManager().markDirty();
    }

    private static boolean isSameItem(ItemStack toTest, ItemStack ingredient) {
        if(toTest.getItem() != ingredient.getItem())
            return false;
        return ingredient.getComponents().stream().allMatch(component -> component.type() == DataComponents.DAMAGE || (toTest.has(component.type()) && Objects.equals(toTest.get(component.type()), component.value())));
    }

    public boolean isInputSlotEmpty(String slot) {
        return this.inputs.stream().anyMatch(component -> component.getItemStack().isEmpty() && (slot.isEmpty() || slot.equals(component.getId())));
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