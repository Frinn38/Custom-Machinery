package fr.frinn.custommachinery.common.component.handler;

import com.google.common.collect.Maps;
import fr.frinn.custommachinery.api.component.ICapabilityComponent;
import fr.frinn.custommachinery.api.component.IMachineComponentManager;
import fr.frinn.custommachinery.api.component.ISerializableComponent;
import fr.frinn.custommachinery.api.component.ITickableComponent;
import fr.frinn.custommachinery.api.component.MachineComponentType;
import fr.frinn.custommachinery.api.component.variant.ITickableComponentVariant;
import fr.frinn.custommachinery.api.network.ISyncable;
import fr.frinn.custommachinery.api.network.ISyncableStuff;
import fr.frinn.custommachinery.apiimpl.component.config.RelativeSide;
import fr.frinn.custommachinery.apiimpl.component.config.SideMode;
import fr.frinn.custommachinery.common.component.ItemMachineComponent;
import fr.frinn.custommachinery.common.component.config.SidedItemHandler;
import fr.frinn.custommachinery.common.init.Registration;
import fr.frinn.custommachinery.common.util.CMCollectors;
import fr.frinn.custommachinery.common.util.Utils;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.items.CapabilityItemHandler;
import net.minecraftforge.items.IItemHandler;
import org.jetbrains.annotations.Nullable;

import javax.annotation.Nonnull;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Random;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Consumer;
import java.util.function.Predicate;

public class ItemComponentHandler extends AbstractComponentHandler<ItemMachineComponent> implements ICapabilityComponent, ISerializableComponent, ITickableComponent, ISyncableStuff {

    private final IItemHandler generalHandler = new SidedItemHandler(null, this);
    private final LazyOptional<IItemHandler> capability = LazyOptional.of(() -> generalHandler);
    private final Map<Direction, IItemHandler> sidedHandlers = Maps.newEnumMap(Direction.class);
    private final Map<Direction, LazyOptional<IItemHandler>> sidedWrappers = Maps.newEnumMap(Direction.class);
    private final Random rand = new Random();
    private final List<ITickableComponentVariant> tickableVariants;

    public ItemComponentHandler(IMachineComponentManager manager, List<ItemMachineComponent> components) {
        super(manager, components);
        components.forEach(component -> {
            component.getConfig().setCallback(this::configChanged);
            if(component.getMode().isInput())
                this.inputs.add(component);
            if(component.getMode().isOutput())
                this.outputs.add(component);
        });
        this.tickableVariants = components.stream().map(ItemMachineComponent::getVariant).filter(variant -> variant instanceof ITickableComponentVariant).map(variant -> (ITickableComponentVariant)variant).collect(CMCollectors.toImmutableList());
        for(Direction direction : Direction.values()) {
            IItemHandler handler = new SidedItemHandler(direction, this);
            this.sidedHandlers.put(direction, handler);
            this.sidedWrappers.put(direction, LazyOptional.of(() -> handler));
        }
    }

    private void configChanged(RelativeSide side, SideMode oldMode, SideMode newMode) {
        if(oldMode.isNone() != newMode.isNone()) {
            Direction direction = side.getDirection(getManager().getTile().getBlockState().getValue(BlockStateProperties.HORIZONTAL_FACING));
            this.sidedWrappers.get(direction).invalidate();
            this.sidedWrappers.put(direction, LazyOptional.of(() -> this.sidedHandlers.get(direction)));
            if(oldMode.isNone())
                getManager().getLevel().updateNeighborsAt(getManager().getTile().getBlockPos(), Registration.CUSTOM_MACHINE_BLOCK.get());
        }
    }

    @Override
    public MachineComponentType<ItemMachineComponent> getType() {
        return Registration.ITEM_MACHINE_COMPONENT.get();
    }

    @Override
    public <T> LazyOptional<T> getCapability(@Nonnull Capability<T> cap, @Nullable Direction side) {
        if(cap == CapabilityItemHandler.ITEM_HANDLER_CAPABILITY) {
            if(side == null)
                return this.capability.cast();
            else if(getComponents().stream().anyMatch(component -> !component.getConfig().getSideMode(side).isNone()))
                return this.sidedWrappers.get(side).cast();
        }
        return LazyOptional.empty();
    }

