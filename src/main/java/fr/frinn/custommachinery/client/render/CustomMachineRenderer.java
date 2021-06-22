package fr.frinn.custommachinery.client.render;

import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.vertex.IVertexBuilder;
import com.mojang.datafixers.util.Pair;
import fr.frinn.custommachinery.CustomMachinery;
import fr.frinn.custommachinery.common.data.CustomMachine;
import fr.frinn.custommachinery.common.data.MachineAppearance;
import fr.frinn.custommachinery.common.init.CustomMachineTile;
import fr.frinn.custommachinery.common.util.Color3F;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.BlockModelShapes;
import net.minecraft.client.renderer.IRenderTypeBuffer;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.WorldRenderer;
import net.minecraft.client.renderer.model.IBakedModel;
import net.minecraft.client.renderer.tileentity.TileEntityRenderer;
import net.minecraft.client.renderer.tileentity.TileEntityRendererDispatcher;
import net.minecraft.state.properties.BlockStateProperties;
import net.minecraft.util.Direction;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.vector.Vector3f;
import net.minecraftforge.client.model.data.EmptyModelData;

import javax.annotation.ParametersAreNonnullByDefault;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Function;
import java.util.stream.Stream;

public class CustomMachineRenderer extends TileEntityRenderer<CustomMachineTile> {

    public static final ResourceLocation DEFAULT_MODEL = new ResourceLocation(CustomMachinery.MODID, "block/custom_machine_block");
    private static final Random RAND = new Random();
    private static final Map<ResourceLocation, Pair<AxisAlignedBB, AtomicInteger>> boxToRender = new HashMap<>();

    public CustomMachineRenderer(TileEntityRendererDispatcher dispatcher) {
        super(dispatcher);
    }

    @ParametersAreNonnullByDefault
    @Override
    public void render(CustomMachineTile tile, float partialTicks, MatrixStack matrix, IRenderTypeBuffer buffer, int combinedLight, int combinedOverlay) {
        CustomMachine machine = tile.getMachine();
        matrix.push();
        matrix.translate(0.5F, 0, 0.5F);
        matrix.rotate(Vector3f.YN.rotationDegrees(tile.getBlockState().get(BlockStateProperties.HORIZONTAL_FACING).getHorizontalAngle()));
        matrix.translate(-0.5F, 0, -0.5F);
        renderMachine(machine, partialTicks, matrix, buffer, combinedLight, combinedOverlay, tintIndex -> Color3F.of(Minecraft.getInstance().getBlockColors().getColor(tile.getBlockState(), tile.getWorld(), tile.getPos(), tintIndex)));
        if(boxToRender.containsKey(machine.getId())) {
            WorldRenderer.drawBoundingBox(matrix, buffer.getBuffer(RenderType.LINES), boxToRender.get(machine.getId()).getFirst().expand(1, 1, 1), 1.0F, 0.0F, 0.0F, 1.0F);
            if(boxToRender.get(machine.getId()).getSecond().decrementAndGet() == 0)
                boxToRender.remove(machine.getId());
        }
        matrix.pop();
    }

    public static void renderMachine(CustomMachine machine, float partialTicks, MatrixStack matrix, IRenderTypeBuffer buffer, int combinedLight, int combinedOverlay, Function<Integer, Color3F> blockColor) {
        IBakedModel machineModel = getMachineModel(machine.getAppearance());
        IVertexBuilder builder = buffer.getBuffer(RenderType.getSolid());

        machineModel.getQuads(null, null, RAND, EmptyModelData.INSTANCE).forEach(quad -> {
            Color3F color = Color3F.of(1.0F, 1.0F, 1.0F);
            if(quad.hasTintIndex())
                color = blockColor.apply(quad.getTintIndex());
            builder.addQuad(matrix.getLast(), quad, color.getRed(), color.getGreen(), color.getBlue(), combinedLight, combinedOverlay);
        });
        Stream.of(Direction.values()).forEach(side -> machineModel.getQuads(null, side, RAND, EmptyModelData.INSTANCE).forEach(quad -> {
            Color3F color = Color3F.of(1.0F, 1.0F, 1.0F);
            if(quad.hasTintIndex())
                color = blockColor.apply(quad.getTintIndex());
            builder.addQuad(matrix.getLast(), quad, color.getRed(), color.getGreen(), color.getBlue(), combinedLight, combinedOverlay);
        }));
    }

    public static IBakedModel getMachineModel(MachineAppearance appearance) {
        IBakedModel machineModel;
        switch (appearance.getType()) {
            case BLOCK:
                machineModel =  Minecraft.getInstance().getModelManager().getModel(BlockModelShapes.getModelLocation(appearance.getBlock().getDefaultState()));
                break;
            case BLOCKSTATE:
                machineModel =  Minecraft.getInstance().getModelManager().getModel(appearance.getBlockstate());
                break;
            case MODEL:
                machineModel = Minecraft.getInstance().getModelManager().getModel(appearance.getModel());
                break;
            default:
                machineModel = Minecraft.getInstance().getModelManager().getModel(DEFAULT_MODEL);
                break;
        }
        if(machineModel == Minecraft.getInstance().getModelManager().getMissingModel())
            machineModel = Minecraft.getInstance().getModelManager().getModel(DEFAULT_MODEL);
        return machineModel;
    }

    public static void addRenderBox(ResourceLocation machine, AxisAlignedBB box) {
        boxToRender.put(machine, Pair.of(box, new AtomicInteger(200)));
    }
}
