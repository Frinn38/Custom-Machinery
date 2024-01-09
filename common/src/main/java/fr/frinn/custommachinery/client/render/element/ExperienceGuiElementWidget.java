package fr.frinn.custommachinery.client.render.element;

import com.google.common.collect.Lists;
import fr.frinn.custommachinery.api.guielement.IMachineScreen;
import fr.frinn.custommachinery.common.guielement.ExperienceGuiElement;
import fr.frinn.custommachinery.common.init.Registration;
import fr.frinn.custommachinery.common.util.Utils;
import fr.frinn.custommachinery.impl.guielement.TexturedGuiElementWidget;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;

import java.util.List;

public class ExperienceGuiElementWidget extends TexturedGuiElementWidget<ExperienceGuiElement> {
  private static final Component TITLE = Component.translatable("custommachinery.gui.element.experience.name");

  public ExperienceGuiElementWidget(ExperienceGuiElement element, IMachineScreen screen) {
    super(element, screen, TITLE);
  }

  @Override
  public List<Component> getTooltips() {
    List<Component> tooltips = Lists.newArrayList();
    getScreen().getTile().getComponentManager()
      .getComponent(Registration.EXPERIENCE_MACHINE_COMPONENT.get())
      .ifPresent(component -> {
        if (getElement().getMode().isDisplay()) {
          tooltips.add(TITLE);
          switch(getElement().getDisplayMode()) {
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
              String value = component.getLevels() + "";
              String capacityValue = component.getCapacityLevels() + " levels";
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
              String level = component.getLevels() + "";
              String capacityLevel = component.getCapacityLevels() + " levels";
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
          tooltips.add(getElement().getMode().title());
        }
      });
    return tooltips;
  }

  @Override
  public boolean isClickable() {
    return !this.getElement().getMode().isDisplay();
  }
}
