package fr.frinn.custommachinery.common.integration.jei;

import com.google.common.collect.Lists;
import fr.frinn.custommachinery.CustomMachinery;
import fr.frinn.custommachinery.client.screen.CustomMachineScreen;
import fr.frinn.custommachinery.common.data.gui.ProgressBarGuiElement;
import fr.frinn.custommachinery.common.init.CustomMachineItem;
import fr.frinn.custommachinery.common.init.Registration;
import fr.frinn.custommachinery.common.integration.jei.energy.EnergyIngredientHelper;
import mezz.jei.api.IModPlugin;
import mezz.jei.api.JeiPlugin;
import mezz.jei.api.gui.handlers.IGuiClickableArea;
import mezz.jei.api.gui.handlers.IGuiContainerHandler;
import mezz.jei.api.registration.*;
import net.minecraft.client.Minecraft;
import net.minecraft.util.ResourceLocation;

import javax.annotation.ParametersAreNonnullByDefault;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;

@JeiPlugin
public class CustomMachineJEIPlugin implements IModPlugin {

    public static final ResourceLocation PLUGIN_ID = new ResourceLocation(CustomMachinery.MODID, "jei_plugin");

    @Override
    public ResourceLocation getPluginUid() {
        return PLUGIN_ID;
    }

    @ParametersAreNonnullByDefault
    @Override
    public void registerItemSubtypes(ISubtypeRegistration registration) {
        registration.useNbtForSubtypes(Registration.CUSTOM_MACHINE_ITEM.get());
    }

    @ParametersAreNonnullByDefault
    @Override
    public void registerCategories(IRecipeCategoryRegistration registry) {
        CustomMachinery.MACHINES.forEach((id, machine) -> registry.addRecipeCategories(new CustomMachineRecipeCategory(machine, registry.getJeiHelpers().getGuiHelper())));
    }

    @ParametersAreNonnullByDefault
    @Override
    public void registerRecipes(IRecipeRegistration registry) {
        Minecraft.getInstance().world.getRecipeManager().getRecipesForType(Registration.CUSTOM_MACHINE_RECIPE).forEach(recipe -> {
            if(CustomMachinery.MACHINES.containsKey(recipe.getMachine()))
                registry.addRecipes(Lists.newArrayList(recipe), recipe.getMachine());
            else
                CustomMachinery.LOGGER.error("Invalid machine ID: " + recipe.getMachine() + " in recipe: " + recipe.getId());
        });
    }

    @ParametersAreNonnullByDefault
    @Override
    public void registerIngredients(IModIngredientRegistration registry) {
        registry.register(CustomIngredientTypes.ENERGY, new ArrayList<>(), new EnergyIngredientHelper(), new DummyIngredientRenderer<>());
    }

    @ParametersAreNonnullByDefault
    @Override
    public void registerGuiHandlers(IGuiHandlerRegistration registration) {
        registration.addGuiContainerHandler(CustomMachineScreen.class, new IGuiContainerHandler<CustomMachineScreen>() {
            @Override
            public Collection<IGuiClickableArea> getGuiClickableAreas(CustomMachineScreen screen, double mouseX, double mouseY) {
                ProgressBarGuiElement progress = (ProgressBarGuiElement) screen.getMachine().getGuiElements().stream().filter(element -> element.getType() == Registration.PROGRESS_GUI_ELEMENT.get()).findFirst().orElse(null);
                if(progress != null)
                    return Collections.singleton(IGuiClickableArea.createBasic(progress.getX(), progress.getY(), progress.getWidth(), progress.getHeight(), screen.getMachine().getId()));
                return new ArrayList<>();
            }
        });
    }

    @ParametersAreNonnullByDefault
    @Override
    public void registerRecipeCatalysts(IRecipeCatalystRegistration registration) {
        CustomMachinery.MACHINES.forEach((id, machine) -> registration.addRecipeCatalyst(CustomMachineItem.makeMachineItem(id), id));
    }
}
