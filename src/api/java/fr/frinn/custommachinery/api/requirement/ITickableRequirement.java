package fr.frinn.custommachinery.api.requirement;

import fr.frinn.custommachinery.api.component.IMachineComponent;
import fr.frinn.custommachinery.api.crafting.CraftingResult;
import fr.frinn.custommachinery.api.crafting.ICraftingContext;
import net.minecraft.util.text.ITextComponent;

/**
 * Implement this interface on your {@link IRequirement} to make it executed every tick instead of just the start and end of the process.
 * @param <T> The component this requirement will use.
 */
public interface ITickableRequirement<T extends IMachineComponent> extends IRequirement<T> {

    /**
     * This method is called each tick of the recipe crafting process for each tickable requirement of the recipe,
     * including the first and last tick of the process, so you shouldn't execute your logic in both this method and the "processStart" or "processEnd" methods,
     * or it will be executed twice.
     * Usually that's where you consume/produce per-tick inputs/outputs.
     * @param component The {@link IMachineComponent} used by this requirement.
     * @param context A few useful info about the crafting process, and some utilities methods.
     * @return {@link CraftingResult#success()} if the requirement successfully did its things.
     *         {@link CraftingResult#error(ITextComponent)} if there was an error during the process (example : missing inputs).
     *         {@link CraftingResult#pass()} if the requirement didn't care about this phase.
     */
    CraftingResult processTick(T component, ICraftingContext context);
}
