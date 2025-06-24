package fr.frinn.custommachinery.common.guielement;

import fr.frinn.custommachinery.CustomMachinery;
import fr.frinn.custommachinery.api.codec.NamedCodec;
import fr.frinn.custommachinery.api.guielement.GuiElementType;
import fr.frinn.custommachinery.api.machine.MachineTile;
import fr.frinn.custommachinery.common.component.handler.ItemComponentHandler;
import fr.frinn.custommachinery.common.component.item.ItemMachineComponent;
import fr.frinn.custommachinery.common.init.Registration;
import fr.frinn.custommachinery.impl.guielement.AbstractTexturedGuiElement;
import fr.frinn.custommachinery.impl.util.TextureInfo;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.inventory.AbstractContainerMenu;

import java.util.Collections;
import java.util.List;

public class SplitButtonGuiElement extends AbstractTexturedGuiElement {

    public static final TextureInfo BASE_TEXTURE = CustomMachinery.texture("textures/gui/base_split.png");
    public static final TextureInfo BASE_TEXTURE_HOVERED = CustomMachinery.texture("textures/gui/base_split_hovered.png");
    public static final TextureInfo BASE_TEXTURE_TOGGLE = CustomMachinery.texture("textures/gui/base_split_toogle.png");
    public static final TextureInfo BASE_TEXTURE_TOGGLE_HOVERED = CustomMachinery.texture("textures/gui/base_split_toogle_hovered.png");

    public static final NamedCodec<SplitButtonGuiElement> CODEC = NamedCodec.record(buttonGuiElementInstance ->
            buttonGuiElementInstance.group(
                    AbstractTexturedGuiElement.makePropertiesCodec(BASE_TEXTURE, BASE_TEXTURE_HOVERED).forGetter(SplitButtonGuiElement::getProperties),
                    TextureInfo.CODEC.optionalFieldOf("texture_toggle", BASE_TEXTURE_TOGGLE).forGetter(element -> element.textureToggle),
                    TextureInfo.CODEC.optionalFieldOf("texture_toggle_hovered", BASE_TEXTURE_TOGGLE_HOVERED).forGetter(element -> element.textureToggleHovered),
                    NamedCodec.STRING.listOf().optionalFieldOf("slots", Collections.emptyList()).forGetter(SplitButtonGuiElement::getSlots)
            ).apply(buttonGuiElementInstance, SplitButtonGuiElement::new), "Button gui element"
    );

    private final TextureInfo textureToggle;
    private final TextureInfo textureToggleHovered;
    private final List<String> slots;

    public SplitButtonGuiElement(Properties properties, TextureInfo textureToggle, TextureInfo textureToggleHovered, List<String> slots) {
        super(properties);
        this.textureToggle = textureToggle;
        this.textureToggleHovered = textureToggleHovered;
        this.slots = slots;
    }

    @Override
    public GuiElementType<SplitButtonGuiElement> getType() {
        return Registration.SPLIT_GUI_ELEMENT.get();
    }

    public TextureInfo getTextureToggle() {
        return this.textureToggle;
    }

    public TextureInfo getTextureToggleHovered() {
        return this.textureToggleHovered;
    }

    public List<String> getSlots() {
        return this.slots;
    }

    @Override
    public void handleClick(byte button, MachineTile tile, AbstractContainerMenu container, ServerPlayer player) {
        tile.getComponentManager()
                .getComponentHandler(Registration.ITEM_MACHINE_COMPONENT.get())
                .filter(handler -> handler instanceof ItemComponentHandler)
                .map(handler -> (ItemComponentHandler)handler)
                .ifPresent(handler -> {
                    if(handler.getSplitters().contains(this.getId()))
                        handler.removeSplitter(this.getId());
                    else if(this.slots.isEmpty())
                        handler.addSplitter(this.getId(), handler.getComponents().stream().filter(component -> component.getMode().isInput() && component.getType() == Registration.ITEM_MACHINE_COMPONENT.get()).map(ItemMachineComponent::getId).toList());
                    else
                        handler.addSplitter(this.getId(), this.slots);
                });
        tile.getComponentManager()
                .getComponent(Registration.DATA_MACHINE_COMPONENT.get())
                .ifPresent(component -> component.getData().putBoolean(this.getId(), !component.getData().getBoolean(this.getId())));
    }
}
