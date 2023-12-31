package fr.frinn.custommachinery.client.integration.jei.wrapper;

import fr.frinn.custommachinery.api.guielement.IGuiElement;
import fr.frinn.custommachinery.api.integration.jei.IJEIIngredientWrapper;
import fr.frinn.custommachinery.api.integration.jei.IRecipeHelper;
import fr.frinn.custommachinery.api.requirement.RequirementIOMode;
import fr.frinn.custommachinery.client.integration.jei.experience.ExperienceJEIIngredientRenderer;
import fr.frinn.custommachinery.common.guielement.ExperienceGuiElement;
import fr.frinn.custommachinery.common.init.Registration;
import fr.frinn.custommachinery.common.util.Utils;
import fr.frinn.custommachinery.impl.integration.jei.CustomIngredientTypes;
import fr.frinn.custommachinery.impl.integration.jei.ExperienceStorage;
import mezz.jei.api.gui.builder.IRecipeLayoutBuilder;
import net.minecraft.network.chat.Component;

public class ExperienceIngredientWrapper implements IJEIIngredientWrapper<ExperienceStorage> {
  private final RequirementIOMode mode;
  private final int recipeTime;
  private final ExperienceStorage experience;

  public ExperienceIngredientWrapper(RequirementIOMode mode, float amount, double chance, boolean isPerTick, int recipeTime) {
    this.mode = mode;
    this.recipeTime = recipeTime;
    this.experience = new ExperienceStorage(amount, chance, isPerTick);
  }

  @Override
  public boolean setupRecipe(IRecipeLayoutBuilder builder, int xOffset, int yOffset, IGuiElement element, IRecipeHelper helper) {
    if (
      !(element instanceof ExperienceGuiElement experienceElement)
      || element.getType() != Registration.EXPERIENCE_GUI_ELEMENT.get()
      || !experienceElement.getMode().isDisplay()
    )
      return false;

    builder.addSlot(roleFromMode(this.mode), element.getX() - xOffset + 1, element.getY() - yOffset + 1)
      .setCustomRenderer(CustomIngredientTypes.EXPERIENCE, new ExperienceJEIIngredientRenderer(experienceElement))
      .addIngredient(CustomIngredientTypes.EXPERIENCE, this.experience)
      .addTooltipCallback((recipeSlotView, tooltip) -> {
        Component component;
        String amount = Utils.format(this.experience.getXp());
        if (this.experience.isPerTick()) {
          String totalExperience = Utils.format(this.experience.getXp() * this.recipeTime);
          if (this.mode == RequirementIOMode.INPUT)
            component = Component.translatable("custommachinery.jei.ingredient.xp.pertick.input", totalExperience, "XP", amount, "XP");
          else
            component = Component.translatable("custommachinery.jei.ingredient.xp.pertick.output", totalExperience, "XP", amount, "XP");
        } else {
          if(this.mode == RequirementIOMode.INPUT)
            component = Component.translatable("custommachinery.jei.ingredient.xp.input", amount, "XP");
          else
            component = Component.translatable("custommachinery.jei.ingredient.xp.output", amount, "XP");
        }
        tooltip.set(0, component);
      });
    return true;
  }
}
