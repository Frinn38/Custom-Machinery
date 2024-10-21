package fr.frinn.custommachinery.client.screen.creation.gui;

import fr.frinn.custommachinery.client.screen.creation.MachineEditScreen;
import fr.frinn.custommachinery.client.screen.creation.tabs.GuiTab;
import fr.frinn.custommachinery.client.screen.popup.PopupScreen;
import fr.frinn.custommachinery.client.screen.widget.IntegerEditBox;
import fr.frinn.custommachinery.client.screen.widget.SuggestedEditBox;
import fr.frinn.custommachinery.common.guielement.BackgroundGuiElement;
import fr.frinn.custommachinery.impl.util.TextureSizeHelper;
import net.minecraft.client.Minecraft;
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
import org.jetbrains.annotations.Nullable;

public class BackgroundEditorPopup extends PopupScreen {

    @Nullable
    private final BackgroundGuiElement background;
    private final GuiEditorWidget editor;

    private CycleButton<Mode> mode;
    private SuggestedEditBox texture;
    private IntegerEditBox width;
    private IntegerEditBox height;

    public BackgroundEditorPopup(MachineEditScreen parent, GuiEditorWidget editor) {
        super(parent, 256, 128);
        this.background = parent.getBuilder().getGuiElements().stream().filter(element -> element instanceof BackgroundGuiElement).map(element -> (BackgroundGuiElement)element).findFirst().orElse(null);
        this.editor = editor;
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
        LayoutSettings middle = row.newCellSettings().alignVerticallyMiddle();

        //Title
        row.addChild(new StringWidget(this.xSize - 10, this.font.lineHeight, Component.translatable("custommachinery.gui.creation.gui.background"), this.font), 2, center);

        //Mode
        row.addChild(new StringWidget(Component.translatable("custommachinery.gui.creation.gui.background.mode"), this.font), middle);
        this.mode = row.addChild(CycleButton.builder(Mode::title).displayOnlyValue().withValues(Mode.values()).withInitialValue(mode).create(0, 0, 100, 20, Component.translatable("custommachinery.gui.creation.gui.background.mode"), (button, value) -> this.texture.setEditable(value == Mode.CUSTOM)));

        //Texture
        row.addChild(new StringWidget(Component.translatable("custommachinery.gui.creation.gui.background.texture"), this.font), middle);
        this.texture = row.addChild(new SuggestedEditBox(this.font, 0, 0, 100, 20, Component.translatable("custommachinery.gui.creation.gui.background.texture"), 5));
        this.texture.setMaxLength(Integer.MAX_VALUE);
        if(this.background != null) {
            this.texture.setValue(this.background.getTexture().toString());
            this.texture.hideSuggestions();
        }
        this.texture.addSuggestions(Minecraft.getInstance().getResourceManager().listResources("textures", id -> true).keySet().stream().map(ResourceLocation::toString).toList());
        this.texture.setEditable(mode == Mode.CUSTOM);

        //Width
        row.addChild(new StringWidget(Component.translatable("custommachinery.gui.creation.gui.background.width"), this.font), middle);
        this.width = row.addChild(new IntegerEditBox(this.font, 0, 0, 100, 20, Component.translatable("custommachinery.gui.creation.gui.background.width")));
        this.width.bounds(-1, 256);
        this.width.setTooltip(Tooltip.create(Component.translatable("custommachinery.gui.creation.gui.background.width.tooltip")));
        if(this.background != null)
            this.width.setValue("" + this.background.getWidth());
        else
            this.width.setValue("256");

        //Height
        row.addChild(new StringWidget(Component.translatable("custommachinery.gui.creation.gui.background.height"), this.font), middle);
        this.height = row.addChild(new IntegerEditBox(this.font, 0, 0, 100, 20, Component.translatable("custommachinery.gui.creation.gui.background.height")));
        this.height.bounds(-1, 192);
        this.height.setTooltip(Tooltip.create(Component.translatable("custommachinery.gui.creation.gui.background.height.tooltip")));
        if(this.background != null)
            this.height.setValue("" + this.background.getHeight());
        else
            this.height.setValue("192");

        //Show background
        row.addChild(new StringWidget(Component.translatable("custommachinery.gui.creation.gui.background.show"), this.font), middle);
        Checkbox show = row.addChild(Checkbox.builder(Component.translatable("custommachinery.gui.creation.gui.background.show"), this.font).selected(this.editor.shouldShowBackground()).onValueChange(((checkbox, value) -> this.editor.setShowBackground(value))).tooltip(Tooltip.create(Component.translatable("custommachinery.gui.creation.gui.background.show.tooltip"))).build());

        //Close
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
            if(editScreen.getTabManager().getCurrentTab() instanceof GuiTab tab) {
                int width = this.width.getIntValue() > 0 ? this.width.getIntValue() : 256;
                int height = this.height.getIntValue() > 0 ? this.height.getIntValue() : 192;
                tab.setSize(width, height);
            }
            return;
        }

        ResourceLocation texture = switch (this.mode.getValue()) {
            case DEFAULT -> BackgroundGuiElement.BASE_BACKGROUND;
            case CUSTOM -> ResourceLocation.tryParse(this.texture.getValue());
            case NO_BACKGROUND -> null;
        };

        if(this.background != null) {
            editScreen.getBuilder().getGuiElements().remove(this.background);
            editScreen.getBuilder().getGuiElements().add(new BackgroundGuiElement(texture, this.width.getIntValue(), this.height.getIntValue()));
        } else
            editScreen.getBuilder().getGuiElements().add(new BackgroundGuiElement(texture, this.width.getIntValue(), this.height.getIntValue()));

        if(editScreen.getTabManager().getCurrentTab() instanceof GuiTab tab) {
            int width = this.width.getIntValue() > 0 ? this.width.getIntValue() : TextureSizeHelper.getTextureWidth(texture);
            int height = this.height.getIntValue() > 0 ? this.height.getIntValue() : TextureSizeHelper.getTextureHeight(texture);
            tab.setSize(width, height);
        }
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
