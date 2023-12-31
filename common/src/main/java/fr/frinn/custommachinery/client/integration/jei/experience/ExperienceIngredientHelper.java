package fr.frinn.custommachinery.client.integration.jei.experience;

import fr.frinn.custommachinery.CustomMachinery;
import fr.frinn.custommachinery.impl.integration.jei.CustomIngredientTypes;
import fr.frinn.custommachinery.impl.integration.jei.ExperienceStorage;
import mezz.jei.api.ingredients.IIngredientHelper;
import mezz.jei.api.ingredients.IIngredientType;
import mezz.jei.api.ingredients.subtypes.UidContext;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.Nullable;

public class ExperienceIngredientHelper implements IIngredientHelper<ExperienceStorage> {

  @Override
  public IIngredientType<ExperienceStorage> getIngredientType() {
    return CustomIngredientTypes.EXPERIENCE;
  }

  @Override
  public String getDisplayName(ExperienceStorage xp) {
    return Component.translatable("custommachinery.jei.ingredient.xp", xp.getXp()).getString();
  }

  @Override
  public String getUniqueId(ExperienceStorage xp, UidContext context) {
    return "" + xp.getXp() + xp.getChance() + xp.isPerTick();
  }

  @Override
  public ExperienceStorage copyIngredient(ExperienceStorage xp) {
    return new ExperienceStorage(xp.getXp(), xp.getChance(), xp.isPerTick());
  }

  @Override
  public String getErrorInfo(@Nullable ExperienceStorage energy) {
    return "";
  }

  @Override
  public ResourceLocation getResourceLocation(ExperienceStorage ingredient) {
    return new ResourceLocation(CustomMachinery.MODID, "experience");
  }
}
