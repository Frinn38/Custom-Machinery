package fr.frinn.custommachinery.client.integration.jei.experience;

import com.mojang.blaze3d.vertex.PoseStack;
import fr.frinn.custommachinery.api.integration.jei.JEIIngredientRenderer;
import fr.frinn.custommachinery.common.guielement.ExperienceGuiElement;
import fr.frinn.custommachinery.common.util.Color;
import fr.frinn.custommachinery.common.util.Utils;
import fr.frinn.custommachinery.impl.integration.jei.CustomIngredientTypes;
import fr.frinn.custommachinery.impl.integration.jei.Experience;
import mezz.jei.api.ingredients.IIngredientType;
import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.GuiComponent;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.TooltipFlag;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

public class ExperienceJEIIngredientRenderer extends JEIIngredientRenderer<Experience, ExperienceGuiElement> {
  public ExperienceJEIIngredientRenderer(ExperienceGuiElement element) {
    super(element);
  }

  @Override
  public IIngredientType<Experience> getType() {
    return CustomIngredientTypes.EXPERIENCE;
  }

  @Override
  public int getWidth() {
    return this.element.getWidth() - 2;
  }

  @Override
  public int getHeight() {
    return this.element.getHeight() - 2;
  }

  @Override
  public void render(PoseStack matrix, @Nullable Experience ingredient) {
    int width = this.element.getWidth();
    int height = this.element.getHeight();

    GuiComponent.fill(matrix, element.getX(), element.getY(),
      element.getX() + width, element.getY() + height,
      Color.fromColors(1, 7, 186, 7).getARGB()
    );
  }

  @Override
  public List<Component> getTooltip(Experience ingredient, TooltipFlag iTooltipFlag) {
    List<Component> tooltips = new ArrayList<>();
    String amount = Utils.format(ingredient.getXp());
    if (ingredient.isPoints()) {
      if(ingredient.isPerTick())
        tooltips.add(Component.translatable("custommachinery.jei.ingredient.xp.point.pertick", amount));
      else
        tooltips.add(Component.translatable("custommachinery.jei.ingredient.xp.point", amount));
    } else if (ingredient.isLevels()) {
      if(ingredient.isPerTick())
        tooltips.add(Component.translatable("custommachinery.jei.ingredient.xp.level.pertick", amount));
      else
        tooltips.add(Component.translatable("custommachinery.jei.ingredient.xp.level", amount));
    }
    if(ingredient.getChance() == 0)
      tooltips.add(Component.translatable("custommachinery.jei.ingredient.chance.0").withStyle(ChatFormatting.DARK_RED));
    if(ingredient.getChance() < 1.0D && ingredient.getChance() > 0)
      tooltips.add(Component.translatable("custommachinery.jei.ingredient.chance", (int)(ingredient.getChance() * 100)));
    return tooltips;
  }
}
