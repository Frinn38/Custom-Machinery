package fr.frinn.custommachinery.common.integration.jei.wrapper;

import fr.frinn.custommachinery.api.component.IMachineComponentTemplate;
import fr.frinn.custommachinery.api.guielement.IGuiElement;
import fr.frinn.custommachinery.api.integration.jei.IJEIIngredientWrapper;
import fr.frinn.custommachinery.api.integration.jei.IRecipeHelper;
import fr.frinn.custommachinery.apiimpl.integration.jei.Ingredients;
import fr.frinn.custommachinery.common.data.component.ItemMachineComponent;
import fr.frinn.custommachinery.common.data.component.variant.item.FuelItemComponentVariant;
import fr.frinn.custommachinery.common.data.gui.SlotGuiElement;
import fr.frinn.custommachinery.common.init.Registration;
import fr.frinn.custommachinery.common.integration.jei.CustomMachineJEIPlugin;
import mezz.jei.api.constants.VanillaTypes;
import mezz.jei.api.gui.IRecipeLayout;
import mezz.jei.api.gui.ingredient.IGuiIngredientGroup;
import mezz.jei.api.ingredients.IIngredientRenderer;
import mezz.jei.api.ingredients.IIngredientType;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.world.item.ItemStack;

import java.util.Optional;

public class FuelItemIngredientWrapper implements IJEIIngredientWrapper<ItemStack> {

    private final int amount;

    public FuelItemIngredientWrapper(int amount) {
        this.amount = amount;
    }

    @Override
    public IIngredientType<ItemStack> getJEIIngredientType() {
        return VanillaTypes.ITEM;
    }

    @Override
    public void setIngredient(Ingredients ingredients) {

    }

    @Override
    public boolean setupRecipe(int index, IRecipeLayout layout, int xOffset, int yOffset, IGuiElement element, IIngredientRenderer<ItemStack> renderer, IRecipeHelper helper) {
        if(!(element instanceof SlotGuiElement) || element.getType() != Registration.SLOT_GUI_ELEMENT.get())
            return false;

        SlotGuiElement slotElement = (SlotGuiElement)element;
        Optional<IMachineComponentTemplate<?>> template = helper.getComponentForElement(slotElement);
        return template.map(t -> {
            if(t instanceof ItemMachineComponent.Template && ((ItemMachineComponent.Template)t).getVariant() == FuelItemComponentVariant.INSTANCE) {
                layout.getIngredientsGroup(getJEIIngredientType()).init(index, true, renderer, element.getX() - xOffset, element.getY() - yOffset, element.getWidth() - 2, element.getHeight() - 2, 0, 0);
                IGuiIngredientGroup<ItemStack> group = layout.getIngredientsGroup(VanillaTypes.ITEM);
                group.set(index, CustomMachineJEIPlugin.FUEL_INGREDIENTS);
                group.addTooltipCallback((slotIndex, input, ingredient, tooltip) -> {
                    if(slotIndex != index)
                        return;
                    tooltip.add(new TranslatableComponent("custommachinery.jei.ingredient.fuel.tooltip"));
                });
                return true;
            }
            return false;
        }).orElse(false);
    }
}
