package fr.frinn.custommachinery.common.crafting;

import fr.frinn.custommachinery.api.requirement.IRequirement;
import fr.frinn.custommachinery.api.requirement.RequirementType;
import fr.frinn.custommachinery.common.init.CustomMachineTile;
import fr.frinn.custommachinery.common.init.Registration;
import fr.frinn.custommachinery.common.util.Comparators;
import net.minecraft.world.World;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

public class RecipeFinder {

    private final CustomMachineTile tile;
    private List<CustomMachineRecipe> allRecipes;
    private List<CustomMachineRecipe> worldOnlyRecipes;
    private boolean initialized = false;
    private boolean inventoryChanged = true;

    private int recipeCheckCooldown;

    public RecipeFinder(CustomMachineTile tile) {
        this.tile = tile;
    }

    private void init(World world) {
        this.allRecipes = world.getRecipeManager().getRecipesForType(Registration.CUSTOM_MACHINE_RECIPE).stream().filter(recipe -> recipe.getMachine().equals(tile.getId())).sorted(Comparators.RECIPE_PRIORITY_COMPARATOR.reversed()).collect(Collectors.toList());
        this.worldOnlyRecipes = allRecipes.stream().filter(recipe -> recipe.getRequirements().stream().map(IRequirement::getType).allMatch(RequirementType::isWorldRequirement)).collect(Collectors.toList());
        this.recipeCheckCooldown = world.rand.nextInt(20);
        this.initialized = true;
    }

    public Optional<CustomMachineRecipe> findRecipe(CraftingContext.Mutable context, boolean immediately) {
        if(tile.getWorld() == null)
            return Optional.empty();

        if(!initialized)
            this.init(tile.getWorld());

        if(immediately || this.recipeCheckCooldown-- <= 0) {
            this.recipeCheckCooldown = 20;
            boolean checkWorldOnly = !this.inventoryChanged;
            this.inventoryChanged = false;
            if(checkWorldOnly)
                return this.worldOnlyRecipes.stream().filter(recipe -> recipe.matches(this.tile, context.setRecipe(recipe))).findFirst();
            return this.allRecipes.stream().filter(recipe -> recipe.matches(this.tile, context.setRecipe(recipe))).findFirst();
        }
        return Optional.empty();
    }

    public void setInventoryChanged() {
        this.inventoryChanged = true;
    }
}
