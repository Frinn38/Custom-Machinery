package fr.frinn.custommachinery.common.integration.jei;

import fr.frinn.custommachinery.api.component.IMachineComponent;
import fr.frinn.custommachinery.common.crafting.requirements.IRequirement;

public interface IDisplayInfoRequirement<T extends IMachineComponent> extends IRequirement<T> {

    void setJeiVisible(boolean jeiVisible);

    RequirementDisplayInfo getDisplayInfo();
}
