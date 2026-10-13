package fr.frinn.custommachinery.client.screen.pip;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.QuadInstance;
import com.mojang.blaze3d.vertex.VertexConsumer;
import fr.frinn.custommachinery.client.screen.pip.BlockStatePIPRenderer.BlockStateRenderState;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.navigation.ScreenRectangle;
import net.minecraft.client.gui.render.pip.PictureInPictureRenderer;
import net.minecraft.client.renderer.MultiBufferSource.BufferSource;
import net.minecraft.client.renderer.block.dispatch.BlockStateModel;
import net.minecraft.client.renderer.block.dispatch.BlockStateModelPart;
import net.minecraft.client.renderer.rendertype.RenderTypes;
import net.minecraft.client.renderer.state.gui.pip.PictureInPictureRenderState;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

public class BlockStatePIPRenderer extends PictureInPictureRenderer<BlockStateRenderState> {

    public BlockStatePIPRenderer(BufferSource bufferSource) {
        super(bufferSource);
    }

    @Override
    public Class<BlockStateRenderState> getRenderStateClass() {
        return BlockStateRenderState.class;
    }

    @Override
    protected void renderToTexture(BlockStateRenderState state, PoseStack pose) {
        if(Minecraft.getInstance().level == null)
            return;
        VertexConsumer consumer = this.bufferSource.getBuffer(RenderTypes.solidMovingBlock());
        QuadInstance quadInstance = new QuadInstance();
        List<BlockStateModelPart> parts = new ArrayList<>();
        state.model.collectParts(Minecraft.getInstance().level, BlockPos.ZERO, Blocks.AIR.defaultBlockState(), RandomSource.create(), parts);
        parts.forEach(part -> {
            for(Direction side : Direction.values())
                part.getQuads(side).forEach(quad -> consumer.putBakedQuad(pose.last(), quad, quadInstance));
            part.getQuads(null).forEach(quad -> consumer.putBakedQuad(pose.last(), quad, quadInstance));

        });
    }

    @Override
    protected String getTextureLabel() {
        return "BlockState";
    }

    public record BlockStateRenderState(int x0, int x1, int y0, int y1, float scale, @Nullable ScreenRectangle scissorArea, @Nullable ScreenRectangle bounds, BlockStateModel model) implements PictureInPictureRenderState {
        public BlockStateRenderState(BlockStateModel model) {
            this(0, 16, 0, 16, 1f, null, null, model);
        }

        public BlockStateRenderState(BlockState state) {
            this(Minecraft.getInstance().getModelManager().getBlockStateModelSet().get(state));
        }
    }
}
