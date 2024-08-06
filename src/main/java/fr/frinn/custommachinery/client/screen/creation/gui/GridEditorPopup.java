package fr.frinn.custommachinery.client.screen.creation.gui;

import fr.frinn.custommachinery.client.screen.BaseScreen;
import fr.frinn.custommachinery.client.screen.creation.gui.GuiEditorWidget.GridSettings;
import fr.frinn.custommachinery.client.screen.popup.PopupScreen;
import fr.frinn.custommachinery.client.screen.widget.FloatSlider;
import fr.frinn.custommachinery.client.screen.widget.IntegerSlider;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.Checkbox;
import net.minecraft.client.gui.components.StringWidget;
import net.minecraft.client.gui.layouts.GridLayout;
import net.minecraft.client.gui.layouts.GridLayout.RowHelper;
import net.minecraft.client.gui.layouts.LayoutSettings;
import net.minecraft.network.chat.Component;

public class GridEditorPopup extends PopupScreen {

    private final GuiEditorWidget editor;

    private Checkbox enabled;
    private IntegerSlider xSpacing;
    private IntegerSlider ySpacing;
    private FloatSlider opacity;

    public GridEditorPopup(BaseScreen parent, GuiEditorWidget editor) {
        super(parent, 256, 128);
        this.editor = editor;
    }

    @Override
    protected void init() {
        super.init();
        GridSettings defaultSettings = this.editor.getGridSettings();

        GridLayout layout = new GridLayout(this.x + 5, this.y + 5).spacing(5);
        RowHelper row = layout.createRowHelper(2);
        LayoutSettings center = row.newCellSettings().alignHorizontallyCenter();
        LayoutSettings middle = row.newCellSettings().alignVerticallyMiddle();

        //Title
        row.addChild(new StringWidget(this.xSize - 10, this.font.lineHeight, Component.translatable("custommachinery.gui.creation.gui.grid"), this.font), 2, center);

        //Enabled
        row.addChild(new StringWidget(Component.translatable("custommachinery.gui.creation.gui.grid.enable"), this.font), middle);
        this.enabled = row.addChild(Checkbox.builder(Component.empty(), this.font).selected(defaultSettings.enabled()).onValueChange((button, value) -> this.applySettings()).build());

        //X spacing
        row.addChild(new StringWidget(Component.translatable("custommachinery.gui.creation.gui.grid.spacing.x"), this.font), middle);
        this.xSpacing = row.addChild(IntegerSlider.builder().displayOnlyValue().bounds(1, 50).defaultValue(defaultSettings.xSpacing()).setResponder(value -> this.applySettings()).create(0, 0, 100, 20, Component.translatable("custommachinery.gui.creation.gui.grid.spacing.x")));

        //Y spacing
        row.addChild(new StringWidget(Component.translatable("custommachinery.gui.creation.gui.grid.spacing.y"), this.font), middle);
        this.ySpacing = row.addChild(IntegerSlider.builder().displayOnlyValue().bounds(1, 50).defaultValue(defaultSettings.ySpacing()).setResponder(value -> this.applySettings()).create(0, 0, 100, 20, Component.translatable("custommachinery.gui.creation.gui.grid.spacing.y")));

        //Opacity
        row.addChild(new StringWidget(Component.translatable("custommachinery.gui.creation.gui.grid.opacity"), this.font), middle);
        this.opacity = row.addChild(FloatSlider.builder().displayOnlyValue().decimalsToShow(2).bounds(0.0F, 1.0F).defaultValue(defaultSettings.opacity()).setResponder(value -> this.applySettings()).create(0, 0, 100, 20, Component.translatable("custommachinery.gui.creation.gui.grid.opacity")));

        //Close
        row.addChild(Button.builder(Component.translatable("custommachinery.gui.creation.gui.background.close"), button -> this.parent.closePopup(this)).size(50, 20).build(), 2, center);

        layout.arrangeElements();
        layout.visitWidgets(this::addRenderableWidget);
        this.ySize = layout.getHeight() + 10;
    }

    private void applySettings() {
        this.editor.setGridSettings(new GridSettings(this.enabled.selected(), this.xSpacing.intValue(), this.ySpacing.intValue(), this.opacity.floatValue()));
    }
}
