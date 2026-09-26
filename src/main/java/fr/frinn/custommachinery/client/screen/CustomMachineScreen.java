package fr.frinn.custommachinery.client.screen;

import fr.frinn.custommachinery.api.guielement.IGuiElementWidgetSupplier;
import fr.frinn.custommachinery.api.guielement.IMachineScreen;
import fr.frinn.custommachinery.client.ClientHandler;
import fr.frinn.custommachinery.common.guielement.BackgroundGuiElement;
import fr.frinn.custommachinery.common.init.CustomMachineContainer;
import fr.frinn.custommachinery.common.init.CustomMachineTile;
import fr.frinn.custommachinery.common.machine.CustomMachine;
import fr.frinn.custommachinery.common.util.Comparators;
import fr.frinn.custommachinery.impl.guielement.AbstractGuiElementWidget;
import fr.frinn.custommachinery.impl.guielement.GuiElementWidgetSupplierRegistry;
import net.minecraft.client.gui.GuiGraphicsExtractor;
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
        this.tile = container.getTile();
        this.machine = container.getTile().getMachine();
        int imageWidth = 256;
        int imageHeight = 192;
        BackgroundGuiElement background = container.getTile().getGuiElements().stream()
                .filter(element -> element instanceof BackgroundGuiElement)
                .map(element -> (BackgroundGuiElement)element)
                .findFirst()
                .orElse(null);
        this.background = background;
        if(background != null) {
            imageWidth = background.getWidth();
            imageHeight = background.getHeight();
        }
        super(container, inv, name, imageWidth, imageHeight);
    }

    @SuppressWarnings({"unchecked", "rawtypes"})
    @Override
    protected void init() {
        this.leftPos = (this.width - this.imageWidth) / 2;
        this.topPos = (this.height - this.imageHeight) / 2;
        this.tile.getGuiElements().stream()
                .filter(element -> GuiElementWidgetSupplierRegistry.hasWidgetSupplier(element.getType()))
                .sorted(Comparators.GUI_ELEMENTS_COMPARATOR.reversed())
                .forEach(element -> {
                    IGuiElementWidgetSupplier widgetSupplier = GuiElementWidgetSupplierRegistry.getWidgetSupplier(element.getType());
                    if(widgetSupplier != null) {
                        this.addRenderableWidget(widgetSupplier.get(element, this));
                    }
                });
    }

    @Override
    public void extractBackground(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float partialTick) {
        if(this.background != null && this.background.getTexture() != null)
            ClientHandler.blit(graphics, this.background.getTexture(), this.leftPos, this.topPos, this.imageWidth, this.imageHeight);
    }

    @Override
    public void extractRenderState(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float partialTick) {
        super.extractRenderState(graphics, mouseX, mouseY, partialTick);
    }

    @Override
    protected void extractLabels(GuiGraphicsExtractor graphics, int mouseX, int mouseY) {
        this.children().stream()
                .filter(widget -> widget instanceof AbstractGuiElementWidget<?> element && element.isMouseOver(mouseX, mouseY) && !element.getTooltips().isEmpty())
                .map(widget -> (AbstractGuiElementWidget<?>) widget)
                .min((w1, w2) -> Comparators.GUI_ELEMENTS_COMPARATOR.compare(w1.getElement(), w2.getElement()))
                .ifPresent(element -> graphics.setTooltipForNextFrame(this.font, element.getTooltips().stream().flatMap(tooltip -> this.font.split(tooltip, 1000).stream()).toList(), mouseX - this.leftPos, mouseY - this.topPos));
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
}
