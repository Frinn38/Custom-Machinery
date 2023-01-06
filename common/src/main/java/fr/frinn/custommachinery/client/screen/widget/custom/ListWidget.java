package fr.frinn.custommachinery.client.screen.widget.custom;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.BufferBuilder;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.Tesselator;
import com.mojang.blaze3d.vertex.VertexFormat.Mode;
import fr.frinn.custommachinery.client.screen.widget.custom.ListWidget.Entry;
import fr.frinn.custommachinery.common.util.Color3F;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.util.Mth;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;
import java.util.function.Supplier;

public abstract class ListWidget<T, E extends Entry<T>> extends Widget {

    private final List<E> entries = new ArrayList<>();

    private Consumer<T> callback = null;
    private int scrollbarOffsetX;
    private int scrollbarOffsetY;
    private int entryHeight = 20;
    @Nullable
    private E selected = null;
    private double scrollAmount = 0;
    private boolean scrolling = false;

    public ListWidget(Supplier<Integer> x, Supplier<Integer> y, int width, int height) {
        super(x, y, width, height);
        this.scrollbarOffsetX = width - 6;
        this.scrollbarOffsetY = 0;
    }

    public ListWidget<T, E> selectionCallback(Consumer<T> callback) {
        this.callback = callback;
        return this;
    }

    public ListWidget<T, E> scrollbarOffset(int x, int y) {
        this.scrollbarOffsetX = x;
        this.scrollbarOffsetY = y;
        return this;
    }

    public ListWidget<T, E> entryHeight(int height) {
        this.entryHeight = 20;
        return this;
    }

    public abstract void add(T value);

    public void add(E entry) {
        this.entries.add(entry);
    }

    public void remove(T value) {
        this.entries.removeIf(entry -> entry.getValue() == value);
        if(this.selected == value)
            this.selected = null;
    }

    public void setSelected(E entry) {
        this.selected = entry;
        if(this.callback != null)
            this.callback.accept(entry.getValue());
    }

    public void setSelected(T value) {
        for(E entry : this.entries)
            if(entry.getValue() == value)
                this.selected = entry;
    }

    @Nullable
    public T getSelected() {
        return this.selected == null ? null : this.selected.getValue();
    }

    public int getMaxScroll() {
        return Math.max(0, this.entries.size() * this.entryHeight - this.height);
    }

    @Nullable
    protected final E getEntryAtPosition(double mouseX, double mouseY) {
        int x = this.getX();
        int y = Mth.floor(mouseY - this.getY()) + (int)this.scrollAmount - 4;
        int index = y / this.entryHeight;
        return mouseX < this.getX() + this.scrollbarOffsetX && mouseX >= x && mouseX <= x + this.width && index >= 0 && y >= 0 && index < this.entries.size() ? this.entries.get(index) : null;
    }

    public void render(PoseStack pose, int mouseX, int mouseY, float partialTicks) {
        double s = Minecraft.getInstance().getWindow().getGuiScale();
        int screenHeight = Minecraft.getInstance().getWindow().getScreenHeight() / (int)s;
        RenderSystem.enableScissor(this.getX() * (int)s, (screenHeight - this.getY() - this.height) * (int)s, this.width * (int)s, this.height * (int)s);

        renderEntries(pose, mouseX, mouseY, partialTicks);
        renderScrollbar(pose, this.getX() + this.scrollbarOffsetX, this.getY() + this.scrollbarOffsetY, 6, this.height, partialTicks);

        RenderSystem.disableScissor();
    }

    private void renderEntries(PoseStack pose, int mouseX, int mouseY, float partialTicks) {
        for(int i = 0; i < this.entries.size(); i++) {
            int x = this.getX();
            int y = this.getY() + (int)this.scrollAmount + i * this.entryHeight;
            if((y >= this.getY() && y <= this.getY() + this.height) || (y + this.entryHeight >= this.getY() && y + this.entryHeight <= this.getY() + this.height)) {
                E entry = this.entries.get(i);
                if(this.selected == entry)
                    renderSelection(pose, x, y, partialTicks);
                entry.render(pose, x, y, this.width, this.entryHeight, mouseX, mouseY, partialTicks);
            }
        }
    }

