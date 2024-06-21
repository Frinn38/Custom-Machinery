package fr.frinn.custommachinery.client.screen.popup;

import fr.frinn.custommachinery.client.screen.BaseScreen;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.network.chat.Component;
import net.minecraft.util.FormattedCharSequence;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class InfoPopup extends PopupScreen {

    private final List<Component> text = new ArrayList<>();

    public InfoPopup(BaseScreen parent, int xSize, int ySize) {
        super(parent, xSize, ySize);
    }

    public InfoPopup text(Component... text) {
        this.text.addAll(Arrays.asList(text));
        return this;
    }

    @Override
    protected void init() {
        super.init();
        this.addRenderableWidget(Button.builder(Component.translatable("custommachinery.gui.popup.confirm"), button -> this.parent.closePopup(this)).bounds(this.x + 10, this.y + this.ySize - 30, this.xSize - 20, 20).build());
    }

    @Override
    public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTicks) {
        super.render(graphics, mouseX, mouseY, partialTicks);
        List<FormattedCharSequence> list = this.text.stream().flatMap(component -> font.split(component, this.xSize - 20).stream()).toList();
        for(int i = 0; i < list.size(); i++) {
            FormattedCharSequence text = list.get(i);
            int width = font.width(text);
            int x = (this.xSize - width) / 2 + this.x;
            graphics.drawString(font, text, x, this.y + i * font.lineHeight + 5, 0, false);
        }
    }
}
