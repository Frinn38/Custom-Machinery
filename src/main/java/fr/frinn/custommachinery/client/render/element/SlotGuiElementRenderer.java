package fr.frinn.custommachinery.client.render.element;

import com.mojang.blaze3d.matrix.MatrixStack;
import fr.frinn.custommachinery.api.guielement.IGuiElementRenderer;
import fr.frinn.custommachinery.api.guielement.IMachineScreen;
import fr.frinn.custommachinery.client.screen.CustomMachineScreen;
import fr.frinn.custommachinery.common.config.CMConfig;
import fr.frinn.custommachinery.common.data.gui.SlotGuiElement;
import fr.frinn.custommachinery.common.util.CycleTimer;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.AbstractGui;
import net.minecraft.item.Item;
import net.minecraft.item.Items;

import java.util.List;
import java.util.stream.Collectors;

public class SlotGuiElementRenderer implements IGuiElementRenderer<SlotGuiElement> {

    private static final CycleTimer timer = new CycleTimer(CMConfig.INSTANCE.itemSlotCycleTime.get());

    @Override
    public void renderElement(MatrixStack matrix, SlotGuiElement element, IMachineScreen screen) {
        int posX = element.getX();
        int posY = element.getY();
        int width = element.getWidth();
        int height = element.getHeight();

        Minecraft.getInstance().getTextureManager().bindTexture(element.getTexture());
        AbstractGui.blit(matrix, posX, posY, 0, 0, width, height, width, height);
        if(!element.getItems().isEmpty()) {
            timer.onDraw();
            List<Item> items = element.getItems().stream().flatMap(ingredient -> ingredient.getAll().stream()).collect(Collectors.toList());
            ((CustomMachineScreen)screen).renderTransparentItem(matrix, timer.getOrDefault(items, Items.AIR).getDefaultInstance(), posX + 1, posY + 1);
        }
    }

    @Override
    public void renderTooltip(MatrixStack matrix, SlotGuiElement element, IMachineScreen screen, int mouseX, int mouseY) {

    }
}
