package fr.frinn.custommachinery.client.screen.creation;

import fr.frinn.custommachinery.api.machine.MachineAppearanceProperty;
import fr.frinn.custommachinery.client.screen.creation.appearance.AppearancePropertyBuilderRegistry;
import fr.frinn.custommachinery.client.screen.creation.appearance.IAppearancePropertyBuilder;
import fr.frinn.custommachinery.common.init.Registration;
import fr.frinn.custommachinery.common.machine.builder.MachineAppearanceBuilder;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.components.ContainerObjectSelectionList;

import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.function.Supplier;

public class AppearanceList extends ContainerObjectSelectionList<AppearanceList.AppearanceEntry> {

    private final Supplier<MachineAppearanceBuilder> builder;
    private final MachineEditScreen parent;

    public AppearanceList(Minecraft minecraft, int x, int y, int width, int height, int itemHeight, Supplier<MachineAppearanceBuilder> builder, MachineEditScreen parent) {
        super(minecraft, width, height, y, y + height, itemHeight);
        this.builder = builder;
        this.parent = parent;
        this.setRenderBackground(false);
        this.setRenderHeader(false, 0);
        this.setRenderTopAndBottom(false);
        this.setLeftPos(x);
        this.init();
    }

    public void init() {
        this.clearEntries();
        for(MachineAppearanceProperty<?> property : Registration.APPEARANCE_PROPERTY_REGISTRY) {
            IAppearancePropertyBuilder<?> propertyBuilder = AppearancePropertyBuilderRegistry.getBuilder(property);
            if(propertyBuilder == null)
                continue;
            addEntry(new AppearanceEntry(propertyBuilder));
        }
    }

    public void resize(int x, int y, int width, int height) {
        this.width = width;
        this.height = height;
        this.setLeftPos(x);
        this.y0 = y;
        this.y1 = y + height;
    }

    @Override
    protected int getScrollbarPosition() {
        return this.getRowRight();
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double delta) {
        if(children().stream().map(entry -> entry.widget).anyMatch(widget -> widget.mouseScrolled(mouseX, mouseY, delta)))
            return true;
        return super.mouseScrolled(mouseX, mouseY, delta);
    }

    public class AppearanceEntry extends Entry<AppearanceEntry> {

        private final IAppearancePropertyBuilder<?> builder;
        private final AbstractWidget widget;

        public AppearanceEntry(IAppearancePropertyBuilder<?> builder) {
            this.builder = builder;
            this.widget = createWidget(builder);
        }

        private <T> AbstractWidget createWidget(IAppearancePropertyBuilder<T> builder) {
            return builder.makeWidget(AppearanceList.this.parent, 0, 0, 160, 20, () -> AppearanceList.this.builder.get().getProperty(builder.getType()), property -> {
                if(!Objects.equals(property, AppearanceList.this.builder.get().getProperty(builder.getType()))) {
                    AppearanceList.this.builder.get().setProperty(builder.getType(), property);
                    AppearanceList.this.parent.setChanged();
                }
            });
        }

        @Override
        public List<AbstractWidget> narratables() {
            return Collections.singletonList(this.widget);
        }

        @Override
        public void render(GuiGraphics graphics, int index, int top, int left, int width, int height, int mouseX, int mouseY, boolean hovering, float partialTick) {
            graphics.drawString(Minecraft.getInstance().font, this.builder.title(), left, top + this.widget.getHeight() / 2 - 2, 0, false);
            this.widget.setX(left + width - this.widget.getWidth() - 5);
            this.widget.setY(top);
            this.widget.render(graphics, mouseX, mouseY, partialTick);
            if(this.widget.getTooltip() != null)
                graphics.renderTooltip(Minecraft.getInstance().font, this.widget.getTooltip().toCharSequence(Minecraft.getInstance()), mouseX, mouseY);
        }

        @Override
        public List<AbstractWidget> children() {
            return Collections.singletonList(this.widget);
        }
    }
}
