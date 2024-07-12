package fr.frinn.custommachinery.common.crafting;

import fr.frinn.custommachinery.api.codec.NamedCodec;
import fr.frinn.custommachinery.api.crafting.IProcessor;
import fr.frinn.custommachinery.api.crafting.IProcessorTemplate;
import fr.frinn.custommachinery.api.crafting.ProcessorType;
import fr.frinn.custommachinery.api.machine.MachineTile;
import fr.frinn.custommachinery.common.init.Registration;
import net.minecraft.nbt.CompoundTag;

public record DummyProcessor(MachineTile tile) implements IProcessor {

    @Override
    public void tick() {

    }

    @Override
    public void reset() {

    }

    @Override
    public MachineTile tile() {
        return null;
    }

    @Override
    public void setMachineInventoryChanged() {

    }

    @Override
    public ProcessorType<DummyProcessor> getType() {
        return Registration.DUMMY_PROCESSOR.get();
    }

    @Override
    public CompoundTag serialize() {
        return new CompoundTag();
    }

    @Override
    public void deserialize(CompoundTag nbt) {

    }

    public static class Template implements IProcessorTemplate<DummyProcessor> {

        public static final NamedCodec<Template> CODEC = NamedCodec.unit(Template::new, "Dummy processor");

        @Override
        public ProcessorType<DummyProcessor> getType() {
            return Registration.DUMMY_PROCESSOR.get();
        }

        @Override
        public DummyProcessor build(MachineTile tile) {
            return new DummyProcessor(tile);
        }
    }
}
