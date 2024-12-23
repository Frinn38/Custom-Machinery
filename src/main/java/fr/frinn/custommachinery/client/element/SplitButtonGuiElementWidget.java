package fr.frinn.custommachinery.client.element;

import fr.frinn.custommachinery.api.guielement.IMachineScreen;
import fr.frinn.custommachinery.common.guielement.SplitButtonGuiElement;
import fr.frinn.custommachinery.common.init.Registration;
import fr.frinn.custommachinery.impl.guielement.AbstractGuiElementWidget;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;

public class SplitButtonGuiElementWidget extends AbstractGuiElementWidget<SplitButtonGuiElement> {

    private static final Component TITLE = Component.literal("Split button");

    public SplitButtonGuiElementWidget(SplitButtonGuiElement element, IMachineScreen screen) {
        super(element, screen, TITLE);
    }

    @Override
    protected void renderWidget(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        ResourceLocation texture;
        if(getScreen().getTile().getComponentManager().getComponent(Registration.DATA_MACHINE_COMPONENT.get()).map(component -> component.getData().getBoolean(getElement().getId())).orElse(false)) {
            if(this.isHovered())
                texture = this.getElement().getTextureToggleHovered();
            else
                texture = this.getElement().getTextureToggle();
        } else {
            if(this.isHovered())
                texture = this.getElement().getTextureHovered();
            else
                texture = this.getElement().getTexture();
        }

        graphics.blit(texture, this.getX(), this.getY(), 0, 0, this.width, this.height, this.width, this.height);
    }
}
