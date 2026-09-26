package fr.frinn.custommachinery.client.element;

import fr.frinn.custommachinery.api.guielement.IMachineScreen;
import fr.frinn.custommachinery.common.guielement.SplitButtonGuiElement;
import fr.frinn.custommachinery.common.init.CMRegistration;
import fr.frinn.custommachinery.impl.guielement.AbstractGuiElementWidget;
import fr.frinn.custommachinery.impl.util.TextureInfo;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.network.chat.Component;

public class SplitButtonGuiElementWidget extends AbstractGuiElementWidget<SplitButtonGuiElement> {

    private static final Component TITLE = Component.literal("Split button");

    public SplitButtonGuiElementWidget(SplitButtonGuiElement element, IMachineScreen screen) {
        super(element, screen, TITLE);
    }

    @Override
    protected void extractWidgetRenderState(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float partialTick) {
        TextureInfo texture;
        if(getScreen().getTile().getComponentManager().getComponent(CMRegistration.DATA_MACHINE_COMPONENT.get()).map(component -> component.getData().getBooleanOr(getElement().getId(), false)).orElse(false)) {
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

        graphics.blit(texture.texture(), this.getX(), this.getY(), texture.u(), texture.v(), this.width, this.height, this.width, this.height);
    }
}
