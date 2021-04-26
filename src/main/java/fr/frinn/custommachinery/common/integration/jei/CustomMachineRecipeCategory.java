package fr.frinn.custommachinery.common.integration.jei;

import com.google.common.collect.Lists;
import com.mojang.blaze3d.matrix.MatrixStack;
import fr.frinn.custommachinery.client.render.element.jei.IJEIElementRenderer;
import fr.frinn.custommachinery.common.crafting.CustomMachineRecipe;
import fr.frinn.custommachinery.common.crafting.requirements.IRequirement;
import fr.frinn.custommachinery.common.data.CustomMachine;
import fr.frinn.custommachinery.common.data.gui.IGuiElement;
import fr.frinn.custommachinery.common.init.Registration;
import mezz.jei.api.gui.IRecipeLayout;
import mezz.jei.api.gui.drawable.IDrawable;
import mezz.jei.api.helpers.IGuiHelper;
import mezz.jei.api.ingredients.IIngredientType;
import mezz.jei.api.ingredients.IIngredients;
import mezz.jei.api.recipe.category.IRecipeCategory;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;

import javax.annotation.ParametersAreNonnullByDefault;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

public class CustomMachineRecipeCategory implements IRecipeCategory<CustomMachineRecipe> {

    private final CustomMachine machine;
    private final IGuiHelper guiHelper;

    public CustomMachineRecipeCategory(CustomMachine machine, IGuiHelper guiHelper) {
        this.machine = machine;
        this.guiHelper = guiHelper;
    }

    @Override
    public ResourceLocation getUid() {
        return this.machine.getId();
    }

    @Override
    public Class<? extends CustomMachineRecipe> getRecipeClass() {
        return CustomMachineRecipe.class;
    }

    @Override
    public String getTitle() {
        return this.machine.getName();
    }

    @Override
    public IDrawable getBackground() {
        return guiHelper.createBlankDrawable(128, 96);
    }

    @Override
    public IDrawable getIcon() {
        ItemStack stack = Registration.CUSTOM_MACHINE_ITEM.get().getDefaultInstance();
        stack.getOrCreateTag().putString("id", this.machine.getId().toString());
        return this.guiHelper.createDrawableIngredient(stack);
    }

    @ParametersAreNonnullByDefault
    @Override
    public void setIngredients(CustomMachineRecipe recipe, IIngredients ingredients) {
        recipe.getJEIRequirements().forEach(requirement -> requirement.addJeiIngredients(ingredients));
    }

    @ParametersAreNonnullByDefault
    @Override
    public void setRecipe(IRecipeLayout layout, CustomMachineRecipe recipe, IIngredients ingredients) {
        List<IJEIIngredientRequirement> requirements = recipe.getJEIRequirements();
        AtomicInteger index = new AtomicInteger(0);
        this.machine.getGuiElements().stream().filter(element -> element.getType().getJeiIngredientType() != null).forEach(element -> {
            IIngredientType ingredientType = element.getType().getJeiIngredientType();
            layout.getIngredientsGroup(ingredientType).init(
                    index.get(),
                    true,
                    element.getType().getJeiRenderer(element),
                    element.getX() / 2,
                    element.getY() / 2,
                    (element.getWidth() - 2) / 2,
                    (element.getHeight() - 2) / 2,
                    0,
                    0);
            Object ingredient = this.getIngredientFromRequirements(ingredientType, requirements);
            if(ingredient instanceof List)
                layout.getIngredientsGroup(ingredientType).set(index.get(), (List<?>)ingredient);
            else
                layout.getIngredientsGroup(ingredientType).set(index.get(), ingredient);
            index.incrementAndGet();
        });
    }

    private Object getIngredientFromRequirements(IIngredientType<?> ingredientType, List<IJEIIngredientRequirement> requirements) {
        for (IJEIIngredientRequirement requirement : requirements) {
            if(requirement.getJEIIngredientType() == ingredientType) {
                requirements.remove(requirement);
                return requirement.asJEIIngredient();
            }
        }
        return null;
    }

    @ParametersAreNonnullByDefault
    @Override
    public void draw(CustomMachineRecipe recipe, MatrixStack matrix, double mouseX, double mouseY) {
        matrix.push();
        matrix.scale(0.5F, 0.5F, 0.5F);
        this.machine.getGuiElements().stream()
                .filter(element -> element.getType().getRenderer() instanceof IJEIElementRenderer)
                .forEach(element -> ((IJEIElementRenderer)element.getType().getRenderer()).renderElementInJEI(matrix, element, recipe, (int)mouseX, (int)mouseY));
        matrix.pop();
    }

    @ParametersAreNonnullByDefault
    @Override
    public boolean handleClick(CustomMachineRecipe recipe, double mouseX, double mouseY, int mouseButton) {
        return false;
    }
}
