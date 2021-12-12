package fr.frinn.custommachinery.client.render;

import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.vertex.IVertexBuilder;
import fr.frinn.custommachinery.client.RenderTypes;
import fr.frinn.custommachinery.common.init.Registration;
import fr.frinn.custommachinery.common.init.StructureCreatorItem;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.IRenderTypeBuffer;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.WorldRenderer;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraft.util.math.vector.Vector3f;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.RenderWorldLastEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import java.util.List;

@Mod.EventBusSubscriber(Dist.CLIENT)
public class StructureCreatorRenderer {

    @SubscribeEvent
    public static void renderSelectedBlocks(RenderWorldLastEvent event) {
        if(Minecraft.getInstance().player != null && Minecraft.getInstance().player.getHeldItemMainhand().getItem() == Registration.STRUCTURE_CREATOR_ITEM.get()) {
            MatrixStack matrix = event.getMatrixStack();
            IRenderTypeBuffer.Impl buffer = Minecraft.getInstance().getRenderTypeBuffers().getBufferSource();
            IVertexBuilder builder = buffer.getBuffer(RenderTypes.THICK_LINES);
            Vector3d playerPos = Minecraft.getInstance().gameRenderer.getActiveRenderInfo().getProjectedView();
            List<BlockPos> blocks = StructureCreatorItem.getSelectedBlocks(Minecraft.getInstance().player.getHeldItemMainhand());
            blocks.forEach(pos -> {
                AxisAlignedBB box = new AxisAlignedBB(pos);
                matrix.push();
                matrix.translate(-playerPos.getX(), -playerPos.getY(), -playerPos.getZ());
                WorldRenderer.drawBoundingBox(matrix, builder, box, 1.0F, 0.0F, 0.0F, 1.0F);
                matrix.pop();
            });
            buffer.finish(RenderTypes.THICK_LINES);
        }
    }
}
