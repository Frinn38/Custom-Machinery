package fr.frinn.custommachinery.common.component;

import fr.frinn.custommachinery.api.codec.NamedCodec;
import fr.frinn.custommachinery.api.component.ComponentIOMode;
import fr.frinn.custommachinery.api.component.IComparatorInputComponent;
import fr.frinn.custommachinery.api.component.IMachineComponentManager;
import fr.frinn.custommachinery.api.component.IMachineComponentTemplate;
import fr.frinn.custommachinery.api.component.ISerializableComponent;
import fr.frinn.custommachinery.api.component.ISideConfigComponent;
import fr.frinn.custommachinery.api.component.IVariableComponent;
import fr.frinn.custommachinery.api.component.MachineComponentType;
import fr.frinn.custommachinery.api.component.variant.IComponentVariant;
import fr.frinn.custommachinery.api.network.ISyncable;
import fr.frinn.custommachinery.api.network.ISyncableStuff;
import fr.frinn.custommachinery.common.component.variant.item.DefaultItemComponentVariant;
import fr.frinn.custommachinery.common.component.variant.item.FilterItemComponentVariant;
import fr.frinn.custommachinery.common.component.variant.item.UpgradeItemComponentVariant;
import fr.frinn.custommachinery.common.init.Registration;
import fr.frinn.custommachinery.common.network.syncable.ItemStackSyncable;
import fr.frinn.custommachinery.common.network.syncable.SideConfigSyncable;
import fr.frinn.custommachinery.common.util.ingredient.IIngredient;
import fr.frinn.custommachinery.impl.component.AbstractMachineComponent;
import fr.frinn.custommachinery.impl.component.config.SideConfig;
import fr.frinn.custommachinery.impl.component.variant.ItemComponentVariant;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.items.IItemHandlerModifiable;

import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.function.Consumer;

public class ItemMachineComponent extends AbstractMachineComponent implements ISerializableComponent, ISyncableStuff, IComparatorInputComponent, IVariableComponent<ItemComponentVariant>, ISideConfigComponent, IItemHandlerModifiable {

    private final String id;
    private final int capacity;
    private final int maxInput;
    private final int maxOutput;
    private final List<IIngredient<Item>> filter;
    private final boolean whitelist;
    private ItemStack stack = ItemStack.EMPTY;
    private final ItemComponentVariant variant;
    private final SideConfig config;
    private boolean locked;
    private boolean bypassLimit = false;

    public ItemMachineComponent(IMachineComponentManager manager, ComponentIOMode mode, String id, int capacity, int maxInput, int maxOutput, List<IIngredient<Item>> filter, boolean whitelist, ItemComponentVariant variant, SideConfig.Template configTemplate, boolean locked) {
        super(manager, mode);
        this.id = id;
        this.capacity = capacity;
        this.maxInput = maxInput;
        this.maxOutput = maxOutput;
        this.filter = filter;
        this.whitelist = whitelist;
        this.variant = variant;
        if(this.variant == FilterItemComponentVariant.INSTANCE)
            this.config = SideConfig.Template.DEFAULT_ALL_NONE_DISABLED.build(this);
        else
            this.config = configTemplate.build(this);
        this.locked = locked;
    }

    @Override
    public MachineComponentType<ItemMachineComponent> getType() {
        return Registration.ITEM_MACHINE_COMPONENT.get();
    }

    public String getId() {
        return this.id;
    }

    @Override
    public SideConfig getConfig() {
        return this.config;
    }

    @Override
    public ItemComponentVariant getVariant() {
        return this.variant;
    }

    public boolean isItemValid(ItemStack stack) {
        return this.isItemValid(0, stack);
    }

    public ItemStack getItemStack() {
        return this.stack;
    }

    public int getCapacity() {
        return this.capacity;
    }

    public void setItemStack(ItemStack stack) {
        this.stack = stack;
        getManager().markDirty();
        getManager().getTile().getUpgradeManager().markDirty();
    }

    public ItemStack insertItemBypassLimit(ItemStack stack, boolean simulate) {
        this.bypassLimit = true;
        ItemStack remainder = this.insertItem(0, stack, simulate);
        this.bypassLimit = false;
        return remainder;
    }

    public ItemStack extractItemBypassLimit(int amount, boolean simulate) {
        this.bypassLimit = true;
        ItemStack extracted = this.extractItem(0, amount, simulate);
        this.bypassLimit = false;
        return extracted;
    }

