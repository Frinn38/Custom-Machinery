package fr.frinn.custommachinery.api.integration.jei;

import fr.frinn.custommachinery.api.machine.ICustomMachine;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.ITextComponent;

import java.util.function.BiConsumer;

public interface IDisplayInfo {

    IDisplayInfo addTooltip(ITextComponent text);

    default IDisplayInfo setIcon(ResourceLocation texture) {
        return setIcon(texture, 10, 10, 0, 0);
    }

    default IDisplayInfo setIcon(ResourceLocation texture, int width, int height) {
        return setIcon(texture, width, height, 0, 0);
    }

    IDisplayInfo setIcon(ResourceLocation texture, int width, int height, int u, int v);

    void setClickAction(BiConsumer<ICustomMachine, Integer> clickAction);

    IDisplayInfo setVisible(boolean visible);
}
