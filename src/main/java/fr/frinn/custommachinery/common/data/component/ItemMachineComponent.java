package fr.frinn.custommachinery.common.data.component;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import fr.frinn.custommachinery.common.init.Registration;
import mcjty.theoneprobe.api.IProbeInfo;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.util.Direction;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.registry.Registry;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.util.LazyOptional;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;

public class ItemMachineComponent extends AbstractMachineComponent {

    private String id;
    private int capacity;
    private List<Item> filter;
    private ItemStack stack = ItemStack.EMPTY;

    public ItemMachineComponent(MachineComponentManager manager, Mode mode, String id, int capacity, List<Item> filter) {
        super(manager, mode);
        this.id = id;
        this.capacity = MathHelper.clamp(capacity, 0, 64);
        this.filter = filter;
    }

    @Override
    public MachineComponentType<ItemMachineComponent> getType() {
        return Registration.ITEM_MACHINE_COMPONENT.get();
    }

    public String getId() {
        return this.id;
    }

    public boolean isItemValid(ItemStack stack) {
        return (this.filter.isEmpty() || this.filter.contains(stack.getItem()) && (this.stack.isEmpty() || this.stack.isItemEqual(stack)));
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
        return Math.min(stack.getMaxStackSize() - this.stack.getCount(), this.capacity - this.stack.getCount());
    }

    public void insert(Item item, int amount) {
        if(this.stack.isEmpty())
            this.stack = new ItemStack(item, amount);
        else
            this.stack.grow(amount);
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
    public void addProbeInfo(IProbeInfo info) {
        info.item(this.stack);
    }

    public static class Template implements IMachineComponentTemplate<ItemMachineComponent> {

        @SuppressWarnings("deprecation")
        public static final Codec<ItemMachineComponent.Template> CODEC = RecordCodecBuilder.create(itemMachineComponentTemplate ->
                itemMachineComponentTemplate.group(
                        Codec.STRING.fieldOf("id").forGetter(template -> template.id),
                        Codec.INT.optionalFieldOf("capacity", 64).forGetter(template -> template.capacity),
                        Registry.ITEM.listOf().optionalFieldOf("filter", new ArrayList<>()).forGetter(template -> template.filter),
                        Codec.STRING.optionalFieldOf("mode", Mode.BOTH.toString()).forGetter(template -> template.mode.toString())
                ).apply(itemMachineComponentTemplate, (id, capacity, filter, mode) -> new Template(Mode.value(mode), id, capacity, filter))
        );

        private Mode mode;
        private String id;
        private int capacity;
        private List<Item> filter;

        public Template(Mode mode, String id, int capacity, List<Item> filter) {
            this.mode = mode;
            this.id = id;
            this.capacity = capacity;
            this.filter = filter;
        }

        @Override
        public MachineComponentType<ItemMachineComponent> getType() {
            return Registration.ITEM_MACHINE_COMPONENT.get();
        }

        @Override
        public ItemMachineComponent build(MachineComponentManager manager) {
            return new ItemMachineComponent(manager, mode, id, capacity, filter);
        }
    }
}
