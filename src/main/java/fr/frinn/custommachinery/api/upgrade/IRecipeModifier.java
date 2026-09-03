package fr.frinn.custommachinery.api.upgrade;

import fr.frinn.custommachinery.api.requirement.RequirementIOMode;
import fr.frinn.custommachinery.api.requirement.RequirementType;
import net.minecraft.network.chat.Component;
import org.jetbrains.annotations.Nullable;

/**
 * A modifier applied to a specific {@link fr.frinn.custommachinery.api.requirement.IRequirement} in a mmachine recipe.
 */
public interface IRecipeModifier {

    /**
     * Check if the modifier apply to the currently checked requirement.
     * @param type The type of requirement to check.
     * @param mode The mode (INPUT or OUTPUT) of the requirement to check.
     * @param target A specific property of the requirement, for requirement that have several properties like amount, chance, radius...
     * @return True if the modifier can be applied to the checked requirement.
     */
    boolean shouldApply(RequirementType<?> type, RequirementIOMode mode, @Nullable String target);

    /**
     * Apply the modifier to the default value provided by the requirement.
     * @param original The default value (may have been modified by another modifier).
     * @param upgradeAmount The amount of upgrade items using this modifier currently applied to the machine.
     * @return A modified value that will be used by the requirement.
     */
    double apply(double original, int upgradeAmount);

    /**
     * @return A tooltip that will be displayed when the player hover the upgrade item in an inventory.
     */
    Component tooltip();
}
