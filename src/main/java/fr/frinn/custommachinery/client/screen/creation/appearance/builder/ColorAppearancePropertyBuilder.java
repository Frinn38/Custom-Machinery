package fr.frinn.custommachinery.client.screen.creation.appearance.builder;

import fr.frinn.custommachinery.api.machine.MachineAppearanceProperty;
import fr.frinn.custommachinery.client.screen.BaseScreen;
import fr.frinn.custommachinery.client.screen.creation.appearance.IAppearancePropertyBuilder;
import fr.frinn.custommachinery.client.screen.widget.ColorWidget;
import fr.frinn.custommachinery.common.init.Registration;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.network.chat.Component;

import java.util.function.Consumer;
import java.util.function.Supplier;

public class ColorAppearancePropertyBuilder implements IAppearancePropertyBuilder<Integer> {

    @Override
    public Component title() {
        return Component.translatable("custommachinery.gui.creation.appearance.color");
    }

    @Override
    public MachineAppearanceProperty<Integer> type() {
        return Registration.COLOR_PROPERTY.get();
    }

    @Override
    public AbstractWidget makeWidget(BaseScreen parent, int x, int y, int width, int height, Supplier<Integer> supplier, Consumer<Integer> consumer) {
        return new ColorWidget(x, y, width, height, title(), supplier, consumer, false);
    }
}
