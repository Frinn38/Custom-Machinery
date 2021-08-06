package fr.frinn.custommachinery.common.crafting.requirements;

import fr.frinn.custommachinery.api.components.IMachineComponent;
import fr.frinn.custommachinery.common.crafting.CraftingContext;
import fr.frinn.custommachinery.common.crafting.CraftingResult;

public interface IDelayedRequirement<T extends IMachineComponent> extends IRequirement<T> {

    void setDelay(double delay);

    double getDelay();

    CraftingResult execute(T component, CraftingContext context);
}
