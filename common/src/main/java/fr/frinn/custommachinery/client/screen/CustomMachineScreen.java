package fr.frinn.custommachinery.client.screen;

import fr.frinn.custommachinery.api.guielement.GuiElementType;
import fr.frinn.custommachinery.api.guielement.IMachineScreen;
import fr.frinn.custommachinery.common.guielement.BackgroundGuiElement;
import fr.frinn.custommachinery.common.init.CustomMachineContainer;
import fr.frinn.custommachinery.common.init.CustomMachineTile;
import fr.frinn.custommachinery.common.machine.CustomMachine;
import fr.frinn.custommachinery.common.util.Comparators;
import fr.frinn.custommachinery.impl.guielement.AbstractGuiElementWidget;
import fr.frinn.custommachinery.impl.guielement.GuiElementWidgetSupplierRegistry;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.events.GuiEventListener;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;
import org.jetbrains.annotations.Nullable;

import java.util.Optional;

public class CustomMachineScreen extends AbstractContainerScreen<CustomMachineContainer> implements IMachineScreen {

    private final CustomMachineTile tile;
    private final CustomMachine machine;
    @Nullable
    private final BackgroundGuiElement background;

    public CustomMachineScreen(CustomMachineContainer container, Inventory inv, Component name) {
        super(container, inv, name);
        this.tile = container.getTile();
        this.machine = container.getTile().getMachine();
        this.imageWidth = 256;
        this.imageHeight = 192;
        this.background = this.tile.getGuiElements().stream()
                .filter(element -> element instanceof BackgroundGuiElement)
                .map(element -> (BackgroundGuiElement)element)
                .findFirst()
                .orElse(null);
        if(this.background != null) {
            this.imageWidth = this.background.getWidth();
            this.imageHeight = this.background.getHeight();
        }
    }

    @SuppressWarnings({"unchecked", "rawtypes"})
    @Override
    protected void init() {
        this.leftPos = (this.width - this.imageWidth) / 2;
        this.topPos = (this.height - this.imageHeight) / 2;
        this.tile.getGuiElements().stream()
                .filter(element -> GuiElementWidgetSupplierRegistry.hasWidgetSupplier(element.getType()))
                .sorted(Comparators.GUI_ELEMENTS_COMPARATOR.reversed())
                .forEach(element -> addRenderableWidget(GuiElementWidgetSupplierRegistry.getWidgetSupplier((GuiElementType)element.getType()).get(element, this)));
    }

    @Override
    protected void renderBg(GuiGraphics graphics, float partialTick, int mouseX, int mouseY) {
        renderBackground(graphics);
        if(this.background != null && this.background.getTexture() != null)
            graphics.blit(this.background.getTexture(), this.leftPos, this.topPos, 0, 0, this.imageWidth, this.imageHeight, this.imageWidth, this.imageHeight);
    }

    @Override
    public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        super.render(graphics, mouseX, mouseY, partialTick);
        this.renderTooltip(graphics, mouseX, mouseY);
    }

    @Override
    protected void renderLabels(GuiGraphics graphics, int mouseX, int mouseY) {

    }

    @Override
    public int getX() {
        return this.leftPos;
    }

    @Override
    public int getY() {
        return this.topPos;
    }

    @Override
    public int getWidth() {
        return this.imageWidth;
    }

    @Override
    public int getHeight() {
        return this.imageHeight;
    }

    @Override
    public CustomMachine getMachine() {
        return this.machine;
    }

    @Override
    public CustomMachineTile getTile() {
        return this.tile;
    }

    public Optional<AbstractGuiElementWidget<?>> getElementUnderMouse(double mouseX, double mouseY) {
        for(GuiEventListener widget : this.children()) {
            if(widget instanceof AbstractGuiElementWidget<?> elementWidget && elementWidget.isMouseOver(mouseX, mouseY)) {
                return Optional.of(elementWidget);
            }
        }
        return Optional.empty();
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        return super.mouseClicked(mouseX, mouseY, button);
    }
}
