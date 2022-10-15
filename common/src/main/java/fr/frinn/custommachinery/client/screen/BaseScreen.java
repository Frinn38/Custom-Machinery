package fr.frinn.custommachinery.client.screen;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;

import java.util.ArrayList;
import java.util.List;

public abstract class BaseScreen extends Screen {

    private final List<AbstractWidget> widgets = new ArrayList<>();

    public BaseScreen(Component component) {
        super(component);
    }

    public void drawBackground(PoseStack poseStack, int mouseX, int mouseY, float partialTicks) {

    };

    public <T extends AbstractWidget> T addWidget(T widget) {
        super.addWidget(widget);
        this.widgets.add(widget);
        return widget;
    }

    @Override
    protected void init() {
        this.widgets.clear();
    }

    @Override
    public void render(PoseStack poseStack, int mouseX, int mouseY, float partialTicks) {
        drawBackground(poseStack, mouseX, mouseY, partialTicks);
        this.widgets.forEach(widget -> widget.render(poseStack, mouseX, mouseY, partialTicks));
        this.widgets.stream().filter(AbstractWidget::isHoveredOrFocused).forEach(widget -> widget.renderToolTip(poseStack, mouseX, mouseY));
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }
}
