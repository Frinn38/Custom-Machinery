package fr.frinn.custommachinery.common.data.component.handler;

import fr.frinn.custommachinery.common.crafting.CraftingManager;
import fr.frinn.custommachinery.common.data.component.*;
import fr.frinn.custommachinery.common.init.Registration;
import fr.frinn.custommachinery.common.network.sync.ISyncable;
import fr.frinn.custommachinery.common.network.sync.ISyncableStuff;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.nbt.ListNBT;
import net.minecraft.util.Direction;
import net.minecraftforge.common.ForgeHooks;
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
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Consumer;

public class ItemComponentHandler extends AbstractComponentHandler<ItemMachineComponent> implements IItemHandler, ICapabilityMachineComponent, IComponentSerializable, ITickableMachineComponent, ISyncableStuff {

    private LazyOptional<IItemHandler> capability = LazyOptional.of(() -> this);

    public ItemComponentHandler(MachineComponentManager manager) {
        super(manager);
    }

    @Override
    public MachineComponentType getType() {
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

    public Optional<ItemMachineComponent> getComponentForId(String id) {
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
    public void tick() {
        this.getComponents().stream().filter(component -> component.isFuelSlot() && component.getItemStack() != ItemStack.EMPTY && ForgeHooks.getBurnTime(component.getItemStack()) > 0).forEach(component -> {
            if(getManager().getTile().fuelManager.getFuel() == 0 && getManager().getTile().craftingManager.getStatus() == CraftingManager.STATUS.RUNNING) {
                getManager().getTile().fuelManager.addFuel(ForgeHooks.getBurnTime(component.getItemStack()));
                component.extract(1);
            }
        });
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
        int maxInsert = component.getSpaceForItem(stack);
        int toInsert = Math.min(maxInsert, stack.getCount());
        if(!simulate) {
            component.insert(stack.getItem(), toInsert);
            getManager().markDirty();
        }
        return new ItemStack(stack.getItem(), stack.getCount() - toInsert);
    }

    @Nonnull
    @Override
    public ItemStack extractItem(int index, int amount, boolean simulate) {
        ItemMachineComponent component = this.getComponents().get(index);
        int maxExtract = component.getItemStack().isEmpty() ? 0 : Math.min(component.getItemStack().getCount(), amount);
        if(!simulate) {
            component.extract(maxExtract);
            getManager().markDirty();
        }
        return new ItemStack(component.getItemStack().getItem(), maxExtract);
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

    public int getItemAmount(Item item) {
        AtomicInteger amount = new AtomicInteger();
        this.inputs.stream().filter(component -> component.getItemStack().getItem() == item).forEach(component -> amount.addAndGet(component.getItemStack().getCount()));
        return amount.get();
    }

    public int getSpaceForItem(Item item) {
        int maxStackSize = item.getDefaultInstance().getMaxStackSize();
        AtomicInteger space = new AtomicInteger();
        this.outputs.stream().filter(component -> (component.getItemStack().getItem() == item && component.getItemStack().getCount() < Math.min(maxStackSize, component.getCapacity())) || component.getItemStack().isEmpty()).forEach(component -> {
            if(component.getItemStack().isEmpty())
                space.addAndGet(Math.min(component.getCapacity(), maxStackSize));
            else
                space.addAndGet(Math.min(component.getCapacity() - component.getItemStack().getCount(), maxStackSize - component.getItemStack().getCount()));
        });
        return space.get();
    }

    public void removeFromInputs(Item item, int amount) {
        AtomicInteger toRemove = new AtomicInteger(amount);
        this.inputs.stream().filter(component -> component.getItemStack().getItem() == item).forEach(component -> {
            int maxExtract = Math.min(component.getItemStack().getCount(), toRemove.get());
            toRemove.addAndGet(-maxExtract);
            component.extract(maxExtract);
        });
        getManager().markDirty();
    }

    public void addToOutputs(Item item, int amount) {
        AtomicInteger toAdd = new AtomicInteger(amount);
        this.outputs.stream().filter(component -> (component.getItemStack().getItem() == item && component.getSpaceForItem(item.getDefaultInstance()) > 0) || component.getItemStack().isEmpty()).forEach(component -> {
            int maxInsert = Math.min(component.getSpaceForItem(item.getDefaultInstance()), toAdd.get());
            toAdd.addAndGet(-maxInsert);
            component.insert(item, maxInsert);
        });
        getManager().markDirty();
    }
}
