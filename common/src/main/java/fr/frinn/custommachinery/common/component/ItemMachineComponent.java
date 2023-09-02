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
import fr.frinn.custommachinery.common.init.Registration;
import fr.frinn.custommachinery.common.network.syncable.ItemStackSyncable;
import fr.frinn.custommachinery.common.network.syncable.SideConfigSyncable;
import fr.frinn.custommachinery.common.util.Utils;
import fr.frinn.custommachinery.common.util.ingredient.IIngredient;
import fr.frinn.custommachinery.impl.component.AbstractMachineComponent;
import fr.frinn.custommachinery.impl.component.config.SideConfig;
import fr.frinn.custommachinery.impl.component.variant.ItemComponentVariant;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import org.jetbrains.annotations.Nullable;

import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.function.Consumer;

public class ItemMachineComponent extends AbstractMachineComponent implements ISerializableComponent, ISyncableStuff, IComparatorInputComponent, IVariableComponent<ItemComponentVariant>, ISideConfigComponent {

    private final String id;
    private final int capacity;
    private final int maxInput;
    private final int maxOutput;
    private final List<IIngredient<Item>> filter;
    private final boolean whitelist;
    private ItemStack stack = ItemStack.EMPTY;
    private final ItemComponentVariant variant;
    private final SideConfig config;

    public ItemMachineComponent(IMachineComponentManager manager, ComponentIOMode mode, String id, int capacity, int maxInput, int maxOutput, List<IIngredient<Item>> filter, boolean whitelist, ItemComponentVariant variant, SideConfig.Template configTemplate) {
        super(manager, mode);
        this.id = id;
        this.capacity = capacity;
        this.maxInput = maxInput;
        this.maxOutput = maxOutput;
        this.filter = filter;
        this.whitelist = whitelist;
        this.variant = variant;
        this.config = configTemplate.build(this);
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
        return this.filter.stream().anyMatch(ingredient -> ingredient.test(stack.getItem())) == this.whitelist && this.variant.canAccept(getManager(), stack);
    }

    public int getRemainingSpace() {
        if(!this.stack.isEmpty())
            return this.capacity - this.stack.getCount();
        return this.capacity;
    }

    public ItemStack getItemStack() {
        return this.stack;
    }

    public int getCapacity() {
        return this.capacity;
    }

    public int insert(Item item, int amount, @Nullable CompoundTag nbt, boolean simulate) {
        return insert(item, amount, nbt, simulate, false);
    }

    public int insert(Item item, int amount, @Nullable CompoundTag nbt, boolean simulate, boolean byPassLimit) {
        if(amount <= 0 || item == Items.AIR || !isItemValid(Utils.makeItemStack(item, amount, nbt)))
            return 0;

        //Check the per-tick limit
        if(!byPassLimit)
            amount = Math.min(amount, this.maxInput);

        //Check the stack limit
        amount = Math.min(amount, this.stack.getMaxStackSize() - this.stack.getCount());

        //Check the slot capacity
        amount = Math.min(amount, this.capacity - this.stack.getCount());

        if(this.stack.isEmpty()) {
            if(!simulate) {
                this.stack = Utils.makeItemStack(item, amount, nbt);
                getManager().markDirty();
                getManager().getTile().getUpgradeManager().markDirty();
            }
            return amount;
        } else if(this.stack.getItem() == item && (this.stack.getTag() == null || this.stack.getTag().equals(nbt))){
            amount = Math.min(getRemainingSpace(), amount);
            if(!simulate) {
                this.stack.grow(amount);
                getManager().markDirty();
                getManager().getTile().getUpgradeManager().markDirty();
            }
            return amount;
        }
        return 0;
    }

    public ItemStack extract(int amount, boolean simulate) {
        return extract(amount, simulate, false);
    }

