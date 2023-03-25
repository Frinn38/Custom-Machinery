package fr.frinn.custommachinery.client.render.element;

import com.google.common.collect.Lists;
import fr.frinn.custommachinery.api.component.MachineComponentType;
import fr.frinn.custommachinery.api.guielement.IMachineScreen;
import fr.frinn.custommachinery.common.guielement.DumpGuiElement;
import fr.frinn.custommachinery.common.init.Registration;
import fr.frinn.custommachinery.impl.guielement.TexturedGuiElementWidget;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.network.chat.TranslatableComponent;

import java.util.Iterator;
import java.util.List;
import java.util.Objects;

public class DumpGuiElementWidget extends TexturedGuiElementWidget<DumpGuiElement> {

    private static final Component TITLE = new TranslatableComponent("custommachinery.gui.element.dump.name");
    private final List<Component> tooltips;

    public DumpGuiElementWidget(DumpGuiElement element, IMachineScreen screen) {
        super(element, screen, TITLE);

        this.tooltips = Lists.newArrayList(
                TITLE,
                new TranslatableComponent("custommachinery.gui.element.dump.tooltip", formatComponents(element.getComponents())).withStyle(ChatFormatting.DARK_RED)
        );
    }

    private String formatComponents(List<MachineComponentType<?>> types) {
        StringBuilder builder = new StringBuilder();
        Iterator<MachineComponentType<?>> iterator = types.iterator();
        while(iterator.hasNext()) {
            MachineComponentType<?> type = iterator.next();
            builder.append(Objects.requireNonNull(Registration.MACHINE_COMPONENT_TYPE_REGISTRY.getId(type)).getPath());
            if(iterator.hasNext())
                builder.append(new TextComponent(", "));
        }
        return builder.toString();
    }

    @Override
    public List<Component> getTooltips() {
        return tooltips;
    }

    @Override
    public boolean isClickable() {
        return true;
    }
}