    public boolean isLocked() {
        return this.locked || this.variant == FilterItemComponentVariant.INSTANCE;
    }

    public void setLocked(boolean locked) {
        this.locked = locked;
    }

    @Override
    public void serialize(CompoundTag nbt, HolderLookup.Provider registries) {
        if(!this.stack.isEmpty())
            nbt.put("item", this.stack.save(registries, new CompoundTag()));
        nbt.put("config", this.config.serialize());
    }

    @Override
    public void deserialize(CompoundTag nbt, HolderLookup.Provider registries) {
        if(nbt.contains("item", Tag.TAG_COMPOUND))
            this.stack = ItemStack.parseOptional(registries, nbt.getCompound("item"));
        if(nbt.contains("config"))
            this.config.deserialize(nbt.getCompound("config"));
    }

    @Override
    public void getStuffToSync(Consumer<ISyncable<?, ?>> container) {
        container.accept(ItemStackSyncable.create(() -> this.stack, stack -> this.stack = stack));
        container.accept(SideConfigSyncable.create(this::getConfig, this.config::set));
    }

    @Override
    public int getComparatorInput() {
        return AbstractContainerMenu.getRedstoneSignalFromContainer(new SimpleContainer(this.stack));
    }

    @Override
    public int getSlots() {
        return 1;
    }

    @Override
    public ItemStack getStackInSlot(int slot) {
        return this.stack;
    }

    @Override
    public void setStackInSlot(int slot, ItemStack stack) {
        this.stack = stack;
        this.getManager().markDirty();
    }

    @Override
    public ItemStack insertItem(int slot, ItemStack stack, boolean simulate) {
        if(slot != 0)
            throw new IllegalArgumentException("Can't get capacity for slot " + slot + ", ItemMachineComponent only has slot 0");

        if(stack.isEmpty() || !isItemValid(stack) || (!this.stack.isEmpty() && !ItemStack.isSameItemSameComponents(this.stack, stack)))
            return stack;

        int amountToInsert = stack.getCount();

        //Check the per-tick limit
        if(!this.bypassLimit)
            amountToInsert = Math.min(amountToInsert, this.maxInput);

        //Check the inserted stack max size, in case a mod like AE2 try to insert a stack of non-stackable items
        amountToInsert = Math.min(amountToInsert, stack.getMaxStackSize());

        //Check the current stack limit (if not empty stack)
        if(!this.stack.isEmpty())
            amountToInsert = Math.min(amountToInsert, this.stack.getMaxStackSize() - this.stack.getCount());

        //Check the slot capacity
        amountToInsert = Math.min(amountToInsert, this.capacity - this.stack.getCount());

        //If nothing can be inserted return input
        if(amountToInsert <= 0)
            return stack;

        //If this slot is empty copy the input and insert the max amount
        if(this.stack.isEmpty()) {
            if(!simulate) {
                this.stack = stack.copyWithCount(amountToInsert);
                getManager().markDirty();
                if(this.variant == UpgradeItemComponentVariant.INSTANCE)
                    getManager().getTile().getUpgradeManager().markDirty();
            }
        } else {//If this slot is not empty simply grow the contained stack
            if(!simulate) {
                this.stack.grow(amountToInsert);
                getManager().markDirty();
                if(this.variant == UpgradeItemComponentVariant.INSTANCE)
                    getManager().getTile().getUpgradeManager().markDirty();
            }
        }

        //If everything from input was inserted return empty, else copy input and return remainder
        if(amountToInsert == stack.getCount())
            return ItemStack.EMPTY;
        else
            return stack.copyWithCount(stack.getCount() - amountToInsert);
    }

    @Override
    public ItemStack extractItem(int slot, int amount, boolean simulate) {
        if(slot != 0)
            throw new IllegalArgumentException("Can't get capacity for slot " + slot + ", ItemMachineComponent only has slot 0");

        if(amount <= 0 || this.stack.isEmpty() || !this.variant.canOutput(getManager()))
            return ItemStack.EMPTY;

        //Check output limit
        if(!this.bypassLimit)
            amount = Math.min(amount, this.maxOutput);

        //Check current stack size
        amount = Math.min(amount, this.stack.getCount());

        ItemStack extracted = this.stack.copyWithCount(amount);

        if(!simulate) {
            this.stack.shrink(amount);
            getManager().markDirty();
            if(this.variant == UpgradeItemComponentVariant.INSTANCE)
                getManager().getTile().getUpgradeManager().markDirty();
        }
        return extracted;
    }