    @Override
    public void invalidateCapability() {
        this.capability.invalidate();
        this.sidedWrappers.values().forEach(LazyOptional::invalidate);
    }

    @Override
    public Optional<ItemMachineComponent> getComponentForID(String id) {
        return this.getComponents().stream().filter(component -> component.getId().equals(id)).findFirst();
    }

    @Override
    public void serialize(CompoundTag nbt) {
        ListTag components = new ListTag();
        this.getComponents().forEach(component -> {
            CompoundTag componentNBT = new CompoundTag();
            component.serialize(componentNBT);
            componentNBT.putString("slotID", component.getId());
            components.add(componentNBT);
        });
        nbt.put("items", components);
    }

    @Override
    public void deserialize(CompoundTag nbt) {
        if(nbt.contains("items", Tag.TAG_LIST)) {
            ListTag components = nbt.getList("items", Tag.TAG_COMPOUND);
            components.forEach(inbt -> {
                if (inbt instanceof CompoundTag componentNBT) {
                    if(componentNBT.contains("slotID", Tag.TAG_STRING)) {
                        this.getComponents().stream().filter(component -> component.getId().equals(componentNBT.getString("slotID"))).findFirst().ifPresent(component -> component.deserialize(componentNBT));
                    }
                }
            });
        }
    }

    @Override
    public void serverTick() {
        this.tickableVariants.forEach(variant -> variant.tick(getManager()));
    }

    @Override
    public void getStuffToSync(Consumer<ISyncable<?, ?>> container) {
        this.getComponents().forEach(component -> component.getStuffToSync(container));
    }

    /** RECIPE STUFF **/

    private final List<ItemMachineComponent> inputs = new ArrayList<>();
    private final List<ItemMachineComponent> outputs = new ArrayList<>();

    public int getItemAmount(String slot, Item item, @Nullable CompoundTag nbt) {
        Predicate<ItemMachineComponent> nbtPredicate = component -> nbt == null || nbt.isEmpty() || (component.getItemStack().getTag() != null && Utils.testNBT(component.getItemStack().getTag(), nbt));
        Predicate<ItemMachineComponent> slotPredicate = component -> slot.isEmpty() || component.getId().equals(slot);
        return this.inputs.stream().filter(component -> component.getItemStack().getItem() == item && nbtPredicate.test(component) && slotPredicate.test(component))
                .mapToInt(component -> component.getItemStack().getCount())
                .sum();
    }

    public int getDurabilityAmount(String slot, Item item, @Nullable CompoundTag nbt) {
        Predicate<ItemMachineComponent> nbtPredicate = component -> nbt == null || nbt.isEmpty() || (component.getItemStack().getTag() != null && Utils.testNBT(component.getItemStack().getTag(), nbt));
        Predicate<ItemMachineComponent> slotPredicate = component -> slot.isEmpty() || component.getId().equals(slot);
        return this.inputs.stream().filter(component -> component.getItemStack().getItem() == item && component.getItemStack().isDamageableItem() && nbtPredicate.test(component) && slotPredicate.test(component))
                .mapToInt(component -> component.getItemStack().getMaxDamage() - component.getItemStack().getDamageValue())
                .sum();
    }

    public int getSpaceForItem(String slot, Item item, @Nullable CompoundTag nbt) {
        int maxStackSize = Utils.makeItemStack(item, 1, nbt).getMaxStackSize();
        return this.outputs.stream().filter(component -> canPlaceOutput(component, slot, item, nbt))
                .mapToInt(component -> {
                    if(component.getItemStack().isEmpty())
                        return Math.min(component.getCapacity(), maxStackSize);
                    else
                        return Math.min(component.getCapacity() - component.getItemStack().getCount(), maxStackSize - component.getItemStack().getCount());
                })
                .sum();
    }

