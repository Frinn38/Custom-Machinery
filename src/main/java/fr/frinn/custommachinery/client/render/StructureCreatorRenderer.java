package fr.frinn.custommachinery.client.render;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import fr.frinn.custommachinery.client.RenderTypes;
import fr.frinn.custommachinery.common.init.Registration;
import fr.frinn.custommachinery.common.init.StructureCreatorItem;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.LevelRenderer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.core.BlockPos;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.RenderLevelLastEvent;
import net.minecraftforge.client.event.RenderLevelStageEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import java.util.List;

@Mod.EventBusSubscriber(Dist.CLIENT)
public class StructureCreatorRenderer {

    @SubscribeEvent
    public static void renderSelectedBlocks(final RenderLevelStageEvent event) {
        if(event.getStage() != RenderLevelStageEvent.Stage.AFTER_PARTICLES)
            return;
        if(Minecraft.getInstance().player != null && Minecraft.getInstance().player.getMainHandItem().getItem() == Registration.STRUCTURE_CREATOR_ITEM.get()) {
            PoseStack matrix = event.getPoseStack();
            MultiBufferSource.BufferSource buffer = Minecraft.getInstance().renderBuffers().bufferSource();
            VertexConsumer builder = buffer.getBuffer(RenderTypes.THICK_LINES);
            Vec3 playerPos = Minecraft.getInstance().gameRenderer.getMainCamera().getPosition();
            List<BlockPos> blocks = StructureCreatorItem.getSelectedBlocks(Minecraft.getInstance().player.getMainHandItem());
            blocks.forEach(pos -> {
                AABB box = new AABB(pos);
                matrix.pushPose();
                matrix.translate(-playerPos.x(), -playerPos.y(), -playerPos.z());
                LevelRenderer.renderLineBox(matrix, builder, box, 1.0F, 0.0F, 0.0F, 1.0F);
                matrix.popPose();
            });
            buffer.endBatch(RenderTypes.THICK_LINES);
        }
    }
}
