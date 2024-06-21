package fr.frinn.custommachinery.client.screen.creation.gui;

import fr.frinn.custommachinery.client.screen.creation.MachineEditScreen;
import fr.frinn.custommachinery.client.screen.popup.PopupScreen;
import fr.frinn.custommachinery.client.screen.widget.SuggestedEditBox;
import fr.frinn.custommachinery.common.guielement.BackgroundGuiElement;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.CycleButton;
import net.minecraft.client.gui.components.StringWidget;
import net.minecraft.client.gui.layouts.GridLayout;
import net.minecraft.client.gui.layouts.GridLayout.RowHelper;
import net.minecraft.client.gui.layouts.LayoutSettings;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.Nullable;

public class BackgroundEditorPopup extends PopupScreen {

    @Nullable
    private final BackgroundGuiElement background;

    private CycleButton<Mode> mode;
    private SuggestedEditBox texture;

    public BackgroundEditorPopup(MachineEditScreen parent) {
        super(parent, 256, 128);
        this.background = parent.getBuilder().getGuiElements().stream().filter(element -> element instanceof BackgroundGuiElement).map(element -> (BackgroundGuiElement)element).findFirst().orElse(null);
    }

    @Override
    protected void init() {
        super.init();
        Mode mode = Mode.CUSTOM;
        if(this.background == null || this.background.getTexture() == null)
            mode = Mode.NO_BACKGROUND;
        else if (this.background.getTexture().equals(BackgroundGuiElement.BASE_BACKGROUND))
            mode = Mode.DEFAULT;
        GridLayout layout = new GridLayout(this.x + 5, this.y + 5).spacing(5);
        RowHelper row = layout.createRowHelper(2);
        LayoutSettings center = row.newCellSettings().alignHorizontallyCenter();
        row.addChild(new StringWidget(this.xSize - 10, this.font.lineHeight, Component.translatable("custommachinery.gui.creation.gui.background"), this.font), 2, center);
        row.addChild(new StringWidget(Component.translatable("custommachinery.gui.creation.gui.background.mode"), this.font));
        this.mode = row.addChild(CycleButton.builder(Mode::title).displayOnlyValue().withValues(Mode.values()).withInitialValue(mode).create(0, 0, 100, 20, Component.translatable("custommachinery.gui.creation.gui.background.mode"), (button, value) -> this.texture.setEditable(value == Mode.CUSTOM)));
        row.addChild(new StringWidget(Component.translatable("custommachinery.gui.creation.gui.background.texture"), this.font));
        this.texture = row.addChild(new SuggestedEditBox(this.font, 0, 0, 100, 20, Component.translatable("custommachinery.gui.creation.gui.background.texture"), 5));
        this.texture.setMaxLength(Integer.MAX_VALUE);
        if(this.background != null) {
            this.texture.setValue(this.background.getTexture().toString());
            this.texture.hideSuggestions();
        }
        this.texture.addSuggestions(Minecraft.getInstance().getResourceManager().listResources("textures", id -> true).keySet().stream().map(ResourceLocation::toString).toList());
        this.texture.setEditable(mode == Mode.CUSTOM);
        row.addChild(Button.builder(Component.translatable("custommachinery.gui.creation.gui.background.close"), button -> this.close()).size(50, 20).build(), 2, center);

        layout.arrangeElements();
        layout.visitWidgets(this::addRenderableWidget);
        this.ySize = layout.getHeight() + 10;
    }

    private void close() {
        this.parent.closePopup(this);
    }

    @Override
    public void closed() {
        if(!(this.parent instanceof MachineEditScreen editScreen))
            return;

        if(this.mode.getValue() == Mode.NO_BACKGROUND) {
            if(this.background != null)
                editScreen.getBuilder().getGuiElements().remove(this.background);
            return;
        }

        ResourceLocation texture = switch (this.mode.getValue()) {
            case DEFAULT -> BackgroundGuiElement.BASE_BACKGROUND;
            case CUSTOM -> ResourceLocation.tryParse(this.texture.getValue());
            case NO_BACKGROUND -> null;
        };

        if(this.background != null) {
            editScreen.getBuilder().getGuiElements().remove(this.background);
            editScreen.getBuilder().getGuiElements().add(new BackgroundGuiElement(texture, this.background.getWidth(), this.background.getHeight()));
        } else
            editScreen.getBuilder().getGuiElements().add(new BackgroundGuiElement(texture, -1, -1));
    }

    public enum Mode {
        DEFAULT(Component.translatable("custommachinery.gui.creation.gui.background.default")),
        NO_BACKGROUND(Component.translatable("custommachinery.gui.creation.gui.background.disabled")),
        CUSTOM(Component.translatable("custommachinery.gui.creation.gui.background.custom"));

        private final Component title;

        Mode(Component title) {
            this.title = title;
        }

        public Component title() {
            return this.title;
        }
    }
}
