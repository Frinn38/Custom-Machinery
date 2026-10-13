package fr.frinn.custommachinery.client.screen.creation.appearance.builder;

import fr.frinn.custommachinery.api.machine.MachineAppearanceProperty;
import fr.frinn.custommachinery.client.screen.BaseScreen;
import fr.frinn.custommachinery.client.screen.creation.appearance.IAppearancePropertyBuilder;
import fr.frinn.custommachinery.client.screen.creation.appearance.ItemModelSelectionPopup;
import fr.frinn.custommachinery.client.screen.widget.GroupWidget;
import fr.frinn.custommachinery.common.util.MachineModelLocation.MachineItemModelLocation;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.Tooltip;
import net.minecraft.network.chat.Component;

import java.util.function.Consumer;
import java.util.function.Supplier;

public record ItemModelAppearancePropertyBuilder(Component title, MachineAppearanceProperty<MachineItemModelLocation> type) implements IAppearancePropertyBuilder<MachineItemModelLocation> {

    @Override
    public AbstractWidget makeWidget(BaseScreen parent, int x, int y, int width, int height, Supplier<MachineItemModelLocation> supplier, Consumer<MachineItemModelLocation> consumer) {
        return new ItemModelShowingButtonWidget(parent, x, y, width, height, this.title, supplier, consumer, false);
    }

    public static class ItemModelShowingButtonWidget extends GroupWidget {

        private final Supplier<MachineItemModelLocation> supplier;

        public ItemModelShowingButtonWidget(BaseScreen parent, int x, int y, int width, int height, Component message, Supplier<MachineItemModelLocation> supplier, Consumer<MachineItemModelLocation> consumer, boolean isBlock) {
            super(x, y, width, height, message);
            this.supplier = supplier;
            this.addWidget(Button.builder(message, b -> parent.openPopup(new ItemModelSelectionPopup(parent, supplier, consumer, isBlock))).bounds(x + 20, y, width - 20, height).tooltip(Tooltip.create(Component.translatable("custommachinery.gui.creation.appearance." + (isBlock ? "block" : "item") + ".tooltip"))).build());
        }

        @Override
        protected void extractWidgetRenderState(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float partialTick) {
            super.extractWidgetRenderState(graphics, mouseX, mouseY, partialTick);
            ItemModelSelectionPopup.renderModel(graphics, this.getX() + 10, this.getY() + 10, this.supplier.get(), 16);
        }
    }
}
