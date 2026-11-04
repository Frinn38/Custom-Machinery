package fr.frinn.custommachinery.common.component;

import fr.frinn.custommachinery.api.codec.NamedCodec;
import fr.frinn.custommachinery.api.component.ComponentIOMode;
import fr.frinn.custommachinery.api.component.IMachineComponentManager;
import fr.frinn.custommachinery.api.component.IMachineComponentTemplate;
import fr.frinn.custommachinery.api.component.ISerializableComponent;
import fr.frinn.custommachinery.api.component.MachineComponentType;
import fr.frinn.custommachinery.api.network.ISyncable;
import fr.frinn.custommachinery.api.network.ISyncableStuff;
import fr.frinn.custommachinery.common.component.item.ItemMachineComponent;
import fr.frinn.custommachinery.common.init.Registration;
import fr.frinn.custommachinery.common.network.syncable.IntegerSyncable;
import fr.frinn.custommachinery.common.util.PartialBlockState;
import fr.frinn.custommachinery.common.util.Utils;
import fr.frinn.custommachinery.impl.component.AbstractMachineComponent;
import net.minecraft.core.HolderLookup.Provider;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.world.level.block.Block;

import java.util.Collections;
import java.util.function.Consumer;
import java.util.function.Supplier;

public class DurabilityMachineComponent extends AbstractMachineComponent implements ISerializableComponent, ISyncableStuff {

    private final Supplier<Integer> maxDurability;
    private final boolean showItemDurability;
    private final OnBreak onBreak;
    private final PartialBlockState replace;

    private int currentDurability;

    public DurabilityMachineComponent(IMachineComponentManager manager, int maxDurability, boolean showItemDurability, OnBreak onBreak, PartialBlockState replace) {
        super(manager, ComponentIOMode.BOTH);
        this.maxDurability = this.upgradeableI(maxDurability, "", 1, Integer.MAX_VALUE, (newValue) -> this.currentDurability = Math.min(this.currentDurability, newValue));
        this.showItemDurability = showItemDurability;
        this.onBreak = onBreak;
        this.replace = replace;
        this.currentDurability = maxDurability;
    }

    public boolean shouldShowItemDurability() {
        return this.showItemDurability;
    }

    public int getDurability() {
        return this.currentDurability;
    }

    public int getMaxDurability() {
        return this.maxDurability.get();
    }

    public void setDurability(int durability) {
        this.currentDurability = durability;
        this.getManager().markDirty();
        if(this.currentDurability <= 0)
            switch (this.onBreak) {
                case BREAK -> this.getManager().getLevel().destroyBlock(this.getManager().getTile().getBlockPos(), true);
                case DESTROY -> {
                    if(this.getManager().getTile().getAppearance().shouldKeepInventory())
                        this.dropContent();
                    this.getManager().getLevel().destroyBlock(this.getManager().getTile().getBlockPos(), false);
                }
                case REPLACE_BREAK -> {
                    this.getManager().getLevel().destroyBlock(this.getManager().getTile().getBlockPos(), true);
                    Utils.setBlock(this.getManager().getLevel(), this.getManager().getTile().getBlockPos(), this.replace);
                }
                case REPLACE_DESTROY -> {
                    if(this.getManager().getTile().getAppearance().shouldKeepInventory())
                        this.dropContent();
                    this.getManager().getLevel().destroyBlock(this.getManager().getTile().getBlockPos(), false);
                    Utils.setBlock(this.getManager().getLevel(), this.getManager().getTile().getBlockPos(), this.replace);
                }
            }
    }

    public void repair(int amount) {
        this.setDurability(Math.clamp(this.currentDurability + amount, 0, this.maxDurability.get()));
    }

    public void damage(int amount) {
        this.setDurability(Math.clamp(this.currentDurability - amount, 0, this.maxDurability.get()));
    }

    private void dropContent() {
        this.getManager().getTile().getComponentManager().getComponentHandler(Registration.ITEM_MACHINE_COMPONENT.get())
                .map(handler -> handler.getComponents().stream()
                        .filter(ItemMachineComponent::shouldDrop)
                        .map(component -> component.getItemStack().copy())
                        .filter(stack -> !stack.isEmpty())
                        .toList()
                ).orElse(Collections.emptyList())
                .forEach(stack -> Block.popResource(this.getManager().getLevel(), this.getManager().getTile().getBlockPos(), stack));
    }

    @Override
    public MachineComponentType<DurabilityMachineComponent> getType() {
        return Registration.DURABILITY_MACHINE_COMPONENT.get();
    }

    @Override
    public void serialize(CompoundTag nbt, Provider registries) {
        nbt.putInt("durability", this.currentDurability);
    }

    @Override
    public void deserialize(CompoundTag nbt, Provider registries) {
        if(nbt.contains("durability", Tag.TAG_INT))
            this.currentDurability = Math.min(nbt.getInt("durability"), this.maxDurability.get());
    }

    @Override
    public void getStuffToSync(Consumer<ISyncable<?, ?>> container) {
        container.accept(IntegerSyncable.create(() -> this.currentDurability, durability -> this.currentDurability = durability));
    }

    public enum OnBreak {
        NOTHING,
        BREAK,
        DESTROY,
        REPLACE_BREAK,
        REPLACE_DESTROY
    }

    public record Template(int maxDurability, boolean showItemDurability, OnBreak onBreak, PartialBlockState replace) implements IMachineComponentTemplate<DurabilityMachineComponent> {

        public static final NamedCodec<Template> CODEC = NamedCodec.record(templateInstance ->
                templateInstance.group(
                        NamedCodec.intRange(1, Integer.MAX_VALUE).fieldOf("max").forGetter(Template::maxDurability),
                        NamedCodec.BOOL.optionalFieldOf("item", true).forGetter(Template::showItemDurability),
                        NamedCodec.enumCodec(OnBreak.class).optionalFieldOf("break", OnBreak.NOTHING).forGetter(Template::onBreak),
                        PartialBlockState.CODEC.optionalFieldOf("replace", PartialBlockState.AIR).forGetter(Template::replace)
                ).apply(templateInstance, Template::new), "Durability machine component template"
        );


        @Override
        public MachineComponentType<DurabilityMachineComponent> getType() {
            return Registration.DURABILITY_MACHINE_COMPONENT.get();
        }

        @Override
        public String getId() {
            return "";
        }

        @Override
        public boolean canAccept(Object ingredient, boolean isInput, IMachineComponentManager manager) {
            return false;
        }

        @Override
        public DurabilityMachineComponent build(IMachineComponentManager manager) {
            return new DurabilityMachineComponent(manager, this.maxDurability, this.showItemDurability, this.onBreak, this.replace);
        }
    }
}
