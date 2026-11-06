package fr.frinn.custommachinery.client.screen.pip;

import com.mojang.blaze3d.platform.Lighting;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.QuadInstance;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;
import fr.frinn.custommachinery.api.machine.ICustomMachine;
import fr.frinn.custommachinery.client.screen.pip.StructurePIPRenderer.StructureRenderState;
import fr.frinn.custommachinery.common.config.CMConfig;
import fr.frinn.custommachinery.common.util.BlockIngredient;
import fr.frinn.custommachinery.common.util.BlockStructure;
import fr.frinn.custommachinery.common.util.CycleTimer;
import fr.frinn.custommachinery.common.util.PartialBlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.navigation.ScreenRectangle;
import net.minecraft.client.gui.render.pip.PictureInPictureRenderer;
import net.minecraft.client.renderer.MultiBufferSource.BufferSource;
import net.minecraft.client.renderer.Sheets;
import net.minecraft.client.renderer.block.BlockAndTintGetter;
import net.minecraft.client.renderer.block.dispatch.BlockStateModel;
import net.minecraft.client.renderer.block.dispatch.BlockStateModelPart;
import net.minecraft.client.renderer.state.gui.pip.PictureInPictureRenderState;
import net.minecraft.client.resources.model.geometry.BakedQuad;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.levelgen.structure.BoundingBox;
import org.jetbrains.annotations.Nullable;
import org.joml.Matrix3x2f;
import org.joml.Vector2f;
import org.joml.Vector3f;

import java.util.ArrayList;
import java.util.List;

/**
 * Renders a whole multiblock structure (a map of positions to block models) in a gui, rotated around its center.
 */
public class StructurePIPRenderer extends PictureInPictureRenderer<StructureRenderState> {

    private final CycleTimer timer;

    public StructurePIPRenderer(BufferSource bufferSource) {
        super(bufferSource);
        this.timer = new CycleTimer(CMConfig.CONFIG.blockTagCycleTime);
    }

    @Override
    public Class<StructureRenderState> getRenderStateClass() {
        return StructureRenderState.class;
    }

    @Override
    protected void renderToTexture(StructureRenderState state, PoseStack pose) {
        if(Minecraft.getInstance().level == null || state.structure.getBlocks(Direction.NORTH).isEmpty())
            return;

        this.timer.onDraw();
        Minecraft.getInstance().gameRenderer.getLighting().setupFor(Lighting.Entry.ITEMS_3D);

        Vector3f center = state.structure.box().getCenter().getCenter().toVector3f();
        float yaw = (System.currentTimeMillis() % 12000L) / (float)12000L * (float)(Math.PI * 2);

        pose.mulPose(Axis.XP.rotationDegrees(210F));
        pose.mulPose(Axis.YP.rotation(yaw));
        pose.translate(-center.x, -center.y, -center.z);

        QuadInstance quadInstance = new QuadInstance();
        List<BlockStateModelPart> parts = new ArrayList<>();
        RandomSource random = RandomSource.create();

        state.structure.getBlocks(Direction.NORTH).forEach((pos, ingredients) -> {
            PartialBlockState blockState;
            if(ingredients.size() == 1 && ingredients.getFirst() == BlockIngredient.MACHINE)
                blockState = PartialBlockState.machine(state.machine.getId());
            else
                blockState = this.timer.getOrDefault(ingredients.stream().flatMap(ingredient -> ingredient.getAll().stream()).toList(), PartialBlockState.AIR);

            BlockStateModel model = Minecraft.getInstance().getModelManager().getBlockStateModelSet().get(blockState.getBlockState());
            parts.clear();
            model.collectParts(BlockAndTintGetter.EMPTY, BlockPos.ZERO, blockState.getBlockState(), random, parts);
            VertexConsumer consumer = this.bufferSource.getBuffer(model.hasMaterialFlag(BakedQuad.FLAG_TRANSLUCENT) ? Sheets.translucentBlockSheet() : Sheets.cutoutBlockSheet());
            pose.pushPose();
            pose.translate(pos.getX(), pos.getY(), pos.getZ());
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

    public record StructureRenderState(int x0, int x1, int y0, int y1, float scale, Matrix3x2f pose, @Nullable ScreenRectangle scissorArea, @Nullable ScreenRectangle bounds, BlockStructure structure, ICustomMachine machine) implements PictureInPictureRenderState {

        public StructureRenderState(int x0, int x1, int y0, int y1, Matrix3x2f pose, @Nullable ScreenRectangle scissorArea, BlockStructure structure, ICustomMachine machine) {
            this(x0, x1, y0, y1, computeScale(Math.min(x1 - x0, y1 - y0), structure), new Matrix3x2f(pose), scissorArea, computeBounds(x0, x1, y0, y1, pose, scissorArea), structure, machine);
        }

        @Nullable
        private static ScreenRectangle computeBounds(int x0, int x1, int y0, int y1, Matrix3x2f pose, @Nullable ScreenRectangle scissorArea) {
            Vector2f min = pose.transformPosition(x0, y0, new Vector2f());
            Vector2f max = pose.transformPosition(x1, y1, new Vector2f());
            int left = (int)Math.floor(Math.min(min.x, max.x));
            int top = (int)Math.floor(Math.min(min.y, max.y));
            int right = (int)Math.ceil(Math.max(min.x, max.x));
            int bottom = (int)Math.ceil(Math.max(min.y, max.y));
            ScreenRectangle bounds = new ScreenRectangle(left, top, right - left, bottom - top);
            return scissorArea != null ? scissorArea.intersection(bounds) : bounds;
        }

        private static float computeScale(int size, BlockStructure structure) {
            BoundingBox box = structure.box();
            double sizeX = box.getXSpan();
            double sizeY = box.getYSpan();
            double sizeZ = box.getZSpan();
            float diagonal = (float)Math.sqrt(sizeX * sizeX + sizeY * sizeY + sizeZ * sizeZ);
            return size / diagonal;
        }
    }
}
