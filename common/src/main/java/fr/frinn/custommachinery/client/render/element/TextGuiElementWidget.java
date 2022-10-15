package fr.frinn.custommachinery.client.render.element;

import com.mojang.blaze3d.vertex.PoseStack;
import fr.frinn.custommachinery.api.guielement.IMachineScreen;
import fr.frinn.custommachinery.common.guielement.TextGuiElement;
import fr.frinn.custommachinery.impl.guielement.AbstractGuiElementWidget;
import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.Component;

import java.util.Collections;
import java.util.List;

public class TextGuiElementWidget extends AbstractGuiElementWidget<TextGuiElement> {

    public TextGuiElementWidget(TextGuiElement element, IMachineScreen screen) {
        super(element, screen, element.getText());
    }

    @Override
    public void renderButton(PoseStack poseStack, int mouseX, int mouseY, float partialTicks) {
        int posX = switch (this.getElement().getAlignment()) {
            case CENTER -> this.x - Minecraft.getInstance().font.width(this.getElement().getText().getString()) / 2;
            case RIGHT -> this.x - Minecraft.getInstance().font.width(this.getElement().getText().getString());
            default -> this.x;
        };
        int posY = this.y;
        Minecraft.getInstance().font.draw(poseStack, this.getElement().getText(), posX, posY, this.getElement().getColor());
    }

    @Override
    public List<Component> getTooltips() {
        return Collections.singletonList(this.getElement().getText());
    }

    @Override
    public boolean clicked(double d, double e) {
        return false;
    }
}
