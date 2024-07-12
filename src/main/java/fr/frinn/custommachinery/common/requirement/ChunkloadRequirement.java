package fr.frinn.custommachinery.common.requirement;

import fr.frinn.custommachinery.api.codec.NamedCodec;
import fr.frinn.custommachinery.api.component.MachineComponentType;
import fr.frinn.custommachinery.api.crafting.CraftingResult;
import fr.frinn.custommachinery.api.crafting.ICraftingContext;
import fr.frinn.custommachinery.api.crafting.IRequirementList;
import fr.frinn.custommachinery.api.requirement.IRequirement;
import fr.frinn.custommachinery.api.requirement.RequirementIOMode;
import fr.frinn.custommachinery.api.requirement.RequirementType;
import fr.frinn.custommachinery.common.component.ChunkloadMachineComponent;
import fr.frinn.custommachinery.common.init.Registration;
import net.minecraft.server.level.ServerLevel;

public record ChunkloadRequirement(int radius) implements IRequirement<ChunkloadMachineComponent> {

    public static final NamedCodec<ChunkloadRequirement> CODEC = NamedCodec.record(chunkloadRequirementInstance ->
            chunkloadRequirementInstance.group(
                    NamedCodec.intRange(1, 32).optionalFieldOf("radius", 1).forGetter(requirement -> requirement.radius)
            ).apply(chunkloadRequirementInstance, ChunkloadRequirement::new), "Chunkload requirement"
    );

    @Override
    public RequirementType<ChunkloadRequirement> getType() {
        return Registration.CHUNKLOAD_REQUIREMENT.get();
    }

    @Override
    public MachineComponentType<ChunkloadMachineComponent> getComponentType() {
        return Registration.CHUNKLOAD_MACHINE_COMPONENT.get();
    }

    @Override
    public RequirementIOMode getMode() {
        return RequirementIOMode.OUTPUT;
    }

    @Override
    public boolean test(ChunkloadMachineComponent component, ICraftingContext context) {
        return true;
    }

    @Override
    public void gatherRequirements(IRequirementList<ChunkloadMachineComponent> list) {
        list.processEachTick(((component, context) -> {
            component.setActiveWithTempo((ServerLevel) context.getMachineTile().getLevel(), this.radius, 2);
            return CraftingResult.success();
        }));
    }
}
