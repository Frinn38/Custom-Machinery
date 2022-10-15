package fr.frinn.custommachinery.impl.requirement;

import fr.frinn.custommachinery.api.component.IMachineComponent;
import fr.frinn.custommachinery.api.requirement.IRequirement;
import fr.frinn.custommachinery.api.requirement.RequirementIOMode;

public abstract class AbstractRequirement<T extends IMachineComponent> implements IRequirement<T> {

    private final RequirementIOMode mode;

    public AbstractRequirement(RequirementIOMode mode) {
        this.mode = mode;
    }

    @Override
    public RequirementIOMode getMode() {
        return this.mode;
    }
}
