package fr.frinn.custommachinery.api.crafting;

import fr.frinn.custommachinery.api.integration.jei.IDisplayInfoRequirement;
import fr.frinn.custommachinery.api.integration.jei.IJEIIngredientRequirement;
import fr.frinn.custommachinery.api.requirement.IRequirement;
import net.minecraft.resources.ResourceLocation;

import java.util.List;

public interface IMachineRecipe {

    /**
     * For recipes created via json, the id is "namespace:path/to/recipe".
     * For recipes created via CT or KubeJS, the id is either given by the user, or a random string.
     * @return The id of the recipe.
     */
    ResourceLocation getRecipeId();

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
    default List<IDisplayInfoRequirement> getDisplayInfoRequirements() {
        return getRequirements().stream()
                .filter(requirement -> requirement instanceof IDisplayInfoRequirement)
                .map(requirement -> (IDisplayInfoRequirement)requirement)
                .toList();
    }

    /**
     * @return An Immutable list of all jei ingredient requirements of this recipe.
     */
    @SuppressWarnings("unchecked")
    default <T> List<IJEIIngredientRequirement<T>> getJEIIngredientRequirements() {
        return getRequirements().stream()
                .filter(requirement -> requirement instanceof IJEIIngredientRequirement<?>)
                .map(requirement -> (IJEIIngredientRequirement<T>)requirement)
                .toList();
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
}
