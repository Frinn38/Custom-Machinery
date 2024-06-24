package fr.frinn.custommachinery.common.init;

import fr.frinn.custommachinery.CustomMachinery;
import fr.frinn.custommachinery.api.component.IMachineComponentManager;
import fr.frinn.custommachinery.common.component.LightMachineComponent;
import fr.frinn.custommachinery.common.component.RedstoneMachineComponent;
import fr.frinn.custommachinery.common.component.handler.FluidComponentHandler;
import fr.frinn.custommachinery.common.machine.CustomMachine;
import fr.frinn.custommachinery.common.network.SRefreshCustomMachineTilePacket;
import fr.frinn.custommachinery.common.util.MachineBlockState;
import fr.frinn.custommachinery.common.util.Utils;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.TickTask;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.ItemInteractionResult;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockAndTintGetter;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.Explosion;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.SignalGetter;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.EntityBlock;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.level.storage.loot.LootParams;
import net.minecraft.world.level.storage.loot.parameters.LootContextParams;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;
import net.neoforged.neoforge.fluids.FluidUtil;
import net.neoforged.neoforge.network.PacketDistributor;
import org.jetbrains.annotations.Nullable;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

public class CustomMachineBlock extends Block implements EntityBlock {

    private static final StateArgumentPredicate<EntityType<?>> spawnPredicate = ((state, level, pos, type) -> state.isFaceSturdy(level, pos, Direction.UP) && state.getBlock() instanceof CustomMachineBlock machineBlock && machineBlock.getLightEmission(state, level, pos) < 14);

    public final String renderType;

    public static Properties makeProperties(boolean occlusion) {
        if(occlusion)
            return Properties.ofFullCopy(Blocks.STONE).requiresCorrectToolForDrops().strength(3.5F).dynamicShape().isValidSpawn(spawnPredicate);
        else
            return Properties.ofFullCopy(Blocks.STONE).requiresCorrectToolForDrops().strength(3.5F).noOcclusion().dynamicShape().isValidSpawn(spawnPredicate);
    }

    public CustomMachineBlock(String renderType, boolean occlusion) {
        super(makeProperties(occlusion));
        this.renderType = renderType;
    }

    public CustomMachineBlock() {
        this("translucent", false);
    }

    @SuppressWarnings("deprecation")
    @Override
    public RenderShape getRenderShape(BlockState state) {
        return RenderShape.MODEL;
    }

    @Override
    public ItemInteractionResult useItemOn(ItemStack stack, BlockState state, Level level, BlockPos pos, Player player, InteractionHand hand, BlockHitResult hit) {
        BlockEntity tile = level.getBlockEntity(pos);
        if(tile instanceof CustomMachineTile machine) {
            if (player.getItemInHand(hand).is(Registration.CONFIGURATION_CARD_ITEM.get()))
                return ConfigurationCardItem.pasteConfiguration(level, player, machine, player.getItemInHand(hand));

            if(machine.getComponentManager().getComponentHandler(Registration.FLUID_MACHINE_COMPONENT.get()).map(h -> (FluidComponentHandler)h).map(fluidHandler -> FluidUtil.interactWithFluidHandler(player, hand, fluidHandler)).orElse(false))
                return ItemInteractionResult.SUCCESS;

            if(player instanceof ServerPlayer serverPlayer && !machine.getGuiElements().isEmpty())
                CustomMachineContainer.open(serverPlayer, machine);

            return ItemInteractionResult.SUCCESS;
        }
        return super.useItemOn(stack, state, level, pos, player, hand, hit);
    }

    //When placed by an entity
    @Override
    public void setPlacedBy(Level level, BlockPos pos, BlockState state, @Nullable LivingEntity placer, ItemStack stack) {
        CustomMachineItem.getMachine(stack).ifPresent(machine -> {
            BlockEntity tile = level.getBlockEntity(pos);
            if(tile instanceof CustomMachineTile machineTile) {
                machineTile.setId(machine.getId());
                if(placer != null)
                    machineTile.setOwner(placer);
                if(level instanceof ServerLevel serverLevel && placer != null && placer.getItemInHand(InteractionHand.OFF_HAND) == stack)
                    level.getServer().tell(new TickTask(1, () -> PacketDistributor.sendToPlayersTrackingChunk(serverLevel, new ChunkPos(pos), new SRefreshCustomMachineTilePacket(pos, machine.getId()))));
            }
        });
    }

    //When placed by anything else than an entity
    @SuppressWarnings("deprecation")
    @Override
    public void onPlace(BlockState state, Level level, BlockPos pos, BlockState oldState, boolean isMoving) {
        ResourceLocation id = CustomMachinery.CUSTOM_BLOCK_MACHINES.inverse().get(this);
        if(id != null && level.getBlockEntity(pos) instanceof CustomMachineTile machineTile)
            machineTile.setId(id);
    }

