package fr.frinn.custommachinery.common.requirement;

import fr.frinn.custommachinery.api.codec.NamedCodec;
import fr.frinn.custommachinery.api.component.MachineComponentType;
import fr.frinn.custommachinery.api.crafting.CraftingResult;
import fr.frinn.custommachinery.api.crafting.ICraftingContext;
import fr.frinn.custommachinery.api.requirement.ITickableRequirement;
import fr.frinn.custommachinery.api.requirement.RequirementIOMode;
import fr.frinn.custommachinery.api.requirement.RequirementType;
import fr.frinn.custommachinery.common.component.ChunkloadMachineComponent;
import fr.frinn.custommachinery.common.init.Registration;
import fr.frinn.custommachinery.impl.requirement.AbstractRequirement;
import net.minecraft.server.level.ServerLevel;

public class ChunkloadRequirement extends AbstractRequirement<ChunkloadMachineComponent> implements ITickableRequirement<ChunkloadMachineComponent> {

    public static final NamedCodec<ChunkloadRequirement> CODEC = NamedCodec.record(chunkloadRequirementInstance ->
            chunkloadRequirementInstance.group(
                    NamedCodec.intRange(1, 32).optionalFieldOf("radius", 1).forGetter(requirement -> requirement.radius)
            ).apply(chunkloadRequirementInstance, ChunkloadRequirement::new), "Chunkload requirement"
    );

    private final int radius;

    public ChunkloadRequirement(int radius) {
        super(RequirementIOMode.OUTPUT);
        this.radius = radius;
    }

    @Override
    public RequirementType<ChunkloadRequirement> getType() {
        return Registration.CHUNKLOAD_REQUIREMENT.get();
    }

    @Override
    public MachineComponentType<ChunkloadMachineComponent> getComponentType() {
        return Registration.CHUNKLOAD_MACHINE_COMPONENT.get();
    }

    @Override
    public boolean test(ChunkloadMachineComponent component, ICraftingContext context) {
        return true;
    }

    @Override
    public CraftingResult processStart(ChunkloadMachineComponent component, ICraftingContext context) {
        return CraftingResult.pass();
    }

    @Override
    public CraftingResult processEnd(ChunkloadMachineComponent component, ICraftingContext context) {
        return CraftingResult.pass();
    }

    @Override
    public CraftingResult processTick(ChunkloadMachineComponent component, ICraftingContext context) {
        component.setActiveWithTempo((ServerLevel) context.getMachineTile().getLevel(), this.radius, 2);
        return CraftingResult.pass();
    }
}
