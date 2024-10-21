package fr.frinn.custommachinery.client.render;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import fr.frinn.custommachinery.CustomMachinery;
import fr.frinn.custommachinery.client.RenderTypes;
import fr.frinn.custommachinery.common.config.CMConfig;
import fr.frinn.custommachinery.common.util.CycleTimer;
import fr.frinn.custommachinery.common.util.PartialBlockState;
import fr.frinn.custommachinery.common.util.ingredient.BlockIngredient;
import fr.frinn.custommachinery.common.util.ingredient.IIngredient;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.client.resources.model.ModelResourceLocation;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.pattern.BlockInWorld;
import net.neoforged.neoforge.client.model.data.ModelData;

import java.util.Arrays;
import java.util.Map;
import java.util.function.Function;

public class StructureRenderer {

    private final int time;
    private final long start;
    private final Function<Direction, Map<BlockPos, IIngredient<PartialBlockState>>> blocksGetter;
    private final CycleTimer timer;

    public StructureRenderer(int time, Function<Direction, Map<BlockPos, IIngredient<PartialBlockState>>> blocksGetter) {
        this.time = time;
        this.start = System.currentTimeMillis();
        this.blocksGetter = blocksGetter;
        this.timer = new CycleTimer(CMConfig.CONFIG.blockTagCycleTime);
    }

    public void render(PoseStack matrix, MultiBufferSource buffer, Direction direction, Level world, BlockPos machinePos) {
        Map<BlockPos, IIngredient<PartialBlockState>> blocks = this.blocksGetter.apply(direction);
        this.timer.onDraw();
        blocks.forEach((pos, ingredient) -> {
            matrix.pushPose();
            matrix.translate(pos.getX(), pos.getY(), pos.getZ());
            if(!(pos.getX() == 0 && pos.getY() == 0 && pos.getZ() == 0) && ingredient != BlockIngredient.ANY) {
                PartialBlockState state = timer.get(ingredient.getAll());
                BlockPos blockPos = machinePos.offset(pos);
                if(state != null && state != PartialBlockState.ANY && !state.getBlockState().isAir()) {
                    if(world.getBlockState(blockPos).isAir())
                        renderTransparentBlock(state, matrix, buffer);
                    else if(ingredient.getAll().stream().noneMatch(test -> test.test(new BlockInWorld(world, blockPos, false))))
                        renderNope(matrix, buffer);
                }
            }
            matrix.popPose();
        });
    }

    private void renderTransparentBlock(PartialBlockState state, PoseStack matrix, MultiBufferSource buffer) {
            VertexConsumer builder = buffer.getBuffer(RenderTypes.PHANTOM);
            matrix.translate(0.1F, 0.1F, 0.1F);
            matrix.scale(0.8F, 0.8F, 0.8F);
            BakedModel model = Minecraft.getInstance().getBlockRenderer().getBlockModel(state.getBlockState());
            if(model != Minecraft.getInstance().getModelManager().getMissingModel()) {
                Arrays.stream(Direction.values())
                        .flatMap(direction -> model.getQuads(state.getBlockState(), direction, RandomSource.create(42L)).stream())
                        .forEach(quad -> builder.putBulkData(matrix.last(), quad, 1.0F, 1.0F, 1.0F, 1.0F, LightTexture.FULL_BRIGHT, OverlayTexture.NO_OVERLAY, false));
                model.getQuads(state.getBlockState(), null, RandomSource.create(42L))
                        .forEach(quad -> builder.putBulkData(matrix.last(), quad, 1.0F, 1.0F, 1.0F, 1.0F, LightTexture.FULL_BRIGHT, OverlayTexture.NO_OVERLAY, false));
            }
    }

    private void renderNope(PoseStack matrix, MultiBufferSource buffer) {
        VertexConsumer builder = buffer.getBuffer(RenderTypes.NOPE);
        BakedModel model = Minecraft.getInstance().getModelManager().getModel(ModelResourceLocation.standalone(CustomMachinery.rl("block/nope")));
        matrix.translate(-0.0005, -0.0005, -0.0005);
        matrix.scale(1.001F, 1.001F, 1.001F);
        Arrays.stream(Direction.values())
                .flatMap(direction -> model.getQuads(null, direction, RandomSource.create(42L), ModelData.EMPTY, null).stream())
                .forEach(quad -> builder.putBulkData(matrix.last(), quad, 1.0F, 1.0F, 1.0F, 0.8F, LightTexture.FULL_BRIGHT, OverlayTexture.NO_OVERLAY, false));
        model.getQuads(null, null, RandomSource.create(42L), ModelData.EMPTY, null)
                .forEach(quad -> builder.putBulkData(matrix.last(), quad, 1.0F, 1.0F, 1.0F, 0.8F, LightTexture.FULL_BRIGHT, OverlayTexture.NO_OVERLAY, false));
    }

    public boolean shouldRender() {
        return System.currentTimeMillis() < this.start + this.time;
    }
}
