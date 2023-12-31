package fr.frinn.custommachinery.common.guielement;

import fr.frinn.custommachinery.CustomMachinery;
import fr.frinn.custommachinery.api.codec.NamedCodec;
import fr.frinn.custommachinery.api.guielement.GuiElementType;
import fr.frinn.custommachinery.api.machine.MachineTile;
import fr.frinn.custommachinery.common.init.Registration;
import fr.frinn.custommachinery.common.util.Codecs;
import fr.frinn.custommachinery.common.util.Utils;
import fr.frinn.custommachinery.impl.guielement.AbstractTexturedGuiElement;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.inventory.AbstractContainerMenu;

public class ExperienceGuiElement extends AbstractTexturedGuiElement {
  private static final ResourceLocation BASE_TEXTURE = new ResourceLocation(CustomMachinery.MODID, "textures/gui/base_xp.png");
  private static final ResourceLocation BASE_TEXTURE_HOVERED = new ResourceLocation(CustomMachinery.MODID, "textures/gui/base_xp_hovered.png");
  private final ShowType showType;
  private final Mode mode;

  public static final NamedCodec<ExperienceGuiElement> CODEC = NamedCodec.record(experienceGuiElement ->
    experienceGuiElement.group(
      makePropertiesCodec(
        BASE_TEXTURE,
        BASE_TEXTURE_HOVERED
      ).forGetter(ExperienceGuiElement::getProperties),
      Codecs.fromEnum(ShowType.class).optionalFieldOf("display", ShowType.LEVEL).forGetter(element -> element.showType),
      Codecs.fromEnum(Mode.class).optionalFieldOf("mode", Mode.OUTPUT_ALL).forGetter(element -> element.mode)
    ).apply(experienceGuiElement, ExperienceGuiElement::new), "Experience gui element"
  );

  public ExperienceGuiElement(Properties properties, ShowType showType, Mode mode){
    super(properties);
    this.showType = showType;
    this.mode = mode;
  }

  @Override
  public GuiElementType<ExperienceGuiElement> getType() {
    return Registration.EXPERIENCE_GUI_ELEMENT.get();
  }

  @Override
  public void handleClick(byte button, MachineTile tile, AbstractContainerMenu container, ServerPlayer player) {
    super.handleClick(button, tile, container, player);
    tile.getComponentManager().getComponent(Registration.EXPERIENCE_MACHINE_COMPONENT.get())
        .ifPresent(component -> {
          switch(mode) {
            case INPUT_ONE -> {
              int pointsToExtract;
              if (player.experienceLevel >= 1)
                pointsToExtract = getXpNeededForNextLevel(player.experienceLevel - 1);
              else
                pointsToExtract = player.totalExperience;
              if(component.receiveXp(pointsToExtract, true) == (float) pointsToExtract) {
                component.receiveXp(pointsToExtract, false);
                player.giveExperiencePoints(-pointsToExtract);
              }
            }
            case INPUT_TEN -> {
              int pointsToExtract = 0;
              if (player.experienceLevel >= 10)
                for (int i = player.experienceLevel - 1; i > player.experienceLevel - 11; i--) {
                  pointsToExtract += getXpNeededForNextLevel(i);
                }
              else
                pointsToExtract = player.totalExperience;
              if(component.receiveXp(pointsToExtract, true) == (float) pointsToExtract) {
                component.receiveXp(pointsToExtract, false);
                player.giveExperiencePoints(-pointsToExtract);
              }
            }
            case INPUT_ALL -> {
              int pointsToExtract = player.totalExperience;
              if(component.receiveXp(pointsToExtract, true) == (float) pointsToExtract) {
                component.receiveXp(pointsToExtract, false);
                player.giveExperiencePoints(-pointsToExtract);
              }
            }
            case OUTPUT_ONE -> {
              int pointsToExtract = getXpNeededForNextLevel(player.experienceLevel);
              if (pointsToExtract > component.getXp()) pointsToExtract = Utils.toInt(component.getXp());
              if(component.extractXp(pointsToExtract, true) == (float) pointsToExtract) {
                component.extractXp(pointsToExtract, false);
                player.giveExperiencePoints(pointsToExtract);
              }
            }
            case OUTPUT_TEN -> {
              int pointsToExtract = 0;
              for (int i = player.experienceLevel; i < player.experienceLevel + 10; i++) {
                pointsToExtract += getXpNeededForNextLevel(i);
              }
              if (pointsToExtract > component.getXp()) pointsToExtract = Utils.toInt(component.getXp());
              if(component.extractXp(pointsToExtract, true) == (float) pointsToExtract) {
                component.extractXp(pointsToExtract, false);
                player.giveExperiencePoints(pointsToExtract);
              }
            }
            case OUTPUT_ALL -> {
              int amount = Utils.toInt(component.getXp());
              component.extractXp(amount, false);
              player.giveExperiencePoints(amount);
            }
          }
        });
  }

