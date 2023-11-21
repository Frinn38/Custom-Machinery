package fr.frinn.custommachinery.client.integration.jei.wrapper;

import dev.architectury.registry.fuel.FuelRegistry;
import fr.frinn.custommachinery.api.guielement.IGuiElement;
import fr.frinn.custommachinery.api.integration.jei.IJEIIngredientWrapper;
import fr.frinn.custommachinery.api.integration.jei.IRecipeHelper;
import fr.frinn.custommachinery.client.integration.jei.CustomMachineryJEIPlugin;
import fr.frinn.custommachinery.common.component.ItemMachineComponent;
import fr.frinn.custommachinery.common.component.variant.item.FuelItemComponentVariant;
import fr.frinn.custommachinery.common.guielement.SlotGuiElement;
import fr.frinn.custommachinery.common.init.Registration;
import mezz.jei.api.constants.VanillaTypes;
import mezz.jei.api.gui.builder.IRecipeLayoutBuilder;
import mezz.jei.api.recipe.RecipeIngredientRole;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;

import java.util.List;

public class FuelItemIngredientWrapper implements IJEIIngredientWrapper<ItemStack> {

    private final int amount;

    public FuelItemIngredientWrapper(int amount) {
        this.amount = amount;
    }

    @Override
    public boolean setupRecipe(IRecipeLayoutBuilder builder, int xOffset, int yOffset, IGuiElement element, IRecipeHelper helper) {
        if(!(element instanceof SlotGuiElement slotElement) || element.getType() != Registration.SLOT_GUI_ELEMENT.get())
            return false;

        return helper.getComponentForElement(slotElement).map(t -> {
            if(t instanceof ItemMachineComponent.Template template && template.getVariant() == FuelItemComponentVariant.INSTANCE) {
                List<ItemStack> ingredients = CustomMachineryJEIPlugin.FUEL_INGREDIENTS.stream().filter(stack -> template.canAccept(stack, true, helper.getDummyManager())).toList();
                builder.addSlot(RecipeIngredientRole.INPUT, element.getX() - xOffset + 1, element.getY() - yOffset + 1)
                    .addIngredients(VanillaTypes.ITEM_STACK, ingredients)
                    .addTooltipCallback((view, tooltips) -> {
                        view.getDisplayedIngredient(VanillaTypes.ITEM_STACK).ifPresent(stack ->
                                tooltips.add(Component.translatable("custommachinery.jei.ingredient.fuel.burntime", FuelRegistry.get(stack)).withStyle(ChatFormatting.GRAY))
                        );
                    });
                return true;
            }
            return false;
        }).orElse(false);
    }
}
