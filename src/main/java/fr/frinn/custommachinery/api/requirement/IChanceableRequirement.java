package fr.frinn.custommachinery.api.requirement;

import fr.frinn.custommachinery.api.component.IMachineComponent;
import fr.frinn.custommachinery.api.crafting.ICraftingContext;

import java.util.Random;

/**
 * Implements this interface on your {@link IRequirement} to make them randomly skipped during the process.
 * Meaning that the inputs won't be consumed or outputs won't be produced.
 * The calculations of this chance is left to the requirement.
 * @param <T> The component this requirement will use.
 */
public interface IChanceableRequirement<T extends IMachineComponent> extends IRequirement<T> {

    /**
     * Set the chance (between 0.0 and 1.0) that this requirement will be processed.
     * Used by the requirement codec and both CT and KubeJS integration.
     * @param chance A double between 0.0 and 1.0 that represent the chance of the requirement to be processed.
     */
    void setChance(double chance);

    /**
     * Calculate if the requirement must be processed or skipped.
     * This method is called for each phase of the crafting process,
     * meaning that even if the requirement is skipped on start it can still be processed on end if this method return false at that time.
     * Default implementation is : {@code rand.nextDouble() > chance}
     * @param component The {@link IMachineComponent} used by this requirement.
     * @param rand The same Random instance is shared between all requirements.
     *             You can ignore it if you want to calculate the chance in another way.
     * @param context A few useful info about the crafting process, and some utilities methods.
     * @return True if the requirement must be skipped, false if it must be processed.
     */
    boolean shouldSkip(T component, Random rand, ICraftingContext context);
}
