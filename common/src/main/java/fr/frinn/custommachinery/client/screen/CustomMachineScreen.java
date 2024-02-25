package fr.frinn.custommachinery.client.screen;

import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.platform.Lighting;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import fr.frinn.custommachinery.api.guielement.GuiElementType;
import fr.frinn.custommachinery.api.guielement.IMachineScreen;
import fr.frinn.custommachinery.client.render.ColoredBufferSource;
import fr.frinn.custommachinery.common.guielement.SizeGuiElement;
import fr.frinn.custommachinery.common.init.CustomMachineContainer;
import fr.frinn.custommachinery.common.init.CustomMachineTile;
import fr.frinn.custommachinery.common.machine.CustomMachine;
import fr.frinn.custommachinery.common.network.CGuiElementClickPacket;
import fr.frinn.custommachinery.common.util.Comparators;
import fr.frinn.custommachinery.common.util.slot.FilterSlotItemComponent;
import fr.frinn.custommachinery.impl.guielement.AbstractGuiElementWidget;
import fr.frinn.custommachinery.impl.guielement.GuiElementWidgetSupplierRegistry;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.client.renderer.block.model.ItemTransforms;
import net.minecraft.client.renderer.entity.ItemRenderer;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.network.chat.Component;
import net.minecraft.util.FastColor;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.InventoryMenu;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class CustomMachineScreen extends AbstractContainerScreen<CustomMachineContainer> implements IMachineScreen {

    private final CustomMachineTile tile;
    private final CustomMachine machine;
    private final List<AbstractGuiElementWidget<?>> elementWidgets = new ArrayList<>();

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
        this.elementWidgets.clear();
        this.machine.getGuiElements().stream()
                .filter(element -> GuiElementWidgetSupplierRegistry.hasWidgetSupplier(element.getType()))
                .sorted(Comparators.GUI_ELEMENTS_COMPARATOR.reversed())
                .forEach(element -> addElementWidget(GuiElementWidgetSupplierRegistry.getWidgetSupplier((GuiElementType)element.getType()).get(element, this)));
    }

    private void addElementWidget(AbstractGuiElementWidget<?> widget) {
        this.elementWidgets.add(widget);
        addRenderableWidget(widget);
    }

    @Override
    public void render(PoseStack poseStack, int mouseX, int mouseY, float partialTick) {
        super.render(poseStack, mouseX, mouseY, partialTick);
        this.elementWidgets.forEach(widget -> {
            if(widget.isHoveredOrFocused())
                widget.renderToolTip(poseStack, mouseX, mouseY);
        });
        this.renderTooltip(poseStack, mouseX, mouseY);
    }

    @Override
    protected void renderBg(PoseStack poseStack, float partialTick, int mouseX, int mouseY) {
        this.renderBackground(poseStack);
    }

    @Override
    protected void renderLabels(PoseStack poseStack, int mouseX, int mouseY) {

    }

    @Override
    public void renderSlot(PoseStack pose, Slot slot) {
        if(slot instanceof FilterSlotItemComponent filterSlot && slot.hasItem())
            drawGhostItem(pose, slot.getItem(), slot.x, slot.y, FastColor.ARGB32.color(128, 255, 255, 255));
        else
            super.renderSlot(pose, slot);
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

    @Override
    public void drawTooltips(PoseStack pose, List<Component> tooltips, int mouseX, int mouseY) {
        super.renderComponentTooltip(pose, tooltips, mouseX, mouseY);
    }

    @Override
    public void drawGhostItem(PoseStack pose, ItemStack item, int posX, int posY, int color) {
        ItemRenderer itemRenderer = Minecraft.getInstance().getItemRenderer();

        Minecraft.getInstance().getTextureManager().getTexture(InventoryMenu.BLOCK_ATLAS).setFilter(false, false);
        RenderSystem.setShaderTexture(0, InventoryMenu.BLOCK_ATLAS);
        RenderSystem.enableBlend();
        RenderSystem.blendFunc(GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA);
        RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);

        PoseStack poseStack = RenderSystem.getModelViewStack();
        poseStack.pushPose();
        poseStack.translate(posX, posY, 100.0F + itemRenderer.blitOffset);
        poseStack.translate(8.0, 8.0, 0.0);
        poseStack.scale(1.0F, -1.0F, 1.0F);
        poseStack.scale(16.0F, 16.0F, 16.0F);

        RenderSystem.applyModelViewMatrix();

        PoseStack poseStack2 = new PoseStack();
        ColoredBufferSource bufferSource = new ColoredBufferSource(Minecraft.getInstance().renderBuffers().bufferSource(), color);
        BakedModel model = itemRenderer.getModel(item, null, null, 0);

        if (!model.usesBlockLight())
            Lighting.setupForFlatItems();

        itemRenderer.render(item, ItemTransforms.TransformType.GUI, false, poseStack2, bufferSource, LightTexture.FULL_BRIGHT, OverlayTexture.NO_OVERLAY, model);
        bufferSource.endBatch();
        RenderSystem.enableDepthTest();

        if (!model.usesBlockLight())
            Lighting.setupFor3DItems();

        poseStack.popPose();
        RenderSystem.applyModelViewMatrix();
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        for(AbstractGuiElementWidget<?> elementWidget : this.elementWidgets) {
            if(elementWidget.mouseClicked(mouseX, mouseY, button)) {
                new CGuiElementClickPacket(this.machine.getGuiElements().indexOf(elementWidget.getElement()), (byte)button).sendToServer();
                return true;
            }
        }
        return super.mouseClicked(mouseX, mouseY, button);
    }

    public Optional<AbstractGuiElementWidget<?>> getElementUnderMouse(double mouseX, double mouseY) {
        for(AbstractGuiElementWidget<?> elementWidget : this.elementWidgets) {
            if(elementWidget.isMouseOver(mouseX, mouseY) && elementWidget.isClickable()) {
                return Optional.of(elementWidget);
            }
        }
        return Optional.empty();
    }
}
