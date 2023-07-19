package fr.frinn.custommachinery.client.render.element;

import com.google.common.collect.Lists;
import fr.frinn.custommachinery.api.component.MachineComponentType;
import fr.frinn.custommachinery.api.guielement.IMachineScreen;
import fr.frinn.custommachinery.common.guielement.DumpGuiElement;
import fr.frinn.custommachinery.common.init.Registration;
import fr.frinn.custommachinery.impl.guielement.TexturedGuiElementWidget;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;

import java.util.Iterator;
import java.util.List;
import java.util.Objects;

public class DumpGuiElementWidget extends TexturedGuiElementWidget<DumpGuiElement> {

    private static final Component TITLE = Component.translatable("custommachinery.gui.element.dump.name");
    private final List<Component> tooltips;

    public DumpGuiElementWidget(DumpGuiElement element, IMachineScreen screen) {
        super(element, screen, TITLE);
        this.tooltips = Lists.newArrayList(
                Component.translatable("custommachinery.gui.element.dump.name"),
                Component.translatable("custommachinery.gui.element.dump.tooltip", formatComponents(element.getComponents())).withStyle(ChatFormatting.DARK_RED)
        );
    }

    private String formatComponents(List<MachineComponentType<?>> types) {
        StringBuilder builder = new StringBuilder();
        Iterator<MachineComponentType<?>> iterator = types.iterator();
        while(iterator.hasNext()) {
            MachineComponentType<?> type = iterator.next();
            builder.append(Objects.requireNonNull(Registration.MACHINE_COMPONENT_TYPE_REGISTRY.getId(type)).getPath());
            if(iterator.hasNext())
                builder.append(Component.literal(", "));
        }
        return builder.toString();
    }

    @Override
    public List<Component> getTooltips() {
        if(this.getElement().getTooltips().isEmpty())
            return this.tooltips;
        return this.getElement().getTooltips();
    }

    @Override
    public boolean isClickable() {
        return true;
    }
}
