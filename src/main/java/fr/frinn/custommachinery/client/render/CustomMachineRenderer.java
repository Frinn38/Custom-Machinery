package fr.frinn.custommachinery.client.render;

import com.google.common.util.concurrent.AtomicDouble;
import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.datafixers.util.Pair;
import fr.frinn.custommachinery.common.data.CustomMachine;
import fr.frinn.custommachinery.common.init.CustomMachineTile;
import fr.frinn.custommachinery.common.util.CycleTimer;
import fr.frinn.custommachinery.common.util.PartialBlockState;
import fr.frinn.custommachinery.common.util.ingredient.IIngredient;
import net.minecraft.client.renderer.IRenderTypeBuffer;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.WorldRenderer;
import net.minecraft.client.renderer.tileentity.TileEntityRenderer;
import net.minecraft.client.renderer.tileentity.TileEntityRendererDispatcher;
import net.minecraft.state.properties.BlockStateProperties;
import net.minecraft.util.Direction;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.vector.Vector3f;

import javax.annotation.ParametersAreNonnullByDefault;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

public class CustomMachineRenderer extends TileEntityRenderer<CustomMachineTile> {

    private static final Map<ResourceLocation, Pair<AxisAlignedBB, AtomicDouble>> boxToRender = new HashMap<>();
    private static final Map<ResourceLocation, StructureRenderer> blocksToRender = new HashMap<>();
    private static final CycleTimer timer = new CycleTimer(0);

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
        matrix.rotate(Vector3f.YN.rotationDegrees(machineFacing.getHorizontalAngle()));
        matrix.translate(-0.5F, 0, -0.5F);
        if(boxToRender.containsKey(machine.getId())) {
            WorldRenderer.drawBoundingBox(matrix, buffer.getBuffer(RenderType.LINES), boxToRender.get(machine.getId()).getFirst().expand(1, 1, 1), 1.0F, 0.0F, 0.0F, 1.0F);
            if(boxToRender.get(machine.getId()).getSecond().addAndGet(-partialTicks) <= 0)
                boxToRender.remove(machine.getId());
        }
        matrix.pop();
        if(blocksToRender.containsKey(machine.getId())) {
            StructureRenderer structureRenderer = blocksToRender.get(machine.getId());
            if(structureRenderer.shouldRender())
                structureRenderer.render(matrix, buffer, machineFacing, tile.getWorld(), tile.getPos());
            else
                blocksToRender.remove(machine.getId());
        }
    }

    public static void addRenderBox(ResourceLocation machine, AxisAlignedBB box) {
        boxToRender.put(machine, Pair.of(box, new AtomicDouble(200)));
    }

    public static void addRenderBlock(ResourceLocation machine, Function<Direction, Map<BlockPos, IIngredient<PartialBlockState>>> blocks) {
        blocksToRender.put(machine, new StructureRenderer(20000, blocks));
    }
}


