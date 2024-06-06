package fr.frinn.custommachinery.client.screen.creation.component;

import fr.frinn.custommachinery.api.component.IMachineComponentTemplate;
import fr.frinn.custommachinery.client.screen.BaseScreen;
import fr.frinn.custommachinery.client.screen.popup.PopupScreen;
import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.StringWidget;
import net.minecraft.network.chat.Component;
import org.jetbrains.annotations.Nullable;

import java.util.Optional;
import java.util.function.Consumer;

public abstract class ComponentBuilderPopup<T extends IMachineComponentTemplate<?>> extends PopupScreen {

    public static final Component CONFIRM = Component.translatable("custommachinery.gui.popup.confirm").withStyle(ChatFormatting.GREEN);
    public static final Component CANCEL = Component.translatable("custommachinery.gui.popup.cancel").withStyle(ChatFormatting.RED);

    @Nullable
    private final T baseTemplate;
    private final Consumer<T> onFinish;
    private final Component title;

    public ComponentPropertyList propertyList;

    public ComponentBuilderPopup(BaseScreen parent, @Nullable T template, Consumer<T> onFinish, Component title) {
        super(parent, 256, 196);
        this.onFinish = onFinish;
        this.baseTemplate = template;
        this.title = title;
    }

    public abstract T makeTemplate();

    private void confirm() {
        this.onFinish.accept(this.makeTemplate());
        this.parent.closePopup(this);
    }

    private void cancel() {
        this.parent.closePopup(this);
    }

    public Optional<T> baseTemplate() {
        return Optional.ofNullable(this.baseTemplate);
    }

    @Override
    protected void init() {
        super.init();

        //Title
        this.addRenderableWidget(new StringWidget(this.title, this.font)).setPosition(this.x + (this.xSize - this.font.width(this.title)) / 2, this.y + 5);

        //Properties
        this.propertyList = this.addRenderableWidget(new ComponentPropertyList(this.mc, this.x + 5, this.y + 15, this.xSize - 16, this.ySize - 50, 30));

        //Bottom row - confirm/cancel buttons
        this.addRenderableWidget(Button.builder(CONFIRM, b -> this.confirm()).bounds(this.x + this.xSize / 3 - 25, this.y + this.ySize - 30, 50, 20).build());
        this.addRenderableWidget(Button.builder(CANCEL, b -> this.cancel()).bounds(this.x + this.xSize / 3 * 2 - 25, this.y + this.ySize - 30, 50, 20).build());
    }

    @Override
    public void move(int movedX, int movedY) {
        super.move(movedX, movedY);
        this.propertyList.move(movedX, movedY);
    }

    public boolean checkLong(String s) {
        if(s.isEmpty())
            return true;

        try {
            Long.parseLong(s);
            return true;
        } catch (NumberFormatException e) {
            return false;
        }
    }

    public long parseLong(String s) {
        if(s.isEmpty())
            return 0L;

        try {
            return Long.parseLong(s);
        } catch (NumberFormatException e) {
            return 0L;
        }
    }
}
