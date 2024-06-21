package fr.frinn.custommachinery.client.render;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import fr.frinn.custommachinery.client.RenderTypes;
import fr.frinn.custommachinery.common.init.BoxCreatorItem;
import fr.frinn.custommachinery.common.init.Registration;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.LevelRenderer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

public class BoxCreatorRenderer {

    public static void renderSelectedBlocks(PoseStack pose) {
        if(Minecraft.getInstance().player != null && Minecraft.getInstance().player.getMainHandItem().getItem() == Registration.BOX_CREATOR_ITEM.get()) {
            MultiBufferSource.BufferSource buffer = Minecraft.getInstance().renderBuffers().bufferSource();
            VertexConsumer builder = buffer.getBuffer(RenderTypes.THICK_LINES);
            Vec3 playerPos = Minecraft.getInstance().gameRenderer.getMainCamera().getPosition();
            ItemStack stack = Minecraft.getInstance().player.getMainHandItem();
            CompoundTag nbt = null;//stack.getTagElement(CustomMachinery.MODID);
            if(nbt == null || nbt.isEmpty())
                return;
            BlockPos block1 = BoxCreatorItem.getSelectedBlock(true, stack);
            if(block1 != null) {
                AABB box = new AABB(block1);
                pose.pushPose();
                pose.translate(-playerPos.x(), -playerPos.y(), -playerPos.z());
                LevelRenderer.renderLineBox(pose, builder, box, 0.0F, 0.0F, 1.0F, 1.0F);
                pose.popPose();
            }

            BlockPos block2 = BoxCreatorItem.getSelectedBlock(false, stack);
            if(block2 != null) {
                AABB box = new AABB(block2);
                pose.pushPose();
                pose.translate(-playerPos.x(), -playerPos.y(), -playerPos.z());
                LevelRenderer.renderLineBox(pose, builder, box, 1.0F, 0.0F, 0.0F, 1.0F);
                pose.popPose();
            }

            if(block1 != null && block2 != null) {
                AABB box = new AABB(block1.getX(), block1.getY(), block1.getZ(), block2.getX(), block2.getY(), block2.getZ()).expandTowards(1.0D, 1.0D, 1.0D);
                pose.pushPose();
                pose.translate(-playerPos.x(), -playerPos.y(), -playerPos.z());
                LevelRenderer.renderLineBox(pose, builder, box, 0.0F, 1.0F, 0.0F, 1.0F);
                pose.popPose();
            }

            buffer.endBatch(RenderTypes.THICK_LINES);
        }
    }
}
