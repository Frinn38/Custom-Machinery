package fr.frinn.custommachinery.client.screen.creation.appearance.builder;

import fr.frinn.custommachinery.api.machine.MachineAppearanceProperty;
import fr.frinn.custommachinery.client.screen.BaseScreen;
import fr.frinn.custommachinery.client.screen.creation.appearance.IAppearancePropertyBuilder;
import fr.frinn.custommachinery.client.screen.widget.ColorWidget;
import fr.frinn.custommachinery.common.init.CMRegistration;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.network.chat.Component;

import java.util.function.Consumer;
import java.util.function.Supplier;

public class ColorAppearancePropertyBuilder implements IAppearancePropertyBuilder<String> {

    @Override
    public Component title() {
        return Component.translatable("custommachinery.gui.creation.appearance.color");
    }

    @Override
    public MachineAppearanceProperty<String> type() {
        return CMRegistration.COLOR_PROPERTY.get();
    }

    @Override
    public AbstractWidget makeWidget(BaseScreen parent, int x, int y, int width, int height, Supplier<String> supplier, Consumer<String> consumer) {
        return new ColorWidget(x, y, width, height, title(), supplier, consumer, false);
    }
}
