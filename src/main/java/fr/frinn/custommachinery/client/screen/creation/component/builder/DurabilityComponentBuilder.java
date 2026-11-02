package fr.frinn.custommachinery.client.screen.creation.component.builder;

import com.mojang.brigadier.exceptions.CommandSyntaxException;
import fr.frinn.custommachinery.api.component.MachineComponentType;
import fr.frinn.custommachinery.client.screen.BaseScreen;
import fr.frinn.custommachinery.client.screen.creation.MachineEditScreen;
import fr.frinn.custommachinery.client.screen.creation.component.ComponentBuilderPopup;
import fr.frinn.custommachinery.client.screen.creation.component.IMachineComponentBuilder;
import fr.frinn.custommachinery.client.screen.popup.PopupScreen;
import fr.frinn.custommachinery.client.screen.widget.IntegerEditBox;
import fr.frinn.custommachinery.common.component.DurabilityMachineComponent;
import fr.frinn.custommachinery.common.component.DurabilityMachineComponent.OnBreak;
import fr.frinn.custommachinery.common.component.DurabilityMachineComponent.Template;
import fr.frinn.custommachinery.common.init.Registration;
import fr.frinn.custommachinery.common.util.PartialBlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Checkbox;
import net.minecraft.client.gui.components.CycleButton;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.Items;
import org.jetbrains.annotations.Nullable;

import java.util.Locale;
import java.util.function.Consumer;

public class DurabilityComponentBuilder implements IMachineComponentBuilder<DurabilityMachineComponent, Template> {

    @Override
    public MachineComponentType<DurabilityMachineComponent> type() {
        return Registration.DURABILITY_MACHINE_COMPONENT.get();
    }

    @Override
    public PopupScreen makePopup(MachineEditScreen parent, @Nullable DurabilityMachineComponent.Template template, Consumer<Template> onFinish) {
        return new DurabilityComponentBuilderPopup(parent, template, onFinish);
    }

    @Override
    public void render(GuiGraphics graphics, int x, int y, int width, int height, Template template) {
        graphics.renderFakeItem(Items.ANVIL.getDefaultInstance(), x, y + height / 2 - 8);
        graphics.drawString(Minecraft.getInstance().font, "type: " + template.getType().getId().getPath(), x + 25, y + 5, 0, false);
    }

    public static class DurabilityComponentBuilderPopup extends ComponentBuilderPopup<Template> {

        private IntegerEditBox max;
        private Checkbox showItem;
        private CycleButton<DurabilityMachineComponent.OnBreak> onBreak;
        private EditBox replace;

        public DurabilityComponentBuilderPopup(BaseScreen parent, @Nullable Template template, Consumer<Template> onFinish) {
            super(parent, template, onFinish, Component.translatable("custommachinery.gui.creation.components.durability.title"));
        }

        @Override
        public Template makeTemplate() {
            PartialBlockState state = this.getFromString(this.replace.getValue());
            return new Template(this.max.intValue(), this.showItem.selected(), this.onBreak.getValue(), this.replace.getValue().isBlank() || state == null ? PartialBlockState.AIR : state);
        }

        @Override
        protected void init() {
            super.init();

            //Max durability
            this.max = this.propertyList.add(Component.translatable("custommachinery.gui.creation.components.durability.max"), new IntegerEditBox(this.font, 0, 0, 80, 20, Component.translatable("custommachinery.gui.creation.components.capacity")));
            this.baseTemplate().ifPresentOrElse(template -> this.max.setIntValue(template.maxDurability()), () -> this.max.setIntValue(100));

            //Show item durability
            this.showItem = this.propertyList.add(Component.translatable("custommachinery.gui.creation.components.durability.item"), Checkbox.builder(Component.empty(), this.font).selected(this.baseTemplate().map(Template::showItemDurability).orElse(true)).build());

            //On break
            this.onBreak = this.propertyList.add(Component.translatable("custommachinery.gui.creation.components.durability.break"), CycleButton.<OnBreak>builder(value -> Component.literal(value.name().toLowerCase(Locale.ROOT))).withValues(OnBreak.values()).withInitialValue(this.baseTemplate().map(Template::onBreak).orElse(OnBreak.NOTHING)).displayOnlyValue().create(0, 0, 100, 20, Component.empty()));

            //Replace
            this.replace = this.propertyList.add(Component.translatable("custommachinery.gui.creation.components.durability.replace"), new EditBox(this.font, 0, 0, 150, 20, Component.empty()));
            this.baseTemplate().ifPresent(template -> this.replace.setValue(template.replace().toString()));
        }

        @Nullable
        private PartialBlockState getFromString(String s) {
            try {
                return PartialBlockState.of(s);
            } catch (CommandSyntaxException e) {
                return null;
            }
        }
    }
}
