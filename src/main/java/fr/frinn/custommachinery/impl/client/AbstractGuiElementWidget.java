package fr.frinn.custommachinery.impl.client;

import fr.frinn.custommachinery.api.client.IMachineScreen;
import fr.frinn.custommachinery.api.guielement.IGuiElement;
import fr.frinn.custommachinery.client.ClientHandler;
import fr.frinn.custommachinery.common.network.CGuiElementClickPacket;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.components.Tooltip;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.network.chat.Component;
import net.neoforged.neoforge.client.network.ClientPacketDistributor;

import java.util.List;

public abstract class AbstractGuiElementWidget<T extends IGuiElement> extends AbstractWidget {

    private final T element;
    private final IMachineScreen screen;

    public AbstractGuiElementWidget(T element, IMachineScreen screen, Component title) {
        super(element.getX() + screen.getX(), element.getY() + screen.getY(), element.getWidth(), element.getHeight(), title);
        this.element = element;
        this.screen = screen;
        this.setTooltip(Tooltip.create(ClientHandler.merge(this.getTooltips())));
    }

    public List<Component> getTooltips() {
        return this.element.getTooltips();
    }

    public T getElement() {
        return this.element;
    }

    public IMachineScreen getScreen() {
        return this.screen;
    }

    @Override
    public void updateWidgetNarration(NarrationElementOutput output) {

    }

    @Override
    public boolean mouseClicked(MouseButtonEvent event, boolean doubleClick) {
        boolean clicked =  super.mouseClicked(event, doubleClick);
        if(clicked)
            ClientPacketDistributor.sendToServer(new CGuiElementClickPacket(this.screen.getMachine().getGuiElements().indexOf(this.element), (byte)event.button()));
        return clicked;
    }
}
