package fr.frinn.custommachinery.common.data.gui;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import fr.frinn.custommachinery.CustomMachinery;
import fr.frinn.custommachinery.common.init.Registration;
import net.minecraft.util.ResourceLocation;

import java.util.Optional;

public class PlayerInventoryGuiElement extends AbstractGuiElement {

    private static final ResourceLocation BASE_PLAYER_INVENTORY_TEXTURE = new ResourceLocation(CustomMachinery.MODID, "textures/gui/base_inventory.png");

    public static final Codec<PlayerInventoryGuiElement> CODEC = RecordCodecBuilder.create(playerInventoryGuiElementInstance ->
            playerInventoryGuiElementInstance.group(
                    Codec.INT.fieldOf("x").forGetter(PlayerInventoryGuiElement::getX),
                    Codec.INT.fieldOf("y").forGetter(PlayerInventoryGuiElement::getY),
                    Codec.INT.optionalFieldOf("width").forGetter(element -> Optional.of(element.getWidth())),
                    Codec.INT.optionalFieldOf("height").forGetter(element -> Optional.of(element.getHeight())),
                    Codec.INT.optionalFieldOf("priority").forGetter(element -> Optional.of(element.getPriority())),
                    ResourceLocation.CODEC.optionalFieldOf("texture").forGetter(element -> Optional.of(element.getTexture()))
            ).apply(playerInventoryGuiElementInstance, (x, y, width, height, priority, texture) ->
                    new PlayerInventoryGuiElement(x, y, width.orElse(-1), height.orElse(-1), priority.orElse(0), texture.orElse(BASE_PLAYER_INVENTORY_TEXTURE))
            )
    );

    private ResourceLocation texture;

    public PlayerInventoryGuiElement(int x, int y, int width, int height, int priority, ResourceLocation texture) {
        super(x, y, width, height, priority);
        this.texture = texture;
    }

    @Override
    public GuiElementType getType() {
        return Registration.PLAYER_INVENTORY_GUI_ELEMENT.get();
    }

    public ResourceLocation getTexture() {
        return this.texture;
    }
}
