package fr.frinn.custommachinery.client.screen;

import fr.frinn.custommachinery.api.component.IMachineComponent;
import fr.frinn.custommachinery.api.component.ISideConfigComponent;
import fr.frinn.custommachinery.api.guielement.IComponentGuiElement;
import fr.frinn.custommachinery.api.guielement.IGuiElement;
import fr.frinn.custommachinery.client.screen.popup.ComponentConfigPopup;
import fr.frinn.custommachinery.client.screen.widget.TexturedButton;
import fr.frinn.custommachinery.client.screen.widget.config.ComponentConfigButtonWidget;
import fr.frinn.custommachinery.common.guielement.ConfigGuiElement;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Tooltip;
import net.minecraft.network.chat.Component;
import org.lwjgl.glfw.GLFW;

import java.util.List;
import java.util.Optional;

public class MachineConfigScreen extends BaseScreen {

    private final CustomMachineScreen parent;

    public MachineConfigScreen(CustomMachineScreen parent) {
        super(Component.translatable("custommachinery.gui.config.title", parent.getMachine().getName()), parent.getWidth(), parent.getHeight());
        this.parent = parent;
    }

    private List<IGuiElement> getConfigurableElements() {
        return this.parent.getMachine().getGuiElements().stream()
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
        this.parent.init(Minecraft.getInstance(), this.width, this.height);
        //Highlight elements in blue
        this.getConfigurableElements().forEach(element -> this.addRenderableWidget(new ComponentConfigButtonWidget(
                this.getX() + element.getX(),
                this.getY() + element.getY(),
                element.getWidth(),
                element.getHeight(),
                Component.translatable("custommachinery.gui.config.tooltip"),
                button -> this.openPopup(new ComponentConfigPopup(this.getComponentFromElement(element).getConfig()))
        )));
        //Exit button
        this.parent.getMachine().getGuiElements().stream()
                .filter(element -> element instanceof ConfigGuiElement)
                .findFirst()
                .map(element -> (ConfigGuiElement)element)
                .ifPresent(element -> this.addRenderableWidget(
                        TexturedButton.builder(Component.translatable("custommachinery.gui.config.exit"), element.getTexture(), button -> Minecraft.getInstance().setScreen(this.parent))
                                .bounds(this.getX() + element.getX(), this.getY() + element.getY(), element.getWidth(), element.getHeight())
                                .hovered(element.getTextureHovered())
                                .tooltip(Tooltip.create(Component.translatable("custommachinery.gui.config.exit")))
                                .build()
                ));
    }

    @Override
    public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTicks) {
        graphics.pose().pushPose();
        this.parent.render(graphics, Integer.MAX_VALUE, Integer.MAX_VALUE, partialTicks);
        graphics.pose().translate(0, 0, 50);
        super.render(graphics, mouseX, mouseY, partialTicks);
        graphics.pose().popPose();
    }

    @Override
    public void renderBackground(GuiGraphics graphics) {

    }

    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        if (Minecraft.getInstance().options.keyInventory.matches(keyCode, scanCode) || keyCode == GLFW.GLFW_KEY_ESCAPE) {
            Minecraft.getInstance().setScreen(this.parent);
            return true;
        }
        else return super.keyPressed(keyCode, scanCode, modifiers);
    }
}
