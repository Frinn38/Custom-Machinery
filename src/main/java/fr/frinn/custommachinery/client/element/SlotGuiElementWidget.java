package fr.frinn.custommachinery.client.element;

import fr.frinn.custommachinery.api.client.IMachineScreen;
import fr.frinn.custommachinery.common.config.CMConfig;
import fr.frinn.custommachinery.common.guielement.SlotGuiElement;
import fr.frinn.custommachinery.common.init.CMRegistration;
import fr.frinn.custommachinery.common.util.CycleTimer;
import fr.frinn.custommachinery.common.util.GhostItem;
import fr.frinn.custommachinery.impl.client.TexturedGuiElementRenderable;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.world.item.ItemStack;

import java.util.List;

public class SlotGuiElementWidget extends TexturedGuiElementRenderable<SlotGuiElement> {

    private static final CycleTimer timer = new CycleTimer(CMConfig.CONFIG.itemSlotCycleTime);

    public SlotGuiElementWidget(SlotGuiElement element, IMachineScreen screen) {
        super(element, screen);
    }

    @Override
    public void extractRenderableRenderState(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float partialTicks) {
        super.extractRenderableRenderState(graphics, mouseX, mouseY, partialTicks);

        GhostItem ghost = this.getElement().getGhost();
        if(ghost != GhostItem.EMPTY && !ghost.items().isEmpty() && (ghost.alwaysRender() || this.isSlotEmpty())) {
            timer.onDraw();
            List<ItemStack> items = ghost.items();
            //TODO FakeItemRenderer.render(graphics, timer.getOrDefault(items, Items.AIR).getDefaultInstance(), this.getX() + 1, this.getY() + 1, ghost.color().getARGB());
        }
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
