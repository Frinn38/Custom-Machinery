package fr.frinn.custommachinery.client.render.element;

import com.mojang.blaze3d.vertex.PoseStack;
import fr.frinn.custommachinery.api.guielement.IGuiElementRenderer;
import fr.frinn.custommachinery.api.guielement.IMachineScreen;
import fr.frinn.custommachinery.common.guielement.SizeGuiElement;
import net.minecraft.network.chat.Component;

import java.util.Collections;
import java.util.List;

public class SizeGuiElementRenderer implements IGuiElementRenderer<SizeGuiElement> {

    @Override
    public void renderElement(PoseStack matrix, SizeGuiElement element, IMachineScreen screen) {

    }

    @Override
    public List<Component> getTooltips(SizeGuiElement element, IMachineScreen screen) {
        return Collections.emptyList();
    }
}
