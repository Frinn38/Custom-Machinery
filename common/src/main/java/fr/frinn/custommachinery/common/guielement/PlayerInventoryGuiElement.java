package fr.frinn.custommachinery.common.guielement;

import fr.frinn.custommachinery.CustomMachinery;
import fr.frinn.custommachinery.api.codec.NamedCodec;
import fr.frinn.custommachinery.api.guielement.GuiElementType;
import fr.frinn.custommachinery.common.init.Registration;
import fr.frinn.custommachinery.impl.guielement.AbstractTexturedGuiElement;
import net.minecraft.resources.ResourceLocation;

public class PlayerInventoryGuiElement extends AbstractTexturedGuiElement {

    private static final ResourceLocation BASE_PLAYER_INVENTORY_TEXTURE = new ResourceLocation(CustomMachinery.MODID, "textures/gui/base_inventory.png");

    public static final NamedCodec<PlayerInventoryGuiElement> CODEC = NamedCodec.record(playerInventoryGuiElement ->
            makeBaseTexturedCodec(playerInventoryGuiElement, BASE_PLAYER_INVENTORY_TEXTURE)
                    .apply(playerInventoryGuiElement, PlayerInventoryGuiElement::new), "Player inventory gui element"
    );

    public PlayerInventoryGuiElement(int x, int y, int width, int height, int priority, ResourceLocation texture) {
        super(x, y, width, height, priority, texture);
    }

    @Override
    public GuiElementType<PlayerInventoryGuiElement> getType() {
        return Registration.PLAYER_INVENTORY_GUI_ELEMENT.get();
    }
}
