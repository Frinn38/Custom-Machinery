package fr.frinn.custommachinery.common.data.component;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import fr.frinn.custommachinery.api.codec.CodecLogger;
import fr.frinn.custommachinery.api.component.ComponentIOMode;
import fr.frinn.custommachinery.api.component.IComparatorInputComponent;
import fr.frinn.custommachinery.api.component.IMachineComponentManager;
import fr.frinn.custommachinery.api.component.IMachineComponentTemplate;
import fr.frinn.custommachinery.api.component.ISerializableComponent;
import fr.frinn.custommachinery.api.component.IVariableComponent;
import fr.frinn.custommachinery.api.component.MachineComponentType;
import fr.frinn.custommachinery.api.component.variant.IComponentVariant;
import fr.frinn.custommachinery.api.network.ISyncable;
import fr.frinn.custommachinery.api.network.ISyncableStuff;
import fr.frinn.custommachinery.apiimpl.component.AbstractMachineComponent;
import fr.frinn.custommachinery.apiimpl.component.variant.ItemComponentVariant;
import fr.frinn.custommachinery.common.data.component.variant.item.DefaultItemComponentVariant;
import fr.frinn.custommachinery.common.init.Registration;
import fr.frinn.custommachinery.common.network.syncable.ItemStackSyncable;
import fr.frinn.custommachinery.common.util.Codecs;
import fr.frinn.custommachinery.common.util.ingredient.IIngredient;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;

import java.util.Collections;
import java.util.List;
import java.util.function.Consumer;

public class ItemMachineComponent extends AbstractMachineComponent implements ISerializableComponent, ISyncableStuff, IComparatorInputComponent, IVariableComponent<ItemComponentVariant> {

    private final String id;
    private final int capacity;
    private final List<IIngredient<Item>> filter;
    private final boolean whitelist;
    private ItemStack stack = ItemStack.EMPTY;
    private final ItemComponentVariant variant;

    public ItemMachineComponent(IMachineComponentManager manager, ComponentIOMode mode, String id, int capacity, List<IIngredient<Item>> filter, boolean whitelist, ItemComponentVariant variant) {
        super(manager, mode);
        this.id = id;
        this.capacity = capacity;
        this.filter = filter;
        this.whitelist = whitelist;
        this.variant = variant;
    }

    @Override
    public MachineComponentType<ItemMachineComponent> getType() {
        return Registration.ITEM_MACHINE_COMPONENT.get();
    }

    public String getId() {
        return this.id;
    }

    @Override
    public ItemComponentVariant getVariant() {
        return this.variant;
    }

    public boolean isItemValid(ItemStack stack) {
        return this.filter.stream().anyMatch(ingredient -> ingredient.test(stack.getItem())) == this.whitelist && this.variant.isItemValid(getManager(), stack);
    }

    public ItemStack getItemStack() {
        return this.stack;
    }

    public int getCapacity() {
        return this.capacity;
    }

    public int getSpaceForItem(ItemStack stack) {
        if(!this.isItemValid(stack))
            return 0;
        if(this.stack.isEmpty())
            return Math.min(stack.getMaxStackSize(), this.capacity);
        else if(ItemStack.isSame(stack, this.stack))
            return Math.min(stack.getMaxStackSize() - this.stack.getCount(), this.capacity - this.stack.getCount());
        else
            return 0;
    }

    public void insert(ItemStack stack) {
        if(this.stack.isEmpty())
            this.stack = stack;
        else if(this.stack.getItem() == stack.getItem() && (stack.getTag() == null || stack.getTag().isEmpty() || stack.getTag().equals(this.stack.getTag())))
            this.stack.grow(stack.getCount());
        getManager().markDirty();
    }

    public void extract(int amount) {
        this.stack.shrink(amount);
        getManager().markDirty();
    }

    public void setItemStack(ItemStack stack) {
        this.stack = stack;
        getManager().markDirty();
    }

    @Override
    public void serialize(CompoundTag nbt) {
        nbt.putString("slotID", this.id);
        if(!stack.isEmpty())
            stack.save(nbt);
    }

    @Override
    public void deserialize(CompoundTag nbt) {
        this.stack = ItemStack.of(nbt);
    }

    @Override
    public void getStuffToSync(Consumer<ISyncable<?, ?>> container) {
        container.accept(ItemStackSyncable.create(() -> this.stack, stack -> this.stack = stack));
    }

    @Override
    public int getComparatorInput() {
        return AbstractContainerMenu.getRedstoneSignalFromContainer(new SimpleContainer(this.stack));
    }

    public static class Template implements IMachineComponentTemplate<ItemMachineComponent> {

        public static final Codec<ItemMachineComponent.Template> CODEC = RecordCodecBuilder.create(itemMachineComponentTemplate ->
                itemMachineComponentTemplate.group(
                        Codec.STRING.fieldOf("id").forGetter(template -> template.id),
                        CodecLogger.loggedOptional(Codecs.COMPONENT_MODE_CODEC,"mode", ComponentIOMode.BOTH).forGetter(template -> template.mode),
                        CodecLogger.loggedOptional(Codec.INT,"capacity", 64).forGetter(template -> template.capacity),
                        CodecLogger.loggedOptional(Codecs.list(IIngredient.ITEM),"filter", Collections.emptyList()).forGetter(template -> template.filter),
                        CodecLogger.loggedOptional(Codec.BOOL,"whitelist", false).forGetter(template -> template.whitelist),
                        CodecLogger.loggedOptional(IComponentVariant.codec(Registration.ITEM_MACHINE_COMPONENT, ItemComponentVariant.class),"variant", DefaultItemComponentVariant.INSTANCE).forGetter(template -> template.variant)
                ).apply(itemMachineComponentTemplate, Template::new)
        );

        private final ComponentIOMode mode;
        private final String id;
        private final int capacity;
        private final List<IIngredient<Item>> filter;
        private final boolean whitelist;
        private final ItemComponentVariant variant;

        public Template(String id, ComponentIOMode mode, int capacity, List<IIngredient<Item>> filter, boolean whitelist, ItemComponentVariant variant) {
            this.mode = mode;
            this.id = id;
            this.capacity = capacity;
            this.filter = filter;
            this.whitelist = whitelist;
            this.variant = variant;
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
            if(ingredient instanceof ItemStack) {
                ItemStack stack = (ItemStack)ingredient;
                return this.filter.stream().flatMap(i -> i.getAll().stream()).anyMatch(i -> i == stack.getItem()) == this.whitelist && this.variant.isItemValid(manager, stack);
            } else if(ingredient instanceof List) {
                List<?> list = (List<?>)ingredient;
                return list.stream().allMatch(object -> {
                    if(object instanceof ItemStack) {
                        ItemStack stack = (ItemStack)object;
                        return this.filter.stream().flatMap(i -> i.getAll().stream()).anyMatch(i -> i == stack.getItem()) == this.whitelist && this.variant.isItemValid(manager, stack);
                    }
                    return false;
                });
            }
            return false;
        }

        @Override
        public ItemMachineComponent build(IMachineComponentManager manager) {
            return new ItemMachineComponent(manager, this.mode, this.id, this.capacity, this.filter, this.whitelist, this.variant);
        }
    }
}
