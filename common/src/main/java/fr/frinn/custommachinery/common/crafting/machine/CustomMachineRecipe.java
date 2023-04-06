package fr.frinn.custommachinery.common.crafting.machine;

import com.google.common.base.Suppliers;
import fr.frinn.custommachinery.api.crafting.IMachineRecipe;
import fr.frinn.custommachinery.api.integration.jei.IDisplayInfoRequirement;
import fr.frinn.custommachinery.api.integration.jei.IJEIIngredientRequirement;
import fr.frinn.custommachinery.api.requirement.IRequirement;
import fr.frinn.custommachinery.common.crafting.RecipeChecker;
import fr.frinn.custommachinery.common.init.Registration;
import fr.frinn.custommachinery.common.util.Comparators;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.Container;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.level.Level;

import java.util.List;
import java.util.function.Supplier;
import java.util.stream.Collectors;

public class CustomMachineRecipe implements Recipe<Container>, IMachineRecipe {

    private final ResourceLocation id;
    private final ResourceLocation machine;
    private final int time;
    private final List<IRequirement<?>> requirements;
    private final List<IRequirement<?>> jeiRequirements;
    private final int priority;
    private final int jeiPriority;
    private final boolean resetOnError;
    private final boolean hidden;
    private final Supplier<RecipeChecker<CustomMachineRecipe>> checker = Suppliers.memoize(() -> new RecipeChecker<>(this));

    public CustomMachineRecipe(ResourceLocation id, ResourceLocation machine, int time, List<IRequirement<?>> requirements, List<IRequirement<?>> jeiRequirements, int priority, int jeiPriority, boolean resetOnError, boolean hidden) {
        this.id = id;
        this.machine = machine;
        this.time = time;
        this.requirements = requirements.stream().sorted(Comparators.REQUIREMENT_COMPARATOR).toList();
        this.jeiRequirements = jeiRequirements;
        this.priority = priority;
        this.jeiPriority = jeiPriority;
        this.resetOnError = resetOnError;
        this.hidden = hidden;
    }

    @Override
    public ResourceLocation getMachineId() {
        return this.machine;
    }

    @Override
    public ResourceLocation getId() {
        return this.getRecipeId();
    }

    @Override
    public ResourceLocation getRecipeId() {
        return this.id;
    }

    @Override
    public int getRecipeTime() {
        return this.time;
    }

    @Override
    public List<IRequirement<?>> getRequirements() {
        return this.requirements;
    }

    @Override
    public List<IRequirement<?>> getJeiRequirements() {
        return this.jeiRequirements;
    }

    public List<IJEIIngredientRequirement<?>> getJEIIngredientRequirements() {
        if(this.jeiRequirements.isEmpty())
            return this.requirements.stream().filter(requirement -> requirement instanceof IJEIIngredientRequirement).map(requirement -> (IJEIIngredientRequirement<?>)requirement).collect(Collectors.toList());
        return this.jeiRequirements.stream().filter(requirement -> requirement instanceof IJEIIngredientRequirement).map(requirement -> (IJEIIngredientRequirement<?>)requirement).collect(Collectors.toList());
    }

    public List<IDisplayInfoRequirement> getDisplayInfoRequirements() {
        if(this.jeiRequirements.isEmpty())
            return this.requirements.stream().filter(requirement -> requirement instanceof IDisplayInfoRequirement).map(requirement -> (IDisplayInfoRequirement)requirement).toList();
        return this.jeiRequirements.stream().filter(requirement -> requirement instanceof IDisplayInfoRequirement).map(requirement -> (IDisplayInfoRequirement)requirement).toList();
    }

    @Override
    public int getPriority() {
        return this.priority;
    }

    @Override
    public int getJeiPriority() {
        return this.jeiPriority;
    }

    @Override
    public boolean shouldResetOnError() {
        return this.resetOnError;
    }

    @Override
    public boolean showInJei() {
        return !this.hidden;
    }

    public RecipeChecker<CustomMachineRecipe> checker() {
        return checker.get();
    }

    /** Vanilla Recipe Implementation **/

    @Override
    public RecipeSerializer<?> getSerializer() {
        return Registration.CUSTOM_MACHINE_RECIPE_SERIALIZER.get();
    }

    @Override
    public RecipeType<?> getType() {
        return Registration.CUSTOM_MACHINE_RECIPE.get();
    }

    @Override
    public boolean matches(Container inv, Level worldIn) {
        return false;
    }

    @Override
    public ItemStack assemble(Container inv) {
        return ItemStack.EMPTY;
    }

    @Override
    public boolean canCraftInDimensions(int width, int height) {
        return false;
    }

    @Override
    public ItemStack getResultItem() {
        return ItemStack.EMPTY;
    }
}
