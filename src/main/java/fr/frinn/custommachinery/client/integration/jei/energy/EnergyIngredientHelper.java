package fr.frinn.custommachinery.client.integration.jei.energy;

import fr.frinn.custommachinery.CustomMachinery;
import fr.frinn.custommachinery.apiimpl.integration.jei.CustomIngredientTypes;
import fr.frinn.custommachinery.apiimpl.integration.jei.Energy;
import mezz.jei.api.ingredients.IIngredientHelper;
import mezz.jei.api.ingredients.IIngredientType;
import mezz.jei.api.ingredients.subtypes.UidContext;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.resources.ResourceLocation;

import javax.annotation.Nullable;

public class EnergyIngredientHelper implements IIngredientHelper<Energy> {

    @Override
    public IIngredientType<Energy> getIngredientType() {
        return CustomIngredientTypes.ENERGY;
    }

    @Override
    public String getDisplayName(Energy energy) {
        return new TranslatableComponent("custommachinery.jei.ingredient.energy", energy.getAmount()).getString();
    }

    @Override
    public String getUniqueId(Energy energy, UidContext context) {
        return "" + energy.getAmount() + energy.getChance() + energy.isPerTick();
    }

    //Safe delete when needed
    @SuppressWarnings("removal")
    @Override
    public String getModId(Energy energy) {
        return CustomMachinery.MODID;
    }

    //Safe delete when needed
    @SuppressWarnings("removal")
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

    @Override
    public ResourceLocation getResourceLocation(Energy ingredient) {
        return new ResourceLocation(CustomMachinery.MODID, "energy");
    }
}
