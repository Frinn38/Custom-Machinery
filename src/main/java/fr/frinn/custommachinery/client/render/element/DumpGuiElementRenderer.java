package fr.frinn.custommachinery.client.render.element;

import com.google.common.collect.Lists;
import com.mojang.blaze3d.vertex.PoseStack;
import fr.frinn.custommachinery.api.guielement.IMachineScreen;
import fr.frinn.custommachinery.common.guielement.DumpGuiElement;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TranslatableComponent;

import java.util.List;

public class DumpGuiElementRenderer extends TexturedGuiElementRenderer<DumpGuiElement> {

    private static final List<Component> TOOLTIPS = Lists.newArrayList(
            new TranslatableComponent("custommachinery.gui.element.dump.name"),
            new TranslatableComponent("custommachinery.gui.element.dump.tooltip").withStyle(ChatFormatting.DARK_RED)
    );

    @Override
    public void renderTooltip(PoseStack matrix, DumpGuiElement element, IMachineScreen screen, int mouseX, int mouseY) {
        screen.getScreen().renderComponentTooltip(matrix, TOOLTIPS, mouseX, mouseY);
    }
}
