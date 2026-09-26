package fr.frinn.custommachinery.common.crafting.machine;

import fr.frinn.custommachinery.api.machine.MachineTile;
import fr.frinn.custommachinery.common.crafting.MutableCraftingContext;
import fr.frinn.custommachinery.common.crafting.RecipeChecker;
import fr.frinn.custommachinery.common.init.CMRegistration;
import fr.frinn.custommachinery.common.util.Comparators;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.item.crafting.RecipeHolder;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Optional;

public class MachineRecipeFinder {

    private final MachineTile tile;
    private final MachineProcessor processor;
    private final int baseCooldown;
    private final MutableCraftingContext mutableCraftingContext;
    private final int core;
    private final List<RecipeChecker<CustomMachineRecipe>> recipes = new ArrayList<>();
    private final List<RecipeChecker<CustomMachineRecipe>> okToCheck = new ArrayList<>();
    private boolean inventoryChanged = true;

    private int recipeCheckCooldown;

    public MachineRecipeFinder(MachineTile tile, MachineProcessor processor, int baseCooldown, MutableCraftingContext mutableCraftingContext, int core) {
        this.tile = tile;
        this.processor = processor;
        this.baseCooldown = baseCooldown;
        this.mutableCraftingContext = mutableCraftingContext;
        this.core = core;
    }

    public void init() {
        if(!(this.tile.getLevel() instanceof ServerLevel level))
            throw new IllegalStateException("Broken machine " + tile.getMachine().getId() + "doesn't have a world");
        level.recipeAccess().recipeMap()
                .byType(CMRegistration.CUSTOM_MACHINE_RECIPE.get())
                .stream()
                .filter(recipe -> tile.getMachine().getRecipeIds().contains(recipe.value().getMachineId()))
                .sorted((holder1, holder2) -> Comparators.RECIPE_PRIORITY_COMPARATOR.reversed().compare(holder1.value(), holder2.value()))
                .map(RecipeChecker::new)
                .forEach(this.recipes::add);
        this.recipeCheckCooldown = level.getRandom().nextInt(this.baseCooldown);
    }

    public Optional<RecipeHolder<CustomMachineRecipe>> findRecipe(boolean immediately) {
        if(tile.getLevel() == null)
            return Optional.empty();

        if(immediately || this.recipeCheckCooldown-- <= 0) {
            this.recipeCheckCooldown = this.baseCooldown;
            if(this.inventoryChanged || immediately) {
                this.okToCheck.clear();
                this.okToCheck.addAll(this.recipes);
            }
            Iterator<RecipeChecker<CustomMachineRecipe>> iterator = this.okToCheck.iterator();
            while (iterator.hasNext()) {
                RecipeChecker<CustomMachineRecipe> checker = iterator.next();
                if(!this.inventoryChanged && checker.isInventoryRequirementsOnly() && !immediately)
                    continue;
                if(checker.check(this.tile, this.mutableCraftingContext.setRecipe(checker.getRecipe()), this.inventoryChanged || immediately)) {
                    //Check if the recipe can be run on this core
                    if((!checker.getRecipe().value().getAllowedCores().isEmpty() && !checker.getRecipe().value().getAllowedCores().contains(this.core)) || (checker.getRecipe().value().isSingleCore() && this.processor.getCores().stream().anyMatch(core -> core.getCurrentRecipe() != null && core.getCurrentRecipe().id().equals(checker.getRecipe().id()))))
                        continue;
                    setInventoryChanged(false);
                    return Optional.of(checker.getRecipe());
                }
                if(checker.inventoryRequirementNotOk())
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
