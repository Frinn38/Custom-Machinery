package fr.frinn.custommachinery.client.screen.creation.component.builder;


import fr.frinn.custommachinery.api.component.ComponentIOMode;
import fr.frinn.custommachinery.api.component.MachineComponentType;
import fr.frinn.custommachinery.api.utils.Filter;
import fr.frinn.custommachinery.client.screen.BaseScreen;
import fr.frinn.custommachinery.client.screen.creation.MachineEditScreen;
import fr.frinn.custommachinery.client.screen.creation.component.ComponentBuilderPopup;
import fr.frinn.custommachinery.client.screen.creation.component.ComponentConfigBuilderWidget;
import fr.frinn.custommachinery.client.screen.creation.component.IMachineComponentBuilder;
import fr.frinn.custommachinery.client.screen.popup.PopupScreen;
import fr.frinn.custommachinery.client.screen.widget.IntegerSlider;
import fr.frinn.custommachinery.common.component.item.ItemMachineComponent;
import fr.frinn.custommachinery.common.component.item.ItemMachineComponent.Template;
import fr.frinn.custommachinery.common.init.Registration;
import fr.frinn.custommachinery.impl.component.config.IOSideConfig;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Checkbox;
import net.minecraft.client.gui.components.CycleButton;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.components.Tooltip;
import net.minecraft.network.chat.Component;
import net.minecraft.util.FastColor;
import net.minecraft.world.item.Items;
import org.jetbrains.annotations.Nullable;

import java.util.Collections;
import java.util.Optional;
import java.util.function.Consumer;

public class ItemComponentBuilder implements IMachineComponentBuilder<ItemMachineComponent, Template> {

    @Override
    public MachineComponentType<ItemMachineComponent> type() {
        return Registration.ITEM_MACHINE_COMPONENT.get();
    }

    @Override
    public PopupScreen makePopup(MachineEditScreen parent, @Nullable ItemMachineComponent.Template template, Consumer<Template> onFinish) {
        return new ItemComponentBuilderPopup(parent, template, onFinish);
    }

    @Override
    public void render(GuiGraphics graphics, int x, int y, int width, int height, ItemMachineComponent.Template template) {
        graphics.renderFakeItem(Items.DIAMOND.getDefaultInstance(), x, y + height / 2 - 8);
        graphics.drawString(Minecraft.getInstance().font, "type: " + template.getType().getId().getPath(), x + 25, y + 5, 0, false);
        graphics.drawString(Minecraft.getInstance().font, "id: \"" + template.getId() + "\"", x + 25, y + 15, FastColor.ARGB32.color(255, 128, 0, 0), false);
        graphics.drawString(Minecraft.getInstance().font, "mode: " + template.mode, x + 25, y + 25, FastColor.ARGB32.color(255, 0, 0, 128), false);
    }

    public static class ItemComponentBuilderPopup extends ComponentBuilderPopup<Template> {

        protected EditBox id;
        protected CycleButton<ComponentIOMode> mode;
        protected IntegerSlider capacity;
        protected IntegerSlider maxInput;
        protected IntegerSlider maxOutput;
        protected Checkbox locked;
        private IOSideConfig.Template config;

        public ItemComponentBuilderPopup(BaseScreen parent, @Nullable ItemMachineComponent.Template template, Consumer<ItemMachineComponent.Template> onFinish) {
            super(parent, template, onFinish, Component.translatable("custommachinery.gui.creation.components.item.title"));
        }

        @Override
        public ItemMachineComponent.Template makeTemplate() {
            return new ItemMachineComponent.Template(this.id.getValue(), this.mode.getValue(), this.capacity.intValue(), Optional.of(this.maxInput.intValue()), Optional.of(this.maxOutput.intValue()), this.baseTemplate().map(template -> template.filter).orElse(Filter.empty()), Optional.of(this.mode.getValue().getBaseConfig()), this.locked.selected());
        }

        @Override
        protected void init() {
            super.init();

            //ID
            this.id = this.propertyList.add(Component.translatable("custommachinery.gui.creation.components.id"), new EditBox(Minecraft.getInstance().font, 0, 0, 180, 20, Component.translatable("custommachinery.gui.creation.components.id")));
            this.baseTemplate().ifPresentOrElse(template -> this.id.setValue(template.getId()), () -> this.id.setValue("input"));
            this.id.setTooltip(Tooltip.create(Component.translatable("custommachinery.gui.creation.components.id.tooltip")));

            //Mode
            this.mode = this.propertyList.add(Component.translatable("custommachinery.gui.creation.components.mode"), CycleButton.builder(ComponentIOMode::toComponent).displayOnlyValue().withValues(ComponentIOMode.values()).withInitialValue(ComponentIOMode.BOTH).create(0, 0, 180, 20, Component.translatable("custommachinery.gui.creation.components.mode")));
            this.baseTemplate().ifPresent(template -> this.mode.setValue(template.mode));

            //Capacity
            this.capacity = this.propertyList.add(Component.translatable("custommachinery.gui.creation.components.capacity"), IntegerSlider.builder().bounds(0, 64).defaultValue(64).create(0, 0, 180, 20, Component.translatable("custommachinery.gui.creation.components.capacity")));
            this.baseTemplate().ifPresent(template -> this.capacity.setValue(template.capacity));

            //Max input
            this.maxInput = this.propertyList.add(Component.translatable("custommachinery.gui.creation.components.maxInput"), IntegerSlider.builder().bounds(0, 64).defaultValue(64).create(0, 0, 180, 20, Component.translatable("custommachinery.gui.creation.components.maxInput")));
            this.baseTemplate().ifPresent(template -> this.maxInput.setValue(template.maxInput));

            //Max output
            this.maxOutput = this.propertyList.add(Component.translatable("custommachinery.gui.creation.components.maxOutput"), IntegerSlider.builder().bounds(0, 64).defaultValue(64).create(0, 0, 180, 20, Component.translatable("custommachinery.gui.creation.components.maxOutput")));
            this.baseTemplate().ifPresent(template -> this.maxOutput.setValue(template.maxOutput));

            //Locked
            this.locked = this.propertyList.add(Component.translatable("custommachinery.gui.creation.components.item.locked"), Checkbox.builder(Component.translatable("custommachinery.gui.creation.components.item.locked"), this.font).selected(false).build());
            if(this.baseTemplate().map(template -> template.locked).orElse(false) != this.locked.selected())
                this.locked.onPress();
            this.locked.setTooltip(Tooltip.create(Component.translatable("custommachinery.gui.creation.components.item.locked.tooltip")));

            //Config
            this.baseTemplate().ifPresentOrElse(template -> this.config = template.config, () -> this.config = IOSideConfig.Template.DEFAULT_ALL_INPUT);
            this.propertyList.add(Component.translatable("custommachinery.gui.config.component"), ComponentConfigBuilderWidget.make(0, 0, 180, 20, Component.translatable("custommachinery.gui.config.component"), this.parent, () -> this.config, template -> this.config = template));
        }
    }
}
