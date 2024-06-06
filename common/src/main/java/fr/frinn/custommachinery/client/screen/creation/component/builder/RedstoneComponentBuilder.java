package fr.frinn.custommachinery.client.screen.creation.component.builder;

import com.google.common.collect.ImmutableList;
import fr.frinn.custommachinery.api.component.MachineComponentType;
import fr.frinn.custommachinery.client.screen.BaseScreen;
import fr.frinn.custommachinery.client.screen.creation.MachineEditScreen;
import fr.frinn.custommachinery.client.screen.creation.component.ComponentBuilderPopup;
import fr.frinn.custommachinery.client.screen.creation.component.IMachineComponentBuilder;
import fr.frinn.custommachinery.client.screen.popup.PopupScreen;
import fr.frinn.custommachinery.client.screen.widget.IntegerSlider;
import fr.frinn.custommachinery.common.component.RedstoneMachineComponent;
import fr.frinn.custommachinery.common.component.RedstoneMachineComponent.Template;
import fr.frinn.custommachinery.common.init.Registration;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.CycleButton;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.Items;
import org.jetbrains.annotations.Nullable;

import java.util.function.Consumer;

public class RedstoneComponentBuilder implements IMachineComponentBuilder<RedstoneMachineComponent, Template> {

    @Override
    public MachineComponentType<RedstoneMachineComponent> type() {
        return Registration.REDSTONE_MACHINE_COMPONENT.get();
    }

    @Override
    public PopupScreen makePopup(MachineEditScreen parent, @Nullable Template template, Consumer<Template> onFinish) {
        return new RedstoneComponentBuilderPopup(parent, template, onFinish);
    }

    @Override
    public void render(GuiGraphics graphics, int x, int y, int width, int height, Template template) {
        graphics.renderFakeItem(Items.REDSTONE.getDefaultInstance(), x, y + height / 2 - 8);
        graphics.drawString(Minecraft.getInstance().font, "type: " + template.getType().getId().getPath(), x + 25, y + 5, 0, false);
    }

    public static class RedstoneComponentBuilderPopup extends ComponentBuilderPopup<Template> {

        IntegerSlider powerToPause;
        IntegerSlider craftingPowerOutput;
        IntegerSlider idlePowerOutput;
        IntegerSlider erroredPowerOutput;
        IntegerSlider pausedPowerOutput;
        CycleButton<MachineComponentType<?>> comparatorInputType;
        EditBox comparatorInputId;

        public RedstoneComponentBuilderPopup(BaseScreen parent, @Nullable Template template, Consumer<Template> onFinish) {
            super(parent, template, onFinish, Component.translatable("custommachinery.gui.creation.components.redstone.title"));
        }

        @Override
        public Template makeTemplate() {
            return new Template(this.powerToPause.intValue(), this.craftingPowerOutput.intValue(), this.idlePowerOutput.intValue(), this.erroredPowerOutput.intValue(), this.pausedPowerOutput.intValue(), this.comparatorInputType.getValue(), this.comparatorInputId.getValue());
        }

        @Override
        protected void init() {
            super.init();

            //PowerToPause
            this.powerToPause = this.propertyList.add(Component.translatable("custommachinery.gui.creation.components.redstone.powerToPause"), IntegerSlider.builder().bounds(0, 15).defaultValue(1).displayOnlyValue().create(0, 0, 120, 20, Component.translatable("custommachinery.gui.creation.components.redstone.powerToPause")));
            this.baseTemplate().ifPresent(template -> this.powerToPause.setValue(template.powerToPause()));

            //CraftingPowerOutput
            this.craftingPowerOutput = this.propertyList.add(Component.translatable("custommachinery.gui.creation.components.redstone.craftingPowerOutput"), IntegerSlider.builder().bounds(0, 15).defaultValue(0).displayOnlyValue().create(0, 0, 120, 20, Component.translatable("custommachinery.gui.creation.components.redstone.craftingPowerOutput")));
            this.baseTemplate().ifPresent(template -> this.craftingPowerOutput.setValue(template.craftingPowerOutput()));

            //IdlePowerOutput
            this.idlePowerOutput = this.propertyList.add(Component.translatable("custommachinery.gui.creation.components.redstone.idlePowerOutput"), IntegerSlider.builder().bounds(0, 15).defaultValue(0).displayOnlyValue().create(0, 0, 120, 20, Component.translatable("custommachinery.gui.creation.components.redstone.idlePowerOutput")));
            this.baseTemplate().ifPresent(template -> this.idlePowerOutput.setValue(template.idlePowerOutput()));

            //ErroredPowerOutput
            this.erroredPowerOutput = this.propertyList.add(Component.translatable("custommachinery.gui.creation.components.redstone.erroredPowerOutput"), IntegerSlider.builder().bounds(0, 15).defaultValue(0).displayOnlyValue().create(0, 0, 120, 20, Component.translatable("custommachinery.gui.creation.components.redstone.erroredPowerOutput")));
            this.baseTemplate().ifPresent(template -> this.erroredPowerOutput.setValue(template.erroredPowerOutput()));

            //PausedPowerOutput
            this.pausedPowerOutput = this.propertyList.add(Component.translatable("custommachinery.gui.creation.components.redstone.pausedPowerOutput"), IntegerSlider.builder().bounds(0, 15).defaultValue(0).displayOnlyValue().create(0, 0, 120, 20, Component.translatable("custommachinery.gui.creation.components.redstone.pausedPowerOutput")));
            this.baseTemplate().ifPresent(template -> this.pausedPowerOutput.setValue(template.pausedPowerOutput()));

            //ComparatorInputType
            this.comparatorInputType = this.propertyList.add(Component.translatable("custommachinery.gui.creation.components.redstone.comparatorInputType"), CycleButton.<MachineComponentType<?>>builder(type -> Component.literal(type.getId().toString())).displayOnlyValue().withValues(ImmutableList.copyOf(Registration.MACHINE_COMPONENT_TYPE_REGISTRY)).withInitialValue(Registration.ENERGY_MACHINE_COMPONENT.get()).create(0, 0, 150, 20, Component.translatable("custommachinery.gui.creation.components.redstone.comparatorInputType")));
            this.baseTemplate().ifPresent(template -> this.comparatorInputType.setValue(template.comparatorInputType()));

            //ComparatorInputId
            this.comparatorInputId = this.propertyList.add(Component.translatable("custommachinery.gui.creation.components.redstone.comparatorInputId"), new EditBox(this.font, 0, 0, 150, 20, Component.translatable("custommachinery.gui.creation.components.redstone.comparatorInputType")));
            this.baseTemplate().ifPresent(template -> this.comparatorInputId.setValue(template.comparatorInputId()));
        }
    }
}
