package fr.frinn.custommachinery.client.screen.creation;

import fr.frinn.custommachinery.CustomMachinery;
import fr.frinn.custommachinery.client.ClientHandler;
import fr.frinn.custommachinery.client.screen.BaseScreen;
import fr.frinn.custommachinery.client.screen.creation.TemplateSelectionButton.TemplateSelectionList.TemplateEntry;
import fr.frinn.custommachinery.client.screen.popup.PopupScreen;
import fr.frinn.custommachinery.client.screen.widget.ListWidget;
import fr.frinn.custommachinery.common.machine.CustomMachine;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.AbstractButton;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.events.GuiEventListener;
import net.minecraft.client.gui.layouts.GridLayout;
import net.minecraft.client.gui.layouts.GridLayout.RowHelper;
import net.minecraft.client.gui.layouts.LayoutSettings;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.FastColor;
import org.jetbrains.annotations.Nullable;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

public class TemplateSelectionButton extends AbstractButton {

    private final BaseScreen parent;

    @Nullable
    private CustomMachine template = null;

    public TemplateSelectionButton(int x, int y, int width, int height, BaseScreen parent) {
        super(x, y, width, height, Component.translatable("custommachinery.gui.creation.template.choose"));
        this.parent = parent;
    }

    public void setTemplate(@Nullable CustomMachine template) {
        this.template = template;
        this.setMessage(template == null ? Component.translatable("custommachinery.gui.creation.template.choose") : template.getName());
    }

    @Nullable
    public CustomMachine getTemplate() {
        return this.template;
    }

    @Override
    public void onPress() {
        this.parent.openPopup(new TemplateSelectionPopup(this.parent), "Template selection");
    }

    @Override
    protected void updateWidgetNarration(NarrationElementOutput narrationElementOutput) {

    }

    public class TemplateSelectionPopup extends PopupScreen {

        public TemplateSelectionPopup(BaseScreen parent) {
            super(parent, 128, 150);
        }

        @Override
        protected void init() {
            super.init();
            GridLayout layout = new GridLayout(this.x + 5, this.y + 5);
            layout.defaultCellSetting().paddingBottom(5);
            LayoutSettings center = layout.newCellSettings().alignHorizontallyCenter();
            RowHelper row = layout.createRowHelper(1);
            row.addChild(new TemplateSelectionList(0, 0, this.xSize - 10, 128, 20), center);
            row.addChild(Button.builder(Component.translatable("custommachinery.gui.creation.popup.close"), button -> this.parent.closePopup(this)).size(50, 20).build(), center);
            layout.arrangeElements();
            layout.visitWidgets(this::addRenderableWidget);
            this.ySize = layout.getHeight() + 5;
        }
    }

    public class TemplateSelectionList extends ListWidget<TemplateEntry> {

        public TemplateSelectionList(int x, int y, int width, int height, int itemHeight) {
            super(x, y, width, height, itemHeight, Component.empty());
            TemplateEntry empty = new TemplateEntry(CustomMachinery.rl("empty"));
            this.addEntry(empty);
            CustomMachinery.TEMPLATES.keySet().forEach(template -> {
                TemplateEntry entry = new TemplateEntry(template);
                this.addEntry(entry);
                if(TemplateSelectionButton.this.template != null && TemplateSelectionButton.this.template.getId().equals(template))
                    this.setSelected(entry);
            });
            if(TemplateSelectionButton.this.template == null)
                this.setSelected(empty);
        }

        @Override
        public void setSelected(@Nullable TemplateEntry selected) {
            super.setSelected(selected);
            TemplateSelectionButton.this.setTemplate(selected == null || selected.template == null ? null : selected.template);
        }

        @Override
        protected void renderWidget(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
            super.renderWidget(graphics, mouseX, mouseY, partialTick);
            TemplateEntry entry = this.getEntryAtPosition(mouseX, mouseY);
            if(entry != null && entry.template != null)
                graphics.renderTooltip(Minecraft.getInstance().font, Collections.singletonList(CustomMachinery.TEMPLATES.get(entry.template.getId()).getSecond()), Optional.of(new MachineTooltipComponent(entry.template)), mouseX, mouseY);
        }

        public class TemplateEntry extends ListWidget.Entry {

            @Nullable
            private final CustomMachine template;

            public TemplateEntry(ResourceLocation id) {
                this.template = CustomMachinery.TEMPLATES.get(id) == null ? null : CustomMachinery.TEMPLATES.get(id).getFirst();
            }

            @Override
            protected void render(GuiGraphics graphics, int index, int x, int y, int width, int height, int mouseX, int mouseY, float partialTicks) {
                if(TemplateSelectionList.this.getSelected() == this)
                    ClientHandler.drawDottedRect(graphics, x, y, width - 1, height, FastColor.ARGB32.color(255, 255, 0, 0), 5, 5, 1);
                Component name = this.template == null ? Component.translatable("custommachinery.gui.creation.template.empty") : this.template.getName();
                graphics.drawScrollingString(Minecraft.getInstance().font, name, x + 5, x + width - 5, y + 5, FastColor.ARGB32.color(255, 255, 255, 255));
            }

            @Override
            public List<? extends GuiEventListener> children() {
                return Collections.emptyList();
            }
        }
    }
}
