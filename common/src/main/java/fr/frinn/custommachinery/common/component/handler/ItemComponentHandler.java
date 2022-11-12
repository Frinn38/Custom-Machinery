package fr.frinn.custommachinery.common.component.handler;

import fr.frinn.custommachinery.PlatformHelper;
import fr.frinn.custommachinery.api.component.IMachineComponentManager;
import fr.frinn.custommachinery.api.component.ISerializableComponent;
import fr.frinn.custommachinery.api.component.ITickableComponent;
import fr.frinn.custommachinery.api.component.MachineComponentType;
import fr.frinn.custommachinery.api.component.variant.ITickableComponentVariant;
import fr.frinn.custommachinery.api.network.ISyncable;
import fr.frinn.custommachinery.api.network.ISyncableStuff;
import fr.frinn.custommachinery.common.component.ItemMachineComponent;
import fr.frinn.custommachinery.common.init.Registration;
import fr.frinn.custommachinery.common.util.Utils;
import fr.frinn.custommachinery.common.util.transfer.ICommonItemHandler;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Random;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Consumer;
import java.util.function.Predicate;

public class ItemComponentHandler extends AbstractComponentHandler<ItemMachineComponent> implements ISerializableComponent, ITickableComponent, ISyncableStuff {

    private final Random rand = new Random();
    private final List<ItemMachineComponent> tickableVariants;
    private final ICommonItemHandler handler = PlatformHelper.createItemHandler(this);

    public ItemComponentHandler(IMachineComponentManager manager, List<ItemMachineComponent> components) {
        super(manager, components);
        components.forEach(component -> {
            component.getConfig().setCallback(this.handler::configChanged);
            if(component.getMode().isInput())
                this.inputs.add(component);
            if(component.getMode().isOutput())
                this.outputs.add(component);
        });
        this.tickableVariants = components.stream().filter(component -> component.getVariant() instanceof ITickableComponentVariant).toList();
    }

    public ICommonItemHandler getCommonHandler() {
        return this.handler;
    }

    @Override
    public MachineComponentType<ItemMachineComponent> getType() {
        return Registration.ITEM_MACHINE_COMPONENT.get();
    }

    @Override
    public void onRemoved() {
        this.handler.invalidate();
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

    @SuppressWarnings("unchecked")
    @Override
    public void serverTick() {
        this.handler.tick();
        this.tickableVariants.forEach(component -> ((ITickableComponentVariant<ItemMachineComponent>)component.getVariant()).tick(component));
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
            component.extract(maxExtract, false, true);
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
            int maxInsert = Math.min(component.insert(item, amount, nbt, true, true), toAdd.get());
            toAdd.addAndGet(-maxInsert);
            component.insert(item, maxInsert, nbt, false, true);
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