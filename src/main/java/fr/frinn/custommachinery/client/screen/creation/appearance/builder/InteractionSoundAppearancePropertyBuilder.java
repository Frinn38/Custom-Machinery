package fr.frinn.custommachinery.client.screen.creation.appearance.builder;

import fr.frinn.custommachinery.api.machine.MachineAppearanceProperty;
import fr.frinn.custommachinery.client.screen.BaseScreen;
import fr.frinn.custommachinery.client.screen.creation.appearance.IAppearancePropertyBuilder;
import fr.frinn.custommachinery.client.screen.popup.PopupScreen;
import fr.frinn.custommachinery.client.screen.widget.FloatSlider;
import fr.frinn.custommachinery.client.screen.widget.SoundEditBox;
import fr.frinn.custommachinery.common.init.Registration;
import fr.frinn.custommachinery.common.util.CMSoundType;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.StringWidget;
import net.minecraft.client.gui.components.Tooltip;
import net.minecraft.client.gui.layouts.GridLayout;
import net.minecraft.client.gui.layouts.GridLayout.RowHelper;
import net.minecraft.client.gui.layouts.LayoutSettings;
import net.minecraft.client.particle.SpriteSet;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvent;

import java.util.Objects;
import java.util.function.Consumer;
import java.util.function.Supplier;

public class InteractionSoundAppearancePropertyBuilder implements IAppearancePropertyBuilder<CMSoundType> {
    @Override
    public Component title() {
        return Component.translatable("custommachinery.gui.creation.appearance.interaction_sound");
    }

    @Override
    public MachineAppearanceProperty<CMSoundType> type() {
        return Registration.INTERACTION_SOUND_PROPERTY.get();
    }

    @Override
    public AbstractWidget makeWidget(BaseScreen parent, int x, int y, int width, int height, Supplier<CMSoundType> supplier, Consumer<CMSoundType> consumer) {
        return Button.builder(this.title(), button -> {
            parent.openPopup(new InteractionSoundEditPopup(parent, 205, 220, supplier, consumer), this.title().getString());
        }).bounds(x, y, width, height).tooltip(Tooltip.create(Component.translatable("custommachinery.gui.creation.appearance.interaction_sound.tooltip"))).build();
    }

    private static class InteractionSoundEditPopup extends PopupScreen {

        private final Supplier<CMSoundType> supplier;
        private final Consumer<CMSoundType> consumer;

        private FloatSlider volume;
        private FloatSlider pitch;
        private SoundEditBox breakSound;
        private SoundEditBox stepSound;
        private SoundEditBox placeSound;
        private SoundEditBox hitSound;
        private SoundEditBox fallSound;

        public InteractionSoundEditPopup(BaseScreen parent, int xSize, int ySize, Supplier<CMSoundType> supplier, Consumer<CMSoundType> consumer) {
            super(parent, xSize, ySize);
            this.supplier = supplier;
            this.consumer = consumer;
        }

