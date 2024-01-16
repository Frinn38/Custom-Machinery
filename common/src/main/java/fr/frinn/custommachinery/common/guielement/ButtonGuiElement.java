package fr.frinn.custommachinery.common.guielement;

import fr.frinn.custommachinery.CustomMachinery;
import fr.frinn.custommachinery.api.codec.NamedCodec;
import fr.frinn.custommachinery.api.guielement.GuiElementType;
import fr.frinn.custommachinery.common.init.Registration;
import fr.frinn.custommachinery.impl.codec.DefaultCodecs;
import fr.frinn.custommachinery.impl.guielement.AbstractTexturedGuiElement;
import fr.frinn.custommachinery.impl.util.TextComponentUtils;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;

public class ButtonGuiElement extends AbstractTexturedGuiElement {

    public static final ResourceLocation BASE_TEXTURE = new ResourceLocation(CustomMachinery.MODID, "textures/gui/base_button.png");
    public static final ResourceLocation BASE_TEXTURE_HOVERED = new ResourceLocation(CustomMachinery.MODID, "textures/gui/base_button_hovered.png");
    public static final ResourceLocation BASE_TEXTURE_TOOGLE = new ResourceLocation(CustomMachinery.MODID, "textures/gui/base_button_toogle.png");
    public static final ResourceLocation BASE_TEXTURE_TOOGLE_HOVERED = new ResourceLocation(CustomMachinery.MODID, "textures/gui/base_button_toogle_hovered.png");

    public static final NamedCodec<ButtonGuiElement> CODEC = NamedCodec.record(buttonGuiElementInstance ->
            buttonGuiElementInstance.group(
                    AbstractTexturedGuiElement.makePropertiesCodec(BASE_TEXTURE, BASE_TEXTURE_HOVERED).forGetter(ButtonGuiElement::getProperties),
                    NamedCodec.STRING.fieldOf("id").forGetter(element -> element.id),
                    DefaultCodecs.RESOURCE_LOCATION.optionalFieldOf("texture_toogle", BASE_TEXTURE_TOOGLE).forGetter(element -> element.textureToogle),
                    DefaultCodecs.RESOURCE_LOCATION.optionalFieldOf("texture_toogle_hovered", BASE_TEXTURE_TOOGLE_HOVERED).forGetter(element -> element.textureToogleHovered),
                    NamedCodec.BOOL.optionalFieldOf("toogle", false).forGetter(element -> element.toogle),
                    TextComponentUtils.CODEC.optionalFieldOf("text", Component.literal("")).forGetter(element -> element.text),
                    DefaultCodecs.ITEM_OR_STACK.optionalFieldOf("item", ItemStack.EMPTY).forGetter(element -> element.item),
                    NamedCodec.intRange(1, Integer.MAX_VALUE).optionalFieldOf("hold_time", 1).forGetter(element -> element.holdTime)
            ).apply(buttonGuiElementInstance, ButtonGuiElement::new), "Button gui element"
    );

    private final String id;
    private final ResourceLocation textureToogle;
    private final ResourceLocation textureToogleHovered;
    private final boolean toogle;
    private final Component text;
    private final ItemStack item;
    private final int holdTime;

    public ButtonGuiElement(Properties properties, String id, ResourceLocation textureToogle, ResourceLocation textureToogleHovered, boolean toogle, Component text, ItemStack item, int holdTime) {
        super(properties);
        this.id = id;
        this.textureToogle = textureToogle;
        this.textureToogleHovered = textureToogleHovered;
        this.toogle = toogle;
        this.text = text;
        this.item = item;
        this.holdTime = holdTime;
    }

    @Override
    public GuiElementType<ButtonGuiElement> getType() {
        return Registration.BUTTON_GUI_ELEMENT.get();
    }

    public String getId() {
        return this.id;
    }

    public ResourceLocation getTextureToogle() {
        return this.textureToogle;
    }

    public ResourceLocation getBaseTextureToogleHovered() {
        return this.textureToogleHovered;
    }

    public boolean isToogle() {
        return this.toogle;
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
}
