package fr.frinn.custommachinery.client.render;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.datafixers.util.Pair;
import fr.frinn.custommachinery.CustomMachinery;
import fr.frinn.custommachinery.client.RenderTypes;
import fr.frinn.custommachinery.common.config.CMConfig;
import fr.frinn.custommachinery.common.util.BlockIngredient;
import fr.frinn.custommachinery.common.util.CycleTimer;
import fr.frinn.custommachinery.common.util.PartialBlockState;
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
import java.util.List;
import java.util.Map;
import java.util.function.Function;

public class StructureRenderer {

    private final int time;
    private final long start;
    private final Function<Direction, Map<BlockPos, List<BlockIngredient>>> blocksGetter;
    private final CycleTimer timer;

    public StructureRenderer(int time, Function<Direction, Map<BlockPos, List<BlockIngredient>>> blocksGetter) {
        this.time = time;
        this.start = System.currentTimeMillis();
        this.blocksGetter = blocksGetter;
        this.timer = new CycleTimer(CMConfig.CONFIG.blockTagCycleTime);
    }

    public void render(PoseStack pose, MultiBufferSource buffer, Direction direction, Level world, BlockPos machinePos) {
        Map<BlockPos, List<BlockIngredient>> blocks = this.blocksGetter.apply(direction);
        this.timer.onDraw();
        blocks.forEach((pos, ingredients) -> {
            pose.pushPose();
            pose.translate(pos.getX(), pos.getY(), pos.getZ());
            if(!(pos.getX() == 0 && pos.getY() == 0 && pos.getZ() == 0) && !ingredients.isEmpty() && ingredients.getFirst() != BlockIngredient.ANY) {
                Pair<PartialBlockState, Boolean> pair = this.timer.get(ingredients.stream().flatMap(blockIngredient -> blockIngredient.getAll().stream().map(state -> Pair.of(state, blockIngredient.not()))).toList());
                BlockPos blockPos = machinePos.offset(pos);
                if(pair != null && pair.getFirst() != PartialBlockState.ANY) {
                    if(world.getBlockState(blockPos).isAir())
                        if(pair.getSecond())
                            renderTransparentNotBlock(pair.getFirst(), pose, buffer);
                        else
                            renderTransparentBlock(pair.getFirst(), pose, buffer);
                    else if(ingredients.stream().noneMatch(blockIngredient -> blockIngredient.test(new BlockInWorld(world, blockPos, false))))
                        renderNope(pose, buffer);
                }
            }
            pose.popPose();
        });
    }

    private void renderTransparentBlock(PartialBlockState state, PoseStack pose, MultiBufferSource buffer) {
            VertexConsumer builder = buffer.getBuffer(RenderTypes.PHANTOM);
            pose.translate(0.1F, 0.1F, 0.1F);
            pose.scale(0.8F, 0.8F, 0.8F);
            BakedModel model = Minecraft.getInstance().getBlockRenderer().getBlockModel(state.getBlockState());
            if(model != Minecraft.getInstance().getModelManager().getMissingModel()) {
                Arrays.stream(Direction.values())
                        .flatMap(direction -> model.getQuads(state.getBlockState(), direction, RandomSource.create(42L)).stream())
                        .forEach(quad -> builder.putBulkData(pose.last(), quad, 1.0F, 1.0F, 1.0F, 1.0F, LightTexture.FULL_BRIGHT, OverlayTexture.NO_OVERLAY, false));
                model.getQuads(state.getBlockState(), null, RandomSource.create(42L))
                        .forEach(quad -> builder.putBulkData(pose.last(), quad, 1.0F, 1.0F, 1.0F, 1.0F, LightTexture.FULL_BRIGHT, OverlayTexture.NO_OVERLAY, false));
            }
    }

    private void renderTransparentNotBlock(PartialBlockState state, PoseStack pose, MultiBufferSource buffer) {
        this.renderTransparentBlock(state, pose, buffer);
        this.renderNope(pose, buffer);
    }

    private void renderNope(PoseStack pose, MultiBufferSource buffer) {
        VertexConsumer builder = buffer.getBuffer(RenderTypes.NOPE);
        BakedModel model = Minecraft.getInstance().getModelManager().getModel(ModelResourceLocation.standalone(CustomMachinery.rl("block/nope")));
        pose.translate(-0.0005, -0.0005, -0.0005);
        pose.scale(1.001F, 1.001F, 1.001F);
        Arrays.stream(Direction.values())
                .flatMap(direction -> model.getQuads(null, direction, RandomSource.create(42L), ModelData.EMPTY, null).stream())
                .forEach(quad -> builder.putBulkData(pose.last(), quad, 1.0F, 1.0F, 1.0F, 0.8F, LightTexture.FULL_BRIGHT, OverlayTexture.NO_OVERLAY, false));
        model.getQuads(null, null, RandomSource.create(42L), ModelData.EMPTY, null)
                .forEach(quad -> builder.putBulkData(pose.last(), quad, 1.0F, 1.0F, 1.0F, 0.8F, LightTexture.FULL_BRIGHT, OverlayTexture.NO_OVERLAY, false));
    }

    public boolean shouldRender() {
        return System.currentTimeMillis() < this.start + this.time;
    }
}
