package fr.frinn.custommachinery.common.component;

import fr.frinn.custommachinery.api.codec.NamedCodec;
import fr.frinn.custommachinery.api.component.ComponentIOMode;
import fr.frinn.custommachinery.api.component.IMachineComponentManager;
import fr.frinn.custommachinery.api.component.IMachineComponentTemplate;
import fr.frinn.custommachinery.api.component.ISerializableComponent;
import fr.frinn.custommachinery.api.component.MachineComponentId;
import fr.frinn.custommachinery.api.component.MachineComponentType;
import fr.frinn.custommachinery.api.network.ISyncableStuff;
import fr.frinn.custommachinery.common.init.CMRegistration;
import fr.frinn.custommachinery.common.util.PartialBlockState;
import fr.frinn.custommachinery.common.util.Utils;
import fr.frinn.custommachinery.impl.component.AbstractMachineComponent;
import fr.frinn.custommachinery.impl.network.SyncableList;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;

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
        this.getManager().setChanged();
        if(this.currentDurability <= 0)
            switch (this.onBreak) {
                case BREAK -> this.getManager().getLevel().destroyBlock(this.getManager().getTile().getBlockPos(), true);
                case DESTROY -> this.getManager().getLevel().destroyBlock(this.getManager().getTile().getBlockPos(), false);
                case REPLACE_BREAK -> {
                    this.getManager().getLevel().destroyBlock(this.getManager().getTile().getBlockPos(), true);
                    Utils.setBlock(this.getManager().getLevel(), this.getManager().getTile().getBlockPos(), this.replace);
                }
                case REPLACE_DESTROY -> {
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

    @Override
    public MachineComponentType<DurabilityMachineComponent> getType() {
        return CMRegistration.DURABILITY_MACHINE_COMPONENT.get();
    }

    @Override
    public MachineComponentId<DurabilityMachineComponent> getId() {
        return this.getType().componentId("");
    }

    @Override
    public void serialize(ValueOutput output) {
        output.putInt("durability", this.currentDurability);
    }

    @Override
    public void deserialize(ValueInput input) {
        input.getInt("durability").ifPresent(durability -> this.currentDurability = durability);
    }

    @Override
    public void getStuffToSync(SyncableList list) {
        list.syncInt(() -> this.currentDurability, durability -> this.currentDurability = durability);
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
            return CMRegistration.DURABILITY_MACHINE_COMPONENT.get();
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
