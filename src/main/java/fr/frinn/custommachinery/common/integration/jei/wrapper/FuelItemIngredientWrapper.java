package fr.frinn.custommachinery.common.integration.jei.wrapper;

import fr.frinn.custommachinery.api.component.IMachineComponentTemplate;
import fr.frinn.custommachinery.api.guielement.IGuiElement;
import fr.frinn.custommachinery.api.integration.jei.IJEIIngredientWrapper;
import fr.frinn.custommachinery.api.integration.jei.IRecipeHelper;
import fr.frinn.custommachinery.common.data.component.ItemMachineComponent;
import fr.frinn.custommachinery.common.data.component.variant.item.FuelItemComponentVariant;
import fr.frinn.custommachinery.common.data.gui.SlotGuiElement;
import fr.frinn.custommachinery.common.init.Registration;
import fr.frinn.custommachinery.common.integration.jei.CustomMachineJEIPlugin;
import mezz.jei.api.constants.VanillaTypes;
import mezz.jei.api.gui.builder.IRecipeLayoutBuilder;
import mezz.jei.api.recipe.RecipeIngredientRole;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.world.item.ItemStack;

import java.util.Optional;

public class FuelItemIngredientWrapper implements IJEIIngredientWrapper<ItemStack> {

    private final int amount;

    public FuelItemIngredientWrapper(int amount) {
        this.amount = amount;
    }

    @Override
    public boolean setupRecipe(IRecipeLayoutBuilder builder, int xOffset, int yOffset, IGuiElement element, IRecipeHelper helper) {
        if(!(element instanceof SlotGuiElement slotElement) || element.getType() != Registration.SLOT_GUI_ELEMENT.get())
            return false;

        Optional<IMachineComponentTemplate<?>> template = helper.getComponentForElement(slotElement);
        return template.map(t -> {
            if(t instanceof ItemMachineComponent.Template && ((ItemMachineComponent.Template)t).getVariant() == FuelItemComponentVariant.INSTANCE) {
                builder.addSlot(RecipeIngredientRole.INPUT, element.getX() - xOffset, element.getY() - yOffset)
                    .addIngredients(VanillaTypes.ITEM_STACK, CustomMachineJEIPlugin.FUEL_INGREDIENTS)
                    .addTooltipCallback((view, tooltips) -> {
                        tooltips.add(new TranslatableComponent("custommachinery.jei.ingredient.fuel.amount", this.amount));
                    });
                return true;
            }
            return false;
        }).orElse(false);
    }
}