  public ShowType getShowType() {
    return showType;
  }

  public Mode getMode() {
    return mode;
  }

  private int getXpNeededForNextLevel(int experienceLevel) {
    if (experienceLevel >= 30) {
      return 112 + (experienceLevel - 30) * 9;
    } else {
      return experienceLevel >= 15 ? 37 + (experienceLevel - 15) * 5 : 7 + experienceLevel * 2;
    }
  }

  public enum ShowType {
    LITERAL, LEVEL, BOTH;

    public boolean isLiteral() {
      return this == LITERAL;
    }

    public boolean isLevel() {
      return this == LEVEL;
    }

    public boolean isBoth() {
      return this == BOTH;
    }

    public static ShowType of(String value) {
      if (value.equalsIgnoreCase("literal")) return LITERAL;
      if (value.equalsIgnoreCase("level")) return LEVEL;
      if (value.equalsIgnoreCase("both")) return BOTH;
      return null;
    }
  }

  public enum Mode {
    INPUT_ONE,
    INPUT_TEN,
    INPUT_ALL,
    OUTPUT_ONE,
    OUTPUT_TEN,
    OUTPUT_ALL,
    DISPLAY;

    public boolean isInputOne() {
      return this == INPUT_ONE;
    }

    public boolean isInputAll() {
      return this == INPUT_ALL;
    }

    public boolean isInput() {
      return isInputOne() || isInputAll();
    }

    public boolean isOutputAll() {
      return this == OUTPUT_ALL;
    }

    public boolean isOutputOne() {
      return this == OUTPUT_ONE;
    }

    public boolean isOutput() {
      return isOutputOne() || isOutputAll();
    }

    public boolean isDisplay() {
      return this == DISPLAY;
    }

    public Component title() {
      return switch(this) {
        case INPUT_ONE -> Component.translatable("custommachinery.gui.element.experience.input_one");
        case INPUT_TEN-> Component.translatable("custommachinery.gui.element.experience.input_ten");
        case INPUT_ALL -> Component.translatable("custommachinery.gui.element.experience.input_all");
        case OUTPUT_ONE -> Component.translatable("custommachinery.gui.element.experience.output_one");
        case OUTPUT_TEN -> Component.translatable("custommachinery.gui.element.experience.output_ten");
        case OUTPUT_ALL -> Component.translatable("custommachinery.gui.element.experience.output_all");
        case DISPLAY -> Component.empty();
      };
    }

    public static Mode of(String value) {
      if (value.equalsIgnoreCase("input_one")) return INPUT_ONE;
      if (value.equalsIgnoreCase("input_ten")) return INPUT_TEN;
      if (value.equalsIgnoreCase("input_all")) return INPUT_ALL;
      if (value.equalsIgnoreCase("input")) return INPUT_ALL;
      if (value.equalsIgnoreCase("output_one")) return OUTPUT_ONE;
      if (value.equalsIgnoreCase("output_ten")) return OUTPUT_TEN;
      if (value.equalsIgnoreCase("output_all")) return OUTPUT_ALL;
      if (value.equalsIgnoreCase("output")) return OUTPUT_ALL;
      if (value.equalsIgnoreCase("display")) return DISPLAY;
      return null;
    }
  }
}
