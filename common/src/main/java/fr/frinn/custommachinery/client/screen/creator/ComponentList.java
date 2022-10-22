package fr.frinn.custommachinery.client.screen.creator;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import fr.frinn.custommachinery.CustomMachinery;
import fr.frinn.custommachinery.api.component.IMachineComponent;
import fr.frinn.custommachinery.api.component.MachineComponentType;
import fr.frinn.custommachinery.api.component.builder.IMachineComponentBuilder;
import fr.frinn.custommachinery.client.ClientHandler;
import fr.frinn.custommachinery.client.screen.widget.TexturedButton;
import fr.frinn.custommachinery.common.init.Registration;
import fr.frinn.custommachinery.impl.util.EnumButton;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.components.ObjectSelectionList;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;

import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;
import java.awt.*;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.StreamSupport;

public class ComponentList extends ObjectSelectionList<ComponentList.ComponentEntry> {

    private MachineComponentScreen parent;

    private EnumButton<MachineComponentType<?>> builderTypeWidget;
    private TexturedButton addButton;
    private TexturedButton removeButton;
    private Map<String, AbstractWidget> propertyWidgets;

    public ComponentList(Minecraft mc, int width, int height, int x, int y, int entryHeight, MachineComponentScreen parent) {
        super(mc, width, height, y, y + height, entryHeight);
        this.setLeftPos(x);
        this.setRenderBackground(false);
        this.setRenderTopAndBottom(false);
        this.centerListVertically = false;
        this.setRenderSelection(false);
        this.parent = parent;
        this.builderTypeWidget = new EnumButton<>(
                x,
                y + 115,
                63,
                20,
                button -> {},
                (button, matrix, mouseX, mouseY) -> this.parent.renderTooltip(matrix, new TranslatableComponent("custommachinery.gui.component.buildertypewidget"), mouseX, mouseY),
                MachineComponentType::getTranslatedName,
                StreamSupport.stream(Registration.MACHINE_COMPONENT_TYPE_REGISTRY.spliterator(), false).filter(MachineComponentType::haveGUIBuilder).toList(),
                Registration.ENERGY_MACHINE_COMPONENT.get()
        );
        this.parent.getChildrens().add(this.builderTypeWidget);
        this.addButton = new TexturedButton(
                x,
                y + 136,
                20,
                20,
                TextComponent.EMPTY,
                new ResourceLocation(CustomMachinery.MODID, "textures/gui/creation/create_icon.png"),
                button -> this.addComponent(this.builderTypeWidget.getValue().getGUIBuilder().get()),
                (button, matrix, mouseX, mouseY) -> this.parent.renderTooltip(matrix, new TranslatableComponent("custommachinery.gui.machineloading.create"), mouseX, mouseY)
        );
        this.parent.getChildrens().add(this.addButton);
        this.removeButton = new TexturedButton(
                x + 43,
                y + 136,
                20,
                20,
                TextComponent.EMPTY,
                new ResourceLocation(CustomMachinery.MODID, "textures/gui/creation/delete_icon.png"),
                button -> {
                    if(getSelected() != null)
                        this.removeEntry(this.getSelected());
                },
                (button, matrix, mouseX, mouseY) -> this.parent.renderTooltip(matrix, new TranslatableComponent("custommachinery.gui.machineloading.delete"), mouseX, mouseY)
        );
        this.parent.getChildrens().add(this.removeButton);
        this.propertyWidgets = new HashMap<>();
    }

    public void addComponent(IMachineComponentBuilder<? extends IMachineComponent> builder) {
        if(!this.parent.machine.getComponentBuilders().contains(builder))
            this.parent.machine.getComponentBuilders().add(builder);

        int index = this.addEntry(new ComponentEntry(this, builder));
        if(this.getSelected() == null)
            this.setSelected(this.getEntry(index));
    }

    @ParametersAreNonnullByDefault
    @Override
    protected boolean removeEntry(ComponentEntry entry) {
        this.parent.machine.getComponentBuilders().remove(entry.getComponentBuilder());
        return super.removeEntry(entry);
    }

