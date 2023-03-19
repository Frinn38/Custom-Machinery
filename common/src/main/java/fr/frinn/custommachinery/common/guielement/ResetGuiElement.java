package fr.frinn.custommachinery.common.guielement;

import fr.frinn.custommachinery.CustomMachinery;
import fr.frinn.custommachinery.api.codec.NamedCodec;
import fr.frinn.custommachinery.api.guielement.GuiElementType;
import fr.frinn.custommachinery.api.machine.MachineTile;
import fr.frinn.custommachinery.common.init.Registration;
import fr.frinn.custommachinery.impl.guielement.AbstractTexturedGuiElement;
import net.minecraft.resources.ResourceLocation;

public class ResetGuiElement extends AbstractTexturedGuiElement {

    private static final ResourceLocation BASE_TEXTURE = new ResourceLocation(CustomMachinery.MODID, "textures/gui/base_reset.png");

    public static final NamedCodec<ResetGuiElement> CODEC = NamedCodec.record(resetGuiElement ->
            makeBaseTexturedCodec(resetGuiElement, BASE_TEXTURE)
                    .apply(resetGuiElement, ResetGuiElement::new), "Reset gui element"
    );

    public ResetGuiElement(int x, int y, int width, int height, int priority, ResourceLocation texture) {
        super(x, y, width, height, priority, texture);
    }

    @Override
    public GuiElementType<ResetGuiElement> getType() {
        return Registration.RESET_GUI_ELEMENT.get();
    }

    @Override
    public void handleClick(byte button, MachineTile tile) {
        tile.resetProcess();
    }
}
