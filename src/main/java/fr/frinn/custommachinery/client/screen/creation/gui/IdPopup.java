package fr.frinn.custommachinery.client.screen.creation.gui;

import fr.frinn.custommachinery.client.screen.creation.MachineEditScreen;
import fr.frinn.custommachinery.client.screen.popup.PopupScreen;
import fr.frinn.custommachinery.client.screen.widget.SuggestedEditBox;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.MultiLineTextWidget;
import net.minecraft.client.gui.components.StringWidget;
import net.minecraft.client.gui.layouts.GridLayout;
import net.minecraft.client.gui.layouts.GridLayout.RowHelper;
import net.minecraft.client.gui.layouts.LayoutSettings;
import net.minecraft.network.chat.Component;

import java.util.List;
import java.util.function.Consumer;

public class IdPopup extends PopupScreen {

    private final String from;
    private final Consumer<String> idConsumer;
    private final List<String> suggestedIds;

    private SuggestedEditBox id;

    public IdPopup(MachineEditScreen parent, String from, Consumer<String> idConsumer, List<String> suggestedIds) {
        super(parent, 96, 96);
        this.from = from;
        this.idConsumer = idConsumer;
        this.suggestedIds = suggestedIds;
    }

    @Override
    protected void init() {
        super.init();
        GridLayout layout = new GridLayout(this.x, this.y);
        layout.defaultCellSetting().paddingTop(5);
        RowHelper row = layout.createRowHelper(2);
        LayoutSettings center = row.newCellSettings().alignHorizontallyCenter();
        row.addChild(new StringWidget(this.xSize, this.font.lineHeight, Component.translatable("custommachinery.gui.creation.gui.id"), this.font), 2, center);
        MultiLineTextWidget description = row.addChild(new MultiLineTextWidget(Component.translatable("custommachinery.gui.creation.gui.id.description"), this.font), 2, center);
        description.setCentered(true);
        description.setMaxWidth(this.xSize - 10);
        this.id = row.addChild(new SuggestedEditBox(this.font, 0, 0, 90, 20, Component.empty(), 5), 2, center);
        this.id.setValue(this.from);
        this.id.addSuggestions(this.suggestedIds);
        row.addChild(Button.builder(Component.translatable("custommachinery.gui.popup.confirm"), button -> {
            this.idConsumer.accept(this.id.getValue());
            this.parent.closePopup(this);
        }).size(40, 20).build(), center);
        row.addChild(Button.builder(Component.translatable("custommachinery.gui.popup.cancel"), button -> this.parent.closePopup(this)).size(40, 20).build(), center);

        layout.arrangeElements();
        layout.visitWidgets(this::addRenderableWidget);
    }
}
