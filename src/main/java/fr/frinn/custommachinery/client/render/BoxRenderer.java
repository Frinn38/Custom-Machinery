package fr.frinn.custommachinery.client.render;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.SharedConstants;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.ShapeRenderer;
import net.minecraft.client.renderer.rendertype.RenderTypes;
import net.minecraft.client.renderer.state.level.BlockOutlineRenderState;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.util.ARGB;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.shapes.Shapes;
import org.joml.Quaternionf;

import java.util.Collections;

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
        matrix.mulPose(new Quaternionf().rotateY((float)Math.toRadians(-machineFacing.toYRot())));
        matrix.translate(-0.5F, 0, -0.5F);
        this.renderHitOutline(matrix, buffer.getBuffer(RenderTypes.lines()), 0.0F, 0.0F, 0.0F, new BlockOutlineRenderState(BlockPos.ZERO, false, false, Shapes.create(this.box), Collections.emptyList()), ARGB.color(255, 0, 0), Minecraft.getInstance().gameRenderer.getGameRenderState().windowRenderState.appropriateLineWidth);
        matrix.popPose();
    }

    public boolean shouldRender() {
        return System.currentTimeMillis() < this.start + this.time;
    }

    private void renderHitOutline(PoseStack poseStack, VertexConsumer builder, double camX, double camY, double camZ, BlockOutlineRenderState state, int color, float width) {
        BlockPos pos = state.pos();
        if(SharedConstants.DEBUG_SHAPES)
            ShapeRenderer.renderShape(poseStack, builder, state.shape(), pos.getX() - camX, pos.getY() - camY, pos.getZ() - camZ, ARGB.colorFromFloat(1.0F, 1.0F, 1.0F, 1.0F), width);
        else
            ShapeRenderer.renderShape(poseStack, builder, state.shape(), pos.getX() - camX, pos.getY() - camY, pos.getZ() - camZ, color, width);
    }
}
