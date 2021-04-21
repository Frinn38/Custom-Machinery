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
                    Codec.INT.optionalFieldOf("width", -1).forGetter(AbstractGuiElement::getWidth),
                    Codec.INT.optionalFieldOf("height", -1).forGetter(AbstractGuiElement::getHeight),
                    Codec.INT.optionalFieldOf("priority", 0).forGetter(AbstractGuiElement::getPriority),
                    ResourceLocation.CODEC.optionalFieldOf("texture", BASE_PLAYER_INVENTORY_TEXTURE).forGetter(PlayerInventoryGuiElement::getTexture)
            ).apply(playerInventoryGuiElementInstance, PlayerInventoryGuiElement::new)
    );

    private ResourceLocation texture;

    public PlayerInventoryGuiElement(int x, int y, int width, int height, int priority, ResourceLocation texture) {
        super(x, y, width, height, priority);
        this.texture = texture;
        setBaseTexture(texture);
    }

    @Override
    public GuiElementType getType() {
        return Registration.PLAYER_INVENTORY_GUI_ELEMENT.get();
    }

    public ResourceLocation getTexture() {
        return this.texture;
    }
}
