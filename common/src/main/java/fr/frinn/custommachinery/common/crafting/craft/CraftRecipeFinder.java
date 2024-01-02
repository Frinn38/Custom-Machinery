package fr.frinn.custommachinery.common.crafting.craft;

import fr.frinn.custommachinery.api.machine.MachineTile;
import fr.frinn.custommachinery.common.crafting.CraftingContext;
import fr.frinn.custommachinery.common.crafting.RecipeChecker;
import fr.frinn.custommachinery.common.init.Registration;
import fr.frinn.custommachinery.common.util.Comparators;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Optional;

public class CraftRecipeFinder {

    private final MachineTile tile;
    private final int baseCooldown;
    private List<RecipeChecker<CustomCraftRecipe>> recipes;
    private List<RecipeChecker<CustomCraftRecipe>> okToCheck;
    private boolean inventoryChanged = true;
    private int recipeCheckCooldown;

    public CraftRecipeFinder(MachineTile tile, int baseCooldown) {
        this.tile = tile;
        this.baseCooldown = baseCooldown;
    }

    public void init() {
        if(tile.getLevel() == null)
            throw new IllegalStateException("Broken machine " + tile.getMachine().getId() + "doesn't have a world");
        this.recipes = tile.getLevel().getRecipeManager()
                .getAllRecipesFor(Registration.CUSTOM_CRAFT_RECIPE.get())
                .stream()
                .filter(recipe -> recipe.getMachineId().equals(tile.getMachine().getId()))
                .sorted(Comparators.RECIPE_PRIORITY_COMPARATOR.reversed())
                .map(CustomCraftRecipe::checker)
                .toList();
        this.okToCheck = new ArrayList<>();
        this.recipeCheckCooldown = tile.getLevel().random.nextInt(this.baseCooldown);
    }

    public Optional<CustomCraftRecipe> findRecipe(CraftingContext.Mutable context, boolean immediately) {
        if(tile.getLevel() == null)
            return Optional.empty();

        if(immediately || this.recipeCheckCooldown-- <= 0) {
            this.recipeCheckCooldown = this.baseCooldown;
            if(this.inventoryChanged) {
                this.okToCheck.clear();
                this.okToCheck.addAll(this.recipes);
            }
            Iterator<RecipeChecker<CustomCraftRecipe>> iterator = this.okToCheck.iterator();
            while (iterator.hasNext()) {
                RecipeChecker<CustomCraftRecipe> checker = iterator.next();
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
