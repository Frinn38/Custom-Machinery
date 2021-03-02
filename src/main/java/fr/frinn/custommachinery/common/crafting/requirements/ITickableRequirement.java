package fr.frinn.custommachinery.common.crafting.requirements;

import fr.frinn.custommachinery.common.crafting.CraftingResult;
import fr.frinn.custommachinery.common.data.component.IMachineComponent;
import fr.frinn.custommachinery.common.init.CustomMachineTile;

public interface ITickableRequirement<T extends IMachineComponent> extends IRequirement<T> {

    CraftingResult processTick(T component);
}
