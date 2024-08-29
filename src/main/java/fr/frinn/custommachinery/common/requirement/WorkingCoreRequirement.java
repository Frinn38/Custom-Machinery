package fr.frinn.custommachinery.common.requirement;

import fr.frinn.custommachinery.api.codec.NamedCodec;
import fr.frinn.custommachinery.api.component.MachineComponentType;
import fr.frinn.custommachinery.api.crafting.ICraftingContext;
import fr.frinn.custommachinery.api.crafting.IRequirementList;
import fr.frinn.custommachinery.api.requirement.IRequirement;
import fr.frinn.custommachinery.api.requirement.RequirementIOMode;
import fr.frinn.custommachinery.api.requirement.RequirementType;
import fr.frinn.custommachinery.common.component.WorkingCoreMachineComponent;
import fr.frinn.custommachinery.common.init.Registration;
import fr.frinn.custommachinery.impl.codec.DefaultCodecs;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.Nullable;

import java.util.Optional;

public record WorkingCoreRequirement(int core, @Nullable ResourceLocation recipe) implements IRequirement<WorkingCoreMachineComponent> {

    public static final NamedCodec<WorkingCoreRequirement> CODEC = NamedCodec.record(workingCoreRequirementInstance ->
            workingCoreRequirementInstance.group(
                    NamedCodec.intRange(0, Integer.MAX_VALUE).optionalFieldOf("core", 0).forGetter(WorkingCoreRequirement::core),
                    DefaultCodecs.RESOURCE_LOCATION.optionalFieldOf("recipe").forGetter(requirement -> Optional.ofNullable(requirement.recipe))
            ).apply(workingCoreRequirementInstance, (core, recipe) -> new WorkingCoreRequirement(core, recipe.orElse(null))), "Working core requirement"
    );

    @Override
    public RequirementType<WorkingCoreRequirement> getType() {
        return Registration.WORKING_CORE_REQUIREMENT.get();
    }

    @Override
    public MachineComponentType<WorkingCoreMachineComponent> getComponentType() {
        return Registration.WORKING_CORE_MACHINE_COMPONENT.get();
    }

    @Override
    public RequirementIOMode getMode() {
        return RequirementIOMode.INPUT;
    }

    @Override
    public boolean test(WorkingCoreMachineComponent component, ICraftingContext context) {
        return component.isCoreWorking(this.core, this.recipe, context.getCurrentCore()).isSuccess();
    }

    @Override
    public void gatherRequirements(IRequirementList<WorkingCoreMachineComponent> list) {
        list.worldCondition(((component, context) -> component.isCoreWorking(this.core, this.recipe, context.getCurrentCore())));
    }
}
