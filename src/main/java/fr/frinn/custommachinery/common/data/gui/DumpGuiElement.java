package fr.frinn.custommachinery.common.data.gui;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import fr.frinn.custommachinery.CustomMachinery;
import fr.frinn.custommachinery.api.codec.CodecLogger;
import fr.frinn.custommachinery.api.guielement.GuiElementType;
import fr.frinn.custommachinery.api.machine.MachineTile;
import fr.frinn.custommachinery.apiimpl.guielement.AbstractGuiElement;
import fr.frinn.custommachinery.apiimpl.guielement.AbstractTexturedGuiElement;
import fr.frinn.custommachinery.common.init.Registration;
import fr.frinn.custommachinery.common.util.Codecs;
import net.minecraft.util.ResourceLocation;

import java.util.List;

public class DumpGuiElement extends AbstractTexturedGuiElement {

    private static final ResourceLocation BASE_TEXTURE = new ResourceLocation(CustomMachinery.MODID, "textures/gui/base_dump.png");

    public static final Codec<DumpGuiElement> CODEC = RecordCodecBuilder.create(dumpGuiElement ->
            makeBaseTexturedCodec(dumpGuiElement, BASE_TEXTURE).and(
                    Codecs.list(Codec.STRING).fieldOf("id").forGetter(element -> element.id)
            ).apply(dumpGuiElement, DumpGuiElement::new)
    );

    private final List<String> id;

    public DumpGuiElement(int x, int y, int width, int height, int priority, ResourceLocation texture, List<String> id) {
        super(x, y, width, height, priority, texture);
        this.id = id;
    }

    @Override
    public GuiElementType<DumpGuiElement> getType() {
        return Registration.DUMP_GUI_ELEMENT.get();
    }

    @Override
    public void handleClick(byte button, MachineTile tile) {
        tile.getComponentManager()
                .getComponentHandler(Registration.FLUID_MACHINE_COMPONENT.get())
                .ifPresent(handler -> {
                    handler.getComponents().stream().filter(component -> this.id.contains(component.getId())).forEach(component -> component.recipeExtract(Integer.MAX_VALUE));
                });
    }
}
