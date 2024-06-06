package fr.frinn.custommachinery.client.screen.creation;

import fr.frinn.custommachinery.api.component.IMachineComponentTemplate;
import fr.frinn.custommachinery.client.screen.creation.MachineComponentListWidget.MachineComponentEntry;
import fr.frinn.custommachinery.client.screen.creation.component.IMachineComponentBuilder;
import fr.frinn.custommachinery.client.screen.creation.component.MachineComponentBuilderRegistry;
import fr.frinn.custommachinery.client.screen.creation.tabs.ComponentTab;
import fr.frinn.custommachinery.client.screen.widget.ListWidget;
import fr.frinn.custommachinery.common.machine.builder.CustomMachineBuilder;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.events.GuiEventListener;
import net.minecraft.network.chat.Component;
import org.jetbrains.annotations.Nullable;

import java.util.Collections;
import java.util.List;

public class MachineComponentListWidget extends ListWidget<MachineComponentEntry> {

    private final ComponentTab tab;

    public MachineComponentListWidget(int x, int y, int width, int height, int itemHeight, ComponentTab tab) {
        super(x, y, width, height, itemHeight, Component.empty());
        this.tab = tab;
        this.setRenderSelection();
    }

    public void setup(CustomMachineBuilder builder) {
        this.clear();
        for(IMachineComponentTemplate<?> template : builder.getComponents()) {
            IMachineComponentBuilder<?, ?> componentBuilder = MachineComponentBuilderRegistry.getBuilder(template.getType());
            if(componentBuilder != null)
                this.addEntry(new MachineComponentEntry(template, componentBuilder));
        }
    }

    @Override
    public void setSelected(@Nullable MachineComponentListWidget.MachineComponentEntry selected) {
        super.setSelected(selected);
        this.tab.setupButtons();
    }

    public class MachineComponentEntry extends Entry {

        private IMachineComponentTemplate<?> template;
        private IMachineComponentBuilder<?, ?> builder;

        public MachineComponentEntry(IMachineComponentTemplate<?> template, IMachineComponentBuilder<?, ?> builder) {
            this.template = template;
            this.builder = builder;
        }

        public IMachineComponentTemplate<?> getTemplate() {
            return this.template;
        }

        public void setTemplate(IMachineComponentTemplate<?> template) {
            IMachineComponentBuilder<?, ?> builder = MachineComponentBuilderRegistry.getBuilder(template.getType());
            if(builder != null) {
                MachineComponentListWidget.this.tab.parent.getBuilder().getComponents().remove(this.template);
                this.template = template;
                this.builder = builder;
                MachineComponentListWidget.this.tab.parent.getBuilder().getComponents().add(this.template);
            }
        }

        @Override
        public void render(GuiGraphics graphics, int index, int x, int y, int width, int height, int mouseX, int mouseY, float partialTick) {
            ((IMachineComponentBuilder)this.builder).render(graphics, x, y, width, height, this.template);
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
