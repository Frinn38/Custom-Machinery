package fr.frinn.custommachinery.common.crafting;

import fr.frinn.custommachinery.common.crafting.requirements.RequirementType;
import fr.frinn.custommachinery.common.data.CustomMachine;

public class ComponentNotFoundException extends RuntimeException {

    private final CustomMachineRecipe currentRecipe;
    private final CustomMachine machine;
    private final RequirementType<?> requirementType;

    public ComponentNotFoundException(CustomMachineRecipe currentRecipe, CustomMachine machine, RequirementType<?> requirementType) {
        this.currentRecipe = currentRecipe;
        this.machine = machine;
        this.requirementType = requirementType;

    }

    @Override
    public String getMessage() {
        StringBuilder builder = new StringBuilder();
        builder.append("Invalid Custom Machine recipe: ");
        builder.append(this.currentRecipe.getId());
        builder.append(" | Requirement: ");
        builder.append(this.requirementType.getName());
        builder.append(" try to use a component the machine: ");
        builder.append(this.machine.getId());
        builder.append(" doesn't have !");
        return builder.toString();
    }
}
