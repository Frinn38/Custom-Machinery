package fr.frinn.custommachinery.client.screen.creation.tabs;

import fr.frinn.custommachinery.CustomMachinery;
import fr.frinn.custommachinery.client.screen.BaseScreen;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.TabButton;
import net.minecraft.client.gui.components.tabs.TabManager;
import net.minecraft.resources.ResourceLocation;

public class EditTabButton extends TabButton {

    public EditTabButton(TabManager tabManager, MachineEditTab tab, int width, int height) {
        super(tabManager, tab, width, height);
    }

    @Override
    public MachineEditTab tab() {
        return (MachineEditTab) super.tab();
    }

    @Override
    public void renderWidget(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        ResourceLocation texture = CustomMachinery.rl("textures/gui/creation/tab_button.png");
        //graphics.blitSprite(texture, this.getX(), this.getY(), this.width, this.height, 4, 4, 4, 4, 28, 32, this.isSelected() ? 28 : 0, 0);
        Font font = Minecraft.getInstance().font;
        int color = this.active ? -1 : -6250336;
        BaseScreen.drawCenteredScaledString(graphics, font, this.getMessage(), this.getX() + this.width / 2, this.getY() + this.height / 2, 0.9f, color, true);
    }
}
