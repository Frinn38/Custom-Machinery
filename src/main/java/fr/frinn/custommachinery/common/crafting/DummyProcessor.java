package fr.frinn.custommachinery.common.crafting;

import fr.frinn.custommachinery.api.codec.NamedCodec;
import fr.frinn.custommachinery.api.crafting.ICraftingContext;
import fr.frinn.custommachinery.api.crafting.IProcessor;
import fr.frinn.custommachinery.api.crafting.IProcessorTemplate;
import fr.frinn.custommachinery.api.crafting.ProcessorType;
import fr.frinn.custommachinery.api.machine.MachineTile;
import fr.frinn.custommachinery.common.init.Registration;
import net.minecraft.nbt.CompoundTag;
import org.jetbrains.annotations.Nullable;

public class DummyProcessor implements IProcessor {

    private final MachineTile tile;

    public DummyProcessor(MachineTile tile) {
        this.tile = tile;
    }

    @Override
    public void tick() {

    }

    @Override
    public void reset() {

    }

    @Override
    public MachineTile getTile() {
        return null;
    }

    @Override
    public double getRecipeProgressTime() {
        return 0;
    }

    @Override
    public void setMachineInventoryChanged() {

    }

    @Nullable
    @Override
    public ICraftingContext getCurrentContext() {
        return null;
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
