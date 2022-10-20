package fr.frinn.custommachinery.common.crafting;

import fr.frinn.custommachinery.common.init.CustomMachineTile;
import fr.frinn.custommachinery.common.init.Registration;
import fr.frinn.custommachinery.common.util.Comparators;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

public class RecipeFinder {

    private final CustomMachineTile tile;
    private List<RecipeChecker> recipes;
    private List<RecipeChecker> okToCheck;
    private boolean inventoryChanged = true;

    private int recipeCheckCooldown;

    public RecipeFinder(CustomMachineTile tile) {
        this.tile = tile;
    }

    public void init() {
        if(tile.getWorld() == null)
            throw new IllegalStateException("Broken machine " + tile.getMachine().getId() + "doesn't have a world");
        this.recipes = tile.getWorld().getRecipeManager()
                .getRecipesForType(Registration.CUSTOM_MACHINE_RECIPE)
                .stream()
                .filter(recipe -> recipe.getMachine().equals(tile.getId()))
                .sorted(Comparators.RECIPE_PRIORITY_COMPARATOR.reversed())
                .map(CustomMachineRecipe::checker)
                .collect(Collectors.toList());
        this.okToCheck = new ArrayList<>();
        this.recipeCheckCooldown = tile.getWorld().rand.nextInt(20);
    }

    public Optional<CustomMachineRecipe> findRecipe(CraftingContext.Mutable context, boolean immediately) {
        if(tile.getWorld() == null)
            return Optional.empty();

        if(immediately || this.recipeCheckCooldown-- <= 0) {
            this.recipeCheckCooldown = 20;
            if(this.inventoryChanged) {
                this.okToCheck.clear();
                this.okToCheck.addAll(this.recipes);
            }
            Iterator<RecipeChecker> iterator = this.okToCheck.iterator();
            while (iterator.hasNext()) {
                RecipeChecker checker = iterator.next();
                if(!this.inventoryChanged && checker.isInventoryRequirementsOnly())
                    continue;
                if(checker.check(this.tile, context.setRecipe(checker.getRecipe()), this.inventoryChanged)) {
                    setInventoryChanged(false);
                    return Optional.of(checker.getRecipe());
                }
                if(!checker.isInventoryRequirementsOk())
                    iterator.remove();
            }
            setInventoryChanged(false);
        }
        return Optional.empty();
    }

    public void setInventoryChanged(boolean changed) {
        this.inventoryChanged = changed;
    }
}
