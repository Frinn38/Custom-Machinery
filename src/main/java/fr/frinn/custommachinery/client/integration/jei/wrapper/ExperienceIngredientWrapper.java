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
import fr.frinn.custommachinery.impl.integration.jei.Experience;
import mezz.jei.api.gui.builder.IRecipeLayoutBuilder;
import net.minecraft.network.chat.Component;

public class ExperienceIngredientWrapper implements IJEIIngredientWrapper<Experience> {
  private final RequirementIOMode mode;
  private final int recipeTime;
  private final Experience experience;

  public ExperienceIngredientWrapper(RequirementIOMode mode, int amount, double chance, boolean isPerTick, int recipeTime, Experience.Form type) {
    this.mode = mode;
    this.recipeTime = recipeTime;
    this.experience = new Experience(amount, chance, isPerTick, type);
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
      .addRichTooltipCallback((recipeSlotView, tooltip) -> {
        Component component;
        String amount = Utils.format(this.experience.getXp());
        if (this.experience.isPoints()) {
          if (this.experience.isPerTick()) {
            String totalExperience = Utils.format(this.experience.getXp() * this.recipeTime);
            if (this.mode == RequirementIOMode.INPUT)
              component = Component.translatable("custommachinery.jei.ingredient.xp.pertick.input", totalExperience, "XP", amount, "XP");
            else
              component = Component.translatable("custommachinery.jei.ingredient.xp.pertick.output", totalExperience, "XP", amount, "XP");
          } else {
            if (this.mode == RequirementIOMode.INPUT)
              component = Component.translatable("custommachinery.jei.ingredient.xp.input", amount, "XP");
            else
              component = Component.translatable("custommachinery.jei.ingredient.xp.output", amount, "XP");
          }
        } else {
          if (this.experience.isPerTick()) {
            String totalExperience = Utils.format(this.experience.getLevels() * this.recipeTime);
            if (this.mode == RequirementIOMode.INPUT)
              component = Component.translatable("custommachinery.jei.ingredient.xp.pertick.input", totalExperience, "Level(s)", amount, "Level(s)");
            else
              component = Component.translatable("custommachinery.jei.ingredient.xp.pertick.output", totalExperience, "Level(s)", amount, "Level(s)");
          } else {
            if (this.mode == RequirementIOMode.INPUT)
              component = Component.translatable("custommachinery.jei.ingredient.xp.input", amount, "Level(s)");
            else
              component = Component.translatable("custommachinery.jei.ingredient.xp.output", amount, "Level(s)");
          }
        }
        tooltip.add(component);
      });
    return true;
  }
}
