package fr.frinn.custommachinery.client.element;

import fr.frinn.custommachinery.api.client.IMachineScreen;
import fr.frinn.custommachinery.common.guielement.PlayerInventoryGuiElement;
import fr.frinn.custommachinery.impl.client.TexturedGuiElementRenderable;

public class PlayerInventoryGuiElementRenderable extends TexturedGuiElementRenderable<PlayerInventoryGuiElement> {

    public PlayerInventoryGuiElementRenderable(PlayerInventoryGuiElement element, IMachineScreen screen) {
        super(element, screen);
    }
}
