package fr.frinn.custommachinery.client.screen.creation.component;

import fr.frinn.custommachinery.api.component.MachineComponentType;
import fr.frinn.custommachinery.client.screen.creation.MachineEditScreen;
import fr.frinn.custommachinery.client.screen.popup.PopupScreen;
import fr.frinn.custommachinery.common.init.Registration;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.ObjectSelectionList;
import net.minecraft.client.gui.components.StringWidget;
import net.minecraft.client.gui.layouts.LayoutElement;
import net.minecraft.network.chat.Component;
import net.minecraft.util.FastColor;

import java.util.function.Consumer;

public class ComponentCreationPopup extends PopupScreen {

    public static final Component CONFIRM = Component.translatable("custommachinery.gui.popup.confirm").withStyle(ChatFormatting.GREEN);
    public static final Component CANCEL = Component.translatable("custommachinery.gui.popup.cancel").withStyle(ChatFormatting.RED);

    private final Runnable onChange;

    private ComponentCreationList list;

    public ComponentCreationPopup(MachineEditScreen parent, Runnable onChange) {
        super(parent, 116, 144);
        this.onChange = onChange;
    }

    private void confirm() {
        ComponentCreationList.ComponentCreationListEntry entry = this.list.getSelected();
        if(entry != null && this.parent instanceof MachineEditScreen editScreen) {
            PopupScreen componentCreationPopup = entry.builder.makePopup(editScreen, null, template -> {
                editScreen.getBuilder().getComponents().add(template);
                editScreen.setChanged();
                this.onChange.run();
            });
            editScreen.closePopup(this);
            editScreen.openPopup(componentCreationPopup);
        }
    }

    private void cancel() {
        this.parent.closePopup(this);
    }

    @Override
    protected void init() {
        super.init();
        Component title = Component.translatable("custommachinery.gui.creation.components.create.title");
        this.addRenderableWidget(new StringWidget(this.x, this.y + 5, this.xSize, this.font.lineHeight, title, Minecraft.getInstance().font));
        this.list = this.addRenderableWidget(new ComponentCreationList(this.x + 3, this.y + 15, this.xSize - 10, this.ySize - 50));
        this.addRenderableWidget(Button.builder(CONFIRM, b -> this.confirm()).bounds(this.x + 5, this.y + this.ySize - 30, 50, 20).build());
        this.addRenderableWidget(Button.builder(CANCEL, b -> this.cancel()).bounds(this.x + this.xSize - 55, this.y + this.ySize - 30, 50, 20).build());
    }

    private static class ComponentCreationList extends ObjectSelectionList<ComponentCreationList.ComponentCreationListEntry> implements LayoutElement {

        public ComponentCreationList(int x, int y, int width, int height) {
            super(Minecraft.getInstance(), width, height, y, y + height, 20);
            this.setLeftPos(x);
            this.setRenderBackground(false);
            this.setRenderHeader(false, 0);
            this.setRenderTopAndBottom(false);

            for(MachineComponentType<?> type : Registration.MACHINE_COMPONENT_TYPE_REGISTRY) {
                IMachineComponentBuilder<?, ?> builder = MachineComponentBuilderRegistry.getBuilder(type);
                if(builder != null)
                    this.addEntry(new ComponentCreationListEntry(builder));
            }

            this.setSelected(this.getFirstElement());
        }

        @Override
        protected int getScrollbarPosition() {
            return this.getRowRight();
        }

        @Override
        public int getRowWidth() {
            return this.width - 10;
        }

        @Override
        protected void renderItem(GuiGraphics graphics, int mouseX, int mouseY, float partialTick, int index, int left, int top, int width, int height) {
            ComponentCreationListEntry entry = this.getEntry(index);
            entry.renderBack(graphics, index, top, left, width, height, mouseX, mouseY, entry.isMouseOver(mouseX, mouseY), partialTick);
            if(this.isSelectedItem(index))
                this.renderSelection(graphics, top, width, height, FastColor.ARGB32.color(255, 0, 0, 0), FastColor.ARGB32.color(255, 198, 198, 198));
            entry.render(graphics, index, top, left, width, height, mouseX, mouseY, entry.isMouseOver(mouseX, mouseY), partialTick);
        }

        @Override
        public void setX(int x) {
            this.setLeftPos(x);
        }

        @Override
        public void setY(int y) {
            this.y0 = y;
            this.y1 = y + this.width;
        }

        @Override
        public int getX() {
            return this.x0;
        }

        @Override
        public int getY() {
            return this.y0;
        }

        @Override
        public int getWidth() {
            return this.width;
        }

        @Override
        public int getHeight() {
            return this.height;
        }

        @Override
        public void visitWidgets(Consumer<AbstractWidget> consumer) {

        }

        private static class ComponentCreationListEntry extends Entry<ComponentCreationListEntry> {

            private final IMachineComponentBuilder<?, ?> builder;

            private ComponentCreationListEntry(IMachineComponentBuilder<?, ?> builder) {
                this.builder = builder;
            }

            @Override
            public Component getNarration() {
                return Component.empty();
            }

            @Override
            public void render(GuiGraphics graphics, int index, int top, int left, int width, int height, int mouseX, int mouseY, boolean hovering, float partialTick) {
                graphics.drawString(Minecraft.getInstance().font, this.builder.type().getTranslatedName(), left + 5, top + 5, 0, false);
            }

            @Override
            public boolean mouseClicked(double mouseX, double mouseY, int button) {
                return true;
            }
        }
    }
}
