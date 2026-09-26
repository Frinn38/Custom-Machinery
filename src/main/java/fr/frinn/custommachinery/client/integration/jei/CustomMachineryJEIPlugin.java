package fr.frinn.custommachinery.client.integration.jei;

import com.google.common.collect.Lists;
import fr.frinn.custommachinery.CustomMachinery;
import fr.frinn.custommachinery.api.ICustomMachineryAPI;
import fr.frinn.custommachinery.api.crafting.IMachineRecipe;
import fr.frinn.custommachinery.api.guielement.IGuiElement;
import fr.frinn.custommachinery.client.ClientEvents;
import fr.frinn.custommachinery.client.integration.jei.energy.EnergyIngredientHelper;
import fr.frinn.custommachinery.client.integration.jei.experience.ExperienceIngredientHelper;
import fr.frinn.custommachinery.client.screen.CustomMachineScreen;
import fr.frinn.custommachinery.client.screen.MachineConfigScreen;
import fr.frinn.custommachinery.common.crafting.craft.CustomCraftRecipe;
import fr.frinn.custommachinery.common.crafting.machine.CustomMachineRecipe;
import fr.frinn.custommachinery.common.guielement.PlayerInventoryGuiElement;
import fr.frinn.custommachinery.common.guielement.ProgressBarGuiElement;
import fr.frinn.custommachinery.common.init.CustomMachineItem;
import fr.frinn.custommachinery.common.init.CMRegistration;
import fr.frinn.custommachinery.common.machine.CustomMachine;
import fr.frinn.custommachinery.common.util.Comparators;
import fr.frinn.custommachinery.common.util.slot.FilterSlotItemComponent;
import fr.frinn.custommachinery.impl.integration.jei.CustomIngredientTypes;
import fr.frinn.custommachinery.impl.integration.jei.Energy;
import fr.frinn.custommachinery.impl.integration.jei.Experience;
import fr.frinn.custommachinery.impl.integration.jei.WidgetToJeiIngredientRegistry;
import mezz.jei.api.IModPlugin;
import mezz.jei.api.JeiPlugin;
import mezz.jei.api.constants.VanillaTypes;
import mezz.jei.api.gui.builder.IClickableIngredientFactory;
import mezz.jei.api.gui.handlers.IGhostIngredientHandler;
import mezz.jei.api.gui.handlers.IGuiClickableArea;
import mezz.jei.api.gui.handlers.IGuiContainerHandler;
import mezz.jei.api.ingredients.ITypedIngredient;
import mezz.jei.api.ingredients.subtypes.ISubtypeInterpreter;
import mezz.jei.api.recipe.IFocusFactory;
import mezz.jei.api.recipe.RecipeIngredientRole;
import mezz.jei.api.recipe.types.IRecipeType;
import mezz.jei.api.registration.IGuiHandlerRegistration;
import mezz.jei.api.registration.IModIngredientRegistration;
import mezz.jei.api.registration.IRecipeCatalystRegistration;
import mezz.jei.api.registration.IRecipeCategoryRegistration;
import mezz.jei.api.registration.IRecipeRegistration;
import mezz.jei.api.registration.IRecipeTransferRegistration;
import mezz.jei.api.registration.ISubtypeRegistration;
import mezz.jei.api.runtime.IClickableIngredient;
import mezz.jei.api.runtime.IRecipesGui;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.Rect2i;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.world.item.crafting.RecipeType;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

@JeiPlugin
public class CustomMachineryJEIPlugin implements IModPlugin {

    public static final Identifier PLUGIN_ID = Identifier.fromNamespaceAndPath(CustomMachinery.MODID, "jei_plugin");
    public static final List<ItemStack> FUEL_INGREDIENTS = Lists.newArrayList();
    public static final Map<Identifier, AbstractRecipeCategory<?, ?>> CATEGORIES = new HashMap<>();

    @Override
    public Identifier getPluginUid() {
        return PLUGIN_ID;
    }

    @Override
    public void registerItemSubtypes(ISubtypeRegistration registration) {
        registration.registerSubtypeInterpreter(CMRegistration.CUSTOM_MACHINE_ITEM.get(), MACHINE_ITEM_INTERPRETER);
    }

