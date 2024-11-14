package fr.frinn.custommachinery.client.screen.creation.tabs;

import fr.frinn.custommachinery.CustomMachinery;
import fr.frinn.custommachinery.client.screen.BaseScreen;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.TabButton;
import net.minecraft.client.gui.components.WidgetSprites;
import net.minecraft.client.gui.components.tabs.TabManager;
import net.minecraft.resources.ResourceLocation;

public class EditTabButton extends TabButton {

    public static final WidgetSprites TAB_SPRITE = new WidgetSprites(CustomMachinery.rl("creation/tab_button"), CustomMachinery.rl("creation/tab_button_selected"));
    public static final WidgetSprites INVERTED_SPRITE = new WidgetSprites(CustomMachinery.rl("creation/tab_button_inverted"), CustomMachinery.rl("creation/tab_button_inverted_selected"));

    private final boolean inverted;

    public EditTabButton(TabManager tabManager, MachineEditTab tab, int width, int height, boolean inverted) {
        super(tabManager, tab, width, height);
        this.inverted = inverted;
    }

    @Override
    public MachineEditTab tab() {
        return (MachineEditTab) super.tab();
    }

    @Override
    public void renderWidget(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        ResourceLocation sprite = this.inverted ? INVERTED_SPRITE.get(true, this.isSelected()) : TAB_SPRITE.get(true, this.isSelected());
        graphics.blitSprite(sprite, this.getX(), this.getY(), this.width, this.height);
        Font font = Minecraft.getInstance().font;
        int color = this.active ? -1 : -6250336;
        BaseScreen.drawCenteredScaledString(graphics, font, this.getMessage(), this.getX() + this.width / 2, this.getY() + this.height / 2, 0.9f, color, true);
    }
}
