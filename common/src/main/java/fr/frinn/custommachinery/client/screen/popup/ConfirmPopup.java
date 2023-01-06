package fr.frinn.custommachinery.client.screen.popup;

import com.mojang.blaze3d.vertex.PoseStack;
import fr.frinn.custommachinery.client.screen.widget.custom.ButtonWidget;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TranslatableComponent;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class ConfirmPopup extends PopupScreen {

    public static final Component CONFIRM = new TranslatableComponent("custommachinery.gui.popup.confirm").withStyle(ChatFormatting.GREEN);
    public static final Component CANCEL = new TranslatableComponent("custommachinery.gui.popup.cancel").withStyle(ChatFormatting.RED);

    private final Runnable onConfirm;
    private final List<Component> text = new ArrayList<>();
    @Nullable
    private Runnable onCancel;

    public ConfirmPopup(int xSize, int ySize, Runnable onConfirm) {
        super(xSize, ySize);
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
        addCustomWidget(new ButtonWidget(() -> this.getX() + this.xSize / 4 - 25, () -> this.getY() + this.ySize - 30, 50, 20)
                .title(CANCEL, true)
                .callback(button -> {
                    if(this.onCancel != null)
                        this.onCancel.run();
                    close();
                })
        );
        addCustomWidget(new ButtonWidget(() -> this.getX() + (int)(this.xSize * 0.75) - 25, () -> this.getY() + this.ySize - 30, 50, 20)
                .title(CONFIRM, true)
                .callback(button -> {
                    this.onConfirm.run();
                    close();
                })
        );
    }

    @Override
    public void render(PoseStack pose, int mouseX, int mouseY, float partialTicks) {
        super.render(pose, mouseX, mouseY, partialTicks);
        Font font = Minecraft.getInstance().font;
        for(int i = 0; i < this.text.size(); i++) {
            Component component = this.text.get(i);
            int width = font.width(component);
            int x = (this.xSize - width) / 2 + getX();
            font.draw(pose, component, x, getY() + i * 20 + 5, 0);
        }
    }
}
