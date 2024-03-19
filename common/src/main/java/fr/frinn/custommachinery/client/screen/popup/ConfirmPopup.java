package fr.frinn.custommachinery.client.screen.popup;

import fr.frinn.custommachinery.client.screen.BaseScreen;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.network.chat.Component;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class ConfirmPopup extends PopupScreen {

    public static final Component CONFIRM = Component.translatable("custommachinery.gui.popup.confirm").withStyle(ChatFormatting.GREEN);
    public static final Component CANCEL = Component.translatable("custommachinery.gui.popup.cancel").withStyle(ChatFormatting.RED);

    private final Runnable onConfirm;
    private final List<Component> text = new ArrayList<>();
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

    public ConfirmPopup text(Component... components) {
        this.text.addAll(Arrays.asList(components));
        return this;
    }

    @Override
    protected void init() {
        super.init();
        this.addRenderableWidget(Button.builder(CANCEL, button -> {
            if(this.onCancel != null)
                this.onCancel.run();
            closed();
        }).bounds(this.x + this.xSize / 4 - 25, this.y + this.ySize - 30, 50, 20).build());
        this.addRenderableWidget(Button.builder(CONFIRM, button -> {
            this.onConfirm.run();
            closed();
        }).bounds(this.x + (int)(this.xSize * 0.75) - 25, this.y + this.ySize - 30, 50, 20).build());
    }

    @Override
    public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTicks) {
        super.render(graphics, mouseX, mouseY, partialTicks);
        Font font = Minecraft.getInstance().font;
        for(int i = 0; i < this.text.size(); i++) {
            Component component = this.text.get(i);
            int width = font.width(component);
            int x = (this.xSize - width) / 2 + this.x;
            graphics.drawString(font, component, x, this.y + i * 20 + 5, 0);
        }
    }
}
