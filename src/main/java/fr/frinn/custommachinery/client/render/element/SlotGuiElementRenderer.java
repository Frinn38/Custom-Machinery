package fr.frinn.custommachinery.client.render.element;

import com.mojang.blaze3d.vertex.PoseStack;
import fr.frinn.custommachinery.api.guielement.IGuiElementRenderer;
import fr.frinn.custommachinery.api.guielement.IMachineScreen;
import fr.frinn.custommachinery.client.ClientHandler;
import fr.frinn.custommachinery.client.screen.CustomMachineScreen;
import fr.frinn.custommachinery.common.config.CMConfig;
import fr.frinn.custommachinery.common.data.gui.SlotGuiElement;
import fr.frinn.custommachinery.common.util.CycleTimer;
import net.minecraft.client.gui.GuiComponent;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;

import java.util.List;

public class SlotGuiElementRenderer implements IGuiElementRenderer<SlotGuiElement> {

    private static final CycleTimer timer = new CycleTimer(CMConfig.INSTANCE.itemSlotCycleTime.get());

    @Override
    public void renderElement(PoseStack matrix, SlotGuiElement element, IMachineScreen screen) {
        int posX = element.getX();
        int posY = element.getY();
        int width = element.getWidth();
        int height = element.getHeight();

        ClientHandler.bindTexture(element.getTexture());
        GuiComponent.blit(matrix, posX, posY, 0, 0, width, height, width, height);
        if(!element.getItems().isEmpty()) {
            timer.onDraw();
            List<Item> items = element.getItems().stream().flatMap(ingredient -> ingredient.getAll().stream()).toList();
            ((CustomMachineScreen)screen).renderTransparentItem(matrix, timer.getOrDefault(items, Items.AIR).getDefaultInstance(), posX + 1, posY + 1);
        }
    }

    @Override
    public void renderTooltip(PoseStack matrix, SlotGuiElement element, IMachineScreen screen, int mouseX, int mouseY) {

    }
}