    @Override
    public int getRowWidth() {
        return this.width;
    }

    @Override
    public int getRowLeft() {
        return this.x0;
    }

    @Override
    protected int getScrollbarPosition() {
        return this.x0 + this.width + 6;
    }

    //TODO: Add scrollbar
    @ParametersAreNonnullByDefault
    @Override
    public void render(PoseStack matrix, int mouseX, int mouseY, float partialTicks) {
        double s = Minecraft.getInstance().getWindow().getGuiScale();
        int screenHeight = Minecraft.getInstance().getWindow().getScreenHeight() / (int)s;
        RenderSystem.enableScissor(this.x0 * (int)s, (screenHeight - this.y0 - this.height) * (int)s, this.width * (int)s, this.height * (int)s);
        super.render(matrix, mouseX, mouseY, partialTicks);
        RenderSystem.disableScissor();
        this.builderTypeWidget.render(matrix, mouseX, mouseY, partialTicks);
        this.addButton.render(matrix, mouseX, mouseY, partialTicks);
        this.removeButton.render(matrix, mouseX, mouseY, partialTicks);
        this.propertyWidgets.forEach((name, widget) -> ClientHandler.drawSizedString(this.minecraft.font, matrix, name, this.parent.xPos + 5, widget.y + 5, 40, 1.0F,0));
        this.propertyWidgets.forEach((name, widget) -> widget.render(matrix, mouseX, mouseY, partialTicks));
    }

    @Override
    public void setSelected(@Nullable ComponentList.ComponentEntry entry) {
        super.setSelected(entry);
        this.propertyWidgets.forEach((name, widget) -> this.parent.getChildrens().remove(widget));
        this.propertyWidgets.clear();
        if(entry != null) {
            AtomicInteger index = new AtomicInteger();
            entry.getComponentBuilder().getProperties().forEach(property ->
                this.propertyWidgets.put(property.getName(), property.getAsWidget(this.parent.xPos + 50, this.parent.yPos + index.getAndIncrement() * 25 + 5, 200, 20))
            );
            this.propertyWidgets.forEach((name, widget) -> this.parent.getChildrens().add(widget));
        }
    }

    public static class ComponentEntry extends ObjectSelectionList.Entry<ComponentEntry> {

        private ComponentList list;
        IMachineComponentBuilder<?> componentBuilder;

        public ComponentEntry(ComponentList list, IMachineComponentBuilder<?> componentBuilder) {
            this.list = list;
            this.componentBuilder = componentBuilder;
        }

        @ParametersAreNonnullByDefault
        @Override
        public void render(PoseStack matrix, int index, int y, int x, int width, int height, int mouseX, int mouseY, boolean isMouseOver, float partialTicks) {
            if(this.componentBuilder != null) {
                int nameWidth = Minecraft.getInstance().font.width(this.componentBuilder.getType().getTranslatedName().getString());
                float scale = Mth.clamp((float)(width - 6) / (float) nameWidth, 0, 1.0F);
                matrix.pushPose();
                matrix.translate(x, y, 0);
                matrix.scale(scale, scale, 0.0F);
                if(this.list.getSelected() != this)
                    Minecraft.getInstance().font.draw(matrix, this.componentBuilder.getType().getTranslatedName().getString(), 0, 0, 0);
                else
                    Minecraft.getInstance().font.drawShadow(matrix, this.componentBuilder.getType().getTranslatedName().getString(), 0, 0, Color.RED.getRGB());
                matrix.popPose();
            }
            else Minecraft.getInstance().font.draw(matrix, "NULL", x, y, 0);
        }

        @Override
        public boolean mouseClicked(double mouseX, double mouseY, int button) {
            this.list.setSelected(this);
            return true;
        }

        @Override
        public Component getNarration() {
            return null;
        }

        public IMachineComponentBuilder<?> getComponentBuilder() {
            return this.componentBuilder;
        }
    }
}
