package fr.frinn.custommachinery.client.screen.creation.appearance.builder;

import fr.frinn.custommachinery.api.machine.MachineAppearanceProperty;
import fr.frinn.custommachinery.client.screen.BaseScreen;
import fr.frinn.custommachinery.client.screen.creation.appearance.IAppearancePropertyBuilder;
import fr.frinn.custommachinery.client.screen.popup.PopupScreen;
import fr.frinn.custommachinery.client.screen.widget.FloatSlider;
import fr.frinn.custommachinery.client.screen.widget.IntegerSlider;
import fr.frinn.custommachinery.client.screen.widget.SoundEditBox;
import fr.frinn.custommachinery.common.init.Registration;
import fr.frinn.custommachinery.common.util.sound.AmbientSound;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.Checkbox;
import net.minecraft.client.gui.components.CycleButton;
import net.minecraft.client.gui.components.StringWidget;
import net.minecraft.client.gui.components.Tooltip;
import net.minecraft.client.gui.layouts.GridLayout;
import net.minecraft.client.gui.layouts.GridLayout.RowHelper;
import net.minecraft.client.gui.layouts.LayoutSettings;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundSource;

import java.util.Objects;
import java.util.function.Consumer;
import java.util.function.Supplier;

public class AmbientSoundAppearancePropertyBuilder implements IAppearancePropertyBuilder<AmbientSound> {

    @Override
    public Component title() {
        return Component.translatable("custommachinery.gui.creation.appearance.ambient_sound");
    }

    @Override
    public MachineAppearanceProperty<AmbientSound> type() {
        return Registration.AMBIENT_SOUND_PROPERTY.get();
    }

    @Override
    public AbstractWidget makeWidget(BaseScreen parent, int x, int y, int width, int height, Supplier<AmbientSound> supplier, Consumer<AmbientSound> consumer) {
        return Button.builder(this.title(), button ->
            parent.openPopup(new AmbientSoundEditPopup(parent, 205, 240, supplier, consumer), this.title().getString())
        ).bounds(x, y, width, height).tooltip(Tooltip.create(Component.translatable("custommachinery.gui.creation.appearance.ambient_sound.tooltip"))).build();
    }

    private static class AmbientSoundEditPopup extends PopupScreen {

        private final Supplier<AmbientSound> supplier;
        private final Consumer<AmbientSound> consumer;

        private SoundEditBox sound;
        private FloatSlider volume;
        private FloatSlider pitch;
        private CycleButton<SoundSource> source;
        private Checkbox loop;
        private Checkbox attenuation;
        private IntegerSlider delay;
        private Checkbox relative;

        public AmbientSoundEditPopup(BaseScreen parent, int xSize, int ySize, Supplier<AmbientSound> supplier, Consumer<AmbientSound> consumer) {
            super(parent, xSize, ySize);
            this.supplier = supplier;
            this.consumer = consumer;
        }

        @Override
        protected void init() {
            super.init();
            GridLayout layout = new GridLayout(this.x, this.y);
            layout.defaultCellSetting().paddingTop(5).paddingHorizontal(5);
            LayoutSettings right = layout.newCellSettings().alignHorizontallyLeft();
            LayoutSettings center = layout.newCellSettings().alignHorizontallyCenter();
            LayoutSettings title = layout.newCellSettings().alignVerticallyMiddle();
            RowHelper row = layout.createRowHelper(3);

            //Title
            row.addChild(new StringWidget(this.xSize - 10, this.font.lineHeight, Component.translatable("custommachinery.gui.creation.appearance.ambient_sound"), this.font), 3);

            //Sound
            row.addChild(new StringWidget(Component.translatable("custommachinery.gui.creation.appearance.ambient_sound.sound"), this.font), title);
            this.sound = row.addChild(new SoundEditBox(0, 0, 120, 20, Component.translatable("custommachinery.gui.creation.appearance.ambient_sound.sound")), 2, right);
            if(!this.supplier.get().sound().getLocation().getPath().isEmpty())
                this.sound.setValue(this.supplier.get().sound().getLocation().toString());

            //Volume
            row.addChild(new StringWidget(Component.translatable("custommachinery.gui.creation.appearance.interaction_sound.volume"), this.font), title);
            this.volume = row.addChild(FloatSlider.builder().bounds(0, 5).displayOnlyValue().defaultValue(this.supplier.get().volume()).decimalsToShow(2).create(0, 0, 120, 20, Component.translatable("custommachinery.gui.creation.appearance.interaction_sound.volume")), 2, right);

            //Pitch
            row.addChild(new StringWidget(Component.translatable("custommachinery.gui.creation.appearance.interaction_sound.pitch"), this.font), title);
            this.pitch = row.addChild(FloatSlider.builder().bounds(0, 5).displayOnlyValue().defaultValue(this.supplier.get().pitch()).decimalsToShow(2).create(0, 0, 120, 20, Component.translatable("custommachinery.gui.creation.appearance.interaction_sound.pitch")), 2, right);

            //Source
            row.addChild(new StringWidget(Component.translatable("custommachinery.gui.creation.appearance.ambient_sound.source"), this.font), title);
            this.source = row.addChild(CycleButton.<SoundSource>builder(source -> Component.translatable("soundCategory." + source.getName())).displayOnlyValue().withValues(SoundSource.values()).withInitialValue(this.supplier.get().source()).create(0, 0, 120, 20, Component.translatable("custommachinery.gui.creation.appearance.ambient_sound.source")), 2, right);

            //Loop
            row.addChild(new StringWidget(Component.translatable("custommachinery.gui.creation.appearance.ambient_sound.loop"), this.font), title);
            this.loop = row.addChild(Checkbox.builder(Component.empty(), this.font).selected(this.supplier.get().loop()).build(), 2, right);

            //Attenuation
            row.addChild(new StringWidget(Component.translatable("custommachinery.gui.creation.appearance.ambient_sound.attenuation"), this.font), title);
            this.attenuation = row.addChild(Checkbox.builder(Component.empty(), this.font).selected(this.supplier.get().attenuation()).build(), 2, right);

            //Delay
            row.addChild(new StringWidget(Component.translatable("custommachinery.gui.creation.appearance.ambient_sound.delay"), this.font), title);
            this.delay = row.addChild(IntegerSlider.builder().bounds(0, 100).displayOnlyValue().defaultValue(this.supplier.get().delay()).create(0, 0, 120, 20, Component.translatable("custommachinery.gui.creation.appearance.ambient_sound.delay")), 2, right);

            //Relative
            row.addChild(new StringWidget(Component.translatable("custommachinery.gui.creation.appearance.ambient_sound.relative"), this.font), title);
            this.relative = row.addChild(Checkbox.builder(Component.empty(), this.font).selected(this.supplier.get().relative()).build(), 2, right);

            row.addChild(Button.builder(Component.translatable("custommachinery.gui.config.close"), button -> this.parent.closePopup(this)).size(50, 20).build(), 3, center);

            layout.arrangeElements();
            layout.visitWidgets(this::addRenderableWidget);
        }

        @Override
        public void closed() {
            ResourceLocation soundLoc = ResourceLocation.tryParse(this.sound.getValue());
            SoundEvent event = SoundEvent.createVariableRangeEvent(Objects.requireNonNullElseGet(soundLoc, () -> ResourceLocation.withDefaultNamespace("")));
            this.consumer.accept(new AmbientSound(event, this.volume.floatValue(), this.pitch.floatValue(), this.source.getValue(), this.loop.selected(), this.attenuation.selected(), this.delay.intValue(), this.relative.selected()));
        }
    }
}
