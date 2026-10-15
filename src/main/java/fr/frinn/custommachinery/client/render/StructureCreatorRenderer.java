package fr.frinn.custommachinery.client.render;

import com.mojang.blaze3d.vertex.PoseStack;

public class StructureCreatorRenderer {

    public static void renderSelectedBlocks(PoseStack pose) {
        /*
        if(Minecraft.getInstance().player != null && Minecraft.getInstance().player.getMainHandItem().getItem() == CMRegistration.STRUCTURE_CREATOR_ITEM.get()) {
            MultiBufferSource.BufferSource buffer = Minecraft.getInstance().renderBuffers().bufferSource();
            VertexConsumer builder = buffer.getBuffer(RenderTypes.THICK_LINES);
            Vec3 playerPos = Minecraft.getInstance().gameRenderer.getMainCamera().getPosition();
            List<BlockPos> blocks = StructureCreatorItem.getSelectedBlocks(Minecraft.getInstance().player.getMainHandItem());
            blocks.forEach(pos -> {
                AABB box = new AABB(pos);
                pose.pushPose();
                pose.translate(-playerPos.x(), -playerPos.y(), -playerPos.z());
                LevelRenderer.renderLineBox(pose, builder, box, 1.0F, 0.0F, 0.0F, 1.0F);
                pose.popPose();
            });
            buffer.endBatch(RenderTypes.THICK_LINES);
        }

         */
    }
}
