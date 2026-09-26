package fr.frinn.custommachinery.impl.guielement;

import fr.frinn.custommachinery.api.guielement.IGuiElement;
import fr.frinn.custommachinery.api.guielement.IMachineScreen;
import fr.frinn.custommachinery.common.network.CGuiElementClickPacket;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.narration.NarratedElementType;
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
        output.add(NarratedElementType.HINT, getTooltips().toArray(new Component[0]));
    }

    public void setHeight(int height) {
        this.height = height;
    }

    @Override
    public boolean mouseClicked(MouseButtonEvent event, boolean doubleClick) {
        boolean clicked =  super.mouseClicked(event, doubleClick);
        if(clicked)
            ClientPacketDistributor.sendToServer(new CGuiElementClickPacket(this.screen.getMachine().getGuiElements().indexOf(this.element), (byte)event.button()));
        return clicked;
    }

    /**
     * Needed because Forgified Fabric API mixin makes {@link net.minecraft.client.gui.screens.inventory.AbstractContainerScreen#mouseReleased(MouseButtonEvent)}
     * return early in case any of the widgets return true (which they do by default).
     * This cause the slots of the gui (player inventory gui element) to behave weirdly, items can't be placed.
     * Issue open : <a href="https://github.com/Sinytra/ForgifiedFabricAPI/issues/198">here</a>
     */
    @Override
    public boolean mouseReleased(MouseButtonEvent event) {
        super.mouseReleased(event);
        return false;
    }
}
