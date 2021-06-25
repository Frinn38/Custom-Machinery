package fr.frinn.custommachinery.common.integration.jei;

import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.systems.RenderSystem;
import fr.frinn.custommachinery.client.render.CustomMachineRenderer;
import fr.frinn.custommachinery.client.render.element.jei.IJEIElementRenderer;
import fr.frinn.custommachinery.client.render.element.jei.JEIIngredientRenderer;
import fr.frinn.custommachinery.common.crafting.CustomMachineRecipe;
import fr.frinn.custommachinery.common.crafting.requirements.BlockRequirement;
import fr.frinn.custommachinery.common.crafting.requirements.IRequirement;
import fr.frinn.custommachinery.common.data.CustomMachine;
import fr.frinn.custommachinery.common.data.component.IMachineComponent;
import fr.frinn.custommachinery.common.data.component.MachineComponentManager;
import fr.frinn.custommachinery.common.data.component.handler.IComponentHandler;
import fr.frinn.custommachinery.common.data.gui.IComponentGuiElement;
import fr.frinn.custommachinery.common.data.gui.IGuiElement;
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
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.client.gui.GuiUtils;

import javax.annotation.ParametersAreNonnullByDefault;
import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;

public class CustomMachineRecipeCategory implements IRecipeCategory<CustomMachineRecipe> {

    private static final int ICON_SIZE = 10;
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
        recipe.getJEIIngredientRequirements().forEach(requirement -> {
            IIngredientType<Object> ACTION = requirement.getJEIIngredientWrapper().getJEIIngredientType();
            if(((IRequirement<?>)requirement).getMode() == IRequirement.MODE.INPUT) {
                if(!ingredientInputMap.containsKey(ACTION))
                    ingredientInputMap.put(ACTION, new ArrayList<>());
                ingredientInputMap.get(ACTION).add(requirement.getJEIIngredientWrapper().getJeiIngredients());
            } else {
                if(!ingredientOutputMap.containsKey(ACTION))
                    ingredientOutputMap.put(ACTION, new ArrayList<>());
                ingredientOutputMap.get(ACTION).add(requirement.getJEIIngredientWrapper().getJeiIngredients());
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
        IComponentGuiElement<?> componentElement = (IComponentGuiElement<?>)element;
        for (IJEIIngredientRequirement requirement : requirements) {
            IJEIIngredientWrapper<?> wrapper = requirement.getJEIIngredientWrapper();
            if(wrapper.getJEIIngredientType() == ingredientType && testMode(componentElement, requirement) && (wrapper.getComponentID().isEmpty() || componentElement.getID().equals(wrapper.getComponentID()))) {
                requirements.remove(requirement);
                return wrapper.asJEIIngredient();
            }
        }
        return null;
    }

    private boolean testMode(IComponentGuiElement<?> element, IJEIIngredientRequirement requirement) {
        IMachineComponent.Mode elementMode = this.components.getComponent(element.getComponentType()).flatMap(component -> {
            if(component instanceof IComponentHandler)
                return (Optional<IMachineComponent>)((IComponentHandler<?>)component).getComponentForID(element.getID());
            return Optional.of(component);
        }).map(IMachineComponent::getMode).orElse(IMachineComponent.Mode.NONE);
        IRequirement.MODE requirementMode = ((IRequirement<?>)requirement).getMode();
        return (elementMode.isInput() && requirementMode == IRequirement.MODE.INPUT) || (elementMode.isOutput() && requirementMode == IRequirement.MODE.OUTPUT);
    }

    @ParametersAreNonnullByDefault
    @Override
    public void draw(CustomMachineRecipe recipe, MatrixStack matrix, double mouseX, double mouseY) {
        //Render elements that doesn't have an ingredient/requirement such as the progress bar element
        matrix.push();
        matrix.translate(-this.offsetX, -this.offsetY, 0);
        this.machine.getGuiElements().stream()
                .filter(element -> element.getType().getRenderer() instanceof IJEIElementRenderer)
                .forEach(element -> ((IJEIElementRenderer)element.getType().getRenderer()).renderElementInJEI(matrix, element, recipe, (int)mouseX, (int)mouseY));
        matrix.pop();

        //Render the line between the gui elements and the requirements icons
        AbstractGui.fill(matrix, -3, this.height - ICON_SIZE - 3, this.width + 3, this.height - ICON_SIZE - 2, 0x30000000);

        //Render the requirements that doesn't have a gui element such as command, position, weather etc... with a little icon and a tooltip
        AtomicInteger index = new AtomicInteger();
        RenderSystem.disableDepthTest();
        recipe.getDisplayInfoRequirements().stream().map(IDisplayInfoRequirement::getDisplayInfo).forEach(info -> {
            int x = index.get() * (ICON_SIZE + 2);
            if(info.getIcon() != null) {
                Minecraft.getInstance().getTextureManager().bindTexture(info.getIcon());
                AbstractGui.blit(matrix, x, this.height - ICON_SIZE, info.getU(), info.getV(), ICON_SIZE, ICON_SIZE, ICON_SIZE, ICON_SIZE);
            }
            if(mouseX >= x && mouseX <= x + ICON_SIZE && mouseY >= this.height - ICON_SIZE && mouseY <= this.height && !info.getTooltips().isEmpty() && Minecraft.getInstance().currentScreen != null) {
                GuiUtils.drawHoveringText(matrix, info.getTooltips(), (int)mouseX, (int)mouseY, this.width * 2, this.height, this.width, Minecraft.getInstance().fontRenderer);
            }
            index.incrementAndGet();
        });
        RenderSystem.enableDepthTest();
    }

    @ParametersAreNonnullByDefault
    @Override
    public boolean handleClick(CustomMachineRecipe recipe, double mouseX, double mouseY, int mouseButton) {
        List<IDisplayInfoRequirement<?>> requirements = recipe.getDisplayInfoRequirements();
        for(int i = 0; i < requirements.size(); i++) {
            int x = i * (ICON_SIZE + 2);
            IDisplayInfoRequirement<?> requirement = requirements.get(i);
            if(requirement.getType() == Registration.BLOCK_REQUIREMENT.get() && mouseX >= x && mouseX <= x + ICON_SIZE && mouseY >= this.height - ICON_SIZE && mouseY <= this.height) {
                CustomMachineRenderer.addRenderBox(this.machine.getId(), ((BlockRequirement)requirement).getBox());
                return true;
            }
        }
        return false;
    }
}
