package fr.frinn.custommachinery.client.element;

import fr.frinn.custommachinery.api.guielement.IMachineScreen;
import fr.frinn.custommachinery.common.guielement.SlotGuiElement;
import fr.frinn.custommachinery.common.init.Registration;
import fr.frinn.custommachinery.common.integration.config.CMConfig;
import fr.frinn.custommachinery.common.util.CycleTimer;
import fr.frinn.custommachinery.common.util.GhostItem;
import fr.frinn.custommachinery.impl.guielement.TexturedGuiElementWidget;
import fr.frinn.custommachinery.impl.util.FakeItemRenderer;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;

import java.util.Arrays;
import java.util.List;

public class SlotGuiElementWidget extends TexturedGuiElementWidget<SlotGuiElement> {

    private static final CycleTimer timer = new CycleTimer(() -> CMConfig.get().itemSlotCycleTime);

    public SlotGuiElementWidget(SlotGuiElement element, IMachineScreen screen) {
        super(element, screen, Component.literal("Slot"));
    }

    @Override
    public void renderWidget(GuiGraphics graphics, int mouseX, int mouseY, float partialTicks) {
        super.renderWidget(graphics, mouseX, mouseY, partialTicks);

        GhostItem ghost = this.getElement().getGhost();
        if(ghost != GhostItem.EMPTY && ghost.ingredient().getItems().length != 0 && (ghost.alwaysRender() || this.isSlotEmpty())) {
            timer.onDraw();
            List<Item> items = Arrays.stream(ghost.ingredient().getItems()).map(ItemStack::getItem).toList();
            //graphics.setColor(ghost.color().getRed() / 255f, ghost.color().getGreen() / 255f, ghost.color().getBlue() / 255f, ghost.color().getAlpha() / 255f);
            //graphics.renderFakeItem(timer.getOrDefault(items, Items.AIR).getDefaultInstance(), this.getX() + 1, this.getY() + 1);
            //graphics.setColor(1.0f, 1.0f, 1.0f, 1.0f);
            FakeItemRenderer.render(graphics, timer.getOrDefault(items, Items.AIR).getDefaultInstance(), this.getX() + 1, this.getY() + 1, ghost.color().getARGB());
        }
    }

    @Override
    protected boolean clicked(double mouseX, double mouseY) {
        return false;
    }

    private boolean isSlotEmpty() {
        return this.getScreen().getTile()
                .getComponentManager()
                .getComponentHandler(Registration.ITEM_MACHINE_COMPONENT.get())
                .flatMap(handler -> handler.getComponentForID(this.getElement().getComponentId()))
                .map(component -> component.getItemStack().isEmpty())
                .orElse(true);
    }
}
