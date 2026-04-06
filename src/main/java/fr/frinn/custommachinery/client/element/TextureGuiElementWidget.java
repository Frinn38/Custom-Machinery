package fr.frinn.custommachinery.client.element;

import fr.frinn.custommachinery.api.guielement.IMachineScreen;
import fr.frinn.custommachinery.common.guielement.TextureGuiElement;
import fr.frinn.custommachinery.impl.guielement.TexturedGuiElementWidget;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;

public class TextureGuiElementWidget extends TexturedGuiElementWidget<TextureGuiElement> {

    public TextureGuiElementWidget(TextureGuiElement element, IMachineScreen screen) {
        super(element, screen, Component.literal("Texture"));
    }

    @Override
    public void renderWidget(GuiGraphics graphics, int mouseX, int mouseY, float partialTicks) {
        graphics.pose().pushPose();
        if(this.getElement().getZLevel() != 0)
            graphics.pose().translate(0, 0, this.getElement().getZLevel());
        super.renderWidget(graphics, mouseX, mouseY, partialTicks);
        graphics.pose().popPose();
    }

    @Override
    protected boolean clicked(double mouseX, double mouseY) {
        return false;
    }
}
