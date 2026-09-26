package fr.frinn.custommachinery.client.render;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import fr.frinn.custommachinery.common.init.BoxCreatorItem;
import fr.frinn.custommachinery.common.init.CMRegistration;
import net.minecraft.SharedConstants;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.ShapeRenderer;
import net.minecraft.client.renderer.rendertype.RenderTypes;
import net.minecraft.client.renderer.state.level.BlockOutlineRenderState;
import net.minecraft.core.BlockPos;
import net.minecraft.util.ARGB;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.Shapes;

import java.util.Collections;

public class BoxCreatorRenderer {

    public static void renderSelectedBlocks(PoseStack pose) {
        if(Minecraft.getInstance().player != null && Minecraft.getInstance().player.getMainHandItem().getItem() == CMRegistration.BOX_CREATOR_ITEM.get()) {
            MultiBufferSource.BufferSource buffer = Minecraft.getInstance().renderBuffers().bufferSource();
            VertexConsumer builder = buffer.getBuffer(RenderTypes.lines());
            Vec3 playerPos = Minecraft.getInstance().gameRenderer.getMainCamera().position();
            ItemStack stack = Minecraft.getInstance().player.getMainHandItem();
            BlockPos block1 = BoxCreatorItem.getSelectedBlock(true, stack);
            if(block1 != BlockPos.ZERO) {
                AABB box = new AABB(block1);
                pose.pushPose();
                pose.translate(-playerPos.x(), -playerPos.y(), -playerPos.z());
                BlockOutlineRenderState state = new BlockOutlineRenderState(BlockPos.ZERO, false, false, Shapes.create(box), Collections.emptyList());
                renderHitOutline(pose, builder, 0.0F, 0.0F, 0.0F, state, ARGB.color(0, 0, 255), 10.0F);
                pose.popPose();
            }

            BlockPos block2 = BoxCreatorItem.getSelectedBlock(false, stack);
            if(block2 != BlockPos.ZERO) {
                AABB box = new AABB(block2);
                pose.pushPose();
                pose.translate(-playerPos.x(), -playerPos.y(), -playerPos.z());
                BlockOutlineRenderState state = new BlockOutlineRenderState(BlockPos.ZERO, false, false, Shapes.create(box), Collections.emptyList());
                renderHitOutline(pose, builder, 0.0F, 0.0F, 0.0F, state, ARGB.color(0, 0, 255), 10.0F);
                pose.popPose();
            }

            if(block1 != BlockPos.ZERO && block2 != BlockPos.ZERO) {
                AABB box = new AABB(block1.getX(), block1.getY(), block1.getZ(), block2.getX(), block2.getY(), block2.getZ()).expandTowards(1.0D, 1.0D, 1.0D);
                pose.pushPose();
                pose.translate(-playerPos.x(), -playerPos.y(), -playerPos.z());
                BlockOutlineRenderState state = new BlockOutlineRenderState(BlockPos.ZERO, false, false, Shapes.create(box), Collections.emptyList());
                renderHitOutline(pose, builder, 0.0F, 0.0F, 0.0F, state, ARGB.color(0, 0, 255), 10.0F);
                pose.popPose();
            }

            buffer.endBatch(RenderTypes.lines());
        }
    }

    public static void renderHitOutline(PoseStack poseStack, VertexConsumer builder, double camX, double camY, double camZ, BlockOutlineRenderState state, int color, float width) {
        BlockPos pos = state.pos();
        if(SharedConstants.DEBUG_SHAPES)
            ShapeRenderer.renderShape(poseStack, builder, state.shape(), pos.getX() - camX, pos.getY() - camY, pos.getZ() - camZ, ARGB.colorFromFloat(1.0F, 1.0F, 1.0F, 1.0F), width);
        else
            ShapeRenderer.renderShape(poseStack, builder, state.shape(), pos.getX() - camX, pos.getY() - camY, pos.getZ() - camZ, color, width);
    }
}
