package fr.frinn.custommachinery.client.screen.creation.component;

import fr.frinn.custommachinery.api.component.MachineComponentType;
import fr.frinn.custommachinery.client.screen.creation.MachineEditScreen;
import fr.frinn.custommachinery.client.screen.creation.component.ComponentCreationPopup.ComponentCreationListWidget.ComponentCreationListEntry;
import fr.frinn.custommachinery.client.screen.popup.PopupScreen;
import fr.frinn.custommachinery.client.screen.widget.ListWidget;
import fr.frinn.custommachinery.common.init.Registration;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.StringWidget;
import net.minecraft.client.gui.components.events.GuiEventListener;
import net.minecraft.network.chat.Component;

import java.util.Collections;
import java.util.List;

public class ComponentCreationPopup extends PopupScreen {

    public static final Component CONFIRM = Component.translatable("custommachinery.gui.popup.confirm").withStyle(ChatFormatting.GREEN);
    public static final Component CANCEL = Component.translatable("custommachinery.gui.popup.cancel").withStyle(ChatFormatting.RED);

    private final Runnable onChange;

    private ComponentCreationListWidget list;

    public ComponentCreationPopup(MachineEditScreen parent, Runnable onChange) {
        super(parent, 144, 164);
        this.onChange = onChange;
    }

    private void confirm() {
        ComponentCreationListWidget.ComponentCreationListEntry entry = this.list.getSelected();
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
        this.list = this.addRenderableWidget(new ComponentCreationListWidget(this.x + 3, this.y + 15, this.xSize - 10, this.ySize - 50));
        this.addRenderableWidget(Button.builder(CONFIRM, b -> this.confirm()).bounds(this.x + 5, this.y + this.ySize - 30, 50, 20).build());
        this.addRenderableWidget(Button.builder(CANCEL, b -> this.cancel()).bounds(this.x + this.xSize - 55, this.y + this.ySize - 30, 50, 20).build());
    }

    protected static class ComponentCreationListWidget extends ListWidget<ComponentCreationListEntry> {

        public ComponentCreationListWidget(int x, int y, int width, int height) {
            super(x, y, width, height, 20, Component.empty());
            this.setRenderSelection();

            for(MachineComponentType<?> type : Registration.MACHINE_COMPONENT_TYPE_REGISTRY) {
                IMachineComponentBuilder<?, ?> builder = MachineComponentBuilderRegistry.getBuilder(type);
                if(builder != null)
                    this.addEntry(new ComponentCreationListEntry(builder));
            }

            this.setSelected(this.getEntries().isEmpty() ? null : this.getEntries().get(0));
        }

        protected static class ComponentCreationListEntry extends Entry {

            private final IMachineComponentBuilder<?, ?> builder;

            private ComponentCreationListEntry(IMachineComponentBuilder<?, ?> builder) {
                this.builder = builder;
            }

            @Override
            public void render(GuiGraphics graphics, int index, int x, int y, int width, int height, int mouseX, int mouseY, float partialTick) {
                graphics.drawString(Minecraft.getInstance().font, this.builder.type().getTranslatedName(), x + 5, y + 5, 0, false);
            }

            @Override
            public List<? extends GuiEventListener> children() {
                return Collections.emptyList();
            }

            @Override
            public boolean mouseClicked(double mouseX, double mouseY, int button) {
                return true;
            }
        }
    }
}