    private void renderSelection(PoseStack pose, int x, int y, float partialTicks) {
        Color3F color = Color3F.of(100, 100, 100);
        RenderSystem.setShader(GameRenderer::getPositionColorShader);
        RenderSystem.setShaderColor(1f, 1f, 1f, 1f);
        BufferBuilder builder = Tesselator.getInstance().getBuilder();
        builder.begin(Mode.QUADS, DefaultVertexFormat.POSITION_COLOR);
        builder.vertex(pose.last().pose(), x, y + this.entryHeight, 0).color(color.getRed(), color.getGreen(), color.getBlue(), 1.0F).endVertex();
        builder.vertex(pose.last().pose(), x + this.width, y + this.entryHeight, 0).color(color.getRed(), color.getGreen(), color.getBlue(), 1.0F).endVertex();
        builder.vertex(pose.last().pose(), x + this.width, y, 0).color(color.getRed(), color.getGreen(), color.getBlue(), 1.0F).endVertex();
        builder.vertex(pose.last().pose(), x, y, 0).color(color.getRed(), color.getGreen(), color.getBlue(), 1.0F).endVertex();
        Tesselator.getInstance().end();
    }

    private void renderScrollbar(PoseStack pose, int x, int y, int width, int height, float partialTicks) {
        int maxScroll = this.getMaxScroll();
        if(maxScroll <= 0)
            return;

        RenderSystem.disableTexture();
        RenderSystem.setShader(GameRenderer::getPositionColorShader);
        int barTop = height * height / (this.entries.size() * this.entryHeight);
        barTop = Mth.clamp(barTop, 32, height - 8);
        int barBottom = (int)this.scrollAmount * (height - barTop) / maxScroll + y;
        if (barBottom < y) {
            barBottom = y;
        }

        Tesselator tesselator = Tesselator.getInstance();
        BufferBuilder builder = tesselator.getBuilder();

        builder.begin(Mode.QUADS, DefaultVertexFormat.POSITION_COLOR);
        builder.vertex(x, y + height, 0.0).color(0, 0, 0, 255).endVertex();
        builder.vertex(x + width, y + height, 0.0).color(0, 0, 0, 255).endVertex();
        builder.vertex(x + width, y, 0.0).color(0, 0, 0, 255).endVertex();
        builder.vertex(x, y, 0.0).color(0, 0, 0, 255).endVertex();

        builder.vertex(x, barBottom + barTop, 0.0).color(128, 128, 128, 255).endVertex();
        builder.vertex(x + width, barBottom + barTop, 0.0).color(128, 128, 128, 255).endVertex();
        builder.vertex(x + width, barBottom, 0.0).color(128, 128, 128, 255).endVertex();
        builder.vertex(x, barBottom, 0.0).color(128, 128, 128, 255).endVertex();

        builder.vertex(x, barBottom + barTop - 1, 0.0).color(192, 192, 192, 255).endVertex();
        builder.vertex((x + width - 1), barBottom + barTop - 1, 0.0).color(192, 192, 192, 255).endVertex();
        builder.vertex((x + width - 1), barBottom, 0.0).color(192, 192, 192, 255).endVertex();
        builder.vertex(x, barBottom, 0.0).color(192, 192, 192, 255).endVertex();
        tesselator.end();
    }

    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        this.scrolling = button == 0 && mouseX >= this.getX() + this.scrollbarOffsetX && mouseX <= this.getX() + this.scrollbarOffsetX + 6;
        if (!this.isMouseOver(mouseX, mouseY)) {
            return false;
        } else {
            E entry = this.getEntryAtPosition(mouseX, mouseY);
            if (entry != null)
                this.setSelected(entry);
            return this.scrolling;
        }
    }

    @Override
    public boolean mouseDragged(double mouseX, double mouseY, int button, double dragX, double dragY) {
        if (button == 0 && this.scrolling) {
            if (mouseY < this.getY()) {
                this.scrollAmount = 0.0;
            } else if (mouseY > this.getY() + this.height) {
                this.scrollAmount = this.getMaxScroll();
            } else {
                double d = Math.max(1, this.getMaxScroll());
                int j = Mth.clamp(this.height * this.height / (this.entries.size() * this.entryHeight), 32, this.height - 8);
                double e = Math.max(1.0, d / (double)(this.height - j));
                this.scrollAmount = this.scrollAmount + dragY * e;
            }

            return true;
        } else {
            return super.mouseDragged(mouseX, mouseY, button, dragX, dragY);
        }
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double delta) {
        this.scrollAmount = this.scrollAmount - delta * this.entryHeight / 2.0;
        return true;
    }

    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        if (keyCode == 264) {
            this.scrollAmount -= 20;
            return true;
        } else if (keyCode == 265) {
            this.scrollAmount += 20;
            return true;
        } else {
            return super.keyPressed(keyCode, scanCode, modifiers);
        }
    }

    public static abstract class Entry<T> {

        private final T value;

        public Entry(T value) {
            this.value = value;
        }

        public T getValue() {
            return this.value;
        }

        public abstract void render(PoseStack pose, int x, int y, int width, int height, int mouseX, int mouseY, float partialTicks);
    }
}
