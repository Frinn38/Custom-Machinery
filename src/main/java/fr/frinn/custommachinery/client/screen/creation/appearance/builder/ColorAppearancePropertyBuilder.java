package fr.frinn.custommachinery.client.screen.creation.appearance.builder;

import fr.frinn.custommachinery.CustomMachinery;
import fr.frinn.custommachinery.api.machine.MachineAppearanceProperty;
import fr.frinn.custommachinery.client.screen.BaseScreen;
import fr.frinn.custommachinery.client.screen.creation.appearance.IAppearancePropertyBuilder;
import fr.frinn.custommachinery.client.screen.widget.GroupWidget;
import fr.frinn.custommachinery.client.screen.widget.IntegerEditBox;
import fr.frinn.custommachinery.common.init.Registration;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.components.StateSwitchingButton;
import net.minecraft.client.gui.components.Tooltip;
import net.minecraft.client.gui.components.WidgetSprites;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.network.chat.Component;
import net.minecraft.util.FastColor;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.function.Consumer;
import java.util.function.Supplier;

public class ColorAppearancePropertyBuilder implements IAppearancePropertyBuilder<Integer> {

    @Override
    public Component title() {
        return Component.translatable("custommachinery.gui.creation.appearance.color");
    }

    @Override
    public MachineAppearanceProperty<Integer> type() {
        return Registration.COLOR_PROPERTY.get();
    }

    @Override
    public AbstractWidget makeWidget(BaseScreen parent, int x, int y, int width, int height, Supplier<Integer> supplier, Consumer<Integer> consumer) {
        return new ColorAppearanceWidget(x, y, width, height, title(), supplier, consumer);
    }

    public static class ColorAppearanceWidget extends GroupWidget {

        public ColorAppearanceWidget(int x, int y, int width, int height, Component message, Supplier<Integer> supplier, Consumer<Integer> consumer) {
            super(x, y, width, height, message);
            Map<ChatFormatting, StateSwitchingButton> colorButtonMap = new HashMap<>();
            IntegerEditBox editBox = new IntegerEditBox(Minecraft.getInstance().font, 80, 0, 60, 20, Component.translatable("custommachinery.gui.creation.appearance.color"));
            editBox.bounds(0, Integer.MAX_VALUE);
            editBox.setIntValue(supplier.get());
            editBox.setIntResponder(color -> {
                colorButtonMap.forEach((format, button) -> button.setStateTriggered(Objects.equals(format.getColor(), color)));
                consumer.accept(color);
            });
            editBox.setTooltip(Tooltip.create(Component.translatable("custommachinery.gui.creation.appearance.color.tooltip")));
            this.addWidget(editBox);
            for(int i = 0; i < 16; i++) {
                ChatFormatting format = ChatFormatting.getById(i);
                if(format == null || format.getColor() == null)
                    continue;
                String name = format.getName();
                WidgetSprites sprites = new WidgetSprites(CustomMachinery.rl("creation/style/" + name + "_selected"), CustomMachinery.rl("creation/style/" + name), CustomMachinery.rl("creation/style/" + name + "_selected"), CustomMachinery.rl("creation/style/" + name + "_selected"));
                StateSwitchingButton button = new StateSwitchingButton(i % 8 * 10 - 1, i < 8 ? 0 : 10, 10, 10, Objects.equals(format.getColor(), supplier.get())) {
                    @Override
                    public void onClick(double mouseX, double mouseY, int button) {
                        consumer.accept(format.getColor());
                        editBox.setIntValue(format.getColor());
                    }
                };
                button.initTextureValues(sprites);
                button.setTooltip(Tooltip.create(Component.translatable(format.getName()).withStyle(format)));
                this.addWidget(button);
                colorButtonMap.put(format, button);
            }
            AbstractWidget colorWidget = new AbstractWidget(141, 1, 18, 18, Component.translatable("custommachinery.gui.creation.appearance.color")) {
                @Override
                protected void renderWidget(GuiGraphics graphics, int mouseX, int mouseY, float partialTicks) {
                    graphics.fill(this.getX(), this.getY(), this.getX() + this.getWidth(), this.getY() + this.getHeight(), FastColor.ARGB32.color(255, 0, 0, 0));
                    graphics.fill(this.getX() + 1, this.getY() + 1, this.getX() + this.getWidth() - 1, this.getY() + this.getHeight() - 1, 0xFF000000 | supplier.get());
                }

                @Override
                protected void updateWidgetNarration(NarrationElementOutput pNarrationElementOutput) {

                }

                @Override
                protected boolean clicked(double pMouseX, double pMouseY) {
                    return false;
                }
            };
            this.addWidget(colorWidget);
        }
    }
}
