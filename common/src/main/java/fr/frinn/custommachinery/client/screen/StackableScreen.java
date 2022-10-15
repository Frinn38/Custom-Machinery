package fr.frinn.custommachinery.client.screen;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TextComponent;

import java.util.Stack;

public class StackableScreen extends BaseScreen {

    private final Stack<Screen> stack = new Stack<>();
    private final Runnable onClose;

    public StackableScreen(Runnable onClose) {
        super(TextComponent.EMPTY);
        this.onClose = onClose;
    }

    public void pushScreenLayer(Screen screen) {
        screen.init(Minecraft.getInstance(), this.width, this.height);
        this.stack.push(screen);
    }

    public void popScreenLayer() {
        this.stack.pop();
    }

    @Override
    public void render(PoseStack poseStack, int mouseX, int mouseY, float partialTicks) {
        poseStack.pushPose();
        this.stack.forEach(screen -> {
            if(screen == this.stack.peek())
                screen.render(poseStack, mouseX, mouseY, partialTicks);
            else
                screen.render(poseStack, Integer.MAX_VALUE, Integer.MAX_VALUE, partialTicks);
            poseStack.translate(0, 0, 200.0D);
        });
        poseStack.popPose();
    }

    @Override
    public void resize(Minecraft minecraft, int width, int height) {
        this.stack.forEach(screen -> screen.resize(minecraft, width, height));
    }

    @Override
    protected void init() {
        this.stack.forEach(screen -> screen.init(Minecraft.getInstance(), this.width, this.height));
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if(this.stack.size() == 0)
            return false;
        return this.stack.peek().mouseClicked(mouseX, mouseY, button);
    }

    @Override
    public boolean mouseDragged(double startX, double startY, int button, double endX, double endY) {
        if(this.stack.size() == 0)
            return false;
        return this.stack.peek().mouseDragged(startX, startY, button, endX, endY);
    }

    @Override
    public boolean mouseReleased(double mouseX, double mouseY, int button) {
        if(this.stack.size() == 0)
            return false;
        return this.stack.peek().mouseReleased(mouseX, mouseY, button);
    }

    @Override
    public boolean keyPressed(int i, int j, int k) {
        if(this.stack.size() == 0)
            return false;
        return this.stack.peek().keyPressed(i, j, k);
    }

    @Override
    public Component getTitle() {
        if(this.stack.size() == 0)
            return super.getTitle();
        return this.stack.peek().getTitle();
    }

    @Override
    public void onClose() {
        this.onClose.run();
    }
}
