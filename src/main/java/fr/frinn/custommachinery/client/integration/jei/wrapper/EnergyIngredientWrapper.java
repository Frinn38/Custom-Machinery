package fr.frinn.custommachinery.client.integration.jei.wrapper;

import fr.frinn.custommachinery.api.guielement.IGuiElement;
import fr.frinn.custommachinery.api.integration.jei.IJEIIngredientWrapper;
import fr.frinn.custommachinery.api.integration.jei.IRecipeHelper;
import fr.frinn.custommachinery.api.requirement.RequirementIOMode;
import fr.frinn.custommachinery.client.integration.jei.energy.EnergyJEIIngredientRenderer;
import fr.frinn.custommachinery.common.guielement.EnergyGuiElement;
import fr.frinn.custommachinery.common.init.Registration;
import fr.frinn.custommachinery.common.util.Utils;
import fr.frinn.custommachinery.impl.integration.jei.CustomIngredientTypes;
import fr.frinn.custommachinery.impl.integration.jei.Energy;
import mezz.jei.api.gui.builder.IRecipeLayoutBuilder;
import net.minecraft.network.chat.Component;

public class EnergyIngredientWrapper implements IJEIIngredientWrapper<Energy> {

    private final RequirementIOMode mode;
    private final int recipeTime;
    private final Energy energy;

    public EnergyIngredientWrapper(RequirementIOMode mode, int amount, double chance, boolean isPerTick, int recipeTime) {
        this.mode = mode;
        this.recipeTime = recipeTime;
        this.energy = new Energy(amount, chance, isPerTick);
    }

    @Override
    public boolean setupRecipe(IRecipeLayoutBuilder builder, int xOffset, int yOffset, IGuiElement element, IRecipeHelper helper) {
        if(!(element instanceof EnergyGuiElement energyElement) || element.getType() != Registration.ENERGY_GUI_ELEMENT.get())
            return false;

        builder.addSlot(roleFromMode(this.mode), element.getX() - xOffset + 1, element.getY() - yOffset + 1)
                .setCustomRenderer(CustomIngredientTypes.ENERGY, new EnergyJEIIngredientRenderer(energyElement))
                .addIngredient(CustomIngredientTypes.ENERGY, this.energy)
                .addRichTooltipCallback((recipeSlotView, tooltip) -> {
                    Component component;
                    String amount = Utils.format(this.energy.amount());
                    Component unit = Component.translatable("unit.energy.forge");
                    if(this.energy.isPerTick()) {
                        String totalEnergy = Utils.format(this.energy.amount() * this.recipeTime);
                        if(this.mode == RequirementIOMode.INPUT)
                            component = Component.translatable("custommachinery.jei.ingredient.energy.pertick.input", totalEnergy, unit, amount, unit);
                        else
                            component = Component.translatable("custommachinery.jei.ingredient.energy.pertick.output", totalEnergy, unit, amount, unit);
                    } else {
                        if(this.mode == RequirementIOMode.INPUT)
                            component = Component.translatable("custommachinery.jei.ingredient.energy.input", amount, unit);
                        else
                            component = Component.translatable("custommachinery.jei.ingredient.energy.output", amount, unit);
                    }
                    tooltip.add(component);
                });
        return true;
    }
}
