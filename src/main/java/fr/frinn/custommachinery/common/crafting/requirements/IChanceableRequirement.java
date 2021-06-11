package fr.frinn.custommachinery.common.crafting.requirements;

import fr.frinn.custommachinery.common.crafting.CraftingContext;

import java.util.Random;

public interface IChanceableRequirement {

    boolean testChance(Random rand, CraftingContext context);
}
