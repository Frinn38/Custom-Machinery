package fr.frinn.custommachinery.client.integration.jei.wrapper;

import fr.frinn.custommachinery.api.component.IMachineComponentTemplate;
import fr.frinn.custommachinery.api.guielement.IGuiElement;
import fr.frinn.custommachinery.api.integration.jei.IJEIIngredientWrapper;
import fr.frinn.custommachinery.api.integration.jei.IRecipeHelper;
import fr.frinn.custommachinery.common.component.item.ItemMachineComponent;
import fr.frinn.custommachinery.common.guielement.SlotGuiElement;
import fr.frinn.custommachinery.common.init.Registration;
import mezz.jei.api.constants.VanillaTypes;
import mezz.jei.api.gui.builder.IRecipeLayoutBuilder;
import mezz.jei.api.recipe.RecipeIngredientRole;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

public class ItemFilterIngredientWrapper implements IJEIIngredientWrapper<ItemStack> {

    private final Ingredient ingredient;
    private final List<String> slots;

    public ItemFilterIngredientWrapper(Ingredient ingredient, List<String> slots) {
        this.ingredient = ingredient;
        this.slots = slots;
    }

    @Override
    public boolean setupRecipe(IRecipeLayoutBuilder builder, int xOffset, int yOffset, IGuiElement element, IRecipeHelper helper) {
        if(!(element instanceof SlotGuiElement slotElement) || element.getType() != Registration.SLOT_GUI_ELEMENT.get())
            return false;

        List<ItemStack> ingredients = Arrays.stream(this.ingredient.getItems()).toList();
        Optional<IMachineComponentTemplate<?>> template = helper.getComponentForElement(slotElement);
        if(this.slots.contains(slotElement.getComponentId()) || template.map(t -> t instanceof ItemMachineComponent.Template itemComponentTemplate && itemComponentTemplate.getType() == Registration.ITEM_FILTER_MACHINE_COMPONENT.get() && (this.slots.isEmpty() || this.slots.contains(t.getId()))).orElse(false)) {
            int slotX = element.getX() + (element.getWidth() - 16) / 2;
            int slotY = element.getY() + (element.getHeight() - 16) / 2;
            builder.addSlot(RecipeIngredientRole.INPUT, slotX - xOffset, slotY - yOffset)
                    .addIngredients(VanillaTypes.ITEM_STACK, ingredients);
            return true;
        }
        return false;
    }
}
