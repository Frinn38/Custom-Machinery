package fr.frinn.custommachinery.client.screen;

import com.mojang.blaze3d.platform.InputConstants;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import fr.frinn.custommachinery.CustomMachinery;
import fr.frinn.custommachinery.api.component.IMachineComponent;
import fr.frinn.custommachinery.api.component.ISideConfigComponent;
import fr.frinn.custommachinery.api.guielement.IComponentGuiElement;
import fr.frinn.custommachinery.api.guielement.IGuiElement;
import fr.frinn.custommachinery.client.screen.widget.ComponentConfigButton;
import fr.frinn.custommachinery.common.guielement.ConfigGuiElement;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.ImageButton;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.resources.ResourceLocation;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class MachineConfigScreen extends Screen {

    private static final ResourceLocation EXIT_BUTTON_TEXTURE = new ResourceLocation(CustomMachinery.MODID, "textures/gui/config/exit_config.png");

    private final CustomMachineScreen parent;
    private final List<ComponentConfigButton> configButtons;
    private Button exitButton = null;

    public MachineConfigScreen(CustomMachineScreen parent) {
        super(new TranslatableComponent("custommachinery.gui.config.title", parent.getMachine().getName()));
        this.parent = parent;
        this.configButtons = new ArrayList<>();
    }

    private List<IGuiElement> getConfigurableElements() {
        return this.parent.getMachine().getGuiElements().stream()
                .filter(element -> element instanceof IComponentGuiElement<?> componentElement
                        && componentElement.getComponent(this.parent.getTile().componentManager).isPresent()
                        && componentElement.getComponent(this.parent.getTile().componentManager).get() instanceof ISideConfigComponent
                ).toList();
    }

    private ISideConfigComponent getComponentFromElement(IGuiElement element) {
        if(element instanceof IComponentGuiElement<?> componentGuiElement) {
            Optional<? extends IMachineComponent> optionalComponent = componentGuiElement.getComponent(this.parent.getTile().componentManager);
            if(optionalComponent.isPresent()) {
                IMachineComponent component = optionalComponent.get();
                if(component instanceof ISideConfigComponent sideConfigComponent)
                    return sideConfigComponent;
                throw new IllegalArgumentException("Component of type: " + component.getType().getRegistryName() + " is not side configurable.");
            }
            throw new IllegalArgumentException("Element of type: " + element.getType().getRegistryName() + " don't have a component in the machine: " + this.parent.getMachine().getId());
        }
        throw new IllegalArgumentException("Element of type: " + element.getType().getRegistryName() + " is not a component element.");
    }

    @Override
    protected void init() {
        this.getConfigurableElements().forEach(element -> this.configButtons.add(this.addRenderableWidget(new ComponentConfigButton(
                this.parent.getGuiLeft() + element.getX(),
                this.parent.getGuiTop() + element.getY(),
                element.getWidth(),
                element.getHeight(),
                new TranslatableComponent("custommachinery.gui.config.tooltip"),
                button -> Minecraft.getInstance().pushGuiLayer(new ComponentConfigScreen(this.getComponentFromElement(element).getConfig())),
                (button, pose, mouseX, mouseY) -> renderTooltip(pose, new TranslatableComponent("custommachinery.gui.config.tooltip"), mouseX, mouseY)
        ))));
        this.parent.getMachine().getGuiElements().stream()
                .filter(element -> element instanceof ConfigGuiElement)
                .findFirst()
                .map(element -> (ConfigGuiElement)element)
                .ifPresent(element -> this.exitButton = this.addRenderableWidget(new ImageButton(
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
                        button -> Minecraft.getInstance().popGuiLayer(),
                        new TranslatableComponent("custommachinery.gui.config.exit")
                ) {
                    @Override
                    public void render(PoseStack pose, int mouseX, int mouseY, float partialTicks) {
                        RenderSystem.setShaderColor(0.6F, 0.6F, 0.6F, 1);
                        super.render(pose, mouseX, mouseY, partialTicks);
                        RenderSystem.setShaderColor(1, 1, 1, 1);
                    }
                }));
    }

    @Override
    public void render(PoseStack pose, int mouseX, int mouseY, float partialTicks) {
        super.render(pose, mouseX, mouseY, partialTicks);

        this.configButtons.stream().filter(Button::isHoveredOrFocused).forEach(button -> button.renderToolTip(pose, mouseX, mouseY));
        if(this.exitButton != null && this.exitButton.isHoveredOrFocused())
            renderTooltip(pose, this.exitButton.getMessage(), mouseX, mouseY);
    }

    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        InputConstants.Key mouseKey = InputConstants.getKey(keyCode, scanCode);
        if (super.keyPressed(keyCode, scanCode, modifiers))
            return true;
        else if (Minecraft.getInstance().options.keyInventory.isActiveAndMatches(mouseKey)) {
            this.onClose();
            return true;
        }
        else return false;
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }
}
