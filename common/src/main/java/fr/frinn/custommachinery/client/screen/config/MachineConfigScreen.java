package fr.frinn.custommachinery.client.screen.config;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import fr.frinn.custommachinery.api.component.IMachineComponent;
import fr.frinn.custommachinery.api.component.ISideConfigComponent;
import fr.frinn.custommachinery.api.guielement.IComponentGuiElement;
import fr.frinn.custommachinery.api.guielement.IGuiElement;
import fr.frinn.custommachinery.client.screen.BaseScreen;
import fr.frinn.custommachinery.client.screen.CustomMachineScreen;
import fr.frinn.custommachinery.client.screen.StackableScreen;
import fr.frinn.custommachinery.client.screen.widget.ComponentConfigButton;
import fr.frinn.custommachinery.common.guielement.ConfigGuiElement;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiComponent;
import net.minecraft.client.gui.components.ImageButton;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.network.chat.TranslatableComponent;
import org.lwjgl.glfw.GLFW;

import java.util.List;
import java.util.Optional;

public class MachineConfigScreen extends BaseScreen {

    private final CustomMachineScreen parent;
    private final StackableScreen stackableScreen;

    public MachineConfigScreen(CustomMachineScreen parent, StackableScreen stackableScreen) {
        super(new TranslatableComponent("custommachinery.gui.config.title", parent.getMachine().getName()));
        this.parent = parent;
        this.stackableScreen = stackableScreen;
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
        this.getConfigurableElements().forEach(element -> this.addWidget(new ComponentConfigButton(
                this.parent.getGuiLeft() + element.getX(),
                this.parent.getGuiTop() + element.getY(),
                element.getWidth(),
                element.getHeight(),
                new TranslatableComponent("custommachinery.gui.config.tooltip"),
                button -> this.stackableScreen.pushScreenLayer(new ComponentConfigScreen(this.getComponentFromElement(element).getConfig(), this.stackableScreen)),
                (button, pose, mouseX, mouseY) -> renderTooltip(pose, new TranslatableComponent("custommachinery.gui.config.tooltip"), mouseX, mouseY)
        )));
        this.parent.getMachine().getGuiElements().stream()
                .filter(element -> element instanceof ConfigGuiElement)
                .findFirst()
                .map(element -> (ConfigGuiElement)element)
                .ifPresent(element -> this.addWidget(new ImageButton(
                        this.parent.getGuiLeft() + element.getX(),
                        this.parent.getGuiTop() + element.getY(),
                        element.getWidth(),
                        element.getHeight(),
                        0,
                        0,
                        0,
                        element.getTexture(),
                        element.getWidth(),
                        element.getHeight(),
                        button -> {Minecraft.getInstance().setScreen(this.parent);},
                        new TranslatableComponent("custommachinery.gui.config.exit")
                ) {
                    @Override
                    public void renderButton(PoseStack pose, int mouseX, int mouseY, float partialTicks) {
                        RenderSystem.setShaderColor(0.6F, 0.6F, 0.6F, 1);
                        RenderSystem.setShader(GameRenderer::getPositionTexShader);
                        if(!isHoveredOrFocused())
                            RenderSystem.setShaderTexture(0, element.getTexture());
                        else
                            RenderSystem.setShaderTexture(0, element.getHoveredTexture());
                        GuiComponent.blit(pose, this.x, this.y, 0, 0, this.width, this.height, this.width, this.height);
                        RenderSystem.setShaderColor(1, 1, 1, 1);
                    }
                }));
    }

    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        if (super.keyPressed(keyCode, scanCode, modifiers))
            return true;
        else if (Minecraft.getInstance().options.keyInventory.matches(keyCode, scanCode) || keyCode == GLFW.GLFW_KEY_ESCAPE) {
            Minecraft.getInstance().setScreen(this.parent);
            return true;
        }
        else return false;
    }
}
