package fr.frinn.custommachinery.client.screen.creation.appearance.builder;

import fr.frinn.custommachinery.api.machine.MachineAppearanceProperty;
import fr.frinn.custommachinery.client.screen.BaseScreen;
import fr.frinn.custommachinery.client.screen.creation.appearance.IAppearancePropertyBuilder;
import fr.frinn.custommachinery.client.screen.creation.appearance.ModelSelectionPopup;
import fr.frinn.custommachinery.common.init.Registration;
import fr.frinn.custommachinery.common.util.MachineModelLocation;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.Tooltip;
import net.minecraft.network.chat.Component;

import java.util.function.Consumer;
import java.util.function.Supplier;

public record ModelAppearancePropertyBuilder(Component title, MachineAppearanceProperty<MachineModelLocation> type) implements IAppearancePropertyBuilder<MachineModelLocation> {

    @Override
    public AbstractWidget makeWidget(BaseScreen parent, int x, int y, int width, int height, Supplier<MachineModelLocation> supplier, Consumer<MachineModelLocation> consumer) {
        return Button.builder(this.title, b -> parent.openPopup(new ModelSelectionPopup(parent, supplier, consumer, this.isBlock()))).bounds(x, y, width, height).tooltip(Tooltip.create(Component.translatable("custommachinery.gui.creation.appearance." + (this.isBlock() ? "block" : "item") + ".tooltip"))).build();
    }

    private boolean isBlock() {
        return this.type == Registration.BLOCK_MODEL_PROPERTY.get();
    }
}
