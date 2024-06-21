package fr.frinn.custommachinery.client.screen.creation.appearance.builder;

import fr.frinn.custommachinery.api.machine.MachineAppearanceProperty;
import fr.frinn.custommachinery.client.screen.BaseScreen;
import fr.frinn.custommachinery.client.screen.creation.appearance.IAppearancePropertyBuilder;
import fr.frinn.custommachinery.client.screen.creation.appearance.ModelSelectionPopup;
import fr.frinn.custommachinery.common.init.Registration;
import fr.frinn.custommachinery.common.util.MachineModelLocation;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.components.Button;
import net.minecraft.network.chat.Component;

import java.util.function.Consumer;
import java.util.function.Supplier;

public class ModelAppearancePropertyBuilder implements IAppearancePropertyBuilder<MachineModelLocation> {

    private final Component title;
    private final MachineAppearanceProperty<MachineModelLocation> type;

    public ModelAppearancePropertyBuilder(Component title, MachineAppearanceProperty<MachineModelLocation> type) {
        this.title = title;
        this.type = type;
    }

    @Override
    public Component title() {
        return this.title;
    }

    @Override
    public MachineAppearanceProperty<MachineModelLocation> getType() {
        return this.type;
    }

    @Override
    public AbstractWidget makeWidget(BaseScreen parent, int x, int y, int width, int height, Supplier<MachineModelLocation> supplier, Consumer<MachineModelLocation> consumer) {
        return Button.builder(this.title, b -> parent.openPopup(new ModelSelectionPopup(parent, supplier, consumer, this.type == Registration.BLOCK_MODEL_PROPERTY.get()))).bounds(x, y, width, height).build();
    }
}
