package fr.frinn.custommachinery.client.model;

import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import fr.frinn.custommachinery.CustomMachinery;
import fr.frinn.custommachinery.api.machine.MachineStatus;
import fr.frinn.custommachinery.client.ClientHandler;
import fr.frinn.custommachinery.common.init.CustomMachineItem;
import fr.frinn.custommachinery.common.machine.MachineAppearance;
import fr.frinn.custommachinery.common.util.MachineModelLocation.MachineBlockModelLocation;
import fr.frinn.custommachinery.common.util.MachineModelLocation.MachineItemModelLocation;
import fr.frinn.custommachinery.impl.util.IMachineModelLocation;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.block.dispatch.BlockModelRotation;
import net.minecraft.client.renderer.block.dispatch.BlockStateModel;
import net.minecraft.client.renderer.block.dispatch.BlockStateModelPart;
import net.minecraft.client.renderer.item.ItemModel;
import net.minecraft.client.renderer.item.ItemModelResolver;
import net.minecraft.client.renderer.item.ItemStackRenderState;
import net.minecraft.client.renderer.item.ItemStackRenderState.FoilType;
import net.minecraft.client.renderer.item.ItemStackRenderState.LayerRenderState;
import net.minecraft.client.resources.model.ModelBaker;
import net.minecraft.client.resources.model.ResolvedModel;
import net.minecraft.client.resources.model.cuboid.ItemTransforms;
import net.minecraft.client.resources.model.geometry.QuadCollection;
import net.minecraft.client.resources.model.sprite.TextureSlots;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.resources.Identifier;
import net.minecraft.util.ARGB;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.ItemOwner;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Blocks;
import org.joml.Matrix4fc;
import org.jspecify.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

public record CustomMachineItemModel(QuadCollection defaultModel, ItemTransforms defaultTransform) implements ItemModel {

    @Override
    public void update(ItemStackRenderState output, ItemStack stack, ItemModelResolver resolver, ItemDisplayContext context, @Nullable ClientLevel level, @Nullable ItemOwner owner, int seed) {
        ItemStackRenderState.LayerRenderState layer = output.newLayer();

        if(stack.hasFoil()) {
            layer.setFoilType(FoilType.STANDARD);
            output.setAnimated();
            output.appendModelIdentityElement(FoilType.STANDARD);
        }

        layer.setUsesBlockLight(true);

        MachineAppearance appearance = CustomMachineItem.getMachine(stack).map(machine -> machine.getAppearance(MachineStatus.IDLE)).orElse(null);
        if(appearance == null) {
            this.useDefaultModel(output, layer, context, ARGB.white(1.0F));
        } else {
            IMachineModelLocation.Item loc = appearance.getItemModel();
            if(loc == MachineItemModelLocation.DEFAULT) {
                if(appearance.getBlockModel() == MachineBlockModelLocation.DEFAULT)
                    this.useDefaultModel(output, layer, context, appearance.getColor());
                else {
                    appearance.getBlockModel().location().ifLeft(mimicState ->
                        resolver.appendItemLayers(output, mimicState.getBlock().asItem().getDefaultInstance(), context, level, owner, seed)
                    );
                    appearance.getBlockModel().location().ifRight(id -> this.useStandaloneModel(output, layer, context, id));
                }
            } else {
                loc.location().ifLeft(mimicStack ->
                    resolver.appendItemLayers(output, mimicStack, context, level, owner, seed)
                );
                loc.location().ifRight(id -> this.useStandaloneModel(output, layer, context, id));
            }
        }
    }

    private void useDefaultModel(ItemStackRenderState output, ItemStackRenderState.LayerRenderState layer, ItemDisplayContext context, int tint) {
        output.appendModelIdentityElement(output);
        layer.setItemTransform(this.defaultTransform.getTransform(context));
        layer.prepareQuadList().addAll(this.defaultModel.getAll());
        layer.tintLayers().add(tint);
        if(this.defaultModel.hasMaterialFlag(2))
            output.setAnimated();
    }

    private void useStandaloneModel(ItemStackRenderState output, LayerRenderState layer, ItemDisplayContext context, Identifier modelId) {
        BlockStateModel model = ClientHandler.getStandaloneModel(modelId);
        if(model == null || Minecraft.getInstance().level == null)
            return;
        output.appendModelIdentityElement(model);
        layer.setItemTransform(this.defaultTransform.getTransform(context));
        List<BlockStateModelPart> parts = new ArrayList<>();
        model.collectParts(Minecraft.getInstance().level, BlockPos.ZERO, Blocks.AIR.defaultBlockState(), RandomSource.create(), parts);
        parts.forEach(part -> {
            for(Direction side : Direction.values())
                layer.prepareQuadList().addAll(part.getQuads(side));
            layer.prepareQuadList().addAll(part.getQuads(null));
        });
    }

    public record Unbaked(Identifier defaultModel) implements ItemModel.Unbaked {

        public static final Identifier ID = CustomMachinery.rl("custom_machine_model");
        public static final MapCodec<Unbaked> CODEC = RecordCodecBuilder.mapCodec(builder ->
                builder.group(
                        Identifier.CODEC.optionalFieldOf("default", CustomMachinery.rl("default/custom_machine_default_idle")).forGetter(Unbaked::defaultModel)
                ).apply(builder, Unbaked::new)
        );

        @Override
        public MapCodec<Unbaked> type() {
            return CODEC;
        }

        @Override
        public CustomMachineItemModel bake(BakingContext context, Matrix4fc matrix) {
            ModelBaker baker = context.blockModelBaker();
            ResolvedModel resolvedModel = baker.getModel(this.defaultModel);
            TextureSlots textureSlots = resolvedModel.getTopTextureSlots();
            QuadCollection quads = resolvedModel.bakeTopGeometry(textureSlots, baker, BlockModelRotation.IDENTITY);
            return new CustomMachineItemModel(quads, resolvedModel.getTopTransforms());
        }

        @Override
        public void resolveDependencies(Resolver resolver) {
            resolver.markDependency(this.defaultModel);
        }
    }
}
