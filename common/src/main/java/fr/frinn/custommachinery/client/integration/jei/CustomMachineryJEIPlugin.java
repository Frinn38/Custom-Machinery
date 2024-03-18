package fr.frinn.custommachinery.client.integration.jei;

import com.google.common.collect.Lists;
import dev.architectury.registry.fuel.FuelRegistry;
import fr.frinn.custommachinery.CustomMachinery;
import fr.frinn.custommachinery.api.ICustomMachineryAPI;
import fr.frinn.custommachinery.api.guielement.IGuiElement;
import fr.frinn.custommachinery.client.integration.jei.energy.EnergyIngredientHelper;
import fr.frinn.custommachinery.client.integration.jei.experience.ExperienceIngredientHelper;
import fr.frinn.custommachinery.client.screen.CustomMachineScreen;
import fr.frinn.custommachinery.common.crafting.craft.CustomCraftRecipe;
import fr.frinn.custommachinery.common.crafting.machine.CustomMachineRecipe;
import fr.frinn.custommachinery.common.guielement.ProgressBarGuiElement;
import fr.frinn.custommachinery.common.init.CustomMachineItem;
import fr.frinn.custommachinery.common.init.Registration;
import fr.frinn.custommachinery.common.util.Comparators;
import fr.frinn.custommachinery.common.util.slot.FilterSlotItemComponent;
import fr.frinn.custommachinery.impl.integration.jei.CustomIngredientTypes;
import fr.frinn.custommachinery.impl.integration.jei.WidgetToJeiIngredientRegistry;
import mezz.jei.api.IModPlugin;
import mezz.jei.api.JeiPlugin;
import mezz.jei.api.constants.VanillaTypes;
import mezz.jei.api.gui.handlers.IGhostIngredientHandler;
import mezz.jei.api.gui.handlers.IGuiClickableArea;
import mezz.jei.api.gui.handlers.IGuiContainerHandler;
import mezz.jei.api.ingredients.ITypedIngredient;
import mezz.jei.api.recipe.IFocusFactory;
import mezz.jei.api.recipe.RecipeIngredientRole;
import mezz.jei.api.recipe.RecipeType;
import mezz.jei.api.registration.IGuiHandlerRegistration;
import mezz.jei.api.registration.IModIngredientRegistration;
import mezz.jei.api.registration.IRecipeCatalystRegistration;
import mezz.jei.api.registration.IRecipeCategoryRegistration;
import mezz.jei.api.registration.IRecipeRegistration;
import mezz.jei.api.registration.ISubtypeRegistration;
import mezz.jei.api.runtime.IClickableIngredient;
import mezz.jei.api.runtime.IRecipesGui;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.Rect2i;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

@JeiPlugin
public class CustomMachineryJEIPlugin implements IModPlugin {

    public static final ResourceLocation PLUGIN_ID = new ResourceLocation(CustomMachinery.MODID, "jei_plugin");
    public static final List<ItemStack> FUEL_INGREDIENTS = Lists.newArrayList();

    @Override
    public ResourceLocation getPluginUid() {
        return PLUGIN_ID;
    }

    @Override
    public void registerItemSubtypes(ISubtypeRegistration registration) {
        registration.useNbtForSubtypes(Registration.CUSTOM_MACHINE_ITEM.get());
    }

    @Override
    public void registerCategories(IRecipeCategoryRegistration registry) {
        CustomMachinery.MACHINES.forEach((id, machine) -> {
            if(machine.getProcessorTemplate().getType() == Registration.MACHINE_PROCESSOR.get())
                registry.addRecipeCategories(new CustomMachineRecipeCategory(machine, CMRecipeTypes.create(id, CustomMachineRecipe.class), registry.getJeiHelpers()));
            else if(machine.getProcessorTemplate().getType() == Registration.CRAFT_PROCESSOR.get())
                registry.addRecipeCategories(new CustomCraftRecipeCategory(machine, CMRecipeTypes.create(id, CustomCraftRecipe.class), registry.getJeiHelpers()));
        });
    }

    @Override
    public void registerRecipes(IRecipeRegistration registry) {
        if(Minecraft.getInstance().level == null)
            return;

        Map<ResourceLocation, List<CustomMachineRecipe>> machineRecipes = Minecraft.getInstance().level.getRecipeManager()
                .getAllRecipesFor(Registration.CUSTOM_MACHINE_RECIPE.get())
                .stream()
                .filter(CustomMachineRecipe::showInJei)
                .sorted(Comparators.JEI_PRIORITY_COMPARATOR.reversed())
                .collect(Collectors.groupingBy(CustomMachineRecipe::getMachineId));
        machineRecipes.forEach((id, list) -> {
            RecipeType<CustomMachineRecipe> type = CMRecipeTypes.machine(id);
            if(type != null)
                registry.addRecipes(type, list);
        });

        Map<ResourceLocation, List<CustomCraftRecipe>> craftRecipes = Minecraft.getInstance().level.getRecipeManager()
                .getAllRecipesFor(Registration.CUSTOM_CRAFT_RECIPE.get())
                .stream()
                .filter(CustomCraftRecipe::showInJei)
                .sorted(Comparators.JEI_PRIORITY_COMPARATOR.reversed())
                .collect(Collectors.groupingBy(CustomCraftRecipe::getMachineId));
        craftRecipes.forEach((id, list) -> {
            RecipeType<CustomCraftRecipe> type = CMRecipeTypes.craft(id);
            if(type != null)
                registry.addRecipes(type, list);
        });

        registry.getIngredientManager().getAllIngredients(VanillaTypes.ITEM_STACK).stream().filter(stack -> FuelRegistry.get(stack) > 0).forEach(FUEL_INGREDIENTS::add);
    }

