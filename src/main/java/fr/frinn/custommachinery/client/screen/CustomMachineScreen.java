package fr.frinn.custommachinery.client.screen;

import com.mojang.blaze3d.vertex.PoseStack;
import fr.frinn.custommachinery.api.guielement.GuiElementType;
import fr.frinn.custommachinery.api.guielement.IMachineScreen;
import fr.frinn.custommachinery.common.data.CustomMachine;
import fr.frinn.custommachinery.common.guielement.SizeGuiElement;
import fr.frinn.custommachinery.common.init.CustomMachineContainer;
import fr.frinn.custommachinery.common.init.CustomMachineTile;
import fr.frinn.custommachinery.common.network.CGuiElementClickPacket;
import fr.frinn.custommachinery.common.network.NetworkManager;
import fr.frinn.custommachinery.common.util.Comparators;
import net.minecraft.client.gui.GuiComponent;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.ItemStack;

import java.util.List;

public class CustomMachineScreen extends AbstractContainerScreen<CustomMachineContainer> implements IMachineScreen {

    private final CustomMachineTile tile;
    private final CustomMachine machine;

    public CustomMachineScreen(CustomMachineContainer container, Inventory inv, Component name) {
        super(container, inv, name);
        this.tile = container.tile;
        this.machine = container.tile.getMachine();
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
    protected void renderBg(PoseStack matrix, float partialTicks, int mouseX, int mouseY) {
        this.renderBackground(matrix);

        matrix.pushPose();
        matrix.translate(this.leftPos, this.topPos, 0);
        this.machine.getGuiElements()
                .stream()
                .sorted(Comparators.GUI_ELEMENTS_COMPARATOR.reversed())
                .forEach(element -> ((GuiElementType)element.getType()).getRenderer().renderElement(matrix, element, this));
        matrix.popPose();
    }

    @SuppressWarnings({"unchecked", "rawtypes"})
    @Override
    protected void renderLabels(PoseStack matrix, int mouseX, int mouseY) {
        matrix.pushPose();
        matrix.translate(-this.leftPos, -this.topPos, 0);
        this.renderTooltip(matrix, mouseX, mouseY);
        this.machine.getGuiElements()
                .stream()
                .filter(element -> ((GuiElementType)element.getType()).getRenderer().isHovered(element, this, mouseX - this.leftPos, mouseY - this.topPos))
                .max(Comparators.GUI_ELEMENTS_COMPARATOR)
                .ifPresent(element -> ((GuiElementType)element.getType()).getRenderer().renderTooltip(matrix, element, this, mouseX, mouseY));
        matrix.popPose();
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

    @SuppressWarnings({"unchecked", "rawtypes"})
    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        this.machine.getGuiElements().stream()
                .filter(element -> ((GuiElementType)element.getType()).getRenderer().isHovered(element, this, (int)mouseX - this.leftPos, (int)mouseY - this.topPos))
                .findFirst()
                .ifPresent(element -> NetworkManager.CHANNEL.sendToServer(new CGuiElementClickPacket(this.machine.getGuiElements().indexOf(element), (byte) button)));
        return super.mouseClicked(mouseX, mouseY, button);
    }

    @Override
    public void drawTooltips(PoseStack pose, List<Component> tooltips, int mouseX, int mouseY) {
        super.renderComponentTooltip(pose, tooltips, mouseX, mouseY);
    }

    @Override
    public void drawGhostItem(PoseStack pose, ItemStack item, int posX, int posY) {
        this.itemRenderer.renderAndDecorateItem(item, this.leftPos + posX, this.topPos + posY);

        pose.pushPose();
        pose.translate(0.0F, 0.0F, 200.0F);
        GuiComponent.fill(pose, posX, posY, posX + 16, posY + 16, 822083583);
        pose.popPose();
    }
}
