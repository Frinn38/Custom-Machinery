package fr.frinn.custommachinery.common.integration.jei;

import com.mojang.blaze3d.matrix.MatrixStack;
import fr.frinn.custommachinery.api.guielement.GuiElementType;
import fr.frinn.custommachinery.api.guielement.IGuiElement;
import fr.frinn.custommachinery.api.guielement.jei.IJEIElementRenderer;
import fr.frinn.custommachinery.api.guielement.jei.JEIIngredientRenderer;
import fr.frinn.custommachinery.common.crafting.CustomMachineRecipe;
import fr.frinn.custommachinery.common.data.CustomMachine;
import fr.frinn.custommachinery.common.init.CustomMachineItem;
import fr.frinn.custommachinery.common.integration.jei.wrapper.IJEIIngredientWrapper;
import mezz.jei.api.MethodsReturnNonnullByDefault;
import mezz.jei.api.gui.IRecipeLayout;
import mezz.jei.api.gui.drawable.IDrawable;
import mezz.jei.api.helpers.IGuiHelper;
import mezz.jei.api.ingredients.IIngredientRenderer;
import mezz.jei.api.ingredients.IIngredientType;
import mezz.jei.api.ingredients.IIngredients;
import mezz.jei.api.recipe.category.IRecipeCategory;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.AbstractGui;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.client.gui.GuiUtils;

import javax.annotation.ParametersAreNonnullByDefault;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

@MethodsReturnNonnullByDefault
@ParametersAreNonnullByDefault
public class CustomMachineRecipeCategory implements IRecipeCategory<CustomMachineRecipe> {

    private static final int ICON_SIZE = 10;
    private static final int TOOLTIP_WIDTH = 256;
    private static final int TOOLTIP_HEIGHT = 192;

    private final CustomMachine machine;
    private final IGuiHelper guiHelper;
    private final RecipeHelper recipeHelper;
    private int offsetX;
    private int offsetY;
    private int width;
    private int height;

    public CustomMachineRecipeCategory(CustomMachine machine, IGuiHelper guiHelper) {
        this.machine = machine;
        this.guiHelper = guiHelper;
        this.recipeHelper = new RecipeHelper(machine);
        this.setupRecipeDimensions();
    }

    //Find the minimal size for the recipe layout
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

    //Give all ingredients to jei for a recipe, used when searching for uses or recipes for an ingredient (player press U or R).
    @Override
    public void setIngredients(CustomMachineRecipe recipe, IIngredients ingredients) {
        recipe.getJEIIngredientRequirements().forEach(requirement -> requirement.getJEIIngredientWrapper().setIngredient(ingredients));
    }

    //Set slots, fluid and energy in the layout
    @Override
    public void setRecipe(IRecipeLayout layout, CustomMachineRecipe recipe, IIngredients ingredients) {
        //Set the transfer item button to its place
        layout.moveRecipeTransferButton(this.width - 11, this.height - 11);

        List<IJEIIngredientRequirement> requirements = recipe.getJEIIngredientRequirements();
        AtomicInteger index = new AtomicInteger(0);
        List<IGuiElement> elements = this.machine.getJeiElements().isEmpty() ? this.machine.getGuiElements() : this.machine.getJeiElements();
        elements.stream().filter(element -> element.getType().hasJEIRenderer()).forEach(element -> {
            JEIIngredientRenderer<?, ?> renderer = ((GuiElementType)element.getType()).getJeiRenderer(element);
            IIngredientType<?> ingredientType = renderer.getType();
            //Put each jei element to their place in the recipe gui, and use our custom ingredient renderer to pass the element to render.
            layout.getIngredientsGroup(ingredientType).init(
                    index.get(),
                    true,
                    (IIngredientRenderer)renderer,
                    element.getX() - this.offsetX,
                    element.getY() - this.offsetY,
                    element.getWidth() - 2,
                    element.getHeight() - 2,
                    0,
                    0);

            //Search for ingredients to put in the corresponding slots/fluid and energy bars.
            Iterator<IJEIIngredientRequirement> iterator = requirements.iterator();
            while (iterator.hasNext()) {
                IJEIIngredientWrapper<?> wrapper = iterator.next().getJEIIngredientWrapper();
                //Delegate the element check to the ingredient wrapper, which will delegate to the component template if needed.
                if(wrapper.getJEIIngredientType() == ingredientType && wrapper.setupRecipe(index.get(), layout, element, recipeHelper)) {
                    //If an ingredient is found for this element, remove it from the list, so it can't be added again to another element.
                    iterator.remove();
                    break;
                }
            }
            index.incrementAndGet();
        });
        //TODO: log if some requirements were not placed in any elements ?
    }

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
                    if(renderer.isHoveredInJei(element, x, y, (int)mouseX, (int)mouseY))
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
