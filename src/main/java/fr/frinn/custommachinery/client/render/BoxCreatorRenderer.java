package fr.frinn.custommachinery.client.render;

import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.vertex.IVertexBuilder;
import fr.frinn.custommachinery.CustomMachinery;
import fr.frinn.custommachinery.client.RenderTypes;
import fr.frinn.custommachinery.common.init.BoxCreatorItem;
import fr.frinn.custommachinery.common.init.Registration;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.IRenderTypeBuffer;
import net.minecraft.client.renderer.WorldRenderer;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.RenderWorldLastEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(Dist.CLIENT)
public class BoxCreatorRenderer {

    @SubscribeEvent
    public static void renderSelectedBlocks(final RenderWorldLastEvent event) {
        if(Minecraft.getInstance().player != null && Minecraft.getInstance().player.getHeldItemMainhand().getItem() == Registration.BOX_CREATOR_ITEM.get()) {
            MatrixStack matrix = event.getMatrixStack();
            IRenderTypeBuffer.Impl buffer = Minecraft.getInstance().getRenderTypeBuffers().getBufferSource();
            IVertexBuilder builder = buffer.getBuffer(RenderTypes.THICK_LINES);
            Vector3d playerPos = Minecraft.getInstance().gameRenderer.getActiveRenderInfo().getProjectedView();
            ItemStack stack = Minecraft.getInstance().player.getHeldItemMainhand();
            CompoundNBT nbt = stack.getChildTag(CustomMachinery.MODID);
            if(nbt == null || nbt.isEmpty())
                return;
            BlockPos block1 = BoxCreatorItem.getSelectedBlock(true, stack);
            if(block1 != null) {
                AxisAlignedBB box = new AxisAlignedBB(block1);
                matrix.push();
                matrix.translate(-playerPos.getX(), -playerPos.getY(), -playerPos.getZ());
                WorldRenderer.drawBoundingBox(matrix, builder, box, 0.0F, 0.0F, 1.0F, 1.0F);
                matrix.pop();
            }

            BlockPos block2 = BoxCreatorItem.getSelectedBlock(false, stack);
            if(block2 != null) {
                AxisAlignedBB box = new AxisAlignedBB(block2);
                matrix.push();
                matrix.translate(-playerPos.getX(), -playerPos.getY(), -playerPos.getZ());
                WorldRenderer.drawBoundingBox(matrix, builder, box, 1.0F, 0.0F, 0.0F, 1.0F);
                matrix.pop();
            }
            buffer.finish(RenderTypes.THICK_LINES);
        }
    }
}
