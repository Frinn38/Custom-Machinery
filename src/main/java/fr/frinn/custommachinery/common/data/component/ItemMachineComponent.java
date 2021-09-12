package fr.frinn.custommachinery.common.data.component;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import fr.frinn.custommachinery.api.components.*;
import fr.frinn.custommachinery.api.network.ISyncable;
import fr.frinn.custommachinery.api.network.ISyncableStuff;
import fr.frinn.custommachinery.api.utils.CodecLogger;
import fr.frinn.custommachinery.common.init.Registration;
import fr.frinn.custommachinery.common.network.sync.ItemStackSyncable;
import fr.frinn.custommachinery.common.util.Codecs;
import fr.frinn.custommachinery.common.util.Ingredient;
import net.minecraft.inventory.Inventory;
import net.minecraft.inventory.container.Container;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.util.math.MathHelper;

import java.util.Collections;
import java.util.List;
import java.util.function.Consumer;
import java.util.function.Predicate;

public class ItemMachineComponent extends AbstractMachineComponent implements ISerializableComponent, ISyncableStuff, IComparatorInputComponent, IFilterComponent {

    private String id;
    private int capacity;
    private List<Ingredient.ItemIngredient> filter;
    private boolean whitelist;
    private ItemStack stack = ItemStack.EMPTY;
    private ItemComponentVariant variant;

    public ItemMachineComponent(IMachineComponentManager manager, ComponentIOMode mode, String id, int capacity, List<Ingredient.ItemIngredient> filter, boolean whitelist, ItemComponentVariant variant) {
        super(manager, mode);
        this.id = id;
        this.capacity = MathHelper.clamp(capacity, 0, 64);
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

    public ItemComponentVariant getVariant() {
        return this.variant;
    }

    public boolean isItemValid(ItemStack stack) {
        return this.filter.stream().anyMatch(ingredient -> ingredient.test(stack.getItem())) == this.whitelist && this.variant.isItemValid(this, stack);
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
        else if(ItemStack.areItemsEqual(stack, this.stack))
            return Math.min(stack.getMaxStackSize() - this.stack.getCount(), this.capacity - this.stack.getCount());
        else
            return 0;
    }

    public void insert(ItemStack stack) {
        if(this.stack.isEmpty())
            this.stack = stack;
        else if(this.stack.getItem() == stack.getItem() && (stack.getTag() == null || stack.getTag().isEmpty() || stack.getTag().equals(this.stack.getTag())))
            this.stack.grow(stack.getCount());
    }

    public void extract(int amount) {
        this.stack.shrink(amount);
    }

    public void setItemStack(ItemStack stack) {
        this.stack = stack;
    }

    @Override
    public void serialize(CompoundNBT nbt) {
        nbt.putString("slotID", this.id);
        if(!stack.isEmpty())
            stack.write(nbt);
    }

    @Override
    public void deserialize(CompoundNBT nbt) {
        this.stack = ItemStack.read(nbt);
    }

    @Override
    public void getStuffToSync(Consumer<ISyncable<?, ?>> container) {
        container.accept(ItemStackSyncable.create(() -> this.stack, stack -> this.stack = stack));
    }

    @Override
    public int getComparatorInput() {
        return Container.calcRedstoneFromInventory(new Inventory(this.stack));
    }

    @Override
    public Predicate<Object> getFilter() {
        return object -> {
            if(object instanceof ItemStack) {
                return isItemValid((ItemStack)object);
            }
            else if(object instanceof List) {
                return ((List<Object>)object).stream().allMatch(o -> o instanceof ItemStack && isItemValid((ItemStack)o));
            }
            else return false;
        };
    }

    public static class Template implements IMachineComponentTemplate<ItemMachineComponent> {

        public static final Codec<ItemMachineComponent.Template> CODEC = RecordCodecBuilder.create(itemMachineComponentTemplate ->
                itemMachineComponentTemplate.group(
                        Codec.STRING.fieldOf("id").forGetter(template -> template.id),
                        CodecLogger.loggedOptional(Codecs.COMPONENT_MODE_CODEC,"mode", ComponentIOMode.BOTH).forGetter(template -> template.mode),
                        CodecLogger.loggedOptional(Codec.INT,"capacity", 64).forGetter(template -> template.capacity),
                        CodecLogger.loggedOptional(Codecs.list(Ingredient.ItemIngredient.CODEC),"filter", Collections.emptyList()).forGetter(template -> template.filter),
                        CodecLogger.loggedOptional(Codec.BOOL,"whitelist", false).forGetter(template -> template.whitelist),
                        CodecLogger.loggedOptional(Codecs.ITEM_COMPONENT_VARIANT_CODEC,"variant", ItemComponentVariant.DEFAULT).forGetter(template -> template.variant)
                ).apply(itemMachineComponentTemplate, Template::new)
        );

        private ComponentIOMode mode;
        private String id;
        private int capacity;
        private List<Ingredient.ItemIngredient> filter;
        private boolean whitelist;
        private ItemComponentVariant variant;

        public Template(String id, ComponentIOMode mode, int capacity, List<Ingredient.ItemIngredient> filter, boolean whitelist, ItemComponentVariant variant) {
            this.mode = mode;
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

        @Override
        public ItemMachineComponent build(IMachineComponentManager manager) {
            return new ItemMachineComponent(manager, this.mode, this.id, this.capacity, this.filter, this.whitelist, this.variant);
        }
    }
}
