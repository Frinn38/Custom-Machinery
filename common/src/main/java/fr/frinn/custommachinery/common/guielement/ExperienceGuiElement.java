package fr.frinn.custommachinery.common.guielement;

import fr.frinn.custommachinery.CustomMachinery;
import fr.frinn.custommachinery.api.codec.NamedCodec;
import fr.frinn.custommachinery.api.guielement.GuiElementType;
import fr.frinn.custommachinery.api.machine.MachineTile;
import fr.frinn.custommachinery.common.init.Registration;
import fr.frinn.custommachinery.impl.guielement.AbstractTexturedGuiElement;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.inventory.AbstractContainerMenu;

public class ExperienceGuiElement extends AbstractTexturedGuiElement {
  private static final ResourceLocation BASE_TEXTURE = new ResourceLocation(CustomMachinery.MODID, "textures/gui/base_xp.png");
  private static final ResourceLocation BASE_TEXTURE_HOVERED = new ResourceLocation(CustomMachinery.MODID, "textures/gui/base_xp_hovered.png");
  private final DisplayMode displayMode;
  private final Mode mode;

  public static final NamedCodec<ExperienceGuiElement> CODEC = NamedCodec.record(experienceGuiElement ->
    experienceGuiElement.group(
      makePropertiesCodec(
        BASE_TEXTURE,
        BASE_TEXTURE_HOVERED
      ).forGetter(ExperienceGuiElement::getProperties),
      NamedCodec.enumCodec(DisplayMode.class).optionalFieldOf("display", DisplayMode.LEVEL).forGetter(element -> element.displayMode),
      NamedCodec.enumCodec(Mode.class).optionalFieldOf("mode", Mode.OUTPUT_ALL).forGetter(element -> element.mode)
    ).apply(experienceGuiElement, ExperienceGuiElement::new), "Experience gui element"
  );

  public ExperienceGuiElement(Properties properties, DisplayMode displayMode, Mode mode){
    super(properties);
    this.displayMode = displayMode;
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
            case INPUT_ONE -> component.receiveLevelFromPlayer(1, player);
            case INPUT_TEN -> component.receiveLevelFromPlayer(10, player);
            case INPUT_ALL -> component.receiveLevelFromPlayer(player);
            case OUTPUT_ONE -> component.giveLevelToPlayer(1, player);
            case OUTPUT_TEN -> component.giveLevelToPlayer(10, player);
            case OUTPUT_ALL -> component.giveLevelToPlayer(player);
          }
        });
  }

  public DisplayMode getDisplayMode() {
    return displayMode;
  }

  public Mode getMode() {
    return mode;
  }

  public enum DisplayMode {
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

    public static DisplayMode of(String value) {
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
