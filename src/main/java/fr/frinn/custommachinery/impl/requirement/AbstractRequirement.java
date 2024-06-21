package fr.frinn.custommachinery.impl.requirement;

import fr.frinn.custommachinery.api.component.IMachineComponent;
import fr.frinn.custommachinery.api.integration.jei.DisplayInfoTemplate;
import fr.frinn.custommachinery.api.requirement.IRequirement;
import fr.frinn.custommachinery.api.requirement.RequirementIOMode;
import org.jetbrains.annotations.Nullable;

public abstract class AbstractRequirement<T extends IMachineComponent> implements IRequirement<T> {

    private final RequirementIOMode mode;
    @Nullable
    private DisplayInfoTemplate template;

    public AbstractRequirement(RequirementIOMode mode) {
        this.mode = mode;
    }

    @Override
    public RequirementIOMode getMode() {
        return this.mode;
    }

    @Override
    public void setDisplayInfoTemplate(DisplayInfoTemplate template) {
        this.template = template;
    }

    @Nullable
    @Override
    public DisplayInfoTemplate getDisplayInfoTemplate() {
        return template;
    }
}
