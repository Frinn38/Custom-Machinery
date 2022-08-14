package fr.frinn.custommachinery.client.integration.jei;

import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import com.google.common.collect.ImmutableList;
import com.mojang.blaze3d.platform.InputConstants;
import com.mojang.blaze3d.vertex.PoseStack;
import fr.frinn.custommachinery.api.guielement.IGuiElement;
import fr.frinn.custommachinery.api.integration.jei.IDisplayInfoRequirement;
import fr.frinn.custommachinery.api.integration.jei.IJEIElementRenderer;
import fr.frinn.custommachinery.api.integration.jei.IJEIIngredientRequirement;
import fr.frinn.custommachinery.api.integration.jei.IJEIIngredientWrapper;
import fr.frinn.custommachinery.common.crafting.CustomMachineRecipe;
import fr.frinn.custommachinery.common.init.CustomMachineItem;
import fr.frinn.custommachinery.common.init.Registration;
import fr.frinn.custommachinery.common.machine.CustomMachine;
import mezz.jei.api.constants.VanillaTypes;
import mezz.jei.api.gui.builder.IRecipeLayoutBuilder;
import mezz.jei.api.gui.drawable.IDrawable;
import mezz.jei.api.gui.ingredient.IRecipeSlotsView;
import mezz.jei.api.helpers.IGuiHelper;
import mezz.jei.api.helpers.IJeiHelpers;
import mezz.jei.api.recipe.IFocusGroup;
import mezz.jei.api.recipe.RecipeType;
import mezz.jei.api.recipe.category.IRecipeCategory;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiComponent;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

public class CustomMachineRecipeCategory implements IRecipeCategory<CustomMachineRecipe> {

    private static final int ICON_SIZE = 10;
    private static final int TOOLTIP_WIDTH = 256;
    private static final int TOOLTIP_HEIGHT = 192;

    private final CustomMachine machine;
    private final RecipeType<CustomMachineRecipe> recipeType;
    private final IGuiHelper guiHelper;
    private final RecipeHelper recipeHelper;
    private final LoadingCache<IDisplayInfoRequirement, RequirementDisplayInfo> infoCache;
    private final LoadingCache<CustomMachineRecipe, List<IJEIIngredientWrapper<?>>> wrapperCache;
    private int offsetX;
    private int offsetY;
    private int width;
    private int height;
    private int rowY;
    private int maxIconPerRow;

    public CustomMachineRecipeCategory(CustomMachine machine, RecipeType<CustomMachineRecipe> type, IJeiHelpers helpers) {
        this.machine = machine;
        this.recipeType = type;
        this.guiHelper = helpers.getGuiHelper();
        this.recipeHelper = new RecipeHelper(machine);
        this.setupRecipeDimensions();
        this.infoCache = CacheBuilder.newBuilder().build(new CacheLoader<IDisplayInfoRequirement, RequirementDisplayInfo>() {
            @Override
            public RequirementDisplayInfo load(IDisplayInfoRequirement requirement) {
                RequirementDisplayInfo info = new RequirementDisplayInfo();
                requirement.getDisplayInfo(info);
                return info;
            }
        });
        this.wrapperCache = CacheBuilder.newBuilder().build(new CacheLoader<CustomMachineRecipe, List<IJEIIngredientWrapper<?>>>() {
            @Override
            public List<IJEIIngredientWrapper<?>> load(CustomMachineRecipe recipe) {
                ImmutableList.Builder<IJEIIngredientWrapper<?>> wrappers = ImmutableList.builder();
                recipe.getJEIIngredientRequirements().forEach(requirement -> wrappers.addAll(requirement.getJEIIngredientWrappers()));
                return wrappers.build();
            }
        });
    }