    //Drop the machine block, but only if the player has correct tool or if requires-tool is disabled.
    @Override
    public void playerDestroy(Level level, Player player, BlockPos pos, BlockState state, @Nullable BlockEntity blockEntity, ItemStack tool) {
        if(blockEntity instanceof CustomMachineTile machine && player.hasCorrectToolForDrops(MachineBlockState.CACHE.getUnchecked(machine.getAppearance()), level, pos))
            super.playerDestroy(level, player, pos, state, blockEntity, tool);
    }

    //Drop the content of the machine if the player is in creative mode, as the playerDestroy method won't be called then.
    @Override
    public BlockState playerWillDestroy(Level level, BlockPos pos, BlockState state, Player player) {
        if(player.getAbilities().instabuild && level instanceof ServerLevel serverLevel && level.getBlockEntity(pos) instanceof CustomMachineTile machine)
            machine.getComponentManager().getComponentHandler(Registration.ITEM_MACHINE_COMPONENT.get())
                    .map(handler -> handler.getComponents().stream()
                            .filter(component -> component.getVariant().shouldDrop(machine.getComponentManager()))
                            .map(component -> component.getItemStack().copy())
                            .filter(stack -> !stack.isEmpty())
                            .toList()
                    ).orElse(Collections.emptyList())
                    .forEach(stack -> Block.popResource(serverLevel, pos, stack));
        return super.playerWillDestroy(level, pos, state, player);
    }

    @Override
    public List<ItemStack> getDrops(BlockState state, LootParams.Builder builder) {
        List<ItemStack> drops = super.getDrops(state, builder);
        if(builder.getParameter(LootContextParams.BLOCK_ENTITY) instanceof CustomMachineTile machine) {
            machine.getComponentManager().getComponentHandler(Registration.ITEM_MACHINE_COMPONENT.get())
                    .ifPresent(handler -> handler.getComponents().stream()
                            .filter(component -> component.getVariant().shouldDrop(machine.getComponentManager()))
                            .map(component -> component.getItemStack().copy())
                            .filter(stack -> stack != ItemStack.EMPTY)
                            .forEach(drops::add)
                    );
            drops.add(CustomMachineItem.makeMachineItem(machine.getId()));
        }
        return drops;
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        super.createBlockStateDefinition(builder);
        builder.add(BlockStateProperties.HORIZONTAL_FACING);
    }

    @Nullable
    @Override
    public BlockState getStateForPlacement(BlockPlaceContext context) {
        return defaultBlockState().setValue(BlockStateProperties.HORIZONTAL_FACING, context.getHorizontalDirection().getOpposite());
    }

