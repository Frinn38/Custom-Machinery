package fr.frinn.custommachinery.client.element;

import com.google.common.collect.Lists;
import fr.frinn.custommachinery.api.guielement.IMachineScreen;
import fr.frinn.custommachinery.common.guielement.ExperienceGuiElement;
import fr.frinn.custommachinery.common.init.Registration;
import fr.frinn.custommachinery.common.util.ExperienceUtils;
import fr.frinn.custommachinery.common.util.Utils;
import fr.frinn.custommachinery.impl.guielement.TexturedGuiElementWidget;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;

import java.util.List;

public class ExperienceGuiElementWidget extends TexturedGuiElementWidget<ExperienceGuiElement> {
  private static final Component TITLE = Component.translatable("custommachinery.gui.element.experience.name");

  public ExperienceGuiElementWidget(ExperienceGuiElement element, IMachineScreen screen) {
    super(element, screen, TITLE);
  }

  @Override
  public void renderWidget(GuiGraphics graphics, int mouseX, int mouseY, float partialTicks) {
    if(!this.getElement().getMode().isDisplayBar())
      super.renderWidget(graphics, mouseX, mouseY, partialTicks);
    else
      getScreen().getTile().getComponentManager()
        .getComponent(Registration.EXPERIENCE_MACHINE_COMPONENT.get())
          .ifPresent(component -> {
            String levels = "" + component.getLevels();
            int xPos = this.getX() + this.width / 2 - Minecraft.getInstance().font.width(levels) / 2;
            graphics.drawString(Minecraft.getInstance().font, levels, xPos, this.getY(), 0x80FF20, true);
            graphics.fill(this.getX(), this.getY() + 9, this.getX() + this.width, this.getY() + 12, 0xFF000000);
            int xpDiff = component.getXp() - ExperienceUtils.getXpFromLevel(component.getLevels());
            if(xpDiff > 0) {
              double percent = (double) xpDiff / ExperienceUtils.getXpNeededForNextLevel(component.getLevels());
              graphics.fill(this.getX() + 1, this.getY() + 10, this.getX() + 1 + Math.max((int) Math.ceil(this.width * percent) - 2, 0), this.getY() + 11, 0xFF80FF20);
            }
          });
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
}
