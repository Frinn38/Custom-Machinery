package fr.frinn.custommachinery.client.screen.creation.appearance.builder;

import fr.frinn.custommachinery.api.machine.MachineAppearanceProperty;
import fr.frinn.custommachinery.client.screen.BaseScreen;
import fr.frinn.custommachinery.client.screen.creation.appearance.IAppearancePropertyBuilder;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.components.Checkbox;
import net.minecraft.client.gui.components.Tooltip;
import net.minecraft.network.chat.Component;
import org.jetbrains.annotations.Nullable;

import java.util.function.Consumer;
import java.util.function.Supplier;

public record BooleanAppearancePropertyBuilder(Component title, MachineAppearanceProperty<Boolean> type, @Nullable Component tooltip) implements IAppearancePropertyBuilder<Boolean> {

    @Override
    public AbstractWidget makeWidget(BaseScreen parent, int x, int y, int width, int height, Supplier<Boolean> supplier, Consumer<Boolean> consumer) {
        Checkbox.Builder checkbox = Checkbox.builder(this.title, Minecraft.getInstance().font).pos(x, y).onValueChange((box, selected) -> consumer.accept(selected));
        if(this.tooltip != null)
            checkbox.tooltip(Tooltip.create(this.tooltip));
        if(supplier.get())
            checkbox.selected(true);
        return checkbox.build();
    }
}
