package fr.frinn.custommachinery.client.screen.creation.appearance.builder;

import fr.frinn.custommachinery.api.machine.MachineAppearanceProperty;
import fr.frinn.custommachinery.client.screen.BaseScreen;
import fr.frinn.custommachinery.client.screen.creation.appearance.IAppearancePropertyBuilder;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.network.chat.Component;

import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;

public class TextAppearancePropertyBuilder<T> implements IAppearancePropertyBuilder<T> {

    private final Component title;
    private final MachineAppearanceProperty<T> type;
    private final Function<String, T> parser;
    private final Function<T, String> toString;

    public TextAppearancePropertyBuilder(Component title, MachineAppearanceProperty<T> type, Function<String, T> parser, Function<T, String> toString) {
        this.title = title;
        this.type = type;
        this.parser = parser;
        this.toString = toString;
    }

    @Override
    public Component title() {
        return this.title;
    }

    @Override
    public MachineAppearanceProperty<T> getType() {
        return this.type;
    }

    @Override
    public AbstractWidget makeWidget(BaseScreen parent, int x, int y, int width, int height, Supplier<T> supplier, Consumer<T> consumer) {
        EditBox box = new EditBox(Minecraft.getInstance().font, x, y, width, height, this.title);
        box.setValue(this.toString.apply(supplier.get()));
        box.setResponder(s -> consumer.accept(this.parser.apply(s)));
        return box;
    }
}
