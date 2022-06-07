package fr.frinn.custommachinery.common.guielement;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import fr.frinn.custommachinery.CustomMachinery;
import fr.frinn.custommachinery.api.guielement.GuiElementType;
import fr.frinn.custommachinery.apiimpl.guielement.AbstractTexturedGuiElement;
import fr.frinn.custommachinery.common.init.Registration;
import net.minecraft.resources.ResourceLocation;

public class PlayerInventoryGuiElement extends AbstractTexturedGuiElement {

    private static final ResourceLocation BASE_PLAYER_INVENTORY_TEXTURE = new ResourceLocation(CustomMachinery.MODID, "textures/gui/base_inventory.png");

    public static final Codec<PlayerInventoryGuiElement> CODEC = RecordCodecBuilder.create(playerInventoryGuiElement ->
            makeBaseTexturedCodec(playerInventoryGuiElement, BASE_PLAYER_INVENTORY_TEXTURE)
                .apply(playerInventoryGuiElement, PlayerInventoryGuiElement::new)
    );

    public PlayerInventoryGuiElement(int x, int y, int width, int height, int priority, ResourceLocation texture) {
        super(x, y, width, height, priority, texture);
    }

    @Override
    public GuiElementType<PlayerInventoryGuiElement> getType() {
        return Registration.PLAYER_INVENTORY_GUI_ELEMENT.get();
    }
}