    @Override
    public void registerCategories(IRecipeCategoryRegistration registry) {
        CATEGORIES.clear();
        CustomMachinery.MACHINES.forEach((id, machine) -> {
            AbstractRecipeCategory<?, ?> category = null;
            if(machine.getProcessorTemplate().getType() == CMRegistration.MACHINE_PROCESSOR.get())
                category = new CustomMachineRecipeCategory(machine, CMRecipeTypes.createMachine(id), registry.getJeiHelpers());
            else if(machine.getProcessorTemplate().getType() == CMRegistration.CRAFT_PROCESSOR.get())
                category = new CustomCraftRecipeCategory(machine, CMRecipeTypes.createCraft(id), registry.getJeiHelpers());

            if(category != null) {
                registry.addRecipeCategories(category);
                CATEGORIES.put(machine.getId(), category);
            }
        });
    }

    @Override
    public void registerRecipes(IRecipeRegistration registry) {
        if(Minecraft.getInstance().level == null)
            return;

        Map<Identifier, List<RecipeHolder<CustomMachineRecipe>>> machineRecipes = ClientEvents.getClientRecipes()
                .byType(CMRegistration.CUSTOM_MACHINE_RECIPE.get())
                .stream()
                .map(RecipeHolder::value)
                .filter(CustomMachineRecipe::showInJei)
                .sorted(Comparators.JEI_PRIORITY_COMPARATOR.reversed())
                .map(this::getHolderForRecipe)
                .collect(Collectors.groupingBy(holder -> holder.value().getMachineId()));
        machineRecipes.forEach((id, list) -> {
            IRecipeType<RecipeHolder<CustomMachineRecipe>> type = CMRecipeTypes.machine(id);
            if(type != null)
                registry.addRecipes(type, list);
        });

        Map<Identifier, List<RecipeHolder<CustomCraftRecipe>>> craftRecipes = ClientEvents.getClientRecipes()
                .byType(CMRegistration.CUSTOM_CRAFT_RECIPE.get())
                .stream()
                .map(RecipeHolder::value)
                .filter(CustomCraftRecipe::showInJei)
                .sorted(Comparators.JEI_PRIORITY_COMPARATOR.reversed())
                .map(this::getHolderForRecipe)
                .collect(Collectors.groupingBy(holder -> holder.value().getMachineId()));
        craftRecipes.forEach((id, list) -> {
            IRecipeType<RecipeHolder<CustomCraftRecipe>> type = CMRecipeTypes.craft(id);
            if(type != null)
                registry.addRecipes(type, list);
        });

        registry.getIngredientManager().getAllIngredients(VanillaTypes.ITEM_STACK)
                .stream()
                .filter(stack -> stack.getBurnTime(RecipeType.SMELTING, Minecraft.getInstance().level.fuelValues()) > 0)
                .forEach(FUEL_INGREDIENTS::add);
    }

    @Override
    public void registerIngredients(IModIngredientRegistration registry) {
        registry.register(CustomIngredientTypes.ENERGY, new ArrayList<>(), new EnergyIngredientHelper(), new DummyIngredientRenderer<>(), Energy.CODEC);
        registry.register(CustomIngredientTypes.EXPERIENCE, new ArrayList<>(), new ExperienceIngredientHelper(), new DummyIngredientRenderer<>(), Experience.CODEC);
    }

