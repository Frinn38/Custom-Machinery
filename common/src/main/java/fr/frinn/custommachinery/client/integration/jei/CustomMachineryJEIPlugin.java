package fr.frinn.custommachinery.client.integration.jei;

import com.google.common.collect.Lists;
import dev.architectury.registry.fuel.FuelRegistry;
import fr.frinn.custommachinery.CustomMachinery;
import fr.frinn.custommachinery.api.guielement.IGuiElement;
import fr.frinn.custommachinery.client.integration.jei.energy.EnergyIngredientHelper;
import fr.frinn.custommachinery.client.screen.CustomMachineScreen;
import fr.frinn.custommachinery.common.crafting.craft.CustomCraftRecipe;
import fr.frinn.custommachinery.common.crafting.machine.CustomMachineRecipe;
import fr.frinn.custommachinery.common.guielement.ProgressBarGuiElement;
import fr.frinn.custommachinery.common.init.CustomMachineItem;
import fr.frinn.custommachinery.common.init.Registration;
import fr.frinn.custommachinery.common.util.Comparators;
import fr.frinn.custommachinery.impl.integration.jei.CustomIngredientTypes;
import mezz.jei.api.IModPlugin;
import mezz.jei.api.JeiPlugin;
import mezz.jei.api.constants.VanillaTypes;
import mezz.jei.api.gui.handlers.IGuiClickableArea;
import mezz.jei.api.gui.handlers.IGuiContainerHandler;
import mezz.jei.api.recipe.RecipeType;
import mezz.jei.api.registration.IGuiHandlerRegistration;
import mezz.jei.api.registration.IModIngredientRegistration;
import mezz.jei.api.registration.IRecipeCatalystRegistration;
import mezz.jei.api.registration.IRecipeCategoryRegistration;
import mezz.jei.api.registration.IRecipeRegistration;
import mezz.jei.api.registration.ISubtypeRegistration;
import net.minecraft.client.Minecraft;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
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
    }

    @Override
    public void registerGuiHandlers(IGuiHandlerRegistration registration) {
        registration.addGuiContainerHandler(CustomMachineScreen.class, new IGuiContainerHandler<>() {
            @Override
            public Collection<IGuiClickableArea> getGuiClickableAreas(CustomMachineScreen screen, double mouseX, double mouseY) {
                List<IGuiElement> elements = screen.getMachine().getGuiElements();
                ProgressBarGuiElement progress = (ProgressBarGuiElement) elements.stream().filter(element -> element.getType() == Registration.PROGRESS_GUI_ELEMENT.get()).findFirst().orElse(null);
                if(progress != null) {
                    int posX = progress.getX();
                    int posY = progress.getY();
                    boolean invertAxis = progress.getEmptyTexture().equals(ProgressBarGuiElement.BASE_EMPTY_TEXTURE) && progress.getFilledTexture().equals(ProgressBarGuiElement.BASE_FILLED_TEXTURE) && progress.getDirection() != ProgressBarGuiElement.Orientation.RIGHT && progress.getDirection() != ProgressBarGuiElement.Orientation.LEFT;
                    int width = invertAxis ? progress.getHeight() : progress.getWidth();
                    int height = invertAxis ? progress.getWidth() : progress.getHeight();
                    return Collections.singleton(IGuiClickableArea.createBasic(posX, posY, width, height, CMRecipeTypes.fromID(screen.getMachine().getId())));
                }
                return Collections.emptyList();
            }
        });
    }

    @Override
    public void registerRecipeCatalysts(IRecipeCatalystRegistration registration) {
        CustomMachinery.MACHINES.forEach((id, machine) -> {
            RecipeType<?> type = CMRecipeTypes.fromID(id);
            if(type != null) {
                registration.addRecipeCatalyst(CustomMachineItem.makeMachineItem(id), type);
                machine.getCatalysts().stream().filter(catalyst -> CustomMachinery.MACHINES.containsKey(catalyst) && !catalyst.equals(id)).forEach(catalyst -> registration.addRecipeCatalyst(CustomMachineItem.makeMachineItem(catalyst), type));
            }
        });
    }
}