        @Override
        protected void init() {
            super.init();
            GridLayout layout = new GridLayout(this.x, this.y);
            layout.defaultCellSetting().paddingTop(5).paddingHorizontal(5);
            LayoutSettings center = layout.newCellSettings().alignHorizontallyCenter();
            LayoutSettings right = layout.newCellSettings().alignHorizontallyLeft();
            LayoutSettings title = layout.newCellSettings().alignVerticallyMiddle();
            RowHelper row = layout.createRowHelper(3);

            //Title
            row.addChild(new StringWidget(this.xSize - 10, this.font.lineHeight, Component.translatable("custommachinery.gui.creation.appearance.interaction_sound"), this.font), 3);

            //Volume
            row.addChild(new StringWidget(Component.translatable("custommachinery.gui.creation.appearance.interaction_sound.volume"), this.font), title);
            this.volume = row.addChild(FloatSlider.builder().bounds(0, 5).displayOnlyValue().defaultValue(this.supplier.get().getVolume()).decimalsToShow(2).create(0, 0, 120, 20, Component.translatable("custommachinery.gui.creation.appearance.interaction_sound.volume")), 2, right);

            //Pitch
            row.addChild(new StringWidget(Component.translatable("custommachinery.gui.creation.appearance.interaction_sound.pitch"), this.font), title);
            this.pitch = row.addChild(FloatSlider.builder().bounds(0, 5).displayOnlyValue().defaultValue(this.supplier.get().getPitch()).decimalsToShow(2).create(0, 0, 120, 20, Component.translatable("custommachinery.gui.creation.appearance.interaction_sound.pitch")), 2, right);

            //Break
            row.addChild(new StringWidget(Component.translatable("custommachinery.gui.creation.appearance.interaction_sound.break"), this.font), title);
            this.breakSound = row.addChild(new SoundEditBox(0, 0, 120, 20, Component.translatable("custommachinery.gui.creation.appearance.interaction_sound.break")), 2, right);
            if(!this.supplier.get().getBreakSound().getLocation().getPath().isEmpty())
                this.breakSound.setValue(this.supplier.get().getBreakSound().getLocation().toString());

            //Step
            row.addChild(new StringWidget(Component.translatable("custommachinery.gui.creation.appearance.interaction_sound.step"), this.font), title);
            this.stepSound = row.addChild(new SoundEditBox(0, 0, 120, 20, Component.translatable("custommachinery.gui.creation.appearance.interaction_sound.step")), 2, right);
            if(!this.supplier.get().getStepSound().getLocation().getPath().isEmpty())
                this.stepSound.setValue(this.supplier.get().getStepSound().getLocation().toString());

            //Place
            row.addChild(new StringWidget(Component.translatable("custommachinery.gui.creation.appearance.interaction_sound.place"), this.font), title);
            this.placeSound = row.addChild(new SoundEditBox(0, 0, 120, 20, Component.translatable("custommachinery.gui.creation.appearance.interaction_sound.place")), 2, right);
            if(!this.supplier.get().getPlaceSound().getLocation().getPath().isEmpty())
                this.placeSound.setValue(this.supplier.get().getPlaceSound().getLocation().toString());

            //Hit
            row.addChild(new StringWidget(Component.translatable("custommachinery.gui.creation.appearance.interaction_sound.hit"), this.font), title);
            this.hitSound = row.addChild(new SoundEditBox(0, 0, 120, 20, Component.translatable("custommachinery.gui.creation.appearance.interaction_sound.hit")), 2, right);
            if(!this.supplier.get().getHitSound().getLocation().getPath().isEmpty())
                this.hitSound.setValue(this.supplier.get().getHitSound().getLocation().toString());

            //Fall
            row.addChild(new StringWidget(Component.translatable("custommachinery.gui.creation.appearance.interaction_sound.fall"), this.font), title);
            this.fallSound = row.addChild(new SoundEditBox(0, 0, 120, 20, Component.translatable("custommachinery.gui.creation.appearance.interaction_sound.fall")), 2, right);
            if(!this.supplier.get().getFallSound().getLocation().getPath().isEmpty())
             this.fallSound.setValue(this.supplier.get().getFallSound().getLocation().toString());

            row.addChild(Button.builder(Component.translatable("custommachinery.gui.config.close"), button -> this.parent.closePopup(this)).size(50, 20).build(), 3, center);

            layout.arrangeElements();
            layout.visitWidgets(this::addRenderableWidget);
        }

        @Override
        public void closed() {
            this.consumer.accept(new CMSoundType(this.volume.floatValue(), this.pitch.floatValue(), getSound(this.breakSound), getSound(this.stepSound), getSound(this.placeSound), getSound(this.hitSound), getSound(this.fallSound)));
        }

        private static SoundEvent getSound(SoundEditBox editBox) {
            ResourceLocation soundLoc = ResourceLocation.tryParse(editBox.getValue());
            return SoundEvent.createVariableRangeEvent(Objects.requireNonNullElseGet(soundLoc, () -> ResourceLocation.withDefaultNamespace("")));
        }
    }
}
