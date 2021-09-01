package fr.frinn.custommachinery.common.integration.jei;

import com.mojang.blaze3d.matrix.MatrixStack;
import fr.frinn.custommachinery.api.components.IFilterComponent;
import fr.frinn.custommachinery.api.components.IMachineComponent;
import fr.frinn.custommachinery.api.components.handler.IComponentHandler;
import fr.frinn.custommachinery.client.render.element.jei.IJEIElementRenderer;
import fr.frinn.custommachinery.client.render.element.jei.JEIIngredientRenderer;
import fr.frinn.custommachinery.common.crafting.CustomMachineRecipe;
import fr.frinn.custommachinery.common.crafting.requirements.IRequirement;
import fr.frinn.custommachinery.common.data.CustomMachine;
import fr.frinn.custommachinery.common.data.component.MachineComponentManager;
import fr.frinn.custommachinery.common.data.gui.IComponentGuiElement;
import fr.frinn.custommachinery.common.data.gui.IGuiElement;
import fr.frinn.custommachinery.common.init.CustomMachineItem;
import fr.frinn.custommachinery.common.init.Registration;
import fr.frinn.custommachinery.common.integration.jei.wrapper.IJEIIngredientWrapper;
import mezz.jei.api.gui.IRecipeLayout;
import mezz.jei.api.gui.drawable.IDrawable;
import mezz.jei.api.helpers.IGuiHelper;
import mezz.jei.api.ingredients.IIngredientType;
import mezz.jei.api.ingredients.IIngredients;
import mezz.jei.api.recipe.category.IRecipeCategory;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.AbstractGui;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.client.gui.GuiUtils;

import javax.annotation.ParametersAreNonnullByDefault;
import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Predicate;

public class CustomMachineRecipeCategory implements IRecipeCategory<CustomMachineRecipe> {

