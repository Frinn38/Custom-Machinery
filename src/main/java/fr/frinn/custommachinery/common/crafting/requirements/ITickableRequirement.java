package fr.frinn.custommachinery.common.crafting.requirements;

import fr.frinn.custommachinery.api.component.IMachineComponent;
import fr.frinn.custommachinery.common.crafting.CraftingContext;
import fr.frinn.custommachinery.common.crafting.CraftingResult;

public interface ITickableRequirement<T extends IMachineComponent> extends IRequirement<T> {

    CraftingResult processTick(T component, CraftingContext context);
}
