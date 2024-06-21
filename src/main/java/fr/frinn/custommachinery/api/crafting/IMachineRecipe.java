package fr.frinn.custommachinery.api.crafting;

import fr.frinn.custommachinery.api.integration.jei.IJEIIngredientRequirement;
import fr.frinn.custommachinery.api.requirement.IRequirement;
import net.minecraft.resources.ResourceLocation;

import java.util.List;
import java.util.stream.Collectors;

public interface IMachineRecipe {

    /**
     * @return The id of the machine that can process this recipe.
     */
    ResourceLocation getMachineId();

    /**
     * @return The duration (in ticks) of the recipe.
     */
    int getRecipeTime();

    /**
     * @return An Immutable list of all requirements of this recipe.
     */
    List<IRequirement<?>> getRequirements();

    /**
     * @return An Immutable list of all jei requirements of this recipe.
     */
    List<IRequirement<?>> getJeiRequirements();

    /**
     * @return An Immutable list of all display info requirements of this recipe.
     */
    default List<IRequirement<?>> getDisplayInfoRequirements() {
        if(this.getJeiRequirements().isEmpty())
            return this.getRequirements();
        return this.getJeiRequirements();
    }

    /**
     * @return An Immutable list of all jei ingredient requirements of this recipe.
     */
    default List<IJEIIngredientRequirement<?>> getJEIIngredientRequirements() {
        if(this.getJeiRequirements().isEmpty())
            return this.getRequirements().stream().filter(requirement -> requirement instanceof IJEIIngredientRequirement).map(requirement -> (IJEIIngredientRequirement<?>)requirement).collect(Collectors.toList());
        return this.getJeiRequirements().stream().filter(requirement -> requirement instanceof IJEIIngredientRequirement).map(requirement -> (IJEIIngredientRequirement<?>)requirement).collect(Collectors.toList());
    }
    /**
     * Recipes with higher priorities will be tested first.
     * @return The priority of this recipe, default : 0.
     */
    int getPriority();

    /**
     * Recipes with higher priorities will be shown first in jei recipe gui.
     * @return The priority of this recipe to be shown in jei, default : 0.
     */
    int getJeiPriority();

    /**
     * If set to true the crafting process will reset when one of the recipe requirements error.
     * Default : false
     * @return A boolean which determinate if the machine will pause or reset when a recipe requirement couldn't be fulfilled.
     */
    boolean shouldResetOnError();

    /**
     * If set to true the recipe will be shown in jei recipe gui. If false the recipe will be hidden but still work in the machine.
     */
    boolean showInJei();
}