    //Find the minimal size for the recipe layout
    private void setupRecipeDimensions() {
        if(Minecraft.getInstance().level == null)
            return;
        int minX = Integer.MAX_VALUE;
        int minY = Integer.MAX_VALUE;
        int maxX = 0;
        int maxY = 0;
        List<IGuiElement> elements = this.machine.getJeiElements().isEmpty() ? this.machine.getGuiElements() : this.machine.getJeiElements();
        for(IGuiElement element : elements) {
            if(!(element.getType().getRenderer() instanceof IJEIElementRenderer))
                continue;
            minX = Math.min(minX, element.getX());
            minY = Math.min(minY, element.getY());
            maxX = Math.max(maxX, element.getX() + element.getWidth());
            maxY = Math.max(maxY, element.getY() + element.getHeight());
        }

        this.rowY = Math.max(maxY - minY, 20);
        this.offsetX = Math.max(minX, 0);
        this.offsetY = Math.max(minY, 0);
        this.width = Math.max(maxX - minX, 20);
        this.maxIconPerRow = this.width / (ICON_SIZE + 2);
        int maxDisplayRequirement = Minecraft.getInstance().level.getRecipeManager().getAllRecipesFor(Registration.CUSTOM_MACHINE_RECIPE.get())
                .stream()
                .filter(recipe -> recipe.getMachine().equals(this.machine.getId()))
                .mapToInt(recipe -> recipe.getDisplayInfoRequirements().size())
                .max()
                .orElse(1);
        int rows = maxDisplayRequirement / this.maxIconPerRow + 1;
        this.height = this.rowY + (ICON_SIZE + 2) * rows;
    }

    @Override
    public RecipeType<CustomMachineRecipe> getRecipeType() {
        return this.recipeType;
    }

    //Safe delete when needed
    @SuppressWarnings("removal")
    @Override
    public ResourceLocation getUid() {
        return getRecipeType().getUid();
    }

    //Safe delete when needed
    @SuppressWarnings("removal")
    @Override
    public Class<? extends CustomMachineRecipe> getRecipeClass() {
        return getRecipeType().getRecipeClass();
    }

    @Override
    public Component getTitle() {
        return this.machine.getName();
    }

    @Override
    public IDrawable getBackground() {
        return this.guiHelper.createBlankDrawable(this.width, this.height);
    }

    @Override
    public IDrawable getIcon() {
        return this.guiHelper.createDrawableIngredient(VanillaTypes.ITEM_STACK, CustomMachineItem.makeMachineItem(this.machine.getId()));
    }

    @Override
    public void setRecipe(IRecipeLayoutBuilder builder, CustomMachineRecipe recipe, IFocusGroup focuses) {
        builder.moveRecipeTransferButton(this.width - 11, this.height - 11);

        List<IJEIIngredientWrapper<?>> wrappers = new ArrayList<>(this.wrapperCache.getUnchecked(recipe));
        List<IGuiElement> elements = this.machine.getJeiElements().isEmpty() ? this.machine.getGuiElements() : this.machine.getJeiElements();

        elements.forEach(element -> {
            //Search for ingredients to put in the corresponding slots/fluid and energy bars.
            Iterator<IJEIIngredientWrapper<?>> iterator = wrappers.iterator();
            while (iterator.hasNext()) {
                IJEIIngredientWrapper<?> wrapper = iterator.next();
                //Delegate the element check to the ingredient wrapper, which will delegate to the component template if needed.
                if(wrapper.setupRecipe(builder, this.offsetX, this.offsetY, element, this.recipeHelper)) {
                    //If an ingredient is found for this element, remove it from the list, so it can't be added again to another element.
                    iterator.remove();
                    break;
                }
            }
        });
    }

