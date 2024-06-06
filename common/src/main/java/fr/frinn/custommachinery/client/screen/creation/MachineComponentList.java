package fr.frinn.custommachinery.client.screen.creation;

import fr.frinn.custommachinery.api.component.IMachineComponentTemplate;
import fr.frinn.custommachinery.client.screen.creation.component.IMachineComponentBuilder;
import fr.frinn.custommachinery.client.screen.creation.component.MachineComponentBuilderRegistry;
import fr.frinn.custommachinery.client.screen.creation.tabs.ComponentTab;
import fr.frinn.custommachinery.common.machine.builder.CustomMachineBuilder;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.ObjectSelectionList;
import net.minecraft.network.chat.Component;
import net.minecraft.util.FastColor;
import org.jetbrains.annotations.Nullable;

public class MachineComponentList extends ObjectSelectionList<MachineComponentList.MachineComponentEntry> {

    private final ComponentTab tab;

    public MachineComponentList(Minecraft minecraft, int x, int y, int width, int height, int itemHeight, ComponentTab tab) {
        super(minecraft, width, height, y, y + height, itemHeight);
        this.tab = tab;
        this.setLeftPos(x);
        this.setRenderBackground(false);
        this.setRenderHeader(false, 0);
        this.setRenderTopAndBottom(false);
    }

    public void setup(CustomMachineBuilder builder) {
        this.clearEntries();
        for(IMachineComponentTemplate<?> template : builder.getComponents()) {
            IMachineComponentBuilder<?, ?> componentBuilder = MachineComponentBuilderRegistry.getBuilder(template.getType());
            if(componentBuilder != null)
                this.addEntry(new MachineComponentEntry(template, componentBuilder));
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
    protected void renderItem(GuiGraphics graphics, int mouseX, int mouseY, float partialTick, int index, int left, int top, int width, int height) {
        MachineComponentEntry entry = this.getEntry(index);
        entry.renderBack(graphics, index, top, left, width, height, mouseX, mouseY, entry.isMouseOver(mouseX, mouseY), partialTick);
        if(this.isSelectedItem(index))
            this.renderSelection(graphics, top, width, height, FastColor.ARGB32.color(255, 0, 0, 0), FastColor.ARGB32.color(255, 198, 198, 198));
        entry.render(graphics, index, top, left, width, height, mouseX, mouseY, entry.isMouseOver(mouseX, mouseY), partialTick);
    }

    @Override
    public void setSelected(@Nullable MachineComponentList.MachineComponentEntry selected) {
        super.setSelected(selected);
        this.tab.setupButtons();
    }

    public class MachineComponentEntry extends Entry<MachineComponentEntry> {

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
                MachineComponentList.this.tab.parent.getBuilder().getComponents().remove(this.template);
                this.template = template;
                this.builder = builder;
                MachineComponentList.this.tab.parent.getBuilder().getComponents().add(this.template);
            }
        }

        @Override
        public void render(GuiGraphics graphics, int index, int top, int left, int width, int height, int mouseX, int mouseY, boolean hovering, float partialTick) {
            ((IMachineComponentBuilder)this.builder).render(graphics, left, top, width, height, this.template);
        }

        @Override
        public Component getNarration() {
            return Component.empty();
        }

        @Override
        public boolean mouseClicked(double mouseX, double mouseY, int button) {
            return true;
        }
    }
}
