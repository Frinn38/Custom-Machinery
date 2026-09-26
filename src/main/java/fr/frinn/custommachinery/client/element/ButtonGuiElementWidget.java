package fr.frinn.custommachinery.client.element;

import fr.frinn.custommachinery.api.guielement.IMachineScreen;
import fr.frinn.custommachinery.common.guielement.ButtonGuiElement;
import fr.frinn.custommachinery.common.init.CMRegistration;
import fr.frinn.custommachinery.impl.guielement.AbstractGuiElementWidget;
import fr.frinn.custommachinery.impl.util.TextureInfo;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.network.chat.Component;

public class ButtonGuiElementWidget extends AbstractGuiElementWidget<ButtonGuiElement> {

    private static final Component TITLE = Component.literal("Button");

    public ButtonGuiElementWidget(ButtonGuiElement element, IMachineScreen screen) {
        super(element, screen, TITLE);
    }

    @Override
    public void extractWidgetRenderState(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float partialTick) {
        TextureInfo texture;
        if(getElement().isToggle() && getScreen().getTile().getComponentManager().getComponent(CMRegistration.DATA_MACHINE_COMPONENT.get()).map(component -> component.getData().getBooleanOr(getElement().getId(), false)).orElse(false)) {
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

        if(!getElement().getText().getString().isEmpty())
            graphics.text(Minecraft.getInstance().font, this.getElement().getText(), (int)(this.getX() + this.width / 2.0f - Minecraft.getInstance().font.width(getElement().getText()) / 2.0f), (int)(this.getY() + this.height / 2.0f - Minecraft.getInstance().font.lineHeight / 2.0f), 0);

        if(!getElement().getItem().isEmpty())
            graphics.item(getElement().getItem(), (int)(this.getX() + this.width / 2.0f - 8), (int)(this.getY() + this.height / 2.0f - 8));
    }
}
