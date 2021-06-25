package fr.frinn.custommachinery.common.integration.jei.wrapper;

import fr.frinn.custommachinery.common.crafting.requirements.IRequirement;
import fr.frinn.custommachinery.common.integration.jei.CustomIngredientTypes;
import fr.frinn.custommachinery.common.integration.jei.energy.Energy;
import mezz.jei.api.ingredients.IIngredientType;

import javax.annotation.Nonnull;
import java.util.Collections;
import java.util.List;

public class EnergyIngredientWrapper implements IJEIIngredientWrapper<Energy> {

    private IRequirement.MODE mode;
    private Energy energy;

    public EnergyIngredientWrapper(IRequirement.MODE mode, int amount, double chance, boolean isPerTick) {
        this.mode = mode;
        this.energy = new Energy(amount, chance, isPerTick);
    }

    @Override
    public IIngredientType<Energy> getJEIIngredientType() {
        return CustomIngredientTypes.ENERGY;
    }

    @Override
    public Energy asJEIIngredient() {
        return this.energy;
    }

    @Override
    public List<Energy> getJeiIngredients() {
        return Collections.singletonList(this.energy);
    }

    @Nonnull
    @Override
    public String getComponentID() {
        return "";
    }
}
