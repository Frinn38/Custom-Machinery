package fr.frinn.custommachinery.common.crafting.requirements;

import fr.frinn.custommachinery.api.components.IMachineComponent;

public abstract class AbstractTickableRequirement<T extends IMachineComponent> implements ITickableRequirement<T> {

    private IRequirement.MODE mode;

    public AbstractTickableRequirement(IRequirement.MODE mode) {
        this.mode = mode;
    }

    @Override
    public IRequirement.MODE getMode() {
        return this.mode;
    }
}
