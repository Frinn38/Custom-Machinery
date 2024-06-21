package fr.frinn.custommachinery.api.crafting;

import fr.frinn.custommachinery.api.machine.ICustomMachine;
import fr.frinn.custommachinery.api.requirement.RequirementType;
import net.minecraft.world.item.crafting.RecipeHolder;

public class ComponentNotFoundException extends RuntimeException {

    private final RecipeHolder<?> currentRecipe;
    private final ICustomMachine machine;
    private final RequirementType<?> requirementType;

    public ComponentNotFoundException(RecipeHolder<?> currentRecipe, ICustomMachine machine, RequirementType<?> requirementType) {
        this.currentRecipe = currentRecipe;
        this.machine = machine;
        this.requirementType = requirementType;

    }

    @Override
    public String getMessage() {
        return "Invalid Custom Machine recipe: " +
                this.currentRecipe.id() +
                " | Requirement: " +
                this.requirementType.getName() +
                " try to use a component the machine: " +
                this.machine.getId() +
                " doesn't have !";
    }

    @Override
    public synchronized Throwable fillInStackTrace() {
        return this;
    }
}
