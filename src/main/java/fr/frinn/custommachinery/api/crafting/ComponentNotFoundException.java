package fr.frinn.custommachinery.api.crafting;

import fr.frinn.custommachinery.api.machine.ICustomMachine;
import fr.frinn.custommachinery.api.requirement.RequirementType;
import net.minecraft.resources.ResourceLocation;

public class ComponentNotFoundException extends RuntimeException {

    private final ResourceLocation recipeId;
    private final ICustomMachine machine;
    private final RequirementType<?> requirementType;

    public ComponentNotFoundException(ResourceLocation recipeId, ICustomMachine machine, RequirementType<?> requirementType) {
        this.recipeId = recipeId;
        this.machine = machine;
        this.requirementType = requirementType;
    }

    @Override
    public String getMessage() {
        return "Invalid Custom Machine recipe: " +
                this.recipeId +
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
