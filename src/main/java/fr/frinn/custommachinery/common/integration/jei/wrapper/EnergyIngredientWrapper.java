package fr.frinn.custommachinery.common.integration.jei.wrapper;

import fr.frinn.custommachinery.api.guielement.IGuiElement;
import fr.frinn.custommachinery.api.integration.jei.IJEIIngredientWrapper;
import fr.frinn.custommachinery.api.integration.jei.IRecipeHelper;
import fr.frinn.custommachinery.apiimpl.integration.jei.CustomIngredientTypes;
import fr.frinn.custommachinery.apiimpl.integration.jei.Energy;
import fr.frinn.custommachinery.apiimpl.integration.jei.Ingredients;
import fr.frinn.custommachinery.common.crafting.requirements.IRequirement;
import fr.frinn.custommachinery.common.data.gui.EnergyGuiElement;
import fr.frinn.custommachinery.common.init.Registration;
import mezz.jei.api.gui.IRecipeLayout;
import mezz.jei.api.ingredients.IIngredientRenderer;
import mezz.jei.api.ingredients.IIngredientType;

public class EnergyIngredientWrapper implements IJEIIngredientWrapper<Energy> {

    private final IRequirement.MODE mode;
    private final Energy energy;

    public EnergyIngredientWrapper(IRequirement.MODE mode, int amount, double chance, boolean isPerTick) {
        this.mode = mode;
        this.energy = new Energy(amount, chance, isPerTick);
    }

    @Override
    public IIngredientType<Energy> getJEIIngredientType() {
        return CustomIngredientTypes.ENERGY;
    }

    @Override
    public void setIngredient(Ingredients ingredients) {
        if(this.mode == IRequirement.MODE.INPUT)
            ingredients.addInput(CustomIngredientTypes.ENERGY, this.energy);
        else
            ingredients.addOutput(CustomIngredientTypes.ENERGY, this.energy);
    }

    @Override
    public boolean setupRecipe(int index, IRecipeLayout layout, int xOffset, int yOffset, IGuiElement element, IIngredientRenderer<Energy> renderer, IRecipeHelper helper) {
        if(!(element instanceof EnergyGuiElement) || element.getType() != Registration.ENERGY_GUI_ELEMENT.get())
            return false;

        layout.getIngredientsGroup(getJEIIngredientType()).init(index, this.mode == IRequirement.MODE.INPUT, renderer, element.getX() - xOffset, element.getY() - yOffset, element.getWidth() - 2, element.getHeight() - 2, 0, 0);
        layout.getIngredientsGroup(CustomIngredientTypes.ENERGY).set(index, this.energy);
        return true;
    }
}
