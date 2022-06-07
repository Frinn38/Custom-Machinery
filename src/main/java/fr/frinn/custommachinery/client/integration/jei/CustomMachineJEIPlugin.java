package fr.frinn.custommachinery.client.integration.jei;

import com.google.common.collect.Lists;
import fr.frinn.custommachinery.CustomMachinery;
import fr.frinn.custommachinery.api.guielement.IGuiElement;
import fr.frinn.custommachinery.apiimpl.integration.jei.CustomIngredientTypes;
import fr.frinn.custommachinery.client.integration.jei.energy.EnergyIngredientHelper;
import fr.frinn.custommachinery.client.screen.CustomMachineScreen;
import fr.frinn.custommachinery.common.crafting.CustomMachineRecipe;
import fr.frinn.custommachinery.common.guielement.ProgressBarGuiElement;
import fr.frinn.custommachinery.common.init.CustomMachineItem;
import fr.frinn.custommachinery.common.init.Registration;
import fr.frinn.custommachinery.common.util.Comparators;
import fr.frinn.custommachinery.common.util.Utils;
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
public class CustomMachineJEIPlugin implements IModPlugin {

    public static final ResourceLocation PLUGIN_ID = new ResourceLocation(CustomMachinery.MODID, "jei_plugin");
    public static final List<ItemStack> FUEL_INGREDIENTS = Lists.newArrayList();

    private MachineRecipeTypes types;

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
        types = new MachineRecipeTypes();
        CustomMachinery.MACHINES.forEach((id, machine) -> registry.addRecipeCategories(new CustomMachineRecipeCategory(machine, types.fromID(id), registry.getJeiHelpers())));
    }

    @Override
    public void registerRecipes(IRecipeRegistration registry) {
        if(Minecraft.getInstance().level == null)
            return;
        Map<ResourceLocation, List<CustomMachineRecipe>> recipes = Minecraft.getInstance().level.getRecipeManager()
                .getAllRecipesFor(Registration.CUSTOM_MACHINE_RECIPE.get())
                .stream()
                .sorted(Comparators.JEI_PRIORITY_COMPARATOR.reversed())
                .collect(Collectors.groupingBy(CustomMachineRecipe::getMachine));
        recipes.forEach((id, list) -> {
            RecipeType<CustomMachineRecipe> type = types.fromID(id);
            if(type != null)
                registry.addRecipes(type, list);
        });
        registry.getIngredientManager().getAllIngredients(VanillaTypes.ITEM_STACK).stream().filter(Utils::hasBurnTime).forEach(FUEL_INGREDIENTS::add);
    }

    @Override
    public void registerIngredients(IModIngredientRegistration registry) {
        registry.register(CustomIngredientTypes.ENERGY, new ArrayList<>(), new EnergyIngredientHelper(), new DummyIngredientRenderer<>());
    }

    @Override
    public void registerGuiHandlers(IGuiHandlerRegistration registration) {
        registration.addGuiContainerHandler(CustomMachineScreen.class, new IGuiContainerHandler<CustomMachineScreen>() {
            @Override
            public Collection<IGuiClickableArea> getGuiClickableAreas(CustomMachineScreen screen, double mouseX, double mouseY) {
                List<IGuiElement> elements = screen.getMachine().getJeiElements().isEmpty() ? screen.getMachine().getGuiElements() : screen.getMachine().getJeiElements();
                ProgressBarGuiElement progress = (ProgressBarGuiElement) elements.stream().filter(element -> element.getType() == Registration.PROGRESS_GUI_ELEMENT.get()).findFirst().orElse(null);
                if(progress != null) {
                    int posX = progress.getX();
                    int posY = progress.getY();
                    boolean invertAxis = progress.getEmptyTexture().equals(ProgressBarGuiElement.BASE_EMPTY_TEXTURE) && progress.getFilledTexture().equals(ProgressBarGuiElement.BASE_FILLED_TEXTURE) && progress.getDirection() != ProgressBarGuiElement.Direction.RIGHT && progress.getDirection() != ProgressBarGuiElement.Direction.LEFT;
                    int width = invertAxis ? progress.getHeight() : progress.getWidth();
                    int height = invertAxis ? progress.getWidth() : progress.getHeight();
                    return Collections.singleton(IGuiClickableArea.createBasic(posX, posY, width, height, types.fromID(screen.getMachine().getId())));
                }
                return Collections.emptyList();
            }
        });
    }

    @Override
    public void registerRecipeCatalysts(IRecipeCatalystRegistration registration) {
        CustomMachinery.MACHINES.forEach((id, machine) -> {
            RecipeType<CustomMachineRecipe> type = types.fromID(id);
            if(type != null) {
                registration.addRecipeCatalyst(CustomMachineItem.makeMachineItem(id), type);
                machine.getCatalysts().stream().filter(catalyst -> CustomMachinery.MACHINES.containsKey(catalyst) && !catalyst.equals(id)).forEach(catalyst -> registration.addRecipeCatalyst(CustomMachineItem.makeMachineItem(catalyst), type));
            }
        });
    }
}
