package fr.frinn.custommachinery.client.render.element;

import com.google.common.collect.Lists;
import fr.frinn.custommachinery.api.guielement.IMachineScreen;
import fr.frinn.custommachinery.common.guielement.ExperienceGuiElement;
import fr.frinn.custommachinery.common.init.Registration;
import fr.frinn.custommachinery.common.util.Utils;
import fr.frinn.custommachinery.impl.guielement.TexturedGuiElementWidget;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.util.Mth;

import java.util.List;

public class ExperienceGuiElementWidget extends TexturedGuiElementWidget<ExperienceGuiElement> {
  private static final Component TITLE = Component.translatable("custommachinery.gui.element.experience.name");
  private final List<Component> tooltips;
  int experienceLevel = 0;
  float totalExperience = 0;

  public ExperienceGuiElementWidget(ExperienceGuiElement element, IMachineScreen screen) {
    super(element, screen, TITLE);
    this.tooltips = Lists.newArrayList(TITLE);
    getScreen().getTile().getComponentManager()
      .getComponent(Registration.EXPERIENCE_MACHINE_COMPONENT.get())
      .ifPresent(component -> {
        if (element.getMode().isDisplay()) {
          switch(element.getShowType()) {
            case LITERAL -> {
              String value = Utils.format(component.getXp());
              String capacityValue = Utils.format(component.getCapacity()) + " XP";
              tooltips.add(
                Component.translatable("custommachinery.gui.element.experience.tooltip",
                  value,
                  capacityValue
                ).withStyle(ChatFormatting.GRAY)
              );
            }
            case LEVEL -> {
              experienceLevel = 0;
              totalExperience = 0;
              String value = getFromExperiencePoints(Utils.toInt(component.getXp())) + "";
              experienceLevel = 0;
              totalExperience = 0;
              String capacityValue = getFromExperiencePoints(Utils.toInt(component.getCapacity())) + " levels";
              tooltips.add(
                Component.translatable("custommachinery.gui.element.experience.tooltip",
                  value,
                  capacityValue
                ).withStyle(ChatFormatting.GRAY)
              );
            }
            case BOTH -> {
              String literal = Utils.format(component.getXp());
              String capacityLiteral = Utils.format(component.getCapacity()) + "XP";
              experienceLevel = 0;
              totalExperience = 0;
              String level = getFromExperiencePoints(Utils.toInt(component.getXp())) + "";
              experienceLevel = 0;
              totalExperience = 0;
              String capacityLevel = getFromExperiencePoints(Utils.toInt(component.getCapacity())) + " levels";
              tooltips.addAll(
                Lists.newArrayList(
                  Component.translatable("custommachinery.gui.element.experience.tooltip",
                    literal,
                    capacityLiteral
                  ).withStyle(ChatFormatting.GRAY),
                  Component.translatable(
                    "custommachinery.gui.element.experience.tooltip",
                    level,
                    capacityLevel
                  ).withStyle(ChatFormatting.GRAY)
                )
              );
            }
          }
        } else {
          tooltips.clear();
          tooltips.add(element.getMode().title());
        }
      });
  }

  private int getXpNeededForNextLevel(int experienceLevel) {
    if (experienceLevel >= 30) {
      return 112 + (experienceLevel - 30) * 9;
    } else {
      return experienceLevel >= 15 ? 37 + (experienceLevel - 15) * 5 : 7 + experienceLevel * 2;
    }
  }

  private int getFromExperiencePoints(int xpPoints) {
    float experienceProgress = (float)xpPoints / (float) this.getXpNeededForNextLevel(experienceLevel);
    totalExperience = Mth.clamp(totalExperience + xpPoints, 0, Integer.MAX_VALUE);

    while(experienceProgress < 0.0F) {
      float f = experienceProgress * (float) this.getXpNeededForNextLevel(experienceLevel);
      if (experienceLevel > 0) {
        experienceLevel -= 1;
        experienceProgress = 1.0F + f / (float) this.getXpNeededForNextLevel(experienceLevel);
      } else {
        experienceLevel -= 1;
        experienceProgress = 0.0F;
      }
    }

    while(experienceProgress >= 1.0F) {
      experienceProgress = (experienceProgress - 1.0F) * (float) this.getXpNeededForNextLevel(experienceLevel);
      experienceLevel += 1;
      experienceProgress /= (float) this.getXpNeededForNextLevel(experienceLevel);
    }
    return experienceLevel;
  }

  @Override
  public List<Component> getTooltips() {
    if(this.getElement().getTooltips().isEmpty())
      return this.tooltips;
    return this.getElement().getTooltips();
  }

  @Override
  public boolean isClickable() {
    return true;
  }
}
