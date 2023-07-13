package fr.frinn.custommachinery.common.requirement;

import fr.frinn.custommachinery.api.codec.NamedCodec;
import fr.frinn.custommachinery.api.component.MachineComponentType;
import fr.frinn.custommachinery.api.crafting.CraftingResult;
import fr.frinn.custommachinery.api.crafting.ICraftingContext;
import fr.frinn.custommachinery.api.requirement.RequirementIOMode;
import fr.frinn.custommachinery.api.requirement.RequirementType;
import fr.frinn.custommachinery.common.component.DataMachineComponent;
import fr.frinn.custommachinery.common.init.Registration;
import fr.frinn.custommachinery.impl.requirement.AbstractRequirement;

public class ButtonRequirement extends AbstractRequirement<DataMachineComponent> {

    public static final NamedCodec<ButtonRequirement> CODEC = NamedCodec.record(buttonRequirementInstance ->
            buttonRequirementInstance.group(
                    NamedCodec.STRING.fieldOf("id").forGetter(requirement -> requirement.id),
                    NamedCodec.BOOL.optionalFieldOf("inverse", false).forGetter(requirement -> requirement.inverse)
            ).apply(buttonRequirementInstance, ButtonRequirement::new), "Button requirement"
    );

    private final String id;
    private final boolean inverse;

    public ButtonRequirement(String id, boolean inverse) {
        super(RequirementIOMode.INPUT);
        this.id = id;
        this.inverse = inverse;
    }

    @Override
    public RequirementType<ButtonRequirement> getType() {
        return Registration.BUTTON_REQUIREMENT.get();
    }

    @Override
    public MachineComponentType<DataMachineComponent> getComponentType() {
        return Registration.DATA_MACHINE_COMPONENT.get();
    }

    @Override
    public boolean test(DataMachineComponent component, ICraftingContext context) {
        return component.getData().getBoolean(this.id) == !this.inverse;
    }

    @Override
    public CraftingResult processStart(DataMachineComponent component, ICraftingContext context) {
        return CraftingResult.pass();
    }

    @Override
    public CraftingResult processEnd(DataMachineComponent component, ICraftingContext context) {
        return CraftingResult.pass();
    }
}