    private boolean canPlaceOutput(ItemMachineComponent component, @Nullable String slot, Item item, @Nullable CompoundTag nbt) {
        ItemStack stack = Utils.makeItemStack(item, 1, nbt);

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
        if(component.getItemStack().getItem() != item)
            return false;

        //Check if the stack present in the slot can accept more items
        if(component.getItemStack().getCount() >= Math.min(stack.getMaxStackSize(), component.getCapacity()))
            return false;

        //Check if both items can be merged, using vanilla method
        return ItemStack.isSameItemSameTags(component.getItemStack(), stack);
    }

    public int getSpaceForDurability(String slot, Item item, @Nullable CompoundTag nbt) {
        Predicate<ItemMachineComponent> nbtPredicate = component -> nbt == null || nbt.isEmpty() || (component.getItemStack().getTag() != null && Utils.testNBT(component.getItemStack().getTag(), nbt));
        Predicate<ItemMachineComponent> slotPredicate = component -> slot.isEmpty() || component.getId().equals(slot);
        return this.inputs.stream().filter(component -> component.getItemStack().getItem() == item && component.getItemStack().isDamageableItem() && nbtPredicate.test(component) && slotPredicate.test(component))
                .mapToInt(component -> component.getItemStack().getDamageValue())
                .sum();
    }

    public void removeFromInputs(String slot, Item item, int amount, @Nullable CompoundTag nbt) {
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

    public void removeDurability(String slot, Item item, int amount, @Nullable CompoundTag nbt, boolean canBreak) {
        AtomicInteger toRemove = new AtomicInteger(amount);
        Predicate<ItemMachineComponent> nbtPredicate = component -> nbt == null || nbt.isEmpty() || (component.getItemStack().getTag() != null && Utils.testNBT(component.getItemStack().getTag(), nbt));
        Predicate<ItemMachineComponent> slotPredicate = component -> slot.isEmpty() || component.getId().equals(slot);
        this.inputs.stream().filter(component -> component.getItemStack().getItem() == item && component.getItemStack().isDamageableItem() && nbtPredicate.test(component) && slotPredicate.test(component)).forEach(component -> {
            int maxRemove = Math.min(component.getItemStack().getMaxDamage() - component.getItemStack().getDamageValue(), toRemove.get());
            toRemove.addAndGet(-maxRemove);
            ItemStack stack = component.getItemStack();
            if(stack.hurt(maxRemove, rand, null) && canBreak) {
                stack.shrink(1);
                stack.setDamageValue(0);
            }
        });
        getManager().markDirty();
    }

    public void addToOutputs(String slot, Item item, int amount, @Nullable CompoundTag nbt) {
        AtomicInteger toAdd = new AtomicInteger(amount);
        this.outputs.stream().filter(component -> canPlaceOutput(component, slot, item, nbt)).forEach(component -> {
            int maxInsert = Math.min(component.getSpaceForItem(item.getDefaultInstance()), toAdd.get());
            toAdd.addAndGet(-maxInsert);
            ItemStack stack = new ItemStack(item, maxInsert);
            if(nbt != null && !nbt.isEmpty())
                stack.setTag(nbt.copy());
            component.insert(stack);
        });
        getManager().markDirty();
    }

    public void repairItem(String slot, Item item, int amount, @Nullable CompoundTag nbt) {
        AtomicInteger toRepair = new AtomicInteger(amount);
        Predicate<ItemMachineComponent> nbtPredicate = component -> nbt == null || nbt.isEmpty() || (component.getItemStack().getTag() != null && Utils.testNBT(component.getItemStack().getTag(), nbt));
        Predicate<ItemMachineComponent> slotPredicate = component -> slot.isEmpty() || component.getId().equals(slot);
        this.inputs.stream().filter(component -> component.getItemStack().getItem() == item && component.getItemStack().isDamageableItem() && nbtPredicate.test(component) && slotPredicate.test(component)).forEach(component -> {
            int maxRepair = Math.min(component.getItemStack().getDamageValue(), toRepair.get());
            toRepair.addAndGet(-maxRepair);
            component.getItemStack().setDamageValue(component.getItemStack().getDamageValue() - maxRepair);
        });
        getManager().markDirty();
    }
}