    @Override
    public void registerIngredients(IModIngredientRegistration registry) {
        registry.register(CustomIngredientTypes.ENERGY, new ArrayList<>(), new EnergyIngredientHelper(), new DummyIngredientRenderer<>());
        registry.register(CustomIngredientTypes.EXPERIENCE, new ArrayList<>(), new ExperienceIngredientHelper(), new DummyIngredientRenderer<>());
    }

    @Override
    public void registerGuiHandlers(IGuiHandlerRegistration registration) {
        registration.addGuiContainerHandler(CustomMachineScreen.class, new IGuiContainerHandler<>() {
            @Override
            public Optional<IClickableIngredient<?>> getClickableIngredientUnderMouse(CustomMachineScreen screen, double mouseX, double mouseY) {
                return screen.getElementUnderMouse(mouseX, mouseY)
                        .flatMap(widget -> Optional.ofNullable(WidgetToJeiIngredientRegistry.getIngredient(widget, mouseX, mouseY, registration.getJeiHelpers())));
            }

            @Override
            public Collection<IGuiClickableArea> getGuiClickableAreas(CustomMachineScreen screen, double mouseX, double mouseY) {
                List<IGuiElement> elements = screen.getMachine().getGuiElements();
                return elements.stream().filter(element -> element instanceof ProgressBarGuiElement).map(element -> {
                    ProgressBarGuiElement progress = (ProgressBarGuiElement)element;
                    int posX = progress.getX();
                    int posY = progress.getY();
                    boolean invertAxis = progress.getEmptyTexture().equals(ProgressBarGuiElement.BASE_EMPTY_TEXTURE) && progress.getFilledTexture().equals(ProgressBarGuiElement.BASE_FILLED_TEXTURE) && progress.getDirection() != ProgressBarGuiElement.Orientation.RIGHT && progress.getDirection() != ProgressBarGuiElement.Orientation.LEFT;
                    int width = invertAxis ? progress.getHeight() : progress.getWidth();
                    int height = invertAxis ? progress.getWidth() : progress.getHeight();
                    return createBasic(posX, posY, width, height, screen.getMachine().getId());
                }).toList();
            }
        });
        registration.addGhostIngredientHandler(CustomMachineScreen.class, new IGhostIngredientHandler<>() {
            @Override
            public <I> List<Target<I>> getTargetsTyped(CustomMachineScreen screen, ITypedIngredient<I> ingredient, boolean doStart) {
                if(ingredient.getIngredient() instanceof ItemStack stack) {
                    return screen.getMenu().slots.stream()
                            .filter(slot -> slot instanceof FilterSlotItemComponent)
                            .map(slot -> {
                                FilterSlotItemComponent filterSlot = (FilterSlotItemComponent) slot;
                                return new Target<I>() {
                                    @Override
                                    public Rect2i getArea() {
                                        return new Rect2i(screen.getX() + filterSlot.x, screen.getY() + filterSlot.y, 16, 16);
                                    }

                                    @Override
                                    public void accept(I ingredient) {
                                        filterSlot.setFromClient(stack);
                                    }
                                };
                            }).collect(Collectors.toList());
                }
                return Collections.emptyList();
            }

            @Override
            public void onComplete() {

            }
        });
    }

    @Override
    public void registerRecipeCatalysts(IRecipeCatalystRegistration registration) {
        CustomMachinery.MACHINES.forEach((id, machine) -> {
            machine.getRecipeIds().forEach(recipeId -> {
                RecipeType<?> type = CMRecipeTypes.fromID(recipeId);
                if(type != null) {
                    List<ResourceLocation> catalysts = machine.getCatalysts();
                    if(!catalysts.contains(id))
                        registration.addRecipeCatalyst(CustomMachineItem.makeMachineItem(id), type);
                    machine.getCatalysts().forEach(catalyst -> {
                        if(CustomMachinery.MACHINES.containsKey(catalyst))
                            registration.addRecipeCatalyst(CustomMachineItem.makeMachineItem(catalyst), type);
                        else if(Registration.REGISTRIES.get(Registries.ITEM).contains(catalyst))
                            registration.addRecipeCatalyst(new ItemStack(Registration.REGISTRIES.get(Registries.ITEM).get(catalyst)), type);
                        else
                            ICustomMachineryAPI.INSTANCE.logger().error("Invalid catalyst '{}' for machine '{}'. Not a machine or item id", catalyst, id);
                    });
                }
            });
        });
    }

    private static IGuiClickableArea createBasic(int xPos, int yPos, int width, int height, ResourceLocation id) {
        Rect2i area = new Rect2i(xPos, yPos, width, height);
        ItemStack stack = CustomMachineItem.makeMachineItem(id);
        return new IGuiClickableArea() {
            @Override
            public Rect2i getArea() {
                return area;
            }

            @Override
            public void onClick(IFocusFactory factory, IRecipesGui recipesGui) {
                recipesGui.show(factory.createFocus(RecipeIngredientRole.CATALYST, VanillaTypes.ITEM_STACK, stack));
            }
        };
    }
}
