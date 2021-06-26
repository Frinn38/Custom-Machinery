package fr.frinn.custommachinery.common.data.gui;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import fr.frinn.custommachinery.CustomMachinery;
import fr.frinn.custommachinery.common.init.Registration;
import net.minecraft.util.ResourceLocation;

public class PlayerInventoryGuiElement extends TexturedGuiElement {

    private static final ResourceLocation BASE_PLAYER_INVENTORY_TEXTURE = new ResourceLocation(CustomMachinery.MODID, "textures/gui/base_inventory.png");

    public static final Codec<PlayerInventoryGuiElement> CODEC = RecordCodecBuilder.create(playerInventoryGuiElementInstance ->
            playerInventoryGuiElementInstance.group(
                    Codec.intRange(0, Integer.MAX_VALUE).fieldOf("x").forGetter(AbstractGuiElement::getX),
                    Codec.intRange(0, Integer.MAX_VALUE).fieldOf("y").forGetter(AbstractGuiElement::getY),
                    Codec.intRange(-1, Integer.MAX_VALUE).optionalFieldOf("width", -1).forGetter(AbstractGuiElement::getWidth),
                    Codec.intRange(-1, Integer.MAX_VALUE).optionalFieldOf("height", -1).forGetter(AbstractGuiElement::getHeight),
                    Codec.INT.optionalFieldOf("priority", 0).forGetter(AbstractGuiElement::getPriority),
                    ResourceLocation.CODEC.optionalFieldOf("texture", BASE_PLAYER_INVENTORY_TEXTURE).forGetter(PlayerInventoryGuiElement::getTexture)
            ).apply(playerInventoryGuiElementInstance, PlayerInventoryGuiElement::new)
    );

    public PlayerInventoryGuiElement(int x, int y, int width, int height, int priority, ResourceLocation texture) {
        super(x, y, width, height, priority, texture);
    }

    @Override
    public GuiElementType<PlayerInventoryGuiElement> getType() {
        return Registration.PLAYER_INVENTORY_GUI_ELEMENT.get();
    }
}
