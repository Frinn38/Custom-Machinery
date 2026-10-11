package fr.frinn.custommachinery.client.element;

import fr.frinn.custommachinery.api.client.IMachineScreen;
import fr.frinn.custommachinery.common.guielement.PlayerInventoryGuiElement;
import fr.frinn.custommachinery.impl.client.TexturedGuiElementRenderable;

public class PlayerInventoryGuiElementWidget extends TexturedGuiElementRenderable<PlayerInventoryGuiElement> {

    public PlayerInventoryGuiElementWidget(PlayerInventoryGuiElement element, IMachineScreen screen) {
        super(element, screen);
    }
}
