package fr.frinn.custommachinery.api.crafting;

import fr.frinn.custommachinery.api.machine.MachineTile;
import fr.frinn.custommachinery.api.requirement.IRequirement;
import fr.frinn.custommachinery.api.requirement.RequirementType;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.Nullable;

/**
 * Provide various information about the actual crafting process, like the current IMachineRecipe or the MachineTile executing this recipe.
 * This is passed to each IRequirement of the IMachineRecipe when they are executed.
 */
public interface ICraftingContext {

    /**
     * @return The {@link MachineTile} currently processing the recipe.
     */
    MachineTile getMachineTile();

    /**
     * @return The {@link IMachineRecipe} currently processed by the machine.
     */
    IMachineRecipe getRecipe();

    /**
     * @return The id of the {@link IMachineRecipe} currently processed by the machine.
     */
    ResourceLocation getRecipeId();

    /**
     * This time is usually in ticks, but may vary depending on what is returned by {@link ICraftingContext#getModifiedSpeed} return.
     * @return The remaining time before the end of the crafting process.
     */
    double getRemainingTime();

    /**
     * @return The base speed (in ticks) of for the processing of the current recipe. By default, '1.0' unless another base speed is set using {@link ICraftingContext#setBaseSpeed(double)}.
     * This value does not take in account the upgrades that might be applied to the machine, use {@link ICraftingContext#getModifiedSpeed()} if you need the final speed value.
     */
    double getBaseSpeed();

    /**
     * Allows to set the base speed of the recipe crafting process. Upgrades will be applied on top of this value.
     * @param baseSpeed The new base speed (how much the recipe crafting process progress each tick)
     */
    void setBaseSpeed(double baseSpeed);

    /**
     * By default, the recipe processing speed is 1 per tick, but can be speeded up or slowed down if the machine have some upgrades modifiers.
     * @return The speed of the crafting process.
     */
    double getModifiedSpeed();


    /**
     * Used to apply all currently active machine upgrades to an {@link IRequirement} value.
     * @param value The value to modify (example an amount of item, energy etc...).
     * @param requirement The requirement the value depends, because machine upgrades can target a specific {@link RequirementType}.
     * @param target The name of the value to modify, or null, because machine upgrades can target a specific value of a requirement.
     * @return The modified value, or the same value if no upgrades could be applied.
     */
    double getModifiedValue(double value, IRequirement<?> requirement, @Nullable String target);

    /**
     * Same as the method above but round the value to a {@link Long}
     */
    long getIntegerModifiedValue(double value, IRequirement<?> requirement, @Nullable String target);

    /**
     * Use this method only for requirements that will be executed every tick of the crafting process.
     * @param value The value to modify (example an amount of item, energy etc...).
     * @param requirement The requirement the value depends, because machine upgrades can target a specific {@link RequirementType}.
     * @param target The name of the value to modify, or null, because machine upgrades can target a specific value of a requirement.
     * @return The modified value, or the same value if no upgrades could be applied.
     */
    double getPerTickModifiedValue(double value, IRequirement<?> requirement, @Nullable String target);

    /**
     * Same as the method above but round the value to a {@link Long}
     */
    long getPerTickIntegerModifiedValue(double value, IRequirement<?> requirement, @Nullable String target);
}
