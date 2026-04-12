package fr.frinn.custommachinery.client.screen.creation.appearance.builder;

import fr.frinn.custommachinery.api.machine.MachineAppearanceProperty;
import fr.frinn.custommachinery.client.screen.BaseScreen;
import fr.frinn.custommachinery.client.screen.creation.appearance.IAppearancePropertyBuilder;
import fr.frinn.custommachinery.client.screen.popup.PopupScreen;
import fr.frinn.custommachinery.client.screen.widget.FloatSlider;
import fr.frinn.custommachinery.client.screen.widget.ListWidget;
import fr.frinn.custommachinery.client.screen.widget.SoundEditBox;
import fr.frinn.custommachinery.common.init.Registration;
import fr.frinn.custommachinery.common.util.sound.CMSoundType;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.StringWidget;
import net.minecraft.client.gui.components.Tooltip;
import net.minecraft.client.gui.components.events.GuiEventListener;
import net.minecraft.client.gui.layouts.GridLayout;
import net.minecraft.client.gui.layouts.GridLayout.RowHelper;
import net.minecraft.client.gui.layouts.LayoutSettings;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;

import java.util.List;
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
        return Button.builder(this.title(), button ->
            parent.openPopup(new InteractionSoundEditPopup(parent, 205, 240, supplier, consumer), this.title().getString())
        ).bounds(x, y, width, height).tooltip(Tooltip.create(Component.translatable("custommachinery.gui.creation.appearance.interaction_sound.tooltip"))).build();
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
        private SoundEditBox openSound;
        private SoundEditBox closeSound;

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
            RowHelper row = layout.createRowHelper(1);

            //Title
            row.addChild(new StringWidget(this.xSize - 10, this.font.lineHeight, Component.translatable("custommachinery.gui.creation.appearance.interaction_sound"), this.font), center);

            //List
            InteractionSoundBuilderList list = row.addChild(new InteractionSoundBuilderList(0, 0, this.xSize - 10, this.ySize - 50, 30, Component.empty()), center);

            //Volume
            this.volume = list.addWidget(Component.translatable("custommachinery.gui.creation.appearance.interaction_sound.volume"), FloatSlider.builder().bounds(0, 5).displayOnlyValue().defaultValue(this.supplier.get().getVolume()).decimalsToShow(2).create(0, 0, 120, 20, Component.translatable("custommachinery.gui.creation.appearance.interaction_sound.volume")));

            //Pitch
            this.pitch = list.addWidget(Component.translatable("custommachinery.gui.creation.appearance.interaction_sound.pitch"), FloatSlider.builder().bounds(0, 5).displayOnlyValue().defaultValue(this.supplier.get().getPitch()).decimalsToShow(2).create(0, 0, 120, 20, Component.translatable("custommachinery.gui.creation.appearance.interaction_sound.pitch")));

            //Break
            this.breakSound = list.addWidget(Component.translatable("custommachinery.gui.creation.appearance.interaction_sound.break"), new SoundEditBox(0, 0, 120, 20, Component.translatable("custommachinery.gui.creation.appearance.interaction_sound.break")));
            if(!this.supplier.get().getBreakSound().getLocation().getPath().isEmpty())
                this.breakSound.setValue(this.supplier.get().getBreakSound().getLocation().toString());

            //Step
            this.stepSound = list.addWidget(Component.translatable("custommachinery.gui.creation.appearance.interaction_sound.step"), new SoundEditBox(0, 0, 120, 20, Component.translatable("custommachinery.gui.creation.appearance.interaction_sound.step")));
            if(!this.supplier.get().getStepSound().getLocation().getPath().isEmpty())
                this.stepSound.setValue(this.supplier.get().getStepSound().getLocation().toString());

            //Place
            this.placeSound = list.addWidget(Component.translatable("custommachinery.gui.creation.appearance.interaction_sound.place"), new SoundEditBox(0, 0, 120, 20, Component.translatable("custommachinery.gui.creation.appearance.interaction_sound.place")));
            if(!this.supplier.get().getPlaceSound().getLocation().getPath().isEmpty())
                this.placeSound.setValue(this.supplier.get().getPlaceSound().getLocation().toString());

            //Hit
            this.hitSound = list.addWidget(Component.translatable("custommachinery.gui.creation.appearance.interaction_sound.hit"), new SoundEditBox(0, 0, 120, 20, Component.translatable("custommachinery.gui.creation.appearance.interaction_sound.hit")));
            if(!this.supplier.get().getHitSound().getLocation().getPath().isEmpty())
                this.hitSound.setValue(this.supplier.get().getHitSound().getLocation().toString());

            //Fall
            this.fallSound = list.addWidget(Component.translatable("custommachinery.gui.creation.appearance.interaction_sound.fall"), new SoundEditBox(0, 0, 120, 20, Component.translatable("custommachinery.gui.creation.appearance.interaction_sound.fall")));
            if(!this.supplier.get().getFallSound().getLocation().getPath().isEmpty())
             this.fallSound.setValue(this.supplier.get().getFallSound().getLocation().toString());

            //Open
            this.openSound = list.addWidget(Component.translatable("custommachinery.gui.creation.appearance.interaction_sound.open"), new SoundEditBox(0, 0, 120, 20, Component.translatable("custommachinery.gui.creation.appearance.interaction_sound.open")));
            if(!this.supplier.get().getOpenSound().getLocation().equals(SoundEvents.EMPTY.getLocation()))
                this.openSound.setValue(this.supplier.get().getOpenSound().getLocation().toString());

            //Fall
            this.closeSound = list.addWidget(Component.translatable("custommachinery.gui.creation.appearance.interaction_sound.close"), new SoundEditBox(0, 0, 120, 20, Component.translatable("custommachinery.gui.creation.appearance.interaction_sound.close")));
            if(!this.supplier.get().getCloseSound().getLocation().equals(SoundEvents.EMPTY.getLocation()))
                this.closeSound.setValue(this.supplier.get().getCloseSound().getLocation().toString());

            row.addChild(Button.builder(Component.translatable("custommachinery.gui.config.close"), button -> this.parent.closePopup(this)).size(50, 20).build(), center);

            layout.arrangeElements();
            layout.visitWidgets(this::addRenderableWidget);
        }

        @Override
        public void closed() {
            this.consumer.accept(new CMSoundType(this.volume.floatValue(), this.pitch.floatValue(), getSound(this.breakSound), getSound(this.stepSound), getSound(this.placeSound), getSound(this.hitSound), getSound(this.fallSound), getSound(this.openSound), getSound(this.closeSound)));
        }

        private static SoundEvent getSound(SoundEditBox editBox) {
            if(editBox.getValue().isEmpty())
                return SoundEvents.EMPTY;
            ResourceLocation soundLoc = ResourceLocation.tryParse(editBox.getValue());
            return SoundEvent.createVariableRangeEvent(Objects.requireNonNullElseGet(soundLoc, () -> ResourceLocation.withDefaultNamespace("")));
        }

        public static class InteractionSoundBuilderList extends ListWidget<InteractionSoundBuilderList.InteractionSoundBuilderEntry> {

            public InteractionSoundBuilderList(int x, int y, int width, int height, int itemHeight, Component message) {
                super(x, y, width, height, itemHeight, message);
            }

            public <W extends AbstractWidget> W addWidget(Component title, W widget) {
                this.addEntry(new InteractionSoundBuilderEntry(title, widget));
                return widget;
            }

            public static class InteractionSoundBuilderEntry extends ListWidget.Entry {

                private final Component title;
                private final AbstractWidget widget;

                public InteractionSoundBuilderEntry(Component title, AbstractWidget widget) {
                    this.title = title;
                    this.widget = widget;
                }

                @Override
                protected void render(GuiGraphics graphics, int index, int x, int y, int width, int height, int mouseX, int mouseY, float partialTicks) {
                    graphics.drawString(Minecraft.getInstance().font, this.title, x, y + height / 2 - Minecraft.getInstance().font.lineHeight, 0xFFFFFFFF);
                    this.widget.setPosition(x + width - this.widget.getWidth() - 10, y);
                }

                @Override
                public List<GuiEventListener> children() {
                    return List.of(this.widget);
                }
            }
        }
    }
}
