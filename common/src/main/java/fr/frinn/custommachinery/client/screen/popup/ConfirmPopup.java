package fr.frinn.custommachinery.client.screen.popup;

import fr.frinn.custommachinery.client.screen.BaseScreen;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.MultiLineTextWidget;
import net.minecraft.client.gui.components.StringWidget;
import net.minecraft.client.gui.layouts.GridLayout;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class ConfirmPopup extends PopupScreen {

    public static final Component CONFIRM = Component.translatable("custommachinery.gui.popup.confirm").withStyle(ChatFormatting.GREEN);
    public static final Component CANCEL = Component.translatable("custommachinery.gui.popup.cancel").withStyle(ChatFormatting.RED);

    private final Runnable onConfirm;
    private final List<Component> text = new ArrayList<>();
    private Component title;
    @Nullable
    private Runnable onCancel;

    public ConfirmPopup(BaseScreen parent, int xSize, int ySize, Runnable onConfirm) {
        super(parent, xSize, ySize);
        this.onConfirm = onConfirm;
        this.onCancel = null;
    }

    public ConfirmPopup cancelCallback(Runnable callback) {
        this.onCancel = callback;
        return this;
    }

    public ConfirmPopup title(Component title) {
        this.title = title;
        return this;
    }

    public ConfirmPopup text(Component... components) {
        this.text.addAll(Arrays.asList(components));
        return this;
    }

    public void confirm() {
        this.onConfirm.run();
        this.parent.closePopup(this);
    }

    public void cancel() {
        if(this.onCancel != null)
            this.onCancel.run();
        this.parent.closePopup(this);
    }

    @Override
    protected void init() {
        super.init();
        GridLayout layout = new GridLayout(this.x, this.y);
        GridLayout.RowHelper row = layout.createRowHelper(2);
        row.defaultCellSetting().paddingTop(5);
        row.addChild(new StringWidget(this.xSize, 10, this.title, Minecraft.getInstance().font).alignCenter(), 2);
        MutableComponent text = Component.empty();
        for(Component component : this.text)
            text.append("\n").append(component);
        MultiLineTextWidget textWidget = new MultiLineTextWidget(text, Minecraft.getInstance().font).setCentered(true).setMaxWidth(this.xSize - 10);
        this.ySize = textWidget.getHeight() + 50;
        row.addChild(textWidget, 2, row.newCellSettings().alignHorizontallyCenter());
        row.addChild(Button.builder(CONFIRM, b -> this.confirm()).bounds(0, 0, 50, 20).build(), row.newCellSettings().alignHorizontallyCenter());
        row.addChild(Button.builder(CANCEL, b -> this.cancel()).bounds(0, 0, 50, 20).build(), row.newCellSettings().alignHorizontallyCenter());
        layout.arrangeElements();
        layout.visitWidgets(this::addRenderableWidget);
    }
}
