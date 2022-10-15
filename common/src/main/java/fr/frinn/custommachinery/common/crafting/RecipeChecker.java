package fr.frinn.custommachinery.common.crafting;

import fr.frinn.custommachinery.api.component.IMachineComponent;
import fr.frinn.custommachinery.api.crafting.ICraftingContext;
import fr.frinn.custommachinery.api.requirement.IRequirement;
import fr.frinn.custommachinery.common.init.CustomMachineTile;

import java.util.ArrayList;
import java.util.List;

public class RecipeChecker {

    private final CustomMachineRecipe recipe;
    private final List<IRequirement<?>> inventoryRequirements;
    private final List<IRequirement<?>> checkedInventoryRequirements;
    private final List<IRequirement<?>> worldRequirements;
    private final boolean isInventoryRequirementsOnly;
    private boolean inventoryRequirementsOk = false;

    public RecipeChecker(CustomMachineRecipe recipe) {
        this.recipe = recipe;
        this.inventoryRequirements = recipe.getRequirements().stream().filter(r -> !r.getType().isWorldRequirement()).toList();
        this.checkedInventoryRequirements = new ArrayList<>();
        this.worldRequirements = recipe.getRequirements().stream().filter(r -> r.getType().isWorldRequirement()).toList();
        this.isInventoryRequirementsOnly = recipe.getRequirements().stream().noneMatch(r -> r.getType().isWorldRequirement());
    }

    public boolean check(CustomMachineTile tile, ICraftingContext context, boolean inventoryChanged) {
        if (inventoryChanged) {
            this.checkedInventoryRequirements.clear();
            this.inventoryRequirementsOk = false;

            for (IRequirement<?> requirement : this.inventoryRequirements) {
                if (this.checkedInventoryRequirements.contains(requirement))
                    continue;
                this.checkedInventoryRequirements.add(requirement);
                if (!checkRequirement(requirement, tile, context))
                    return false;
            }

            this.inventoryRequirementsOk = true;
        }

        if (!this.inventoryRequirementsOk)
            return false;
        else
            return this.worldRequirements.stream().allMatch(r -> checkRequirement(r, tile, context));
    }

    public CustomMachineRecipe getRecipe() {
        return this.recipe;
    }

    public boolean isInventoryRequirementsOnly() {
        return this.isInventoryRequirementsOnly;
    }

    public boolean isInventoryRequirementsOk() {
        return this.inventoryRequirementsOk;
    }

    private <T extends IMachineComponent> boolean checkRequirement(IRequirement<T> requirement, CustomMachineTile tile, ICraftingContext context) {
        return tile.componentManager.getComponent(requirement.getComponentType()).map(c -> requirement.test(c, context)).orElse(false);
    }
}
