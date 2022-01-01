package fr.frinn.custommachinery.common.integration.jei;

import com.google.common.collect.Lists;
import fr.frinn.custommachinery.CustomMachinery;
import fr.frinn.custommachinery.api.guielement.IGuiElement;
import fr.frinn.custommachinery.apiimpl.integration.jei.CustomIngredientTypes;
import fr.frinn.custommachinery.client.screen.CustomMachineScreen;
import fr.frinn.custommachinery.common.data.gui.ProgressBarGuiElement;
import fr.frinn.custommachinery.common.init.CustomMachineItem;
import fr.frinn.custommachinery.common.init.Registration;
import fr.frinn.custommachinery.common.integration.jei.energy.EnergyIngredientHelper;
import fr.frinn.custommachinery.common.util.Comparators;
import mezz.jei.api.IModPlugin;
import mezz.jei.api.JeiPlugin;
import mezz.jei.api.MethodsReturnNonnullByDefault;
import mezz.jei.api.constants.VanillaTypes;
import mezz.jei.api.gui.handlers.IGuiClickableArea;
import mezz.jei.api.gui.handlers.IGuiContainerHandler;
import mezz.jei.api.registration.*;
import net.minecraft.client.Minecraft;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.IRecipeType;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.common.ForgeHooks;

import javax.annotation.ParametersAreNonnullByDefault;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

@JeiPlugin
@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public class CustomMachineJEIPlugin implements IModPlugin {

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
        CustomMachinery.MACHINES.forEach((id, machine) -> registry.addRecipeCategories(new CustomMachineRecipeCategory(machine, registry.getJeiHelpers().getGuiHelper())));
    }

    @Override
    public void registerRecipes(IRecipeRegistration registry) {
        if(Minecraft.getInstance().world == null)
            return;
        Minecraft.getInstance().world.getRecipeManager()
                .getRecipesForType(Registration.CUSTOM_MACHINE_RECIPE)
                .stream()
                .sorted(Comparators.JEI_PRIORITY_COMPARATOR.reversed())
                .forEach(recipe -> {
                    if(CustomMachinery.MACHINES.containsKey(recipe.getMachine()))
                        registry.addRecipes(Lists.newArrayList(recipe), recipe.getMachine());
                    else
                        CustomMachinery.LOGGER.error("Invalid machine ID: " + recipe.getMachine() + " in recipe: " + recipe.getId());
                }
        );
        registry.getIngredientManager().getAllIngredients(VanillaTypes.ITEM).stream().filter(stack -> ForgeHooks.getBurnTime(stack, IRecipeType.SMELTING) > 0).forEach(FUEL_INGREDIENTS::add);
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
                    return Collections.singleton(IGuiClickableArea.createBasic(posX, posY, width, height, screen.getMachine().getId()));
                }
                return Collections.emptyList();
            }
        });
    }

    @Override
    public void registerRecipeCatalysts(IRecipeCatalystRegistration registration) {
        CustomMachinery.MACHINES.forEach((id, machine) -> {
            registration.addRecipeCatalyst(CustomMachineItem.makeMachineItem(id), id);
            machine.getCatalysts().stream().filter(catalyst -> CustomMachinery.MACHINES.containsKey(catalyst) && !catalyst.equals(id)).forEach(catalyst -> registration.addRecipeCatalyst(CustomMachineItem.makeMachineItem(catalyst), id));
        });
    }
}
