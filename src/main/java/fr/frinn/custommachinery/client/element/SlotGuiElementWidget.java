package fr.frinn.custommachinery.client.element;

import fr.frinn.custommachinery.api.guielement.IMachineScreen;
import fr.frinn.custommachinery.common.config.CMConfig;
import fr.frinn.custommachinery.common.guielement.SlotGuiElement;
import fr.frinn.custommachinery.common.init.CMRegistration;
import fr.frinn.custommachinery.common.util.CycleTimer;
import fr.frinn.custommachinery.common.util.GhostItem;
import fr.frinn.custommachinery.impl.guielement.TexturedGuiElementWidget;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.core.Holder;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.Item;

import java.util.List;

public class SlotGuiElementWidget extends TexturedGuiElementWidget<SlotGuiElement> {

    private static final CycleTimer timer = new CycleTimer(CMConfig.CONFIG.itemSlotCycleTime);

    public SlotGuiElementWidget(SlotGuiElement element, IMachineScreen screen) {
        super(element, screen, Component.literal("Slot"));
    }

    @Override
    public void extractWidgetRenderState(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float partialTicks) {
        super.extractWidgetRenderState(graphics, mouseX, mouseY, partialTicks);

        GhostItem ghost = this.getElement().getGhost();
        if(ghost != GhostItem.EMPTY && ghost.ingredient().getValues().size() != 0 && (ghost.alwaysRender() || this.isSlotEmpty())) {
            timer.onDraw();
            List<Item> items = ghost.ingredient().getValues().stream().map(Holder::value).toList();
            //TODO FakeItemRenderer.render(graphics, timer.getOrDefault(items, Items.AIR).getDefaultInstance(), this.getX() + 1, this.getY() + 1, ghost.color().getARGB());
        }
    }

    @Override
    public boolean mouseClicked(MouseButtonEvent event, boolean doubleClick) {
        return false;
    }

    private boolean isSlotEmpty() {
        return this.getScreen().getTile()
                .getComponentManager()
                .getComponentHandler(CMRegistration.ITEM_MACHINE_COMPONENT.get())
                .flatMap(handler -> handler.getComponentForID(this.getElement().getComponentId()))
                .map(component -> component.getItemStack().isEmpty())
                .orElse(true);
    }
}
