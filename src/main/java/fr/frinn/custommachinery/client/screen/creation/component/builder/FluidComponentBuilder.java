package fr.frinn.custommachinery.client.screen.creation.component.builder;

import fr.frinn.custommachinery.api.component.ComponentIOMode;
import fr.frinn.custommachinery.api.component.MachineComponentType;
import fr.frinn.custommachinery.client.render.FluidRenderer;
import fr.frinn.custommachinery.client.screen.BaseScreen;
import fr.frinn.custommachinery.client.screen.creation.MachineEditScreen;
import fr.frinn.custommachinery.client.screen.creation.component.ComponentBuilderPopup;
import fr.frinn.custommachinery.client.screen.creation.component.ComponentConfigBuilderWidget;
import fr.frinn.custommachinery.client.screen.creation.component.FilterConfigPopup;
import fr.frinn.custommachinery.client.screen.creation.component.FilterConfigPopup.FilterBuilderHelper;
import fr.frinn.custommachinery.client.screen.creation.component.IMachineComponentBuilder;
import fr.frinn.custommachinery.client.screen.popup.PopupScreen;
import fr.frinn.custommachinery.common.component.FluidMachineComponent;
import fr.frinn.custommachinery.common.component.FluidMachineComponent.Template;
import fr.frinn.custommachinery.common.init.Registration;
import fr.frinn.custommachinery.common.util.Filter;
import fr.frinn.custommachinery.impl.component.config.IOSideConfig;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.Checkbox;
import net.minecraft.client.gui.components.CycleButton;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.components.Tooltip;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.FastColor;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.material.Fluids;
import net.neoforged.neoforge.fluids.FluidStack;
import org.jetbrains.annotations.Nullable;

import java.util.function.Consumer;
import java.util.stream.Stream;

public class FluidComponentBuilder implements IMachineComponentBuilder<FluidMachineComponent, Template> {

    @Override
    public MachineComponentType<FluidMachineComponent> type() {
        return Registration.FLUID_MACHINE_COMPONENT.get();
    }

    @Override
    public PopupScreen makePopup(MachineEditScreen parent, @Nullable Template template, Consumer<Template> onFinish) {
        return new FluidComponentBuilderPopup(parent, template, onFinish);
    }

    @Override
    public void render(GuiGraphics graphics, int x, int y, int width, int height, Template template) {
        graphics.renderFakeItem(Items.WATER_BUCKET.getDefaultInstance(), x, y + height / 2 - 8);
        graphics.drawString(Minecraft.getInstance().font, "type: " + template.getType().getId().getPath(), x + 25, y + 5, 0, false);
        graphics.drawString(Minecraft.getInstance().font, "id: \"" + template.getId() + "\"", x + 25, y + 15, FastColor.ARGB32.color(255, 128, 0, 0), false);
        graphics.drawString(Minecraft.getInstance().font, "mode: " + template.mode(), x + 25, y + 25, FastColor.ARGB32.color(255, 0, 0, 128), false);
    }

    public static class FluidComponentBuilderPopup extends ComponentBuilderPopup<Template> {

        private EditBox id;
        private CycleButton<ComponentIOMode> mode;
        private EditBox capacity;
        private EditBox maxInput;
        private EditBox minInput;
        private EditBox maxOutput;
        private EditBox minOutput;
        private Filter<Fluid> filter;
        private Checkbox unique;
        private IOSideConfig.Template config;

        public FluidComponentBuilderPopup(BaseScreen parent, @Nullable Template template, Consumer<Template> onFinish) {
            super(parent, template, onFinish, Component.translatable("custommachinery.gui.creation.components.fluid.title"));
        }

        @Override
        public Template makeTemplate() {
            return new Template(this.id.getValue(), (int)this.parseLong(this.capacity.getValue()), (int)this.parseLong(this.maxInput.getValue()), (int)this.parseLong(this.minInput.getValue()), (int)this.parseLong(this.maxOutput.getValue()), (int)this.parseLong(this.minOutput.getValue()), this.filter, this.mode.getValue(), this.config, this.unique.selected());
        }

