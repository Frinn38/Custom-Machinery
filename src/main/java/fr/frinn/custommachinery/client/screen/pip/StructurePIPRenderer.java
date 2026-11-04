package fr.frinn.custommachinery.client.screen.pip;

import com.mojang.blaze3d.platform.Lighting;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.QuadInstance;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;
import fr.frinn.custommachinery.client.screen.pip.StructurePIPRenderer.StructureRenderState;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.navigation.ScreenRectangle;
import net.minecraft.client.gui.render.pip.PictureInPictureRenderer;
import net.minecraft.client.renderer.MultiBufferSource.BufferSource;
import net.minecraft.client.renderer.block.dispatch.BlockStateModel;
import net.minecraft.client.renderer.block.dispatch.BlockStateModelPart;
import net.minecraft.client.renderer.Sheets;
import net.minecraft.client.renderer.block.BlockAndTintGetter;
import net.minecraft.client.resources.model.geometry.BakedQuad;
import net.minecraft.client.renderer.state.gui.pip.PictureInPictureRenderState;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.block.state.BlockState;
import org.joml.Matrix3x2f;
import org.joml.Vector2f;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Renders a whole multiblock structure (a map of positions to block models) in a gui, rotated around its center.
 */
public class StructurePIPRenderer extends PictureInPictureRenderer<StructureRenderState> {

    public StructurePIPRenderer(BufferSource bufferSource) {
        super(bufferSource);
    }

    @Override
    public Class<StructureRenderState> getRenderStateClass() {
        return StructureRenderState.class;
    }

    @Override
    protected void renderToTexture(StructureRenderState state, PoseStack pose) {
        if(Minecraft.getInstance().level == null || state.blocks.isEmpty())
            return;
        Minecraft.getInstance().gameRenderer.getLighting().setupFor(Lighting.Entry.ITEMS_3D);

        pose.mulPose(Axis.XP.rotationDegrees(210F));
        pose.mulPose(Axis.YP.rotation(state.yaw));
        pose.translate(-state.center.x, -state.center.y, -state.center.z);

        QuadInstance quadInstance = new QuadInstance();
        List<BlockStateModelPart> parts = new ArrayList<>();
        RandomSource random = RandomSource.create();
        state.blocks.forEach((blockPos, block) -> {
            parts.clear();
            block.model().collectParts(BlockAndTintGetter.EMPTY, BlockPos.ZERO, block.state(), random, parts);
            VertexConsumer consumer = this.bufferSource.getBuffer(block.model().hasMaterialFlag(BakedQuad.FLAG_TRANSLUCENT) ? Sheets.translucentBlockSheet() : Sheets.cutoutBlockSheet());
            pose.pushPose();
            pose.translate(blockPos.getX(), blockPos.getY(), blockPos.getZ());
            for(BlockStateModelPart part : parts) {
                for(Direction side : Direction.values())
                    part.getQuads(side).forEach(quad -> consumer.putBakedQuad(pose.last(), quad, quadInstance));
                part.getQuads(null).forEach(quad -> consumer.putBakedQuad(pose.last(), quad, quadInstance));
            }
            pose.popPose();
        });
        this.bufferSource.endBatch();
    }

    @Override
    protected float getTranslateY(int height, int guiScale) {
        return height / 2.0F;
    }

    @Override
    protected String getTextureLabel() {
        return "Structure";
    }

    public record PreviewBlock(BlockState state, BlockStateModel model) {}

    public record StructureRenderState(int x0, int x1, int y0, int y1, float scale, Matrix3x2f pose, @Nullable ScreenRectangle scissorArea, @Nullable ScreenRectangle bounds, Map<BlockPos, PreviewBlock> blocks, org.joml.Vector3f center, float yaw) implements PictureInPictureRenderState {

        public StructureRenderState(int x0, int x1, int y0, int y1, float scale, Matrix3x2f pose, @Nullable ScreenRectangle scissorArea, Map<BlockPos, PreviewBlock> blocks, org.joml.Vector3f center, float yaw) {
            this(x0, x1, y0, y1, scale, new Matrix3x2f(pose), scissorArea, computeBounds(x0, x1, y0, y1, pose, scissorArea), blocks, center, yaw);
        }

        private static @Nullable ScreenRectangle computeBounds(int x0, int x1, int y0, int y1, Matrix3x2f pose, @Nullable ScreenRectangle scissorArea) {
            Vector2f min = pose.transformPosition(x0, y0, new Vector2f());
            Vector2f max = pose.transformPosition(x1, y1, new Vector2f());
            int left = (int)Math.floor(Math.min(min.x, max.x));
            int top = (int)Math.floor(Math.min(min.y, max.y));
            int right = (int)Math.ceil(Math.max(min.x, max.x));
            int bottom = (int)Math.ceil(Math.max(min.y, max.y));
            ScreenRectangle bounds = new ScreenRectangle(left, top, right - left, bottom - top);
            return scissorArea != null ? scissorArea.intersection(bounds) : bounds;
        }
    }
}
