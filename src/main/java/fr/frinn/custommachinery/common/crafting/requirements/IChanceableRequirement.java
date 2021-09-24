package fr.frinn.custommachinery.common.crafting.requirements;

import fr.frinn.custommachinery.api.component.IMachineComponent;
import fr.frinn.custommachinery.common.crafting.CraftingContext;

import java.util.Random;

public interface IChanceableRequirement<T extends IMachineComponent> extends IRequirement<T> {

    void setChance(double chance);

    boolean testChance(T component, Random rand, CraftingContext context);
}
