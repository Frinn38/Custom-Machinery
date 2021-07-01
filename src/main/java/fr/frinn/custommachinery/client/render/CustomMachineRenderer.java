package fr.frinn.custommachinery.client.render;

import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.vertex.IVertexBuilder;
import com.mojang.datafixers.util.Pair;
import fr.frinn.custommachinery.common.data.CustomMachine;
import fr.frinn.custommachinery.common.init.CustomMachineTile;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.*;
import net.minecraft.client.renderer.model.IBakedModel;
import net.minecraft.client.renderer.tileentity.TileEntityRenderer;
import net.minecraft.client.renderer.tileentity.TileEntityRendererDispatcher;
import net.minecraft.item.ItemStack;
import net.minecraft.state.properties.BlockStateProperties;
import net.minecraft.util.Direction;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.vector.Vector3f;
import net.minecraftforge.client.model.data.IModelData;
import net.minecraftforge.client.model.data.ModelDataMap;

import javax.annotation.ParametersAreNonnullByDefault;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;
import java.util.concurrent.atomic.AtomicInteger;

public class CustomMachineRenderer extends TileEntityRenderer<CustomMachineTile> {

    private static final Random RAND = new Random();
    private static final Map<ResourceLocation, Pair<AxisAlignedBB, AtomicInteger>> boxToRender = new HashMap<>();

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
        MachineRenderer.INSTANCE.renderMachineBlock(tile.getWorld(), tile.getPos(), machineFacing, matrix, buffer, combinedOverlay, tile.getModelData());
        if(boxToRender.containsKey(machine.getId())) {
            WorldRenderer.drawBoundingBox(matrix, buffer.getBuffer(RenderType.LINES), boxToRender.get(machine.getId()).getFirst().expand(1, 1, 1), 1.0F, 0.0F, 0.0F, 1.0F);
            if(boxToRender.get(machine.getId()).getSecond().decrementAndGet() == 0)
                boxToRender.remove(machine.getId());
        }
        matrix.pop();
    }

    public static void addRenderBox(ResourceLocation machine, AxisAlignedBB box) {
        boxToRender.put(machine, Pair.of(box, new AtomicInteger(200)));
    }
}


