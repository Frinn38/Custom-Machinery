package fr.frinn.custommachinery.client.render;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Vector3f;
import net.minecraft.client.renderer.LevelRenderer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.core.Direction;
import net.minecraft.world.phys.AABB;

public class BoxRenderer {

    private final int time;
    private final long start;
    private final AABB box;

    public BoxRenderer(int time, AABB box) {
        this.time = time;
        this.start = System.currentTimeMillis();
        this.box = box.expandTowards(1, 1, 1);
    }

    public void render(PoseStack matrix, MultiBufferSource buffer, Direction machineFacing) {
        matrix.pushPose();
        matrix.translate(0.5F, 0, 0.5F);
        matrix.mulPose(Vector3f.YN.rotationDegrees(machineFacing.toYRot()));
        matrix.translate(-0.5F, 0, -0.5F);
        LevelRenderer.renderLineBox(matrix, buffer.getBuffer(RenderType.LINES), this.box, 1.0F, 0.0F, 0.0F, 1.0F);
        matrix.popPose();
    }

    public boolean shouldRender() {
        return System.currentTimeMillis() < this.start + this.time;
    }
}
