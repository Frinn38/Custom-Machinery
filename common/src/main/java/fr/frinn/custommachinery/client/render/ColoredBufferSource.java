package fr.frinn.custommachinery.client.render;

import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.util.FastColor;
import net.minecraft.world.inventory.InventoryMenu;

public class ColoredBufferSource implements MultiBufferSource {

    private final MultiBufferSource.BufferSource wrapped;
    private final int color;

    public ColoredBufferSource(MultiBufferSource.BufferSource wrapped, int color) {
        this.wrapped = wrapped;
        this.color = color;
    }

    @Override
    public VertexConsumer getBuffer(RenderType renderType) {
        if(renderType == RenderType.glintDirect())
            return new ColoredVertexConsumer(this.wrapped.getBuffer(renderType), this.color);
        return new ColoredVertexConsumer(this.wrapped.getBuffer(RenderType.entityTranslucentCull(InventoryMenu.BLOCK_ATLAS)), this.color);
    }

    public void endBatch() {
        this.wrapped.endBatch();
    }

    public static class ColoredVertexConsumer implements VertexConsumer {

        private final VertexConsumer wrapped;
        private final int red;
        private final int green;
        private final int blue;
        private final int alpha;

        public ColoredVertexConsumer(VertexConsumer wrapped, int color) {
            this.wrapped = wrapped;
            this.red = FastColor.ARGB32.red(color);
            this.green = FastColor.ARGB32.green(color);
            this.blue = FastColor.ARGB32.blue(color);
            this.alpha = FastColor.ARGB32.alpha(color);
        }

        @Override
        public VertexConsumer vertex(double x, double y, double z) {
            this.wrapped.vertex(x, y, z);
            return this;
        }

        @Override
        public VertexConsumer color(int r, int g, int b, int a) {
            this.wrapped.color(r * red / 255, g * green / 255, b * blue / 255, a * alpha / 255);
            return this;
        }

        @Override
        public VertexConsumer uv(float u, float v) {
            this.wrapped.uv(u, v);
            return this;
        }

        @Override
        public VertexConsumer overlayCoords(int u, int v) {
            this.wrapped.overlayCoords(u, v);
            return this;
        }

        @Override
        public VertexConsumer uv2(int u, int v) {
            this.wrapped.uv2(u, v);
            return this;
        }

        @Override
        public VertexConsumer normal(float x, float y, float z) {
            this.wrapped.normal(x, y, z);
            return this;
        }

        @Override
        public void endVertex() {
            this.wrapped.endVertex();
        }

        @Override
        public void defaultColor(int r, int g, int b, int a) {
            this.wrapped.defaultColor(r * red / 255, g * green / 255, b * blue / 255, a * alpha / 255);
        }

        @Override
        public void unsetDefaultColor() {
            this.wrapped.unsetDefaultColor();
        }
    }
}
