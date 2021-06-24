package fr.frinn.custommachinery.common.integration.jei;

import fr.frinn.custommachinery.common.crafting.requirements.IRequirement;
import fr.frinn.custommachinery.common.data.component.IMachineComponent;

public interface IDisplayInfoRequirement<T extends IMachineComponent> extends IRequirement<T> {

    RequirementDisplayInfo getDisplayInfo();
}
