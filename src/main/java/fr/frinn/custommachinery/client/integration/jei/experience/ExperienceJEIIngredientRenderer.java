package fr.frinn.custommachinery.client.integration.jei.experience;

import fr.frinn.custommachinery.api.integration.jei.JEIIngredientRenderer;
import fr.frinn.custommachinery.common.guielement.ExperienceGuiElement;
import fr.frinn.custommachinery.common.util.Color;
import fr.frinn.custommachinery.common.util.ExperienceUtils;
import fr.frinn.custommachinery.impl.integration.jei.CustomIngredientTypes;
import fr.frinn.custommachinery.impl.integration.jei.Experience;
import mezz.jei.api.gui.builder.ITooltipBuilder;
import mezz.jei.api.ingredients.IIngredientType;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.TooltipFlag;
import org.jetbrains.annotations.Nullable;

import java.util.Collections;
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
  public void render(GuiGraphics graphics, @Nullable Experience ingredient) {
    int width = this.getWidth();
    int height = this.getHeight();

    if(this.element.getMode().isDisplayBar()) {
      String levels = "" + (ingredient != null ? ingredient.isLevels() ? ingredient.xp() : ExperienceUtils.getLevelFromXp(ingredient.xp()) : 0);
      int xPos = width / 2 - Minecraft.getInstance().font.width(levels) / 2;
      graphics.drawString(Minecraft.getInstance().font, levels, xPos, 0, 0x80FF20, true);
      graphics.fill(0, height - 3, width, height, 0xFF000000);
      if(ingredient != null && ingredient.isPoints()) {
        int level = ExperienceUtils.getLevelFromXp(ingredient.xp());
        int xpDiff = ingredient.xp() - ExperienceUtils.getXpFromLevel(level);
        if(xpDiff > 0) {
          double percent = (double) xpDiff / ExperienceUtils.getXpNeededForNextLevel(level);
          graphics.fill(1, height - 2, 1 + Math.max((int) Math.ceil(width * percent) - 2, 0), height - 1, 0xFF80FF20);
        }
      }
    } else {
      graphics.fill(element.getX(), element.getY(), this.element.getX() + width, this.element.getY() + height, Color.fromColors(1, 7, 186, 7).getARGB());
    }
  }

  //Safe to remove
  @SuppressWarnings("removal")
  @Override
  public List<Component> getTooltip(Experience ingredient, TooltipFlag flag) {
    return Collections.emptyList();
  }

  @Override
  public void getTooltip(ITooltipBuilder builder, Experience ingredient, TooltipFlag flag) {

  }
}