    @Override
    public int getSlotLimit(int slot) {
        if(slot == 0)
            return this.capacity;
        throw new IllegalArgumentException("Can't get capacity for slot " + slot + ", ItemMachineComponent only has slot 0");
    }

    @Override
    public boolean isItemValid(int slot, ItemStack stack) {
        return this.filter.stream().anyMatch(ingredient -> ingredient.test(stack.getItem())) == this.whitelist && this.variant.canAccept(getManager(), stack);
    }


    public record Template(String id,
                           ComponentIOMode mode,
                           int capacity,
                           int maxInput,
                           int maxOutput,
                           List<IIngredient<Item>> filter,
                           boolean whitelist,
                           ItemComponentVariant variant,
                           SideConfig.Template config,
                           boolean locked
    ) implements IMachineComponentTemplate<ItemMachineComponent> {

        public static final NamedCodec<ItemMachineComponent.Template> CODEC = NamedCodec.record(itemMachineComponentTemplate ->
                itemMachineComponentTemplate.group(
                        NamedCodec.STRING.fieldOf("id").forGetter(template -> template.id),
                        ComponentIOMode.CODEC.optionalFieldOf("mode", ComponentIOMode.BOTH).forGetter(template -> template.mode),
                        NamedCodec.INT.optionalFieldOf("capacity", 64).forGetter(template -> template.capacity),
                        NamedCodec.INT.optionalFieldOf("max_input").forGetter(template -> template.maxInput == template.capacity ? Optional.empty() : Optional.of(template.maxInput)),
                        NamedCodec.INT.optionalFieldOf("max_output").forGetter(template -> template.maxOutput == template.capacity ? Optional.empty() : Optional.of(template.maxOutput)),
                        IIngredient.ITEM.listOf().optionalFieldOf("filter", Collections.emptyList()).forGetter(template -> template.filter),
                        NamedCodec.BOOL.optionalFieldOf("whitelist", false).forGetter(template -> template.whitelist),
                        IComponentVariant.codec(Registration.ITEM_MACHINE_COMPONENT).orElse(DefaultItemComponentVariant.INSTANCE).forGetter(template -> template.variant),
                        SideConfig.Template.CODEC.optionalFieldOf("config").forGetter(template -> template.config == template.mode.getBaseConfig() ? Optional.empty() : Optional.of(template.config)),
                        NamedCodec.BOOL.optionalFieldOf("locked", false).aliases("lock").forGetter(template -> template.locked)
                ).apply(itemMachineComponentTemplate, (id, mode, capacity, maxInput, maxOutput, filter, whitelist, variant, config, locked) ->
                        new Template(id, mode, capacity, maxInput.orElse(capacity), maxOutput.orElse(capacity), filter, whitelist, (ItemComponentVariant) variant, config.orElse(mode.getBaseConfig()), locked)
                ), "Item machine component"
        );

        public ItemComponentVariant getVariant() {
            return this.variant;
        }

        @Override
        public MachineComponentType<ItemMachineComponent> getType() {
            return Registration.ITEM_MACHINE_COMPONENT.get();
        }

        @Override
        public String getId() {
            return this.id;
        }

        @Override
        public boolean canAccept(Object ingredient, boolean isInput, IMachineComponentManager manager) {
            if(this.mode != ComponentIOMode.BOTH && isInput != this.mode.isInput())
                return false;
            if(ingredient instanceof ItemStack stack)
                return this.filter.stream().flatMap(i -> i.getAll().stream()).anyMatch(i -> i == stack.getItem()) == this.whitelist && this.variant.canAccept(manager, stack);
            else if(ingredient instanceof List<?> list) {
                return list.stream().allMatch(object -> {
                    if(object instanceof ItemStack stack)
                        return this.filter.stream().flatMap(i -> i.getAll().stream()).anyMatch(i -> i == stack.getItem()) == this.whitelist && this.variant.canAccept(manager, stack);
                    return false;
                });
            }
            return false;
        }

        @Override
        public ItemMachineComponent build(IMachineComponentManager manager) {
            return new ItemMachineComponent(manager, this.mode, this.id, this.capacity, this.maxInput, this.maxOutput, this.filter, this.whitelist, this.variant, this.config, this.locked);
        }
    }
}
