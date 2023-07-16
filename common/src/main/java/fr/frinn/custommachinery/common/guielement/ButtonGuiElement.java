package fr.frinn.custommachinery.common.guielement;

import com.mojang.datafixers.Products.P11;
import fr.frinn.custommachinery.CustomMachinery;
import fr.frinn.custommachinery.api.codec.NamedCodec;
import fr.frinn.custommachinery.api.guielement.GuiElementType;
import fr.frinn.custommachinery.common.init.Registration;
import fr.frinn.custommachinery.common.util.TextComponentUtils;
import fr.frinn.custommachinery.impl.codec.DefaultCodecs;
import fr.frinn.custommachinery.impl.guielement.AbstractTexturedGuiElement;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;

public class ButtonGuiElement extends AbstractTexturedGuiElement {

    public static final ResourceLocation BASE_TEXTURE = new ResourceLocation(CustomMachinery.MODID, "textures/gui/base_button.png");
    public static final ResourceLocation BASE_TEXTURE_TOOGLE = new ResourceLocation(CustomMachinery.MODID, "textures/gui/base_button_toogle.png");

    public static final NamedCodec<ButtonGuiElement> CODEC = NamedCodec.record(buttonGuiElementInstance -> {
                var p6 = makeBaseTexturedCodec(buttonGuiElementInstance, BASE_TEXTURE);
                return new P11<>(p6.t1(), p6.t2(), p6.t3(), p6.t4(), p6.t5(), p6.t6(),
                            NamedCodec.STRING.fieldOf("id").forGetter(element -> element.id),
                            DefaultCodecs.RESOURCE_LOCATION.optionalFieldOf("texture_toogle", BASE_TEXTURE_TOOGLE).forGetter(element -> element.textureToogle),
                            NamedCodec.BOOL.optionalFieldOf("toogle", false).forGetter(element -> element.toogle),
                            TextComponentUtils.CODEC.optionalFieldOf("text", Component.literal("")).forGetter(element -> element.text),
                            DefaultCodecs.ITEM_OR_STACK.optionalFieldOf("item", ItemStack.EMPTY).forGetter(element -> element.item)
                        ).apply(buttonGuiElementInstance, ButtonGuiElement::new);
            }, "Button gui element"
    );

    private final String id;
    private final ResourceLocation textureToogle;
    private final boolean toogle;
    private final Component text;
    private final ItemStack item;

    public ButtonGuiElement(int x, int y, int width, int height, int priority, ResourceLocation texture, String id, ResourceLocation textureToogle, boolean toogle, Component text, ItemStack item) {
        super(x, y, width, height, priority, texture);
        this.id = id;
        this.textureToogle = textureToogle;
        this.toogle = toogle;
        this.text = text;
        this.item = item;
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

    public boolean isToogle() {
        return this.toogle;
    }

    public Component getText() {
        return this.text;
    }

    public ItemStack getItem() {
        return this.item;
    }
}
