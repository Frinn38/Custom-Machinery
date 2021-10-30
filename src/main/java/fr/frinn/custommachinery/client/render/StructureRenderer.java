package fr.frinn.custommachinery.client.render;

import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.vertex.IVertexBuilder;
import fr.frinn.custommachinery.CustomMachinery;
import fr.frinn.custommachinery.client.RenderTypes;
import fr.frinn.custommachinery.common.config.CMConfig;
import fr.frinn.custommachinery.common.util.CycleTimer;
import fr.frinn.custommachinery.common.util.PartialBlockState;
import fr.frinn.custommachinery.common.util.ingredient.BlockIngredient;
import fr.frinn.custommachinery.common.util.ingredient.IIngredient;
import net.minecraft.block.material.Material;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.IRenderTypeBuffer;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.client.renderer.model.IBakedModel;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.util.CachedBlockInfo;
import net.minecraft.util.Direction;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.client.model.data.EmptyModelData;

import java.util.Arrays;
import java.util.Map;
import java.util.Random;
import java.util.function.Function;

public class StructureRenderer {

    private int time;
    private long start;
    private Function<Direction, Map<BlockPos, IIngredient<PartialBlockState>>> blocksGetter;
    private CycleTimer timer;

    public StructureRenderer(int time, Function<Direction, Map<BlockPos, IIngredient<PartialBlockState>>> blocksGetter) {
        this.time = time;
        this.start = System.currentTimeMillis();
        this.blocksGetter = blocksGetter;
        this.timer = new CycleTimer(CMConfig.INSTANCE.blockTagCycleTime.get());
    }

    public void render(MatrixStack matrix, IRenderTypeBuffer buffer, Direction direction, World world, BlockPos machinePos) {
        Map<BlockPos, IIngredient<PartialBlockState>> blocks = this.blocksGetter.apply(direction);
        this.timer.onDraw();
        blocks.forEach((pos, ingredient) -> {
            matrix.push();
            matrix.translate(pos.getX(), pos.getY(), pos.getZ());
            if(!(pos.getX() == 0 && pos.getY() == 0 && pos.getZ() == 0) && ingredient != BlockIngredient.ANY) {
                PartialBlockState state = timer.get(ingredient.getAll());
                BlockPos blockPos = machinePos.add(pos);
                if(state != null && state != PartialBlockState.ANY && state.getBlockState().getMaterial() != Material.AIR) {
                    if(world.getBlockState(blockPos).getMaterial() == Material.AIR)
                        renderTransparentBlock(state, matrix, buffer);
                    else if(ingredient.getAll().stream().noneMatch(test -> test.test(new CachedBlockInfo(world, blockPos, false))))
                        renderNope(matrix, buffer);
                }
            }
            matrix.pop();
        });
    }

    private void renderTransparentBlock(PartialBlockState state, MatrixStack matrix, IRenderTypeBuffer buffer) {
            IVertexBuilder builder = buffer.getBuffer(RenderTypes.PHANTOM);
            matrix.translate(0.1F, 0.1F, 0.1F);
            matrix.scale(0.8F, 0.8F, 0.8F);
            IBakedModel model = Minecraft.getInstance().getBlockRendererDispatcher().getModelForState(state.getBlockState());
            if(model != Minecraft.getInstance().getModelManager().getMissingModel()) {
                Arrays.stream(Direction.values())
                        .flatMap(direction -> model.getQuads(state.getBlockState(), direction, new Random(42L), EmptyModelData.INSTANCE).stream())
                        .forEach(quad -> builder.addVertexData(matrix.getLast(), quad, 1.0F, 1.0F, 1.0F, 0.8F, LightTexture.packLight(15, 15), OverlayTexture.NO_OVERLAY));
                model.getQuads(state.getBlockState(), null, new Random(42L), EmptyModelData.INSTANCE)
                        .forEach(quad -> builder.addVertexData(matrix.getLast(), quad, 1.0F, 1.0F, 1.0F, 0.8F, LightTexture.packLight(15, 15), OverlayTexture.NO_OVERLAY));
            }
    }

    private void renderNope(MatrixStack matrix, IRenderTypeBuffer buffer) {
        IVertexBuilder builder = buffer.getBuffer(RenderTypes.NOPE);
        IBakedModel model = Minecraft.getInstance().getModelManager().getModel(new ResourceLocation(CustomMachinery.MODID, "block/nope"));
        matrix.translate(-0.0005, -0.0005, -0.0005);
        matrix.scale(1.001F, 1.001F, 1.001F);
        Arrays.stream(Direction.values())
                .flatMap(direction -> model.getQuads(null, direction, new Random(42L), EmptyModelData.INSTANCE).stream())
                .forEach(quad -> builder.addVertexData(matrix.getLast(), quad, 1.0F, 1.0F, 1.0F, 1.0F, LightTexture.packLight(15, 15), OverlayTexture.NO_OVERLAY));
        model.getQuads(null, null, new Random(42L), EmptyModelData.INSTANCE)
                .forEach(quad -> builder.addVertexData(matrix.getLast(), quad, 1.0F, 1.0F, 1.0F, 1.0F, LightTexture.packLight(15, 15), OverlayTexture.NO_OVERLAY));
    }

    public boolean shouldRender() {
        return System.currentTimeMillis() < this.start + this.time;
    }
}
