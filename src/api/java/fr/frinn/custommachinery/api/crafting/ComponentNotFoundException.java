package fr.frinn.custommachinery.api.crafting;

import fr.frinn.custommachinery.api.machine.ICustomMachine;
import fr.frinn.custommachinery.api.requirement.RequirementType;

public class ComponentNotFoundException extends RuntimeException {

    private final IMachineRecipe currentRecipe;
    private final ICustomMachine machine;
    private final RequirementType<?> requirementType;

    public ComponentNotFoundException(IMachineRecipe currentRecipe, ICustomMachine machine, RequirementType<?> requirementType) {
        this.currentRecipe = currentRecipe;
        this.machine = machine;
        this.requirementType = requirementType;

    }

    @Override
    public String getMessage() {
        StringBuilder builder = new StringBuilder();
        builder.append("Invalid Custom Machine recipe: ");
        builder.append(this.currentRecipe.getRecipeId());
        builder.append(" | Requirement: ");
        builder.append(this.requirementType.getName());
        builder.append(" try to use a component the machine: ");
        builder.append(this.machine.getId());
        builder.append(" doesn't have !");
        return builder.toString();
    }
}
