package fr.frinn.custommachinery.client.screen.creation.appearance.builder;

import fr.frinn.custommachinery.api.machine.MachineAppearanceProperty;
import fr.frinn.custommachinery.client.screen.BaseScreen;
import fr.frinn.custommachinery.client.screen.creation.appearance.IAppearancePropertyBuilder;
import fr.frinn.custommachinery.common.init.Registration;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.network.chat.Component;

import java.util.Arrays;
import java.util.Locale;
import java.util.Objects;
import java.util.function.Consumer;
import java.util.function.Supplier;

public class ColorAppearancePropertyBuilder implements IAppearancePropertyBuilder<Integer> {

    @Override
    public Component title() {
        return Component.literal("Color");
    }

    @Override
    public MachineAppearanceProperty<Integer> getType() {
        return Registration.COLOR_PROPERTY.get();
    }

    @Override
    public AbstractWidget makeWidget(BaseScreen parent, int x, int y, int width, int height, Supplier<Integer> supplier, Consumer<Integer> consumer) {
        EditBox editBox = new EditBox(Minecraft.getInstance().font, x, y, width, height, title());
        String color = "" + supplier.get();
        color = Arrays.stream(ChatFormatting.values()).filter(format -> format.isColor() && Objects.equals(format.getColor(), supplier.get())).findFirst().map(format -> format.name().toLowerCase(Locale.ROOT)).orElse(color);
        editBox.setValue(color);
        editBox.setResponder(s -> {
            if(s.isEmpty()) {
                consumer.accept(0xFFFFFF);
                return;
            }
            try {
                consumer.accept(Integer.parseInt(s));
                return;
            } catch (NumberFormatException ignored) {}
            try {
                consumer.accept(ChatFormatting.valueOf(s.toUpperCase(Locale.ROOT)).getColor());
            } catch (Throwable ignored) {}
        });
        /*
        editBox.setFilter(s -> {
            try {
                Integer.parseInt(s);
                return true;
            } catch (NumberFormatException ignored) {}
            try {
                ChatFormatting.valueOf(s.toUpperCase(Locale.ROOT));
                return true;
            } catch (IllegalArgumentException ignored) {}
            return false;
        });
         */
        return editBox;
    }
}
