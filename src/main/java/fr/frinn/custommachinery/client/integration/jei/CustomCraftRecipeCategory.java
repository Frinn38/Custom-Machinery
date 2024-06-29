package fr.frinn.custommachinery.client.integration.jei;

import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.collect.ImmutableList;
import fr.frinn.custommachinery.api.component.IMachineComponentTemplate;
import fr.frinn.custommachinery.api.integration.jei.IJEIIngredientWrapper;
import fr.frinn.custommachinery.api.requirement.RequirementIOMode;
import fr.frinn.custommachinery.client.integration.jei.wrapper.ItemIngredientWrapper;
import fr.frinn.custommachinery.common.component.item.ItemMachineComponent;
import fr.frinn.custommachinery.common.crafting.craft.CustomCraftRecipe;
import fr.frinn.custommachinery.common.init.Registration;
import fr.frinn.custommachinery.common.machine.CustomMachine;
import mezz.jei.api.helpers.IJeiHelpers;
import mezz.jei.api.recipe.RecipeType;
import net.minecraft.world.item.crafting.Ingredient;

import java.util.List;

public class CustomCraftRecipeCategory extends AbstractRecipeCategory<CustomCraftRecipe> {

    public CustomCraftRecipeCategory(CustomMachine machine, RecipeType<CustomCraftRecipe> type, IJeiHelpers helpers) {
        super(machine, type, helpers);

        this.wrapperCache = CacheBuilder.newBuilder().build(new CacheLoader<>() {
            @Override
            public List<IJEIIngredientWrapper<?>> load(CustomCraftRecipe recipe) {
                ImmutableList.Builder<IJEIIngredientWrapper<?>> wrappers = ImmutableList.builder();
                recipe.getJEIIngredientRequirements().forEach(requirement -> wrappers.addAll(requirement.getJEIIngredientWrappers(recipe)));
                String resultSlot = machine.getComponentTemplates().stream()
                        .filter(template -> template instanceof ItemMachineComponent.Template slotTemplate && slotTemplate.getType() == Registration.ITEM_RESULT_MACHINE_COMPONENT.get())
                        .findFirst()
                        .map(IMachineComponentTemplate::getId)
                        .orElse("");

                wrappers.add(new ItemIngredientWrapper(RequirementIOMode.OUTPUT, Ingredient.of(recipe.getOutput()), recipe.getOutput().getCount(), 1.0, false, resultSlot, false));
                return wrappers.build();
            }
        });
    }
}
