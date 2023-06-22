package fr.frinn.custommachinery.client.render.element;

import com.google.common.collect.Lists;
import fr.frinn.custommachinery.api.guielement.IMachineScreen;
import fr.frinn.custommachinery.common.guielement.ResetGuiElement;
import fr.frinn.custommachinery.impl.guielement.TexturedGuiElementWidget;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;

import java.util.List;

public class ResetGuiElementWidget extends TexturedGuiElementWidget<ResetGuiElement> {

    private static final Component TITLE = Component.translatable("custommachinery.gui.element.reset.name");
    private static final List<Component> TOOLTIPS = Lists.newArrayList(
            TITLE,
            Component.translatable("custommachinery.gui.element.reset.tooltip").withStyle(ChatFormatting.DARK_RED)
    );

    public ResetGuiElementWidget(ResetGuiElement element, IMachineScreen screen) {
        super(element, screen, TITLE);
    }

    @Override
    public List<Component> getTooltips() {
        return TOOLTIPS;
    }
}
