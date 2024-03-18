package fr.frinn.custommachinery.client.screen;

import fr.frinn.custommachinery.api.guielement.GuiElementType;
import fr.frinn.custommachinery.api.guielement.IMachineScreen;
import fr.frinn.custommachinery.common.guielement.SizeGuiElement;
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

import java.util.Optional;

public class CustomMachineScreen extends AbstractContainerScreen<CustomMachineContainer> implements IMachineScreen {

    private final CustomMachineTile tile;
    private final CustomMachine machine;

    public CustomMachineScreen(CustomMachineContainer container, Inventory inv, Component name) {
        super(container, inv, name);
        this.tile = container.getTile();
        this.machine = container.getTile().getMachine();
        this.imageWidth = 256;
        this.imageHeight = 192;
        this.machine.getGuiElements().stream()
                .filter(element -> element instanceof SizeGuiElement)
                .map(element -> (SizeGuiElement)element)
                .findFirst()
                .ifPresent(size -> {
                    this.imageWidth = size.getWidth();
                    this.imageHeight = size.getHeight();
                });
    }

    @SuppressWarnings({"unchecked", "rawtypes"})
    @Override
    protected void init() {
        this.leftPos = (this.width - this.imageWidth) / 2;
        this.topPos = (this.height - this.imageHeight) / 2;
        this.machine.getGuiElements().stream()
                .filter(element -> GuiElementWidgetSupplierRegistry.hasWidgetSupplier(element.getType()))
                .sorted(Comparators.GUI_ELEMENTS_COMPARATOR.reversed())
                .forEach(element -> addRenderableWidget(GuiElementWidgetSupplierRegistry.getWidgetSupplier((GuiElementType)element.getType()).get(element, this)));
    }

    @Override
    protected void renderBg(GuiGraphics graphics, float partialTick, int mouseX, int mouseY) {
        renderBackground(graphics);
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

    @Override
    public CustomMachineScreen getScreen() {
        return this;
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
