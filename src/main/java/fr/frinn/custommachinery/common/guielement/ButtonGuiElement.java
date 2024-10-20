package fr.frinn.custommachinery.common.guielement;

import fr.frinn.custommachinery.CustomMachinery;
import fr.frinn.custommachinery.api.codec.NamedCodec;
import fr.frinn.custommachinery.api.guielement.GuiElementType;
import fr.frinn.custommachinery.api.machine.MachineTile;
import fr.frinn.custommachinery.common.init.Registration;
import fr.frinn.custommachinery.common.util.TaskDelayer;
import fr.frinn.custommachinery.impl.codec.DefaultCodecs;
import fr.frinn.custommachinery.impl.guielement.AbstractTexturedGuiElement;
import fr.frinn.custommachinery.impl.util.TextComponentUtils;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.ItemStack;

public class ButtonGuiElement extends AbstractTexturedGuiElement {

    public static final ResourceLocation BASE_TEXTURE = CustomMachinery.rl("textures/gui/base_button.png");
    public static final ResourceLocation BASE_TEXTURE_HOVERED = CustomMachinery.rl("textures/gui/base_button_hovered.png");
    public static final ResourceLocation BASE_TEXTURE_TOGGLE = CustomMachinery.rl("textures/gui/base_button_toogle.png");
    public static final ResourceLocation BASE_TEXTURE_TOGGLE_HOVERED = CustomMachinery.rl("textures/gui/base_button_toogle_hovered.png");

    public static final NamedCodec<ButtonGuiElement> CODEC = NamedCodec.record(buttonGuiElementInstance ->
            buttonGuiElementInstance.group(
                    AbstractTexturedGuiElement.makePropertiesCodec(BASE_TEXTURE, BASE_TEXTURE_HOVERED).forGetter(ButtonGuiElement::getProperties),
                    DefaultCodecs.RESOURCE_LOCATION.optionalFieldOf("texture_toggle", BASE_TEXTURE_TOGGLE).forGetter(element -> element.textureToggle),
                    DefaultCodecs.RESOURCE_LOCATION.optionalFieldOf("texture_toggle_hovered", BASE_TEXTURE_TOGGLE_HOVERED).forGetter(element -> element.textureToggleHovered),
                    NamedCodec.BOOL.optionalFieldOf("toggle", false).forGetter(element -> element.toggle),
                    TextComponentUtils.CODEC.optionalFieldOf("text", Component.empty()).forGetter(element -> element.text.getString().isEmpty() ? Component.empty() : element.text),
                    DefaultCodecs.ITEM_OR_STACK.optionalFieldOf("item", ItemStack.EMPTY).forGetter(element -> element.item),
                    NamedCodec.intRange(1, Integer.MAX_VALUE).optionalFieldOf("hold_time", 1).forGetter(element -> element.holdTime)
            ).apply(buttonGuiElementInstance, ButtonGuiElement::new), "Button gui element"
    );

    private final ResourceLocation textureToggle;
    private final ResourceLocation textureToggleHovered;
    private final boolean toggle;
    private final Component text;
    private final ItemStack item;
    private final int holdTime;

    public ButtonGuiElement(Properties properties, ResourceLocation textureToggle, ResourceLocation textureToggleHovered, boolean toggle, Component text, ItemStack item, int holdTime) {
        super(properties);
        this.textureToggle = textureToggle;
        this.textureToggleHovered = textureToggleHovered;
        this.toggle = toggle;
        this.text = text;
        this.item = item;
        this.holdTime = holdTime;
    }

    @Override
    public GuiElementType<ButtonGuiElement> getType() {
        return Registration.BUTTON_GUI_ELEMENT.get();
    }

    public ResourceLocation getTextureToggle() {
        return this.textureToggle;
    }

    public ResourceLocation getTextureToggleHovered() {
        return this.textureToggleHovered;
    }

    public boolean isToggle() {
        return this.toggle;
    }

    public Component getText() {
        return this.text;
    }

    public ItemStack getItem() {
        return this.item;
    }

    public int getHoldTime() {
        return this.holdTime;
    }

    @Override
    public void handleClick(byte button, MachineTile tile, AbstractContainerMenu container, ServerPlayer player) {
        if(this.holdTime <= 0)
            return;
        tile.getComponentManager()
                .getComponent(Registration.DATA_MACHINE_COMPONENT.get())
                .ifPresent(component -> {
                    if(this.toggle)
                        component.getData().putBoolean(this.getId(), !component.getData().getBoolean(this.getId()));
                    else {
                        component.getData().putBoolean(this.getId(), true);
                        TaskDelayer.enqueue(this.holdTime, () -> {
                            component.getData().putBoolean(this.getId(), false);
                            component.getManager().markDirty();
                        });
                    }
                    component.getManager().markDirty();
                    tile.getProcessor().setSearchImmediately();
                    tile.getProcessor().setMachineInventoryChanged();
                });
    }
}
