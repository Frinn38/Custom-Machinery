package fr.frinn.custommachinery.api.integration.jei;

import fr.frinn.custommachinery.api.requirement.IRequirement;
import fr.frinn.custommachinery.apiimpl.integration.jei.Energy;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.common.util.Lazy;
import net.minecraftforge.fluids.FluidStack;

/**
 * Implements this interface in your {@link IRequirement} to indicate the JEI integration that this requirement represents an ingredient for JEI.
 * Ingredients supported by jei are {@link ItemStack} and {@link FluidStack}. CM also add {@link Energy}.
 * @param <T> The ingredient represented by this requirement.
 */
public interface IJEIIngredientRequirement<T> {

    /**
     * A wrapper is needed here to keep jei optional, so jei classes are loaded only if this method is called.
     * You can create a new wrapper each time this method is called, or make it a {@link Lazy} variable.
     * But do not create a new instance of the wrapper outside of this method as it will load jei class, resulting on a crash if jei is not present.
     * @return A wrapper that will act as a bridge between the requirement and the jei integration.
     */
    IJEIIngredientWrapper<T> getJEIIngredientWrapper();
}
