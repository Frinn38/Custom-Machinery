package fr.frinn.custommachinery.client.render.element;

import com.google.common.collect.Lists;
import com.mojang.blaze3d.vertex.PoseStack;
import fr.frinn.custommachinery.api.guielement.IGuiElementRenderer;
import fr.frinn.custommachinery.api.guielement.IMachineScreen;
import fr.frinn.custommachinery.client.ClientHandler;
import fr.frinn.custommachinery.common.data.gui.ResetGuiElement;
import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.GuiComponent;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TranslatableComponent;

import java.util.List;

public class ResetGuiElementRenderer implements IGuiElementRenderer<ResetGuiElement> {

    private static final List<Component> TOOLTIPS = Lists.newArrayList(
            new TranslatableComponent("custommachinery.gui.element.reset.name"),
            new TranslatableComponent("custommachinery.gui.element.reset.tooltip").withStyle(ChatFormatting.DARK_RED)
    );

    @Override
    public void renderElement(PoseStack matrix, ResetGuiElement element, IMachineScreen screen) {
        int posX = element.getX();
        int posY = element.getY();
        int width = element.getWidth();
        int height = element.getHeight();

        ClientHandler.bindTexture(element.getTexture());
        GuiComponent.blit(matrix, posX, posY, 0, 0, width, height, width, height);
    }

    @Override
    public void renderTooltip(PoseStack matrix, ResetGuiElement element, IMachineScreen screen, int mouseX, int mouseY) {
        screen.getScreen().renderComponentTooltip(matrix, TOOLTIPS, mouseX, mouseY);
    }
}