    @Override
    public void draw(CustomMachineRecipe recipe, IRecipeSlotsView slotsView, PoseStack matrix, double mouseX, double mouseY) {
        //Render elements that doesn't have an ingredient/requirement such as the progress bar element
        List<IGuiElement> elements = this.machine.getJeiElements().isEmpty() ? this.machine.getGuiElements() : this.machine.getJeiElements();
        elements.stream()
                .filter(element -> element.getType().getRenderer() instanceof IJEIElementRenderer)
                .forEach(element -> {
                    int x = element.getX() - this.offsetX;
                    int y = element.getY() - this.offsetY;
                    IJEIElementRenderer<IGuiElement> renderer = (IJEIElementRenderer<IGuiElement>)element.getType().getRenderer();
                    matrix.pushPose();
                    matrix.translate(-this.offsetX, -this.offsetY, 0);
                    renderer.renderElementInJEI(matrix, element, recipe, (int)mouseX, (int)mouseY);
                    matrix.popPose();
                });

        //Render the line between the gui elements and the requirements icons
        GuiComponent.fill(matrix, -3, this.rowY, this.width + 3, this.rowY + 1, 0x30000000);

        //Render the requirements that don't have a gui element such as command, position, weather etc... with a little icon and a tooltip
        AtomicInteger index = new AtomicInteger();
        AtomicInteger row = new AtomicInteger(0);
        recipe.getDisplayInfoRequirements().stream().map(this.infoCache).forEach(info -> {
            int x = index.get() * (ICON_SIZE + 2) - 2;
            int y = this.rowY + 2 + (ICON_SIZE + 2) * row.get();
            if(index.incrementAndGet() >= this.maxIconPerRow) {
                index.set(0);
                row.incrementAndGet();
            }
            matrix.pushPose();
            matrix.translate(x, y, 0.0D);
            info.renderIcon(matrix, ICON_SIZE);
            matrix.popPose();
        });
    }

    @Override
    public List<Component> getTooltipStrings(CustomMachineRecipe recipe, IRecipeSlotsView view, double mouseX, double mouseY) {
        //First, check if any gui element is hovered and if so return its tooltips.
        List<IGuiElement> elements = this.machine.getJeiElements().isEmpty() ? this.machine.getGuiElements() : this.machine.getJeiElements();
        for(IGuiElement element : elements) {
            if(element.getType().getRenderer() instanceof IJEIElementRenderer) {
                IJEIElementRenderer<IGuiElement> renderer = (IJEIElementRenderer<IGuiElement>) element.getType().getRenderer();
                int x = element.getX() - this.offsetX;
                int y = element.getY() - this.offsetY;
                if(renderer.isHoveredInJei(element, x, y, (int)mouseX, (int)mouseY))
                    return renderer.getJEITooltips(element, recipe);
            }
        }

        //Then do the same with display info requirements.
        int index = 0;
        int row = 0;
        for(RequirementDisplayInfo info : recipe.getDisplayInfoRequirements().stream().map(this.infoCache).toList()) {
            int x = index * (ICON_SIZE + 2) - 2;
            int y = this.rowY + 2 + (ICON_SIZE + 2) * row;
            if(index++ >= this.maxIconPerRow) {
                index = 0;
                row++;
            }

            if(mouseX >= x && mouseX <= x + ICON_SIZE && mouseY >= y && mouseY <= y + ICON_SIZE && Minecraft.getInstance().screen != null)
                return info.getTooltips();
        }

        //If the mouse hover nothing from cm, return no tooltips.
        return Collections.emptyList();
    }

    @Override
    public boolean handleInput(CustomMachineRecipe recipe, double mouseX, double mouseY, InputConstants.Key mouseButton) {
        AtomicInteger index = new AtomicInteger();
        AtomicInteger row = new AtomicInteger(0);
        return recipe.getDisplayInfoRequirements().stream().map(this.infoCache).anyMatch(info -> {
            int x = index.get() * (ICON_SIZE + 2) - 2;
            int y = this.rowY + 2 + (ICON_SIZE + 2) * row.get();
            if(index.incrementAndGet() >= this.maxIconPerRow) {
                index.set(0);
                row.incrementAndGet();
            }
            if(mouseX >= x && mouseX <= x + ICON_SIZE && mouseY >= y && mouseY <= y + ICON_SIZE && Minecraft.getInstance().screen != null)
                return info.handleClick(this.machine, mouseButton);
            return false;
        });
    }
}
