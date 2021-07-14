package fr.frinn.custommachinery.client.render;

import com.google.common.util.concurrent.AtomicDouble;
import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.vertex.IVertexBuilder;
import com.mojang.datafixers.util.Pair;
import fr.frinn.custommachinery.CustomMachinery;
import fr.frinn.custommachinery.client.RenderTypes;
import fr.frinn.custommachinery.common.data.CustomMachine;
import fr.frinn.custommachinery.common.init.CustomMachineTile;
import fr.frinn.custommachinery.common.util.PartialBlockState;
import net.minecraft.block.Blocks;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.*;
import net.minecraft.client.renderer.model.IBakedModel;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.client.renderer.tileentity.TileEntityRenderer;
import net.minecraft.client.renderer.tileentity.TileEntityRendererDispatcher;
import net.minecraft.state.properties.BlockStateProperties;
import net.minecraft.util.CachedBlockInfo;
import net.minecraft.util.Direction;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.vector.Vector3f;
import net.minecraft.world.World;
import net.minecraftforge.client.model.data.EmptyModelData;
import org.spongepowered.asm.mixin.injection.At;

import javax.annotation.ParametersAreNonnullByDefault;
import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Function;
import java.util.stream.Stream;

public class CustomMachineRenderer extends TileEntityRenderer<CustomMachineTile> {

    private static final Map<ResourceLocation, Pair<AxisAlignedBB, AtomicDouble>> boxToRender = new HashMap<>();
    private static final Map<ResourceLocation, Pair<Function<Direction, Map<BlockPos, PartialBlockState>>, AtomicDouble>> blocksToRender = new HashMap<>();

    public CustomMachineRenderer(TileEntityRendererDispatcher dispatcher) {
        super(dispatcher);
    }

    @ParametersAreNonnullByDefault
    @Override
    public void render(CustomMachineTile tile, float partialTicks, MatrixStack matrix, IRenderTypeBuffer buffer, int combinedLight, int combinedOverlay) {
        if(tile.getWorld() == null)
            return;
        CustomMachine machine = tile.getMachine();
        Direction machineFacing = tile.getBlockState().get(BlockStateProperties.HORIZONTAL_FACING);
        matrix.push();
        matrix.translate(0.5F, 0, 0.5F);
        matrix.rotate(Vector3f.YN.rotationDegrees(machineFacing.getOpposite().getHorizontalAngle()));
        matrix.translate(-0.5F, 0, -0.5F);
        if(boxToRender.containsKey(machine.getId())) {
            WorldRenderer.drawBoundingBox(matrix, buffer.getBuffer(RenderType.LINES), boxToRender.get(machine.getId()).getFirst().expand(1, 1, 1), 1.0F, 0.0F, 0.0F, 1.0F);
            if(boxToRender.get(machine.getId()).getSecond().addAndGet(-partialTicks) <= 0)
                boxToRender.remove(machine.getId());
        }
        matrix.pop();
        if(blocksToRender.containsKey(machine.getId())) {
            Map<BlockPos, PartialBlockState> blocks = blocksToRender.get(machine.getId()).getFirst().apply(machineFacing);
            blocks.forEach((pos, state) -> {
                matrix.push();
                matrix.translate(pos.getX(), pos.getY(), pos.getZ());
                if(!(pos.getX() == 0 && pos.getY() == 0 && pos.getZ() == 0) && state != PartialBlockState.ANY) {
                    renderTransparentBlock(tile.getWorld(), tile.getPos().add(pos), state, matrix, buffer);
                }
                matrix.pop();
            });
            if(blocksToRender.get(machine.getId()).getSecond().addAndGet(-partialTicks) <= 0)
                blocksToRender.remove(machine.getId());
        }
    }

    private static void renderTransparentBlock(World world, BlockPos pos, PartialBlockState state, MatrixStack matrix, IRenderTypeBuffer buffer) {
        if(world.getBlockState(pos).matchesBlock(Blocks.AIR) && state.getBlockState().getBlock() != Blocks.AIR) {
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
        } else if(!state.test(new CachedBlockInfo(world, pos, false))) {
            IVertexBuilder builder = buffer.getBuffer(RenderTypes.NOPE);
            IBakedModel model = Minecraft.getInstance().getModelManager().getModel(new ResourceLocation(CustomMachinery.MODID, "block/nope"));
            matrix.translate(-0.0005, -0.0005, -0.0005);
            matrix.scale(1.001F, 1.001F, 1.001F);
            Arrays.stream(Direction.values())
                    .flatMap(direction -> model.getQuads(Blocks.STONE.getDefaultState(), direction, new Random(42L), EmptyModelData.INSTANCE).stream())
                    .forEach(quad -> builder.addVertexData(matrix.getLast(), quad, 1.0F, 1.0F, 1.0F, 1.0F, LightTexture.packLight(15, 15), OverlayTexture.NO_OVERLAY));
            model.getQuads(state.getBlockState(), null, new Random(42L), EmptyModelData.INSTANCE)
                    .forEach(quad -> builder.addVertexData(matrix.getLast(), quad, 1.0F, 1.0F, 1.0F, 1.0F, LightTexture.packLight(15, 15), OverlayTexture.NO_OVERLAY));
        }
    }

    public static void addRenderBox(ResourceLocation machine, AxisAlignedBB box) {
        boxToRender.put(machine, Pair.of(box, new AtomicDouble(200)));
    }

    public static void addRenderBlock(ResourceLocation machine, Function<Direction, Map<BlockPos, PartialBlockState>> blocks) {
        blocksToRender.put(machine, Pair.of(blocks, new AtomicDouble(2000)));
    }
}


