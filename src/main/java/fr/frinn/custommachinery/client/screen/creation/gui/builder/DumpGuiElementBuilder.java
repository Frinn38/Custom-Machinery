package fr.frinn.custommachinery.client.screen.creation.gui.builder;

import fr.frinn.custommachinery.api.component.MachineComponentType;
import fr.frinn.custommachinery.api.guielement.GuiElementType;
import fr.frinn.custommachinery.client.screen.BaseScreen;
import fr.frinn.custommachinery.client.screen.creation.MachineEditScreen;
import fr.frinn.custommachinery.client.screen.creation.gui.GuiElementBuilderPopup;
import fr.frinn.custommachinery.client.screen.creation.gui.IGuiElementBuilder;
import fr.frinn.custommachinery.client.screen.creation.gui.MutableProperties;
import fr.frinn.custommachinery.client.screen.popup.PopupScreen;
import fr.frinn.custommachinery.common.guielement.DumpGuiElement;
import fr.frinn.custommachinery.common.init.CMRegistration;
import fr.frinn.custommachinery.impl.guielement.AbstractGuiElement.Properties;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.components.StringWidget;
import net.minecraft.client.gui.components.Tooltip;
import net.minecraft.client.gui.layouts.GridLayout.RowHelper;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.function.Consumer;
import java.util.stream.Collectors;

public class DumpGuiElementBuilder implements IGuiElementBuilder<DumpGuiElement> {

    @Override
    public GuiElementType<DumpGuiElement> type() {
        return CMRegistration.DUMP_GUI_ELEMENT.get();
    }

    @Override
    public DumpGuiElement make(Properties properties, @Nullable DumpGuiElement from) {
        if(from != null)
            return new DumpGuiElement(properties, from.getComponents(), from.getTanks());
        else
            return new DumpGuiElement(properties, Collections.singletonList(CMRegistration.FLUID_MACHINE_COMPONENT.get()), Collections.emptyList());
    }

    @Override
    public PopupScreen makeConfigPopup(MachineEditScreen parent, MutableProperties properties, @Nullable DumpGuiElement from, Consumer<DumpGuiElement> onFinish) {
        return new DumpGuiElementBuilderPopup(parent, properties, from, onFinish);
    }

    public static class DumpGuiElementBuilderPopup extends GuiElementBuilderPopup<DumpGuiElement> {

        private EditBox components;
        private EditBox tanks;

        public DumpGuiElementBuilderPopup(BaseScreen parent, MutableProperties properties, @Nullable DumpGuiElement from, Consumer<DumpGuiElement> onFinish) {
            super(parent, properties, from, onFinish);
        }

        @Override
        public DumpGuiElement makeElement() {
            List<MachineComponentType<?>> components = new ArrayList<>();
            for(String s : this.components.getValue().split(",")) {
                Identifier id = Identifier.tryParse(s);
                if (id == null)
                    continue;
                CMRegistration.MACHINE_COMPONENT_TYPE_REGISTRY.get(id).ifPresent(type -> components.add(type.value()));
            }
            List<String> tanks = Arrays.stream(this.tanks.getValue().split(",")).filter(s -> !s.isEmpty()).toList();
            return new DumpGuiElement(this.properties.build(), components, tanks);
        }

        @Override
        public void addWidgets(RowHelper row) {
            this.addTexture(row, Component.translatable("custommachinery.gui.creation.gui.texture"), this.properties::setTexture, DumpGuiElement.BASE_TEXTURE);
            this.addTexture(row, Component.translatable("custommachinery.gui.creation.gui.texture_hovered"), this.properties::setTextureHovered, DumpGuiElement.BASE_TEXTURE_HOVERED);
            this.addPriority(row);
            row.addChild(new StringWidget(Component.translatable("custommachinery.gui.creation.gui.dump.components"), this.font));
            this.components = row.addChild(new EditBox(this.font, 0, 0, 100, 20, Component.translatable("custommachinery.gui.creation.gui.dump.components")));
            this.components.setValue(this.baseElement == null ? CMRegistration.FLUID_GUI_ELEMENT.get().getId().toString() : this.listToString(this.baseElement.getComponents().stream().map(type -> type.getId().toString()).toList()));
            this.components.setTooltip(Tooltip.create(Component.translatable("custommachinery.gui.creation.gui.dump.components.tooltip")));
            row.addChild(new StringWidget(Component.translatable("custommachinery.gui.creation.gui.dump.tanks"), this.font));
            this.tanks = row.addChild(new EditBox(this.font, 0, 0, 100, 20, Component.translatable("custommachinery.gui.creation.gui.dump.tanks")));
            this.tanks.setValue(this.baseElement == null ? "" : this.listToString(this.baseElement.getTanks()));
            this.tanks.setTooltip(Tooltip.create(Component.translatable("custommachinery.gui.creation.gui.dump.tanks.tooltip")));
        }

        private <T> String listToString(List<T> list) {
            StringBuilder builder = new StringBuilder();
            Iterator<T> iterator = list.iterator();
            while (iterator.hasNext()) {
                T next = iterator.next();
                builder.append(next);
                if(iterator.hasNext())
                    builder.append(",");
            }
            return builder.toString();
        }
    }
}
