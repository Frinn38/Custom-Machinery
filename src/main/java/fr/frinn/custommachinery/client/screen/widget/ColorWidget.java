package fr.frinn.custommachinery.client.screen.widget;

import fr.frinn.custommachinery.CustomMachinery;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.components.Tooltip;
import net.minecraft.client.gui.components.WidgetSprites;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.network.chat.Component;
import net.minecraft.util.ARGB;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.function.Consumer;
import java.util.function.Supplier;

public class ColorWidget extends GroupWidget {

    public ColorWidget(int x, int y, int width, int height, Component message, Supplier<String> supplier, Consumer<String> consumer, boolean twoLines) {
        super(x, y, width, height, message);
        Map<ChatFormatting, ToggleImageButton> colorButtonMap = new HashMap<>();
        EditBox editBox = new EditBox(Minecraft.getInstance().font, 80, 0, 60, 20, Component.translatable("custommachinery.gui.creation.appearance.color"));
        editBox.setValue(supplier.get());
        editBox.setResponder(color -> {
            colorButtonMap.forEach((format, button) -> button.setToggle(Objects.equals(format.getColor().toString(), color)));
            consumer.accept(color);
        });
        editBox.setTooltip(Tooltip.create(Component.translatable("custommachinery.gui.creation.appearance.color.tooltip")));
        if(twoLines)
            editBox.setPosition(0, 0);
        this.addWidget(editBox);

        for (int i = 0; i < 16; i++) {
            ChatFormatting format = ChatFormatting.getById(i);
            if (format == null || format.getColor() == null)
                continue;
            String name = format.getName();
            WidgetSprites sprites = new WidgetSprites(CustomMachinery.rl("creation/style/" + name + "_selected"), CustomMachinery.rl("creation/style/" + name), CustomMachinery.rl("creation/style/" + name + "_selected"), CustomMachinery.rl("creation/style/" + name + "_selected"));
            ToggleImageButton button = new ToggleImageButton(i % 8 * 10 - 1, i < 8 ? 0 : 10, 10, 10, sprites, b -> {
                consumer.accept(format.getColor().toString());
                editBox.setValue(format.getColor().toString());
            });
            button.setTooltip(Tooltip.create(Component.translatable(format.getName()).withStyle(format == ChatFormatting.BLACK ? ChatFormatting.WHITE : format)));
            if(twoLines)
                button.setPosition(i % 8 * 10 - 1, (i < 8 ? 0 : 10) + 20);
            this.addWidget(button);
            colorButtonMap.put(format, button);
        }

        AbstractWidget colorWidget = getColorWidget(supplier, twoLines);
        this.addWidget(colorWidget);
    }

    private AbstractWidget getColorWidget(Supplier<String> supplier, boolean twoLines) {
        AbstractWidget colorWidget = new AbstractWidget(141, 1, 18, 18, Component.translatable("custommachinery.gui.creation.appearance.color")) {
            @Override
            protected void extractWidgetRenderState(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float partialTicks) {
                graphics.fill(this.getX(), this.getY(), this.getX() + this.getWidth(), this.getY() + this.getHeight(), ARGB.color(255, 0, 0, 0));
                graphics.fill(this.getX() + 1, this.getY() + 1, this.getX() + this.getWidth() - 1, this.getY() + this.getHeight() - 1, 0xFF000000 | Integer.parseInt(supplier.get()));
            }

            @Override
            protected void updateWidgetNarration(NarrationElementOutput pNarrationElementOutput) {

            }

            @Override
            public boolean mouseClicked(MouseButtonEvent event, boolean doubleClick) {
                return false;
            }
        };
        if(twoLines)
            colorWidget.setPosition(61, 1);
        return colorWidget;
    }
}
