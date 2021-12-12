package fr.frinn.custommachinery.client.render;

import com.mojang.blaze3d.matrix.MatrixStack;
import net.minecraft.client.renderer.IRenderTypeBuffer;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.WorldRenderer;
import net.minecraft.util.Direction;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.vector.Vector3f;

public class BoxRenderer {

    private final int time;
    private final long start;
    private final AxisAlignedBB box;

    public BoxRenderer(int time, AxisAlignedBB box) {
        this.time = time;
        this.start = System.currentTimeMillis();
        this.box = box.expand(1, 1, 1);
    }

    public void render(MatrixStack matrix, IRenderTypeBuffer buffer, Direction machineFacing) {
        matrix.push();
        matrix.translate(0.5F, 0, 0.5F);
        matrix.rotate(Vector3f.YN.rotationDegrees(machineFacing.getHorizontalAngle()));
        matrix.translate(-0.5F, 0, -0.5F);
        WorldRenderer.drawBoundingBox(matrix, buffer.getBuffer(RenderType.LINES), this.box, 1.0F, 0.0F, 0.0F, 1.0F);
        matrix.pop();
    }

    public boolean shouldRender() {
        return System.currentTimeMillis() < this.start + this.time;
    }
}
