package fr.frinn.custommachinery.common.crafting.requirements;

import fr.frinn.custommachinery.common.crafting.CraftingContext;
import fr.frinn.custommachinery.common.data.component.IMachineComponent;

import java.util.Random;

public interface IChanceableRequirement<T extends IMachineComponent> extends IRequirement<T> {

    boolean testChance(T component, Random rand, CraftingContext context);
}
