package fr.frinn.custommachinery.common.crafting;

import fr.frinn.custommachinery.api.crafting.IMachineRecipe;
import fr.frinn.custommachinery.api.integration.jei.IDisplayInfoRequirement;
import fr.frinn.custommachinery.api.integration.jei.IJEIIngredientRequirement;
import fr.frinn.custommachinery.api.requirement.IRequirement;
import fr.frinn.custommachinery.common.init.Registration;
import fr.frinn.custommachinery.common.util.Comparators;
import mcp.MethodsReturnNonnullByDefault;
import net.minecraft.inventory.IInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.IRecipe;
import net.minecraft.item.crafting.IRecipeSerializer;
import net.minecraft.item.crafting.IRecipeType;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.World;
import net.minecraftforge.common.util.Lazy;

import javax.annotation.ParametersAreNonnullByDefault;
import java.util.List;
import java.util.stream.Collectors;

@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public class CustomMachineRecipe implements IRecipe<IInventory>, IMachineRecipe {

    private final ResourceLocation id;
    private final ResourceLocation machine;
    private final int time;
    private final List<IRequirement<?>> requirements;
    private final List<IRequirement<?>> jeiRequirements;
    private final int priority;
    private final int jeiPriority;

    public CustomMachineRecipe(ResourceLocation id, ResourceLocation machine, int time, List<IRequirement<?>> requirements, List<IRequirement<?>> jeiRequirements, int priority, int jeiPriority) {
        this.id = id;
        this.machine = machine;
        this.time = time;
        this.requirements = requirements.stream().sorted(Comparators.REQUIREMENT_COMPARATOR).collect(Collectors.toList());
        this.jeiRequirements = jeiRequirements;
        this.priority = priority;
        this.jeiPriority = priority;
    }

    public ResourceLocation getMachine() {
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
            return this.requirements.stream().filter(requirement -> requirement instanceof IDisplayInfoRequirement).map(requirement -> (IDisplayInfoRequirement)requirement).collect(Collectors.toList());
        return this.jeiRequirements.stream().filter(requirement -> requirement instanceof IDisplayInfoRequirement).map(requirement -> (IDisplayInfoRequirement)requirement).collect(Collectors.toList());
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
    public IRecipeSerializer<?> getSerializer() {
        return Registration.CUSTOM_MACHINE_RECIPE_SERIALIZER.get();
    }

    @Override
    public IRecipeType<?> getType() {
        return Registration.CUSTOM_MACHINE_RECIPE;
    }

    @Override
    public boolean matches(IInventory inv, World worldIn) {
        return false;
    }

    @Override
    public ItemStack getCraftingResult(IInventory inv) {
        return ItemStack.EMPTY;
    }

    @Override
    public boolean canFit(int width, int height) {
        return false;
    }

    @Override
    public ItemStack getRecipeOutput() {
        return ItemStack.EMPTY;
    }

    private final Lazy<RecipeChecker> checker = Lazy.of(() -> new RecipeChecker(this));
    public RecipeChecker checker() {
        return checker.get();
    }
}
