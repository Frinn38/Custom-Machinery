package fr.frinn.custommachinery.common.crafting;

import fr.frinn.custommachinery.api.codec.NamedCodec;
import fr.frinn.custommachinery.api.crafting.IProcessor;
import fr.frinn.custommachinery.api.crafting.IProcessorTemplate;
import fr.frinn.custommachinery.api.crafting.ProcessorType;
import fr.frinn.custommachinery.api.machine.MachineTile;
import fr.frinn.custommachinery.common.init.CMRegistration;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;

public record DummyProcessor(MachineTile tile) implements IProcessor {

    @Override
    public void tick() {

    }

    @Override
    public void reset() {

    }

    @Override
    public MachineTile tile() {
        throw new IllegalStateException("Trying to get machine tile from dummy processor");
    }

    @Override
    public void setMachineInventoryChanged() {

    }

    @Override
    public ProcessorType<DummyProcessor> getType() {
        return CMRegistration.DUMMY_PROCESSOR.get();
    }

    @Override
    public void serialize(ValueOutput output) {

    }

    @Override
    public void deserialize(ValueInput input) {

    }

    public static class Template implements IProcessorTemplate<DummyProcessor> {

        public static final NamedCodec<Template> CODEC = NamedCodec.unit(Template::new, "Dummy processor");

        @Override
        public ProcessorType<DummyProcessor> getType() {
            return CMRegistration.DUMMY_PROCESSOR.get();
        }

        @Override
        public DummyProcessor build(MachineTile tile) {
            return new DummyProcessor(tile);
        }
    }
}
