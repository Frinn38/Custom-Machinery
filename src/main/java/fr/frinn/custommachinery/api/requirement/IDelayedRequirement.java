package fr.frinn.custommachinery.api.requirement;

import fr.frinn.custommachinery.api.component.IMachineComponent;
import fr.frinn.custommachinery.api.crafting.CraftingResult;
import fr.frinn.custommachinery.api.crafting.ICraftingContext;
import net.minecraft.network.chat.Component;

/**
 * Implements this interface on your {@link IRequirement} to make it execute at some time of the recipe process which is not the start, the end or each tick.
 * The delay can be fixed, or left to the creator of the recipe.
 * @param <T> The component this requirement will use.
 */
public interface IDelayedRequirement<T extends IMachineComponent> extends IRequirement<T> {

    /**
     * Set the delay (between 0.0 and 1.0) with which the requirement will be executed.
     * 0.0 represents the start of the process and 1.0 the end.
     * @param delay A double between 0.0 and 1.0.
     */
    void setDelay(double delay);

    /**
     * The delay must not be exactly 0.0 or 1.0, in that case the requirement will be ignored.
     * @return The delay between the start of the process and the execution of this requirement.
     */
    double getDelay();

    /**
     * This method is called when the crafting process reach the specified delay.
     * @param component The {@link IMachineComponent} used by this requirement.
     * @param context A few useful info about the crafting process, and some utilities methods.
     * @return {@link CraftingResult#success()} if the requirement successfully did its things.
     *         {@link CraftingResult#error(Component)} if there was an error during the process (example : missing inputs).
     *         {@link CraftingResult#pass()} if the requirement didn't care about this phase.
     */
    CraftingResult execute(T component, ICraftingContext context);
}
