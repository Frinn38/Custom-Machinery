package fr.frinn.custommachinery.api.crafting;

import fr.frinn.custommachinery.api.component.IMachineComponent;
import fr.frinn.custommachinery.api.requirement.RequirementIOMode;
import net.minecraft.network.chat.Component;

/**
 * This interface is passed to the {@link fr.frinn.custommachinery.api.requirement.IRequirement#gatherRequirements(IRequirementList)}
 * method to collect every {@link RequirementFunction} that the processor must check or process for this requirement.
 * Each method in this list has different behaviours
 */
public interface IRequirementList<C extends IMachineComponent> {

    /**
     * The function will be executed on the first tick of the crafting process.
     */
    void processOnStart(RequirementFunction<C> function);

    /**
     * The function will be executed on the last tick of the crafting process.
     */
    void processOnEnd(RequirementFunction<C> function);

    /**
     * The function will be executed on each tick of the crafting process.
     */
    void processEachTick(RequirementFunction<C> function);

    /**
     * The function will be executed each tick of the crafting process, even if another requirement returned an error.
     * Do not consume inputs or outputs in this function, use {@link IRequirementList#processEachTick(RequirementFunction)} instead.
     */
    void worldCondition(RequirementFunction<C> function);

    /**
     * The function will be executed each tick of the crafting process, even if another requirement returned an error.
     * This function is executed only if the machine's inventory changed since last check.
     * Do not consume inputs or outputs in this function, use {@link IRequirementList#processEachTick(RequirementFunction)} instead.
     */
    void inventoryCondition(RequirementFunction<C> function);

    /**
     * The function will be executed when the specified delay is met.
     * @param baseDelay A percentage of the crafting process time, 0.0 is the first tick and 1.0 is the last tick.
     */
    void processDelayed(double baseDelay, RequirementFunction<C> function);

    /**
     * The function will be executed on first tick if the mode is input or last tick if the mode is output, or at the delay (if specified by the requirement).
     */
    void process(RequirementIOMode mode, RequirementFunction<C> function);

    /**
     * A function to process at the specified time, depending on which of the {@link IRequirementList} method is used to pass this function.
     * @param <C> The {@link IMachineComponent} that will be passed to the function, to interact with the machine or the world.
     */
    interface RequirementFunction<C extends IMachineComponent> {
        /**
         * @return {@link CraftingResult#success()} if the requirement was successfully processed or {@link CraftingResult#error(Component)} otherwise.
         */
        CraftingResult process(C component, ICraftingContext context);
    }
}