    private static final int ICON_SIZE = 10;
    private static final int TOOLTIP_WIDTH = 256;
    private static final int TOOLTIP_HEIGHT = 192;

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
        List<IGuiElement> elements = this.machine.getJeiElements().isEmpty() ? this.machine.getGuiElements() : this.machine.getJeiElements();
        for(IGuiElement element : elements) {
            if(!element.getType().hasJEIRenderer() && !(element.getType().getRenderer() instanceof IJEIElementRenderer))
                continue;
            minX = Math.min(minX, element.getX());
            minY = Math.min(minY, element.getY());
            maxX = Math.max(maxX, element.getX() + element.getWidth());
            maxY = Math.max(maxY, element.getY() + element.getHeight());
        }
        this.offsetX = Math.max(minX, 0);
        this.offsetY = Math.max(minY, 0);
        this.width = Math.max(maxX - minX, 20);
        this.height = Math.max(maxY - minY, 20) + ICON_SIZE + 4;
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
        return this.machine.getName().getString();
    }

    @Override
    public IDrawable getBackground() {
        return guiHelper.createBlankDrawable(this.width, this.height);
    }

    @Override
    public IDrawable getIcon() {
        return this.guiHelper.createDrawableIngredient(CustomMachineItem.makeMachineItem(this.machine.getId()));
    }

    @ParametersAreNonnullByDefault
    @Override
    public void setIngredients(CustomMachineRecipe recipe, IIngredients ingredients) {
        Map<IIngredientType<Object>, List<List<Object>>> ingredientInputMap = new HashMap<>();
        Map<IIngredientType<Object>, List<List<Object>>> ingredientOutputMap = new HashMap<>();
        recipe.getJEIIngredientRequirements().forEach(requirement -> {
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
        List<IJEIIngredientRequirement> requirements = recipe.getJEIIngredientRequirements();
        AtomicInteger index = new AtomicInteger(0);
        List<IGuiElement> elements = this.machine.getJeiElements().isEmpty() ? this.machine.getGuiElements() : this.machine.getJeiElements();
        elements.stream().filter(element -> element.getType().hasJEIRenderer()).forEach(element -> {
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
        IComponentGuiElement<?> componentElement = (IComponentGuiElement<?>)element;
        for (IJEIIngredientRequirement requirement : requirements) {
            IJEIIngredientWrapper<?> wrapper = requirement.getJEIIngredientWrapper();
            if(wrapper.getJEIIngredientType() == ingredientType && ((componentElement.getID().equals(wrapper.getComponentID()) || wrapper.getComponentID().isEmpty()) && testMode(componentElement, requirement))) {
                requirements.remove(requirement);
                return wrapper.asJEIIngredient();
            }
        }
        return null;
    }

    private boolean testMode(IComponentGuiElement<?> element, IJEIIngredientRequirement requirement) {
        return this.components.getComponent(element.getComponentType()).flatMap(component -> {
            if(component instanceof IComponentHandler)
                return (Optional<IMachineComponent>)((IComponentHandler<?>)component).getComponentForID(element.getID());
            return Optional.of(component);
        }).map(component -> {
            IRequirement.MODE requirementMode = ((IRequirement<?>)requirement).getMode();
            if(component instanceof IFilterComponent) {
                Predicate<Object> filter = ((IFilterComponent)component).getFilter();
                if(!filter.test(requirement.getJEIIngredientWrapper().asJEIIngredient()))
                    return false;
            }
            if(((IRequirement<?>) requirement).getType() == Registration.DURABILITY_REQUIREMENT.get())
                return component.getMode().isInput();
            return (component.getMode().isInput() && requirementMode == IRequirement.MODE.INPUT) || (component.getMode().isOutput() && requirementMode == IRequirement.MODE.OUTPUT);
        }).orElse(false);
    }

    @ParametersAreNonnullByDefault
    @Override
    public void draw(CustomMachineRecipe recipe, MatrixStack matrix, double mouseX, double mouseY) {
        //Render elements that doesn't have an ingredient/requirement such as the progress bar element
        List<IGuiElement> elements = this.machine.getJeiElements().isEmpty() ? this.machine.getGuiElements() : this.machine.getJeiElements();
        elements.stream()
                .filter(element -> element.getType().getRenderer() instanceof IJEIElementRenderer)
                .forEach(element -> {
                    int x = element.getX() - this.offsetX;
                    int y = element.getY() - this.offsetY;
                    IJEIElementRenderer<IGuiElement> renderer = (IJEIElementRenderer<IGuiElement>)element.getType().getRenderer();
                    matrix.push();
                    matrix.translate(-this.offsetX, -this.offsetY, 0);
                    renderer.renderElementInJEI(matrix, element, recipe, (int)mouseX, (int)mouseY);
                    matrix.pop();
                    if(mouseX >= x && mouseX <= x + element.getWidth() && mouseY >= y && mouseY <= y + element.getHeight())
                        GuiUtils.drawHoveringText(matrix, renderer.getJEITooltips(element, recipe), (int)mouseX, (int)mouseY, TOOLTIP_WIDTH, TOOLTIP_HEIGHT, TOOLTIP_WIDTH, Minecraft.getInstance().fontRenderer);
                });

        //Render the line between the gui elements and the requirements icons
        AbstractGui.fill(matrix, -3, this.height - ICON_SIZE - 3, this.width + 3, this.height - ICON_SIZE - 2, 0x30000000);

        //Render the requirements that doesn't have a gui element such as command, position, weather etc... with a little icon and a tooltip
        AtomicInteger index = new AtomicInteger();
        recipe.getDisplayInfoRequirements().stream().map(IDisplayInfoRequirement::getDisplayInfo).filter(RequirementDisplayInfo::isVisible).forEach(info -> {
            int x = index.get() * (ICON_SIZE + 2);
            if(info.getIcon() != null) {
                Minecraft.getInstance().getTextureManager().bindTexture(info.getIcon());
                AbstractGui.blit(matrix, x, this.height - ICON_SIZE, info.getU(), info.getV(), ICON_SIZE, ICON_SIZE, ICON_SIZE, ICON_SIZE);
            }
            if(mouseX >= x && mouseX <= x + ICON_SIZE && mouseY >= this.height - ICON_SIZE && mouseY <= this.height && !info.getTooltips().isEmpty() && Minecraft.getInstance().currentScreen != null)
                GuiUtils.drawHoveringText(matrix, info.getTooltips(), (int)mouseX, (int)mouseY, TOOLTIP_WIDTH, TOOLTIP_HEIGHT, TOOLTIP_WIDTH, Minecraft.getInstance().fontRenderer);
            index.incrementAndGet();
        });
    }

    @ParametersAreNonnullByDefault
    @Override
    public boolean handleClick(CustomMachineRecipe recipe, double mouseX, double mouseY, int mouseButton) {
        AtomicInteger index = new AtomicInteger();
        return recipe.getDisplayInfoRequirements().stream().map(IDisplayInfoRequirement::getDisplayInfo).filter(RequirementDisplayInfo::isVisible).anyMatch(info -> {
            int x = index.get() * (ICON_SIZE + 2);
            if(mouseX >= x && mouseX <= x + ICON_SIZE && mouseY >= this.height - ICON_SIZE && mouseY <= this.height)
                return info.handleClick(this.machine, mouseButton);
            index.incrementAndGet();
            return false;
        });
    }
}