        @Override
        public Component canCreate() {
            if(this.id.getValue().isEmpty())
                return Component.translatable("custommachinery.gui.creation.gui.id.missing");
            else if(this.parent instanceof MachineEditScreen screen && screen.getBuilder().getComponents().stream().anyMatch(template -> template.getType() == Registration.FLUID_MACHINE_COMPONENT.get() && this.baseTemplate().map(base -> base != template).orElse(true) && template.getId().equals(this.id.getValue())))
                return Component.translatable("custommachinery.gui.creation.gui.id.duplicate", this.id.getValue());
            else
                return Component.empty();
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
            this.baseTemplate().ifPresent(template -> this.mode.setValue(template.mode()));

            //Capacity
            this.capacity = this.propertyList.add(Component.translatable("custommachinery.gui.creation.components.capacity"), new EditBox(this.font, 0, 0, 180, 20, Component.translatable("custommachinery.gui.creation.components.capacity")));
            this.capacity.setFilter(this::checkLong);
            this.baseTemplate().ifPresentOrElse(template -> this.capacity.setValue("" + template.capacity()), () -> this.capacity.setValue("10000"));

            //Max input
            this.maxInput = this.propertyList.add(Component.translatable("custommachinery.gui.creation.components.maxInput"), new EditBox(this.font, 0, 0, 180, 20, Component.translatable("custommachinery.gui.creation.components.maxInput")));
            this.maxInput.setFilter(this::checkLong);
            this.baseTemplate().ifPresentOrElse(template -> this.maxInput.setValue("" + template.maxInput()), () -> this.maxInput.setValue("10000"));

            //Min input
            this.minInput = this.propertyList.add(Component.translatable("custommachinery.gui.creation.components.minInput"), new EditBox(this.font, 0, 0, 180, 20, Component.translatable("custommachinery.gui.creation.components.minInput")));
            this.minInput.setFilter(this::checkLong);
            this.baseTemplate().ifPresentOrElse(template -> this.minInput.setValue("" + template.minInput()), () -> this.minInput.setValue("0"));

            //Max output
            this.maxOutput = this.propertyList.add(Component.translatable("custommachinery.gui.creation.components.maxOutput"), new EditBox(this.font, 0, 0, 180, 20, Component.translatable("custommachinery.gui.creation.components.maxOutput")));
            this.maxOutput.setFilter(this::checkLong);
            this.baseTemplate().ifPresentOrElse(template -> this.maxOutput.setValue("" + template.maxOutput()), () -> this.maxOutput.setValue("10000"));

            //Min output
            this.minOutput = this.propertyList.add(Component.translatable("custommachinery.gui.creation.components.minOutput"), new EditBox(this.font, 0, 0, 180, 20, Component.translatable("custommachinery.gui.creation.components.minOutput")));
            this.minOutput.setFilter(this::checkLong);
            this.baseTemplate().ifPresentOrElse(template -> this.minOutput.setValue("" + template.minOutput()), () -> this.minOutput.setValue("0"));

            //Filter
            this.baseTemplate().ifPresentOrElse(template -> this.filter = template.filter(), () -> this.filter = Filter.empty());
            this.propertyList.add(Component.translatable("custommachinery.gui.creation.components.filter"), Button.builder(Component.translatable("custommachinery.gui.creation.components.filter"), button -> this.parent.openPopup(new FilterConfigPopup<>(this.parent, () -> this.filter, filter -> this.filter = filter, new FluidFilterHelper()), "Fluid Filter")).size(180, 20).build());

            //Unique
            this.unique = this.propertyList.add(Component.translatable("custommachinery.gui.creation.components.fluid.unique"), Checkbox.builder(Component.translatable("custommachinery.gui.creation.components.fluid.unique"), this.font).selected(false).build());
            if(this.baseTemplate().map(FluidMachineComponent.Template::unique).orElse(false) != this.unique.selected())
                this.unique.onPress();

            //Config
            this.baseTemplate().ifPresentOrElse(template -> this.config = template.config(), () -> this.config = IOSideConfig.Template.DEFAULT_ALL_INPUT);
            this.propertyList.add(Component.translatable("custommachinery.gui.config.component"), ComponentConfigBuilderWidget.make(0, 0, 180, 20, Component.translatable("custommachinery.gui.config.component"), this.parent, () -> this.config, template -> this.config = template));
        }
    }

    private static class FluidFilterHelper implements FilterBuilderHelper<Fluid> {

        @Override
        public void renderSingle(Fluid single, GuiGraphics graphics, int mouseX, int mouseY, float partialTicks) {
            FluidRenderer.renderFluid(graphics.pose(), 0, 0, 16, 16, new FluidStack(single, 1), 1);
        }

        @Override
        public Component tooltip(Fluid single) {
            return single.getFluidType().getDescription();
        }

        @Override
        public Registry<Fluid> registry() {
            return BuiltInRegistries.FLUID;
        }

        @Override
        public Stream<ResourceLocation> getAll() {
            return registry().entrySet().stream().filter(entry -> entry.getValue().defaultFluidState().isSource()).map(entry -> entry.getKey().location());
        }

        @Override
        public Fluid defaultValue() {
            return Fluids.EMPTY;
        }
    }
}
