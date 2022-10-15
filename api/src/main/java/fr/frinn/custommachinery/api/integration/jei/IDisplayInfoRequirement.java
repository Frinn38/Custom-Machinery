package fr.frinn.custommachinery.api.integration.jei;

import fr.frinn.custommachinery.api.requirement.IRequirement;

/**
 * Implements this interface on your {@link IRequirement} to make the jei integration render your requirement in the form of a small icon at the bottom of the recipe gui.
 * Default icon is a 10x10 green "+" but this can be customized.
 */
public interface IDisplayInfoRequirement {

    /**
     * Called by the jei integration to collect all display info from the requirement.
     * Use the passed {@link IDisplayInfo} to add some tooltips, a custom icon or a click callback to the requirement in the jei recipe.
     * @param info The display info of the requirement.
     */
    void getDisplayInfo(IDisplayInfo info);
}
