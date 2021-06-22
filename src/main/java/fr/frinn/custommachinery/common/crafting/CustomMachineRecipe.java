package fr.frinn.custommachinery.common.crafting;

import fr.frinn.custommachinery.common.crafting.requirements.IRequirement;
import fr.frinn.custommachinery.common.data.component.IMachineComponent;
import fr.frinn.custommachinery.common.init.CustomMachineTile;
import fr.frinn.custommachinery.common.init.Registration;
import fr.frinn.custommachinery.common.integration.jei.IJEIIngredientRequirement;
import fr.frinn.custommachinery.common.integration.jei.IJEIRequirement;
import fr.frinn.custommachinery.common.util.Comparators;
import net.minecraft.item.crafting.IRecipeSerializer;
import net.minecraft.item.crafting.IRecipeType;
import net.minecraft.util.ResourceLocation;

import java.util.List;
import java.util.stream.Collectors;

public class CustomMachineRecipe extends DummyRecipe {

    private ResourceLocation id;
    private ResourceLocation machine;
    private int time;
    private List<IRequirement<?>> requirements;
    private int priority;

    public CustomMachineRecipe(ResourceLocation id, ResourceLocation machine, int time, List<IRequirement<?>> requirements, int priority) {
        this.id = id;
        this.machine = machine;
        this.time = time;
        this.requirements = requirements.stream().sorted(Comparators.REQUIREMENT_COMPARATOR).collect(Collectors.toList());
        this.priority = priority;
    }

    public ResourceLocation getMachine() {
        return this.machine;
    }

    @Override
    public ResourceLocation getId() {
        return this.id;
    }

    public void setId(ResourceLocation id) {
        this.id = id;
    }

    public int getRecipeTime() {
        return this.time;
    }

    public List<IRequirement<?>> getRequirements() {
        return this.requirements;
    }

    public List<IJEIIngredientRequirement> getJEIIngredientRequirements() {
        return this.requirements.stream().filter(requirement -> requirement instanceof IJEIIngredientRequirement).map(requirement -> (IJEIIngredientRequirement)requirement).collect(Collectors.toList());
    }

    public List<IJEIRequirement> getJEIRequirements() {
        return this.requirements.stream().filter(requirement -> requirement instanceof IJEIRequirement).map(requirement -> (IJEIRequirement)requirement).collect(Collectors.toList());
    }

    public boolean matches(CustomMachineTile tile, CraftingContext context) {
        return this.getMachine().equals(tile.getMachine().getId()) && this.requirements.stream().allMatch(requirement -> {
            if(tile.componentManager.hasComponent(requirement.getComponentType())) {
                 IMachineComponent component = tile.componentManager.getComponentRaw(requirement.getComponentType());
                 return ((IRequirement)requirement).test(component, context);
            }
            else return false;
        });
    }

    public int getPriority() {
        return this.priority;
    }

    @Override
    public IRecipeSerializer<?> getSerializer() {
        return Registration.CUSTOM_MACHINE_RECIPE_SERIALIZER.get();
    }

    @Override
    public IRecipeType<?> getType() {
        return Registration.CUSTOM_MACHINE_RECIPE;
    }
}
