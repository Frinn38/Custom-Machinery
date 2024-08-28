package fr.frinn.custommachinery.impl.util;

import com.mojang.blaze3d.platform.Lighting;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.CrashReport;
import net.minecraft.CrashReportCategory;
import net.minecraft.ReportedException;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.util.FastColor;
import net.minecraft.world.inventory.InventoryMenu;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;

public class FakeItemRenderer {

    public static void render(GuiGraphics graphics, ItemStack stack, int x, int y, int color) {
        if(stack.isEmpty())
            return;

        BakedModel bakedmodel = Minecraft.getInstance().getItemRenderer().getModel(stack, Minecraft.getInstance().level, Minecraft.getInstance().player, 42);
        graphics.pose().pushPose();
        graphics.pose().translate(x + 8, y + 8, 150);

        try {
            graphics.pose().scale(16.0F, -16.0F, 16.0F);
            boolean flatItem = !bakedmodel.usesBlockLight();
            if (flatItem)
                Lighting.setupForFlatItems();

            WrappedBufferSource buffer = new WrappedBufferSource(Minecraft.getInstance().renderBuffers().bufferSource(), color);

            Minecraft.getInstance()
                    .getItemRenderer()
                    .render(stack, ItemDisplayContext.GUI, false, graphics.pose(), buffer, LightTexture.FULL_BRIGHT, OverlayTexture.NO_OVERLAY, bakedmodel);

            buffer.end();

            if(flatItem)
                Lighting.setupFor3DItems();
        } catch (Throwable throwable) {
            CrashReport crashreport = CrashReport.forThrowable(throwable, "Rendering item");
            CrashReportCategory crashreportcategory = crashreport.addCategory("Item being rendered");
            crashreportcategory.setDetail("Item Type", () -> String.valueOf(stack.getItem()));
            crashreportcategory.setDetail("Item Components", () -> String.valueOf(stack.getComponents()));
            crashreportcategory.setDetail("Item Foil", () -> String.valueOf(stack.hasFoil()));
            throw new ReportedException(crashreport);
        }

        graphics.pose().popPose();
    }

    private record WrappedBufferSource(MultiBufferSource.BufferSource wrapped, int color) implements MultiBufferSource {
        @Override
        public VertexConsumer getBuffer(RenderType type) {
            return new WrappedVertexConsumer(this.wrapped.getBuffer(RenderType.entityTranslucentCull(InventoryMenu.BLOCK_ATLAS)), this.color);
        }

        public void end() {
            this.wrapped.endBatch();
        }
    }

    private record WrappedVertexConsumer(VertexConsumer wrapped, int color) implements VertexConsumer {

        @Override
        public VertexConsumer addVertex(float x, float y, float z) {
            this.wrapped.addVertex(x, y, z);
            return this;
        }

        @Override
        public VertexConsumer setColor(int red, int green, int blue, int alpha) {
            this.wrapped.setColor(red * FastColor.ARGB32.red(this.color) / 255, green * FastColor.ARGB32.green(this.color) / 255, blue * FastColor.ARGB32.blue(this.color) / 255, alpha * FastColor.ARGB32.alpha(this.color) / 255);
            return this;
        }

        @Override
        public VertexConsumer setUv(float u, float v) {
            this.wrapped.setUv(u, v);
            return this;
        }

        @Override
        public VertexConsumer setUv1(int u, int v) {
            this.wrapped.setUv1(u, v);
            return this;
        }

        @Override
        public VertexConsumer setUv2(int u, int v) {
            this.wrapped.setUv2(u, v);
            return this;
        }

        @Override
        public VertexConsumer setNormal(float x, float y, float z) {
            this.wrapped.setNormal(x, y, z);
            return this;
        }
    }
}