    public ItemStack extract(int amount, boolean simulate, boolean byPassLimit) {
        if(amount <= 0 || this.stack.isEmpty() || !this.variant.canOutput(getManager()))
            return ItemStack.EMPTY;

        if(!byPassLimit)
            amount = Math.min(amount, this.maxOutput);

        amount = Math.min(amount, this.stack.getCount());
        ItemStack removed = Utils.makeItemStack(this.stack.getItem(), amount, this.stack.getTag());
        if(!simulate) {
            this.stack.shrink(amount);
            getManager().markDirty();
            getManager().getTile().getUpgradeManager().markDirty();
        }
        return removed;
    }

    public void setItemStack(ItemStack stack) {
        this.stack = stack;
        getManager().markDirty();
        getManager().getTile().getUpgradeManager().markDirty();
    }

    @Override
    public void serialize(CompoundTag nbt) {
        if(!stack.isEmpty())
            stack.save(nbt);
        nbt.put("config", this.config.serialize());
    }

    @Override
    public void deserialize(CompoundTag nbt) {
        this.stack = ItemStack.of(nbt);
        if(nbt.contains("config"))
            this.config.deserialize(nbt.get("config"));
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

    public static class Template implements IMachineComponentTemplate<ItemMachineComponent> {

        public static final NamedCodec<ItemMachineComponent.Template> CODEC = NamedCodec.record(itemMachineComponentTemplate ->
                itemMachineComponentTemplate.group(
                        NamedCodec.STRING.fieldOf("id").forGetter(template -> template.id),
                        ComponentIOMode.CODEC.optionalFieldOf("mode", ComponentIOMode.BOTH).forGetter(template -> template.mode),
                        NamedCodec.INT.optionalFieldOf("capacity", 64).forGetter(template -> template.capacity),
                        NamedCodec.INT.optionalFieldOf("max_input").forGetter(template -> Optional.of(template.maxInput)),
                        NamedCodec.INT.optionalFieldOf("max_output").forGetter(template -> Optional.of(template.maxOutput)),
                        IIngredient.ITEM.listOf().optionalFieldOf("filter", Collections.emptyList()).forGetter(template -> template.filter),
                        NamedCodec.BOOL.optionalFieldOf("whitelist", false).forGetter(template -> template.whitelist),
                        IComponentVariant.codec(Registration.ITEM_MACHINE_COMPONENT).orElse(DefaultItemComponentVariant.INSTANCE).forGetter(template -> template.variant),
                        SideConfig.Template.CODEC.optionalFieldOf("config").forGetter(template -> Optional.of(template.config))
                ).apply(itemMachineComponentTemplate, (id, mode, capacity, maxInput, maxOutput, filter, whitelist, variant, config) ->
                        new Template(id, mode, capacity, maxInput.orElse(capacity), maxOutput.orElse(capacity), filter, whitelist, (ItemComponentVariant) variant, config.orElse(mode.getBaseConfig()))
                ), "Item machine component"
        );

        private final ComponentIOMode mode;
        private final String id;
        private final int capacity;
        private final int maxInput;
        private final int maxOutput;
        private final List<IIngredient<Item>> filter;
        private final boolean whitelist;
        private final ItemComponentVariant variant;
        private final SideConfig.Template config;

        public Template(String id, ComponentIOMode mode, int capacity, int maxInput, int maxOutput, List<IIngredient<Item>> filter, boolean whitelist, ItemComponentVariant variant, SideConfig.Template config) {
            this.mode = mode;
            this.id = id;
            this.capacity = capacity;
            this.maxInput = maxInput;
            this.maxOutput = maxOutput;
            this.filter = filter;
            this.whitelist = whitelist;
            this.variant = variant;
            this.config = config;
        }

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
            if(isInput != this.mode.isInput())
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
            return new ItemMachineComponent(manager, this.mode, this.id, this.capacity, this.maxInput, this.maxOutput, this.filter, this.whitelist, this.variant, this.config);
        }
    }
}
