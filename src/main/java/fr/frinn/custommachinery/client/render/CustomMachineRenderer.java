package fr.frinn.custommachinery.client.render;

import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.vertex.IVertexBuilder;
import com.mojang.datafixers.util.Pair;
import fr.frinn.custommachinery.CustomMachinery;
import fr.frinn.custommachinery.common.data.CustomMachine;
import fr.frinn.custommachinery.common.data.MachineAppearance;
import fr.frinn.custommachinery.common.init.CustomMachineTile;
import fr.frinn.custommachinery.common.init.Registration;
import fr.frinn.custommachinery.common.util.Color3F;
import net.minecraft.block.BlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.*;
import net.minecraft.client.renderer.model.BakedQuad;
import net.minecraft.client.renderer.model.IBakedModel;
import net.minecraft.client.renderer.model.ModelResourceLocation;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.client.renderer.tileentity.TileEntityRenderer;
import net.minecraft.client.renderer.tileentity.TileEntityRendererDispatcher;
import net.minecraft.item.ItemStack;
import net.minecraft.state.properties.BlockStateProperties;
import net.minecraft.util.Direction;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.vector.Vector3f;
import net.minecraft.world.IBlockDisplayReader;
import net.minecraftforge.client.model.data.EmptyModelData;
import net.minecraftforge.client.model.data.IModelData;
import net.minecraftforge.client.model.data.ModelDataMap;

import javax.annotation.ParametersAreNonnullByDefault;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Function;
import java.util.stream.Stream;

public class CustomMachineRenderer extends TileEntityRenderer<CustomMachineTile> {

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
        renderMachineBlock(machine, tile.getWorld(), tile.getPos(), matrix, buffer, combinedOverlay, tile.getModelData());
        if(boxToRender.containsKey(machine.getId())) {
            WorldRenderer.drawBoundingBox(matrix, buffer.getBuffer(RenderType.LINES), boxToRender.get(machine.getId()).getFirst().expand(1, 1, 1), 1.0F, 0.0F, 0.0F, 1.0F);
            if(boxToRender.get(machine.getId()).getSecond().decrementAndGet() == 0)
                boxToRender.remove(machine.getId());
        }
        matrix.pop();
    }

    public static void renderMachineBlock(CustomMachine machine, IBlockDisplayReader world, BlockPos pos, MatrixStack matrix, IRenderTypeBuffer buffer, int combinedOverlay, IModelData data) {
        IBakedModel model = CustomMachineBakedModel.INSTANCE;
        BlockState state = world.getBlockState(pos);
        IVertexBuilder builder = buffer.getBuffer(RenderType.getSolid());
        Minecraft.getInstance().getBlockRendererDispatcher().getBlockModelRenderer().renderModel(world, model, state, pos, matrix, builder, true, RAND, 0, combinedOverlay, data);
    }

    public static void renderMachineItem(CustomMachine machine, ItemStack stack, MatrixStack matrix, IRenderTypeBuffer buffer, int combinedLight, int combinedOverlay) {
        IBakedModel model = CustomMachineBakedModel.INSTANCE;
        IVertexBuilder builder = ItemRenderer.getEntityGlintVertexBuilder(buffer, RenderTypeLookup.func_239219_a_(stack, false), true, stack.hasEffect());
        IModelData data = new ModelDataMap.Builder().withInitial(CustomMachineBakedModel.APPEARANCE, machine.getAppearance()).build();

        for(Direction direction : Direction.values()) {
            RAND.setSeed(42L);
            Minecraft.getInstance().getItemRenderer().renderQuads(matrix, builder, model.getQuads(null, direction, RAND, data), stack, combinedLight, combinedOverlay);
        }
        RAND.setSeed(0L);
        Minecraft.getInstance().getItemRenderer().renderQuads(matrix, builder, model.getQuads(null, null, RAND, data), stack, combinedLight, combinedOverlay);
    }

    public static void addRenderBox(ResourceLocation machine, AxisAlignedBB box) {
        boxToRender.put(machine, Pair.of(box, new AtomicInteger(200)));
    }
}
