package fr.frinn.custommachinery.client.screen;

import fr.frinn.custommachinery.api.component.IMachineComponent;
import fr.frinn.custommachinery.api.component.ISideConfigComponent;
import fr.frinn.custommachinery.api.guielement.IComponentGuiElement;
import fr.frinn.custommachinery.api.guielement.IGuiElement;
import fr.frinn.custommachinery.client.screen.popup.ComponentConfigPopup;
import fr.frinn.custommachinery.client.screen.widget.TexturedButton;
import fr.frinn.custommachinery.client.screen.widget.config.ComponentConfigButton;
import fr.frinn.custommachinery.common.guielement.ConfigGuiElement;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.Tooltip;
import net.minecraft.client.input.KeyEvent;
import net.minecraft.network.chat.Component;
import org.joml.Vector2i;

import java.util.List;
import java.util.Optional;

public class MachineConfigScreen extends BaseScreen {

    private final CustomMachineScreen parent;

    public MachineConfigScreen(CustomMachineScreen parent) {
        super(Component.translatable("custommachinery.gui.config.title", parent.getMachine().getName()), parent.getWidth(), parent.getHeight());
        this.parent = parent;
    }

    private List<IGuiElement> getConfigurableElements() {
        return this.parent.getTile().getGuiElements().stream()
                .filter(element -> element instanceof IComponentGuiElement<?> componentElement
                        && componentElement.getComponent(this.parent.getTile().getComponentManager()).isPresent()
                        && componentElement.getComponent(this.parent.getTile().getComponentManager()).get() instanceof ISideConfigComponent configComponent
                        && configComponent.getConfig().isEnabled()
                ).toList();
    }

    private ISideConfigComponent getComponentFromElement(IGuiElement element) {
        if(element instanceof IComponentGuiElement<?> componentGuiElement) {
            Optional<? extends IMachineComponent> optionalComponent = componentGuiElement.getComponent(this.parent.getTile().getComponentManager());
            if(optionalComponent.isPresent()) {
                IMachineComponent component = optionalComponent.get();
                if(component instanceof ISideConfigComponent sideConfigComponent)
                    return sideConfigComponent;
                throw new IllegalArgumentException("Component of type: " + component.getType().getId() + " is not side configurable.");
            }
            throw new IllegalArgumentException("Element of type: " + element.getType().getId() + " don't have a component in the machine: " + this.parent.getMachine().getId());
        }
        throw new IllegalArgumentException("Element of type: " + element.getType().getId() + " is not a component element.");
    }

    @Override
    protected void init() {
        super.init();
        this.parent.init(this.width, this.height);
        //Highlight elements in specified color
        this.getConfigurableElements().forEach(element -> {
            ISideConfigComponent component = this.getComponentFromElement(element);
            ComponentConfigPopup popup = new ComponentConfigPopup(this, component);
            this.addRenderableWidget(new ComponentConfigButton(
                    this.x + element.getX(),
                    this.y + element.getY(),
                    element.getWidth(),
                    element.getHeight(),
                    Component.translatable("custommachinery.gui.config.tooltip"),
                    button -> {
                        this.closePopup(popup);
                        this.openPopup(popup);
                        Vector2i pos = getStartingPos(this.getConfigurableElements().indexOf(element));
                        popup.move(pos.x * 20, pos.y * 20);
                    },
                    component.getConfig().getColor(),
                    popup
            ));
        });

        //Exit button
        this.parent.getTile().getGuiElements().stream()
                .filter(element -> element instanceof ConfigGuiElement)
                .findFirst()
                .map(element -> (ConfigGuiElement)element)
                .ifPresent(element -> this.addRenderableWidget(
                        TexturedButton.builder(Component.translatable("custommachinery.gui.config.exit"), element.getTexture().texture(), button -> Minecraft.getInstance().setScreen(this.parent))
                                .bounds(this.x + element.getX(), this.y + element.getY(), element.getWidth(), element.getHeight())
                                .hovered(element.getTextureHovered().texture())
                                .tooltip(Tooltip.create(Component.translatable("custommachinery.gui.config.exit")))
                                .build()
                ));
    }

    @Override
    public void extractRenderState(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float partialTicks) {
        graphics.pose().pushMatrix();
        this.parent.extractRenderState(graphics, Integer.MAX_VALUE, Integer.MAX_VALUE, partialTicks);
        graphics.nextStratum();
        super.extractRenderState(graphics, mouseX, mouseY, partialTicks);
        graphics.pose().popMatrix();
    }

    @Override
    public void extractBackground(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float partialTicks) {

    }

    @Override
    public boolean keyPressed(KeyEvent event) {
        if (Minecraft.getInstance().options.keyInventory.matches(event) || event.isEscape()) {
            Minecraft.getInstance().setScreen(this.parent);
            return true;
        }
        else return super.keyPressed(event);
    }

    private static Vector2i getStartingPos(int np) {
        // (dx, dy) is a vector - direction in which we move right now
        int dx = 0;
        int dy = 1;
        // length of current segment
        int segment_length = 1;

        // current position (x, y) and how much of current segment we passed
        int x = 0;
        int y = 0;
        int segment_passed = 0;
        if (np == 0){
            return new Vector2i();
        }
        for (int n = 0; n < np; ++n) {
            // make a step, add 'direction' vector (dx, dy) to current position (x, y)
            x += dx;
            y += dy;
            ++segment_passed;

            if (segment_passed == segment_length) {
                // done with current segment
                segment_passed = 0;

                // 'rotate' directions
                int buffer = dy;
                dy = -dx;
                dx = buffer;

                // increase segment length if necessary
                if (dx == 0) {
                    ++segment_length;
                }
            }
        }
        return new Vector2i(x, y);
    }
}
