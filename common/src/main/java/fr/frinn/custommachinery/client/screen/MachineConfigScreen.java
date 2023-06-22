package fr.frinn.custommachinery.client.screen;

import com.mojang.blaze3d.vertex.PoseStack;
import fr.frinn.custommachinery.api.component.IMachineComponent;
import fr.frinn.custommachinery.api.component.ISideConfigComponent;
import fr.frinn.custommachinery.api.guielement.IComponentGuiElement;
import fr.frinn.custommachinery.api.guielement.IGuiElement;
import fr.frinn.custommachinery.client.screen.popup.ComponentConfigPopup;
import fr.frinn.custommachinery.client.screen.widget.custom.ButtonWidget;
import fr.frinn.custommachinery.client.screen.widget.custom.config.ComponentConfigButtonWidget;
import fr.frinn.custommachinery.common.guielement.ConfigGuiElement;
import net.minecraft.client.Minecraft;
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
        this.getConfigurableElements().forEach(element -> this.addCustomWidget(new ComponentConfigButtonWidget(
                () -> this.getX() + element.getX(),
                () -> this.getY() + element.getY(),
                element.getWidth(),
                element.getHeight())
                .tooltip(Component.translatable("custommachinery.gui.config.tooltip"))
                .callback(button -> this.openPopup(new ComponentConfigPopup(this.getComponentFromElement(element).getConfig())))
        ));
        this.parent.getMachine().getGuiElements().stream()
                .filter(element -> element instanceof ConfigGuiElement)
                .findFirst()
                .map(element -> (ConfigGuiElement)element)
                .ifPresent(element -> this.addCustomWidget(new ButtonWidget(() -> this.getX() + element.getX(), () -> this.getY() + element.getY(), element.getWidth(), element.getHeight())
                        .texture(element.getTexture())
                        .hoverTexture(element.getHoveredTexture())
                        .noBackground()
                        .callback(button -> Minecraft.getInstance().setScreen(this.parent))
                        .tooltip(Component.translatable("custommachinery.gui.config.exit"))
                ));
    }

    @Override
    public void render(PoseStack pose, int mouseX, int mouseY, float partialTicks) {
        pose.pushPose();
        this.parent.render(pose, Integer.MAX_VALUE, Integer.MAX_VALUE, partialTicks);
        pose.translate(0, 0, 500);
        super.render(pose, mouseX, mouseY, partialTicks);
        pose.popPose();
    }

    @Override
    public void renderBackground(PoseStack pose) {

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
