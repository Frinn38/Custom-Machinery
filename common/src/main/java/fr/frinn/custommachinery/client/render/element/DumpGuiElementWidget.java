package fr.frinn.custommachinery.client.render.element;

import com.google.common.collect.Lists;
import fr.frinn.custommachinery.api.guielement.IMachineScreen;
import fr.frinn.custommachinery.common.guielement.DumpGuiElement;
import fr.frinn.custommachinery.impl.guielement.TexturedGuiElementWidget;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TranslatableComponent;

import java.util.List;

public class DumpGuiElementWidget extends TexturedGuiElementWidget<DumpGuiElement> {

    private static final Component TITLE = new TranslatableComponent("custommachinery.gui.element.dump.name");
    private static final List<Component> TOOLTIPS = Lists.newArrayList(
            TITLE,
            new TranslatableComponent("custommachinery.gui.element.dump.tooltip").withStyle(ChatFormatting.DARK_RED)
    );

    public DumpGuiElementWidget(DumpGuiElement element, IMachineScreen screen) {
        super(element, screen, TITLE);
    }

    @Override
    public List<Component> getTooltips() {
        return TOOLTIPS;
    }

    @Override
    public boolean isClickable() {
        return true;
    }
}
