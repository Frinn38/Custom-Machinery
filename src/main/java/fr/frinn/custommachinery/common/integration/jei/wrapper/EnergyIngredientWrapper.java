package fr.frinn.custommachinery.common.integration.jei.wrapper;

import fr.frinn.custommachinery.api.guielement.IGuiElement;
import fr.frinn.custommachinery.api.integration.jei.IJEIIngredientWrapper;
import fr.frinn.custommachinery.api.integration.jei.IRecipeHelper;
import fr.frinn.custommachinery.api.requirement.RequirementIOMode;
import fr.frinn.custommachinery.apiimpl.integration.jei.CustomIngredientTypes;
import fr.frinn.custommachinery.apiimpl.integration.jei.Energy;
import fr.frinn.custommachinery.common.data.gui.EnergyGuiElement;
import fr.frinn.custommachinery.common.init.Registration;
import fr.frinn.custommachinery.common.integration.jei.energy.EnergyJEIIngredientRenderer;
import mezz.jei.api.gui.builder.IRecipeLayoutBuilder;

public class EnergyIngredientWrapper implements IJEIIngredientWrapper<Energy> {

    private final RequirementIOMode mode;
    private final Energy energy;

    public EnergyIngredientWrapper(RequirementIOMode mode, int amount, double chance, boolean isPerTick) {
        this.mode = mode;
        this.energy = new Energy(amount, chance, isPerTick);
    }

    @Override
    public boolean setupRecipe(IRecipeLayoutBuilder builder, int xOffset, int yOffset, IGuiElement element, IRecipeHelper helper) {
        if(!(element instanceof EnergyGuiElement energyElement) || element.getType() != Registration.ENERGY_GUI_ELEMENT.get())
            return false;

        builder.addSlot(roleFromMode(this.mode), element.getX() - xOffset, element.getY() - yOffset)
                .setCustomRenderer(CustomIngredientTypes.ENERGY, new EnergyJEIIngredientRenderer(energyElement))
                .addIngredient(CustomIngredientTypes.ENERGY, this.energy);
        return true;
    }
}