    @Override
    public void registerGuiHandlers(IGuiHandlerRegistration registration) {
        registration.addGuiContainerHandler(CustomMachineScreen.class, new IGuiContainerHandler<>() {
            @Override
            public List<Rect2i> getGuiExtraAreas(CustomMachineScreen containerScreen) {
                if(Minecraft.getInstance().screen instanceof MachineConfigScreen screen)
                    return screen.popups().stream().map(popup -> new Rect2i(popup.x, popup.y, popup.xSize, popup.ySize)).toList();
                return Collections.emptyList();
            }

            @Override
            public Optional<IClickableIngredient<?>> getClickableIngredientUnderMouse(IClickableIngredientFactory builder, CustomMachineScreen screen, double mouseX, double mouseY) {
                return screen.getElementUnderMouse(mouseX, mouseY)
                        .flatMap(widget -> Optional.ofNullable(WidgetToJeiIngredientRegistry.getIngredient(widget, mouseX, mouseY, registration.getJeiHelpers())));
            }

            @Override
            public Collection<IGuiClickableArea> getGuiClickableAreas(CustomMachineScreen screen, double mouseX, double mouseY) {
                List<IGuiElement> elements = screen.getTile().getGuiElements();
                return elements.stream().filter(element -> element instanceof ProgressBarGuiElement).map(element -> createBasic(element.getX(), element.getY(), element.getWidth(), element.getHeight(), screen.getMachine().getId(), element.getTooltips().isEmpty())).toList();
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
        CustomMachinery.MACHINES.forEach((id, machine) ->
            machine.getRecipeIds().forEach(recipeId -> {
                IRecipeType<?> type;
                if(machine.getProcessorTemplate().getType() == CMRegistration.MACHINE_PROCESSOR.get())
                    type = CMRecipeTypes.machine(id);
                else if(machine.getProcessorTemplate().getType() == CMRegistration.CRAFT_PROCESSOR.get())
                    type = CMRecipeTypes.craft(id);
                else
                    type = null;
                if(type != null) {
                    List<Identifier> catalysts = machine.getCatalysts();
                    if(!catalysts.contains(id))
                        registration.addCraftingStation(type, CustomMachineItem.makeMachineItem(id));
                    machine.getCatalysts().forEach(catalyst -> {
                        if(CustomMachinery.MACHINES.containsKey(catalyst))
                            registration.addCraftingStation(type, CustomMachineItem.makeMachineItem(catalyst));
                        else if(BuiltInRegistries.ITEM.containsKey(catalyst))
                            registration.addCraftingStation(type, BuiltInRegistries.ITEM.getValue(catalyst).getDefaultInstance());
                        else
                            ICustomMachineryAPI.INSTANCE.logger().error("Invalid catalyst '{}' for machine '{}'. Not a machine or item id", catalyst, id);
                    });
                }
            })
        );
    }

    @Override
    public void registerRecipeTransferHandlers(IRecipeTransferRegistration registration) {
        CMRecipeTypes.all().forEach(type -> {
            if(CustomMachinery.MACHINES.containsKey(type.getUid()) && CustomMachinery.MACHINES.get(type.getUid()).getGuiElements().stream().anyMatch(element -> element instanceof PlayerInventoryGuiElement))
                registration.addRecipeTransferHandler(new CMRecipeTransferHandler(type, registration.getTransferHelper(), registration.getJeiHelpers().getStackHelper()), type);
        });
    }

    private static IGuiClickableArea createBasic(int xPos, int yPos, int width, int height, Identifier id, boolean showTooltips) {
        Rect2i area = new Rect2i(xPos, yPos, width, height);
        ItemStack stack = CustomMachineItem.makeMachineItem(id);
        return new IGuiClickableArea() {
            @Override
            public Rect2i getArea() {
                return area;
            }

            @Override
            public void onClick(IFocusFactory factory, IRecipesGui recipesGui) {
                recipesGui.show(factory.createFocus(RecipeIngredientRole.CRAFTING_STATION, VanillaTypes.ITEM_STACK, stack));
            }

            @Override
            public boolean isTooltipEnabled() {
                return showTooltips;
            }
        };
    }

    public static final ISubtypeInterpreter<ItemStack> MACHINE_ITEM_INTERPRETER = (ingredient, context) ->
            Optional.ofNullable(ingredient.get(CMRegistration.MACHINE_DATA.get())).map(Identifier::toString).orElse("dummy");

    public static void reloadMachines(Map<Identifier, CustomMachine> machines) {
        machines.forEach((id, machine) -> {
            AbstractRecipeCategory<?, ?> category = CATEGORIES.get(id);
            if(category != null)
                category.updateMachine(machine);
        });
    }

    @SuppressWarnings("unchecked")
    private <T extends IMachineRecipe> RecipeHolder<T> getHolderForRecipe(T recipe) {
        if(Minecraft.getInstance().level == null)
            throw new IllegalStateException("Trying to get recipes from client side when level is null");
        return ClientEvents.getClientRecipes().byType((net.minecraft.world.item.crafting.RecipeType<T>)recipe.getType())
                .stream()
                .filter(holder -> holder.value() == recipe)
                .findFirst()
                .orElseThrow(() -> new IllegalStateException("No registered holder for recipe: " + recipe));
    }
}