    @Nullable
    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new CustomMachineTile(pos, state);
    }

    @Nullable
    @Override
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level level, BlockState state, BlockEntityType<T> type) {
        if(level.isClientSide())
            return Utils.createTickerHelper(type, Registration.CUSTOM_MACHINE_TILE.get(), CustomMachineTile::clientTick);
        else
            return Utils.createTickerHelper(type, Registration.CUSTOM_MACHINE_TILE.get(), CustomMachineTile::serverTick);
    }

    @SuppressWarnings("deprecation")
    @Override
    public boolean isSignalSource(BlockState state) {
        return true;
    }

    @SuppressWarnings("deprecation")
    @Override
    public boolean hasAnalogOutputSignal(BlockState state) {
        return true;
    }

    @SuppressWarnings("deprecation")
    @Override
    public int getAnalogOutputSignal(BlockState state, Level level, BlockPos pos) {
        BlockEntity tile = level.getBlockEntity(pos);
        if(tile instanceof CustomMachineTile)
            return ((CustomMachineTile)tile).getComponentManager().getComponent(Registration.REDSTONE_MACHINE_COMPONENT.get()).map(RedstoneMachineComponent::getComparatorInput).orElse(0);
        return 0;
    }

    @SuppressWarnings("deprecation")
    @Override
    public int getDirectSignal(BlockState state, BlockGetter level, BlockPos pos, Direction side) {
        BlockEntity tile = level.getBlockEntity(pos);
        if(tile instanceof CustomMachineTile)
            return ((CustomMachineTile)tile).getComponentManager().getComponent(Registration.REDSTONE_MACHINE_COMPONENT.get()).map(RedstoneMachineComponent::getPowerOutput).orElse(0);
        return 0;
    }

    @SuppressWarnings("deprecation")
    @Override
    public int getSignal(BlockState state, BlockGetter level, BlockPos pos, Direction side) {
        BlockEntity tile = level.getBlockEntity(pos);
        if(tile instanceof CustomMachineTile)
            return ((CustomMachineTile)tile).getComponentManager().getComponent(Registration.REDSTONE_MACHINE_COMPONENT.get()).map(RedstoneMachineComponent::getPowerOutput).orElse(0);
        return 0;
    }

    @SuppressWarnings("deprecation")
    @Override
    public float getDestroyProgress(BlockState state, Player player, BlockGetter level, BlockPos pos) {
        return Optional.ofNullable(level.getBlockEntity(pos))
                .filter(tile -> tile instanceof CustomMachineTile)
                .map(tile -> (CustomMachineTile)tile)
                .map(CustomMachineTile::getAppearance)
                .map(appearance -> Utils.getMachineBreakSpeed(appearance, level, pos, player))
                .orElseGet(() -> {
                    //Don't call super.getDestroyProgress here as it will trigger BlockBehaviourWorldlyBlockMixin#handleWorldlyBreakableCondition
                    //which crash the game because it returns a value in a non-cancellable Mixin
                    float f = state.getDestroySpeed(level, pos);
                    if (f == -1.0f) {
                        return 0.0f;
                    }
                    int i = player.hasCorrectToolForDrops(state) ? 30 : 100;
                    return player.getDestroySpeed(state) / f / (float)i;
                });
    }

    @SuppressWarnings("deprecation")
    @Override
    public VoxelShape getCollisionShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return Optional.ofNullable(level.getBlockEntity(pos))
                .filter(tile -> tile instanceof CustomMachineTile)
                .map(tile -> ((CustomMachineTile)tile).getAppearance().getCollisionShape().apply(state.getValue(BlockStateProperties.HORIZONTAL_FACING)))
                .orElse(super.getCollisionShape(state, level, pos, context));
    }

    @SuppressWarnings("deprecation")
    @Override
    public VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return Optional.ofNullable(level.getBlockEntity(pos))
                .filter(tile -> tile instanceof CustomMachineTile)
                .map(tile -> ((CustomMachineTile)tile).getAppearance().getShape().apply(state.getValue(BlockStateProperties.HORIZONTAL_FACING)))
                .orElse(super.getShape(state, level, pos, context));
    }

    @Override
    public ItemStack getCloneItemStack(BlockState state, HitResult result, LevelReader level, BlockPos pos, Player player) {
        BlockEntity tile = level.getBlockEntity(pos);
        if(tile instanceof CustomMachineTile customMachineTile) {
            CustomMachine machine = customMachineTile.getMachine();
            return CustomMachineItem.makeMachineItem(machine.getId());
        }
        return super.getCloneItemStack(state, result, level, pos, player);
    }

    @Override
    public float getExplosionResistance(BlockState state, BlockGetter level, BlockPos pos, Explosion explosion) {
        return Optional.ofNullable(level.getBlockEntity(pos))
                .filter(tile -> tile instanceof CustomMachineTile)
                .map(tile -> ((CustomMachineTile)tile).getAppearance().getResistance())
                .orElse(super.getExplosionResistance());
    }

    @Override
    public SoundType getSoundType(BlockState state, LevelReader level, BlockPos pos, @Nullable Entity entity) {
        return Optional.ofNullable(level.getBlockEntity(pos))
                .filter(blockEntity -> blockEntity instanceof CustomMachineTile)
                .map(tile -> ((CustomMachineTile)tile).getAppearance().getInteractionSound())
                .orElse(super.getSoundType(state));
    }

    @Override
    public float getFriction(BlockState state, LevelReader level, BlockPos pos, @Nullable Entity entity) {
        return this.getFriction();
    }

    @Override
    public int getLightEmission(BlockState state, BlockGetter level, BlockPos pos) {
        BlockEntity tile = level.getBlockEntity(pos);
        if(tile instanceof CustomMachineTile) {
            IMachineComponentManager manager = ((CustomMachineTile) tile).getComponentManager();
            return manager.getComponent(Registration.LIGHT_MACHINE_COMPONENT.get()).map(LightMachineComponent::getMachineLight).orElse(0);
        }
        return 0;
    }

    @Override
    public boolean shouldDisplayFluidOverlay(BlockState state, BlockAndTintGetter level, BlockPos pos, FluidState fluidState) {
        return true;
    }

    @Override
    public boolean canHarvestBlock(BlockState state, BlockGetter level, BlockPos pos, Player player) {
        return Optional.ofNullable(level.getBlockEntity(pos))
                .filter(blockEntity -> blockEntity instanceof CustomMachineTile)
                .map(blockEntity -> (CustomMachineTile)blockEntity)
                .map(machine -> player.hasCorrectToolForDrops(MachineBlockState.CACHE.getUnchecked(machine.getAppearance())))
                .orElse(player.hasCorrectToolForDrops(state));
    }

    @Override
    public BlockState rotate(BlockState state, LevelAccessor level, BlockPos pos, Rotation rotation) {
        return state.setValue(BlockStateProperties.HORIZONTAL_FACING, rotation.rotate(state.getValue(BlockStateProperties.HORIZONTAL_FACING)));
    }

    @Override
    public boolean shouldCheckWeakPower(BlockState blockState, SignalGetter levelReader, BlockPos blockPos, Direction direction) {
        return true;
    }
}
