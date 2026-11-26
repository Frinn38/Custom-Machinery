package fr.frinn.custommachinery.client.element;

import fr.frinn.custommachinery.api.client.IMachineScreen;
import fr.frinn.custommachinery.client.screen.pip.GhostItemPIPRenderer.GhostItemRenderState;
import fr.frinn.custommachinery.common.config.CMConfig;
import fr.frinn.custommachinery.common.guielement.SlotGuiElement;
import fr.frinn.custommachinery.common.util.CycleTimer;
import fr.frinn.custommachinery.common.util.GhostItem;
import fr.frinn.custommachinery.impl.client.TexturedGuiElementRenderable;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.navigation.ScreenRectangle;
import net.minecraft.world.item.ItemStack;

import java.util.List;

public class SlotGuiElementRenderable extends TexturedGuiElementRenderable<SlotGuiElement> {

    private static final CycleTimer timer = new CycleTimer(CMConfig.CONFIG.itemSlotCycleTime);

    public SlotGuiElementRenderable(SlotGuiElement element, IMachineScreen screen) {
        super(element, screen);
    }

    @Override
    public void extractRenderableRenderState(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float partialTicks) {
        super.extractRenderableRenderState(graphics, mouseX, mouseY, partialTicks);

        GhostItem ghost = this.getElement().getGhost();
        if(ghost != GhostItem.EMPTY && !ghost.items().isEmpty() && (ghost.alwaysRender() || this.isSlotEmpty())) {
            timer.onDraw();
            List<ItemStack> items = ghost.items();
            ScreenRectangle itemSlot = new ScreenRectangle(this.getElement().getX() + 1 + this.getScreen().getX(), this.getElement().getY() + 1 + this.getScreen().getY(), this.getElement().getWidth() - 2, this.getElement().getHeight() - 2);
            graphics.submitPictureInPictureRenderState(new GhostItemRenderState(itemSlot, graphics.pose(), timer.getOrDefault(items, ItemStack.EMPTY), ghost.color()));
        }
    }

    private boolean isSlotEmpty() {
        return this.getScreen().getTile()
                .getComponentManager()
                .getComponent(this.getElement().getComponentId())
                .map(component -> component.getItemStack().isEmpty())
                .orElse(true);
    }
}
