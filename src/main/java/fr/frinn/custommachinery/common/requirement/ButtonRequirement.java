package fr.frinn.custommachinery.common.requirement;

import fr.frinn.custommachinery.api.codec.NamedCodec;
import fr.frinn.custommachinery.api.component.MachineComponentType;
import fr.frinn.custommachinery.api.crafting.ICraftingContext;
import fr.frinn.custommachinery.api.crafting.IRequirementList;
import fr.frinn.custommachinery.api.requirement.IRequirement;
import fr.frinn.custommachinery.api.requirement.RequirementIOMode;
import fr.frinn.custommachinery.api.requirement.RequirementType;
import fr.frinn.custommachinery.common.component.DataMachineComponent;
import fr.frinn.custommachinery.common.init.Registration;

public record ButtonRequirement(String id, boolean inverse) implements IRequirement<DataMachineComponent> {

    public static final NamedCodec<ButtonRequirement> CODEC = NamedCodec.record(buttonRequirementInstance ->
            buttonRequirementInstance.group(
                    NamedCodec.STRING.fieldOf("id").forGetter(requirement -> requirement.id),
                    NamedCodec.BOOL.optionalFieldOf("inverse", false).forGetter(requirement -> requirement.inverse)
            ).apply(buttonRequirementInstance, ButtonRequirement::new), "Button requirement"
    );

    @Override
    public RequirementType<ButtonRequirement> getType() {
        return Registration.BUTTON_REQUIREMENT.get();
    }

    @Override
    public MachineComponentType<DataMachineComponent> getComponentType() {
        return Registration.DATA_MACHINE_COMPONENT.get();
    }

    @Override
    public RequirementIOMode getMode() {
        return RequirementIOMode.INPUT;
    }

    @Override
    public boolean test(DataMachineComponent component, ICraftingContext context) {
        return component.getData().getBoolean(this.id) == !this.inverse;
    }

    @Override
    public void gatherRequirements(IRequirementList<DataMachineComponent> list) {
        //Only check if the recipe can start, no need for further checks
    }
}
