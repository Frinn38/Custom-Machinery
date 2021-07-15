package fr.frinn.custommachinery.common.crafting.requirements;

import fr.frinn.custommachinery.api.components.IMachineComponent;

public abstract class AbstractRequirement<T extends IMachineComponent> implements IRequirement<T> {

    private MODE mode;

    public AbstractRequirement(MODE mode) {
        this.mode = mode;
    }

    @Override
    public MODE getMode() {
        return this.mode;
    }
}
