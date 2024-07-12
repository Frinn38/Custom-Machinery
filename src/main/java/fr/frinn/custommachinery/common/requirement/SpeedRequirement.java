package fr.frinn.custommachinery.common.requirement;

import fr.frinn.custommachinery.api.codec.NamedCodec;
import fr.frinn.custommachinery.api.component.MachineComponentType;
import fr.frinn.custommachinery.api.crafting.ICraftingContext;
import fr.frinn.custommachinery.api.crafting.IRequirementList;
import fr.frinn.custommachinery.api.requirement.IRequirement;
import fr.frinn.custommachinery.api.requirement.RequirementIOMode;
import fr.frinn.custommachinery.api.requirement.RequirementType;
import fr.frinn.custommachinery.common.init.Registration;
import fr.frinn.custommachinery.impl.component.AbstractMachineComponent;

public class SpeedRequirement implements IRequirement<AbstractMachineComponent> {

    public static final NamedCodec<SpeedRequirement> CODEC = NamedCodec.unit(SpeedRequirement::new, "Speed requirement");

    @Override
    public RequirementType<SpeedRequirement> getType() {
        return Registration.SPEED_REQUIREMENT.get();
    }

    @Override
    public RequirementIOMode getMode() {
        return RequirementIOMode.INPUT;
    }

    @Override
    public boolean test(AbstractMachineComponent component, ICraftingContext context) {
        return false;
    }

    @Override
    public void gatherRequirements(IRequirementList<AbstractMachineComponent> list) {

    }

    @Override
    public MachineComponentType<AbstractMachineComponent> getComponentType() {
        return null;
    }
}
