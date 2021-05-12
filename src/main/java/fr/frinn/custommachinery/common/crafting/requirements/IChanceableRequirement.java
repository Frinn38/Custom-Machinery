package fr.frinn.custommachinery.common.crafting.requirements;

import java.util.Random;

public interface IChanceableRequirement {

    boolean testChance(Random rand);
}
