package fr.frinn.custommachinery.client.screen.creation.appearance.builder;

import fr.frinn.custommachinery.api.machine.MachineAppearanceProperty;
import fr.frinn.custommachinery.client.screen.BaseScreen;
import fr.frinn.custommachinery.client.screen.creation.appearance.IAppearancePropertyBuilder;
import fr.frinn.custommachinery.client.screen.creation.appearance.ModelSelectionPopup;
import fr.frinn.custommachinery.client.screen.widget.GroupWidget;
import fr.frinn.custommachinery.common.init.Registration;
import fr.frinn.custommachinery.common.util.MachineModelLocation;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.Tooltip;
import net.minecraft.network.chat.Component;

import java.util.function.Consumer;
import java.util.function.Supplier;

public record ModelAppearancePropertyBuilder(Component title, MachineAppearanceProperty<MachineModelLocation> type) implements IAppearancePropertyBuilder<MachineModelLocation> {

    @Override
    public AbstractWidget makeWidget(BaseScreen parent, int x, int y, int width, int height, Supplier<MachineModelLocation> supplier, Consumer<MachineModelLocation> consumer) {
        return new ModelShowingButtonWidget(parent, x, y, width, height, this.title, supplier, consumer, this.type == Registration.BLOCK_MODEL_PROPERTY.get());
    }

    public static class ModelShowingButtonWidget extends GroupWidget {

        private final Supplier<MachineModelLocation> supplier;

        public ModelShowingButtonWidget(BaseScreen parent, int x, int y, int width, int height, Component message, Supplier<MachineModelLocation> supplier, Consumer<MachineModelLocation> consumer, boolean isBlock) {
            super(x, y, width, height, message);
            this.supplier = supplier;
            this.addWidget(Button.builder(message, b -> parent.openPopup(new ModelSelectionPopup(parent, supplier, consumer, isBlock))).bounds(x + 20, y, width - 20, height).tooltip(Tooltip.create(Component.translatable("custommachinery.gui.creation.appearance." + (isBlock ? "block" : "item") + ".tooltip"))).build());
        }

        @Override
        protected void renderWidget(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
            super.renderWidget(graphics, mouseX, mouseY, partialTick);
            ModelSelectionPopup.renderModel(graphics, this.getX() + 10, this.getY() + 10, this.supplier.get(), 16);
        }
    }
}
