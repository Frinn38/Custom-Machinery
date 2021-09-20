package fr.frinn.custommachinery.client.render;

import com.mojang.blaze3d.matrix.MatrixStack;
import fr.frinn.custommachinery.common.config.CMConfig;
import fr.frinn.custommachinery.common.init.CustomMachineTile;
import fr.frinn.custommachinery.common.util.PartialBlockState;
import fr.frinn.custommachinery.common.util.ingredient.IIngredient;
import net.minecraft.client.renderer.IRenderTypeBuffer;
import net.minecraft.client.renderer.tileentity.TileEntityRenderer;
import net.minecraft.client.renderer.tileentity.TileEntityRendererDispatcher;
import net.minecraft.state.properties.BlockStateProperties;
import net.minecraft.util.Direction;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;

import javax.annotation.ParametersAreNonnullByDefault;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

public class CustomMachineRenderer extends TileEntityRenderer<CustomMachineTile> {

    private static final Map<ResourceLocation, BoxRenderer> boxToRender = new HashMap<>();
    private static final Map<ResourceLocation, StructureRenderer> blocksToRender = new HashMap<>();

    public CustomMachineRenderer(TileEntityRendererDispatcher dispatcher) {
        super(dispatcher);
    }

    @ParametersAreNonnullByDefault
    @Override
    public void render(CustomMachineTile tile, float partialTicks, MatrixStack matrix, IRenderTypeBuffer buffer, int combinedLight, int combinedOverlay) {
        if(tile.getWorld() == null)
            return;
        ResourceLocation machineId = tile.getId();
        Direction machineFacing = tile.getBlockState().get(BlockStateProperties.HORIZONTAL_FACING);
        if(boxToRender.containsKey(machineId)) {
            BoxRenderer boxRenderer = boxToRender.get(machineId);
            if(boxRenderer.shouldRender())
                boxRenderer.render(matrix, buffer, machineFacing);
            else
                boxToRender.remove(machineId);
        }
        if(blocksToRender.containsKey(machineId)) {
            StructureRenderer structureRenderer = blocksToRender.get(machineId);
            if(structureRenderer.shouldRender())
                structureRenderer.render(matrix, buffer, machineFacing, tile.getWorld(), tile.getPos());
            else
                blocksToRender.remove(machineId);
        }
    }

    public static void addRenderBox(ResourceLocation machine, AxisAlignedBB box) {
        boxToRender.put(machine, new BoxRenderer(CMConfig.INSTANCE.boxRenderTime.get(), box));
    }

    public static void addRenderBlock(ResourceLocation machine, Function<Direction, Map<BlockPos, IIngredient<PartialBlockState>>> blocks) {
        blocksToRender.put(machine, new StructureRenderer(CMConfig.INSTANCE.structureRenderTime.get(), blocks));
    }
}


