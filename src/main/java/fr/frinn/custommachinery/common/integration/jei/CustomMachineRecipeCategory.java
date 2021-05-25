package fr.frinn.custommachinery.common.integration.jei;

import com.mojang.blaze3d.matrix.MatrixStack;
import fr.frinn.custommachinery.client.render.element.jei.IJEIElementRenderer;
import fr.frinn.custommachinery.client.render.element.jei.JEIIngredientRenderer;
import fr.frinn.custommachinery.common.crafting.CustomMachineRecipe;
import fr.frinn.custommachinery.common.crafting.requirements.IRequirement;
import fr.frinn.custommachinery.common.data.CustomMachine;
import fr.frinn.custommachinery.common.data.component.IMachineComponent;
import fr.frinn.custommachinery.common.data.component.MachineComponentManager;
import fr.frinn.custommachinery.common.data.component.handler.IComponentHandler;
import fr.frinn.custommachinery.common.data.gui.IComponentGuiElement;
import fr.frinn.custommachinery.common.data.gui.IGuiElement;
import fr.frinn.custommachinery.common.init.CustomMachineTile;
import fr.frinn.custommachinery.common.init.Registration;
import mezz.jei.api.gui.IRecipeLayout;
import mezz.jei.api.gui.drawable.IDrawable;
import mezz.jei.api.helpers.IGuiHelper;
import mezz.jei.api.ingredients.IIngredientType;
import mezz.jei.api.ingredients.IIngredients;
import mezz.jei.api.recipe.category.IRecipeCategory;
import net.minecraft.client.Minecraft;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;

import javax.annotation.ParametersAreNonnullByDefault;
import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;

public class CustomMachineRecipeCategory implements IRecipeCategory<CustomMachineRecipe> {

    private final CustomMachine machine;
    private final IGuiHelper guiHelper;
    private final MachineComponentManager components;
    private int offsetX;
    private int offsetY;
    private int width;
    private int height;

    public CustomMachineRecipeCategory(CustomMachine machine, IGuiHelper guiHelper) {
        this.machine = machine;
        this.guiHelper = guiHelper;
        this.components = new MachineComponentManager(machine.getComponentTemplates(),null);
        this.setupRecipeDimensions();
    }

    private void setupRecipeDimensions() {
        if(Minecraft.getInstance().world == null)
            return;
        int minX = Integer.MAX_VALUE;
        int minY = Integer.MAX_VALUE;
        int maxX = 0;
        int maxY = 0;
        for(IGuiElement element : this.machine.getGuiElements()) {
            if(!element.getType().hasJEIRenderer() && !(element.getType().getRenderer() instanceof IJEIElementRenderer))
                continue;
            minX = Math.min(minX, element.getX());
            minY = Math.min(minY, element.getY());
            maxX = Math.max(maxX, element.getX() + element.getWidth());
            maxY = Math.max(maxY, element.getY() + element.getHeight());
        }
        this.offsetX = minX;
        this.offsetY = minY;
        this.width = maxX - minX;
        this.height = maxY - minY;
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
        return guiHelper.createBlankDrawable(this.width, this.height);
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
        Map<IIngredientType<Object>, List<List<Object>>> ingredientInputMap = new HashMap<>();
        Map<IIngredientType<Object>, List<List<Object>>> ingredientOutputMap = new HashMap<>();
        recipe.getJEIRequirements().forEach(requirement -> {
            IIngredientType<Object> type = requirement.getJEIIngredientWrapper().getJEIIngredientType();
            if(((IRequirement<?>)requirement).getMode() == IRequirement.MODE.INPUT) {
                if(!ingredientInputMap.containsKey(type))
                    ingredientInputMap.put(type, new ArrayList<>());
                ingredientInputMap.get(type).add(requirement.getJEIIngredientWrapper().getJeiIngredients());
            } else {
                if(!ingredientOutputMap.containsKey(type))
                    ingredientOutputMap.put(type, new ArrayList<>());
                ingredientOutputMap.get(type).add(requirement.getJEIIngredientWrapper().getJeiIngredients());
            }
        });
        ingredientInputMap.forEach(ingredients::setInputLists);
        ingredientOutputMap.forEach(ingredients::setOutputLists);
    }

    @ParametersAreNonnullByDefault
    @Override
    public void setRecipe(IRecipeLayout layout, CustomMachineRecipe recipe, IIngredients ingredients) {
        List<IJEIIngredientRequirement> requirements = recipe.getJEIRequirements();
        AtomicInteger index = new AtomicInteger(0);
        this.machine.getGuiElements().stream().filter(element -> element.getType().hasJEIRenderer()).forEach(element -> {
            JEIIngredientRenderer<?, ?> renderer = element.getType().getJeiRenderer(element);
            IIngredientType ingredientType = renderer.getType();
            layout.getIngredientsGroup(ingredientType).init(
                    index.get(),
                    true,
                    renderer,
                    element.getX() - this.offsetX,
                    element.getY() - this.offsetY,
                    element.getWidth() - 2,
                    element.getHeight() - 2,
                    0,
                    0);
            Object ingredient = this.getIngredientFromRequirements(ingredientType, element, requirements);
            if(ingredient instanceof List)
                layout.getIngredientsGroup(ingredientType).set(index.get(), (List<?>)ingredient);
            else if(ingredient != null)
                layout.getIngredientsGroup(ingredientType).set(index.get(), ingredient);
            index.incrementAndGet();
        });
    }

    private Object getIngredientFromRequirements(IIngredientType<?> ingredientType, IGuiElement element, List<IJEIIngredientRequirement> requirements) {
        if(!(element instanceof IComponentGuiElement))
            return null;
        IMachineComponent.Mode elementMode = getElementMode((IComponentGuiElement<?>)element);
        for (IJEIIngredientRequirement requirement : requirements) {
            IRequirement.MODE requirementMode = getRequirementMode(requirement);
            if(requirement.getJEIIngredientWrapper().getJEIIngredientType() == ingredientType && ((elementMode.isInput() && requirementMode == IRequirement.MODE.INPUT) || (elementMode.isOutput() && requirementMode == IRequirement.MODE.OUTPUT))) {
                requirements.remove(requirement);
                return requirement.getJEIIngredientWrapper().asJEIIngredient();
            }
        }
        return null;
    }

    private IMachineComponent.Mode getElementMode(IComponentGuiElement<?> element) {
        return this.components.getComponent(element.getComponentType()).flatMap(component -> {
            if(component instanceof IComponentHandler)
                return (Optional<IMachineComponent>)((IComponentHandler<?>)component).getComponentForID(element.getID());
            return Optional.of(component);
        }).map(IMachineComponent::getMode).orElse(IMachineComponent.Mode.NONE);
    }

    private IRequirement.MODE getRequirementMode(IJEIIngredientRequirement requirement) {
        return ((IRequirement<?>)requirement).getMode();
    }

    @ParametersAreNonnullByDefault
    @Override
    public void draw(CustomMachineRecipe recipe, MatrixStack matrix, double mouseX, double mouseY) {
        matrix.push();
        matrix.translate(-this.offsetX, -this.offsetY, 0);
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
