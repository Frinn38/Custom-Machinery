package fr.frinn.custommachinery.common.integration.jei.wrapper;

import fr.frinn.custommachinery.api.guielement.IGuiElement;
import fr.frinn.custommachinery.common.crafting.requirements.IRequirement;
import fr.frinn.custommachinery.common.data.gui.EnergyGuiElement;
import fr.frinn.custommachinery.common.init.Registration;
import fr.frinn.custommachinery.common.integration.jei.CustomIngredientTypes;
import fr.frinn.custommachinery.common.integration.jei.RecipeHelper;
import fr.frinn.custommachinery.common.integration.jei.energy.Energy;
import mezz.jei.api.gui.IRecipeLayout;
import mezz.jei.api.ingredients.IIngredientType;
import mezz.jei.api.ingredients.IIngredients;

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
    public void setIngredient(IIngredients ingredients) {
        if(this.mode == IRequirement.MODE.INPUT)
            ingredients.setInput(CustomIngredientTypes.ENERGY, this.energy);
        else
            ingredients.setOutput(CustomIngredientTypes.ENERGY, this.energy);
    }

    @Override
    public boolean setupRecipe(int index, IRecipeLayout layout, IGuiElement element, RecipeHelper helper) {
        if(!(element instanceof EnergyGuiElement) || element.getType() != Registration.ENERGY_GUI_ELEMENT.get())
            return false;

        layout.getIngredientsGroup(CustomIngredientTypes.ENERGY).set(index, this.energy);
        return true;
    }
}
