package fr.frinn.custommachinery.client.render;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import fr.frinn.custommachinery.CustomMachinery;
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
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.RenderLevelLastEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(Dist.CLIENT)
public class BoxCreatorRenderer {

    @SubscribeEvent
    public static void renderSelectedBlocks(final RenderLevelLastEvent event) {
        if(Minecraft.getInstance().player != null && Minecraft.getInstance().player.getMainHandItem().getItem() == Registration.BOX_CREATOR_ITEM.get()) {
            PoseStack matrix = event.getPoseStack();
            MultiBufferSource.BufferSource buffer = Minecraft.getInstance().renderBuffers().bufferSource();
            VertexConsumer builder = buffer.getBuffer(RenderTypes.THICK_LINES);
            Vec3 playerPos = Minecraft.getInstance().gameRenderer.getMainCamera().getPosition();
            ItemStack stack = Minecraft.getInstance().player.getMainHandItem();
            CompoundTag nbt = stack.getTagElement(CustomMachinery.MODID);
            if(nbt == null || nbt.isEmpty())
                return;
            BlockPos block1 = BoxCreatorItem.getSelectedBlock(true, stack);
            if(block1 != null) {
                AABB box = new AABB(block1);
                matrix.pushPose();
                matrix.translate(-playerPos.x(), -playerPos.y(), -playerPos.z());
                LevelRenderer.renderLineBox(matrix, builder, box, 0.0F, 0.0F, 1.0F, 1.0F);
                matrix.popPose();
            }

            BlockPos block2 = BoxCreatorItem.getSelectedBlock(false, stack);
            if(block2 != null) {
                AABB box = new AABB(block2);
                matrix.pushPose();
                matrix.translate(-playerPos.x(), -playerPos.y(), -playerPos.z());
                LevelRenderer.renderLineBox(matrix, builder, box, 1.0F, 0.0F, 0.0F, 1.0F);
                matrix.popPose();
            }

            if(block1 != null && block2 != null) {
                AABB box = new AABB(block1, block2).expandTowards(1.0D, 1.0D, 1.0D);
                matrix.pushPose();
                matrix.translate(-playerPos.x(), -playerPos.y(), -playerPos.z());
                LevelRenderer.renderLineBox(matrix, builder, box, 0.0F, 1.0F, 0.0F, 1.0F);
                matrix.popPose();
            }

            buffer.endBatch(RenderTypes.THICK_LINES);
        }
    }
}
