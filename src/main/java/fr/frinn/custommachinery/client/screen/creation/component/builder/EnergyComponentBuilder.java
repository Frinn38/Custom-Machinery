package fr.frinn.custommachinery.client.screen.creation.component.builder;

import fr.frinn.custommachinery.api.component.MachineComponentType;
import fr.frinn.custommachinery.client.screen.BaseScreen;
import fr.frinn.custommachinery.client.screen.creation.MachineEditScreen;
import fr.frinn.custommachinery.client.screen.creation.component.ComponentBuilderPopup;
import fr.frinn.custommachinery.client.screen.creation.component.ComponentConfigBuilderWidget;
import fr.frinn.custommachinery.client.screen.creation.component.IMachineComponentBuilder;
import fr.frinn.custommachinery.client.screen.popup.PopupScreen;
import fr.frinn.custommachinery.common.component.EnergyMachineComponent;
import fr.frinn.custommachinery.common.component.EnergyMachineComponent.Template;
import fr.frinn.custommachinery.common.init.Registration;
import fr.frinn.custommachinery.impl.component.config.IOSideConfig;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.Items;
import org.jetbrains.annotations.Nullable;

import java.util.function.Consumer;

public class EnergyComponentBuilder implements IMachineComponentBuilder<EnergyMachineComponent, Template> {

    @Override
    public MachineComponentType<EnergyMachineComponent> type() {
        return Registration.ENERGY_MACHINE_COMPONENT.get();
    }

    @Override
    public PopupScreen makePopup(MachineEditScreen parent, @Nullable Template template, Consumer<Template> onFinish) {
        return new EnergyComponentBuilderPopup(parent, template, onFinish);
    }

    @Override
    public void render(GuiGraphics graphics, int x, int y, int width, int height, Template template) {
        graphics.renderFakeItem(Items.LANTERN.getDefaultInstance(), x, y + height / 2 - 8);
        graphics.drawString(Minecraft.getInstance().font, "type: " + template.getType().getId().getPath(), x + 25, y + 5, 0, false);
    }

    public static class EnergyComponentBuilderPopup extends ComponentBuilderPopup<Template> {

        private EditBox capacity;
        private EditBox maxInput;
        private EditBox maxOutput;
        private IOSideConfig.Template config;

        public EnergyComponentBuilderPopup(BaseScreen parent, @Nullable Template template, Consumer<Template> onFinish) {
            super(parent, template, onFinish, Component.translatable("custommachinery.gui.creation.components.energy.title"));
        }

        @Override
        public Template makeTemplate() {
            return new Template(this.parseLong(this.capacity.getValue()), this.parseLong(this.maxInput.getValue()), this.parseLong(this.maxOutput.getValue()), this.config);
        }

        @Override
        protected void init() {
            super.init();

            //Capacity
            this.capacity = this.propertyList.add(Component.translatable("custommachinery.gui.creation.components.capacity"), new EditBox(this.font, 0, 0, 180, 20, Component.translatable("custommachinery.gui.creation.components.capacity")));
            this.capacity.setFilter(this::checkLong);
            this.baseTemplate().ifPresentOrElse(template -> this.capacity.setValue("" + template.capacity()), () -> this.capacity.setValue("10000"));

            //Max input
            this.maxInput = this.propertyList.add(Component.translatable("custommachinery.gui.creation.components.maxInput"), new EditBox(this.font, 0, 0, 180, 20, Component.translatable("custommachinery.gui.creation.components.maxInput")));
            this.maxInput.setFilter(this::checkLong);
            this.baseTemplate().ifPresentOrElse(template -> this.maxInput.setValue("" + template.maxInput()), () -> this.maxInput.setValue("10000"));

            //Max output
            this.maxOutput = this.propertyList.add(Component.translatable("custommachinery.gui.creation.components.maxOutput"), new EditBox(this.font, 0, 0, 180, 20, Component.translatable("custommachinery.gui.creation.components.maxOutput")));
            this.maxOutput.setFilter(this::checkLong);
            this.baseTemplate().ifPresentOrElse(template -> this.maxOutput.setValue("" + template.maxOutput()), () -> this.maxOutput.setValue("10000"));

            //Config
            this.baseTemplate().ifPresentOrElse(template -> this.config = template.config(), () -> this.config = IOSideConfig.Template.DEFAULT_ALL_INPUT);
            this.propertyList.add(Component.translatable("custommachinery.gui.config.component"), ComponentConfigBuilderWidget.make(0, 0, 180, 20, Component.translatable("custommachinery.gui.config.component"), this.parent, () -> this.config, template -> this.config = template));
        }
    }
}
