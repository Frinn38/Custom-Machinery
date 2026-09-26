package fr.frinn.custommachinery.client.render;

import com.mojang.blaze3d.vertex.PoseStack;
import fr.frinn.custommachinery.client.render.CustomMachineRenderer.CustomMachineRenderState;
import fr.frinn.custommachinery.common.config.CMConfig;
import fr.frinn.custommachinery.common.init.CustomMachineTile;
import fr.frinn.custommachinery.common.machine.CustomMachine;
import fr.frinn.custommachinery.common.util.BlockIngredient;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.blockentity.state.BlockEntityRenderState;
import net.minecraft.client.renderer.feature.ModelFeatureRenderer;
import net.minecraft.client.renderer.state.level.CameraRenderState;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.resources.Identifier;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;

public class CustomMachineRenderer implements BlockEntityRenderer<CustomMachineTile, CustomMachineRenderState> {

    private static final Map<Identifier, BoxRenderer> boxToRender = new HashMap<>();
    private static final Map<Identifier, StructureRenderer> blocksToRender = new HashMap<>();

    public CustomMachineRenderer(BlockEntityRendererProvider.Context ignoredContext) {

    }

    @Override
    public CustomMachineRenderState createRenderState() {
        return new CustomMachineRenderState();
    }

    @Override
    public void extractRenderState(CustomMachineTile be, CustomMachineRenderState state, float partialTicks, Vec3 cameraPosition, @Nullable ModelFeatureRenderer.CrumblingOverlay breakProgress) {
        BlockEntityRenderer.super.extractRenderState(be, state, partialTicks, cameraPosition, breakProgress);
        state.machineId = be.getId();
        state.facing = be.getBlockState().getValue(BlockStateProperties.HORIZONTAL_FACING);
    }

    @Override
    public void submit(CustomMachineRenderState state, PoseStack pose, SubmitNodeCollector collector, CameraRenderState renderState) {
        if(boxToRender.containsKey(state.machineId)) {
            BoxRenderer boxRenderer = boxToRender.get(state.machineId);
            if(boxRenderer.shouldRender())
                boxRenderer.render(pose, Minecraft.getInstance().renderBuffers().bufferSource(), state.facing);
            else
                boxToRender.remove(state.machineId);
        }
        if(blocksToRender.containsKey(state.machineId)) {
            StructureRenderer structureRenderer = blocksToRender.get(state.machineId);
            if(structureRenderer.shouldRender())
                structureRenderer.render(pose, Minecraft.getInstance().renderBuffers().bufferSource(), state.facing, Minecraft.getInstance().level, state.blockPos);
            else
                blocksToRender.remove(state.machineId);
        }
    }

    public static void addRenderBox(Identifier machine, AABB box) {
        boxToRender.put(machine, new BoxRenderer(CMConfig.CONFIG.boxRenderTime.get(), box));
    }

    public static void addRenderBlock(Identifier machine, Function<Direction, Map<BlockPos, List<BlockIngredient>>> blocks) {
        blocksToRender.put(machine, new StructureRenderer(CMConfig.CONFIG.structureRenderTime.get(), blocks));
    }

    public static class CustomMachineRenderState extends BlockEntityRenderState {

        public Identifier machineId = CustomMachine.DUMMY_ID;
        public Direction facing = Direction.NORTH;

    }
}


