package fr.frinn.custommachinery.common.integration.jei.energy;

import fr.frinn.custommachinery.CustomMachinery;
import mezz.jei.api.ingredients.IIngredientHelper;
import net.minecraft.util.text.TranslationTextComponent;

import javax.annotation.Nullable;

public class EnergyIngredientHelper implements IIngredientHelper<Energy> {

    @Nullable
    @Override
    public Energy getMatch(Iterable<Energy> iterable, Energy energy) {
        for (Energy energy1 : iterable) {
            if(energy.getAmount() == energy1.getAmount())
                return energy1;
        }
        return null;
    }

    @Override
    public String getDisplayName(Energy energy) {
        return new TranslationTextComponent("custommachinery.jei.ingredient.energy", energy.getAmount()).getString();
    }

    @Override
    public String getUniqueId(Energy energy) {
        return "energy";
    }

    @Override
    public String getModId(Energy energy) {
        return CustomMachinery.MODID;
    }

    @Override
    public String getResourceId(Energy energy) {
        return "energy";
    }

    @Override
    public Energy copyIngredient(Energy energy) {
        return new Energy(energy.getAmount(), energy.getChance(), energy.isPerTick());
    }

    @Override
    public String getErrorInfo(@Nullable Energy energy) {
        return "";
    }
}
