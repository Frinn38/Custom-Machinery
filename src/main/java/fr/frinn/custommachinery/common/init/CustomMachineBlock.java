package fr.frinn.custommachinery.common.init;

import fr.frinn.custommachinery.CustomMachinery;
import fr.frinn.custommachinery.api.component.IMachineComponentManager;
import fr.frinn.custommachinery.common.component.LightMachineComponent;
import fr.frinn.custommachinery.common.component.RedstoneMachineComponent;
import fr.frinn.custommachinery.common.component.handler.FluidComponentHandler;
import fr.frinn.custommachinery.common.component.item.ItemMachineComponent;
import fr.frinn.custommachinery.common.machine.CustomMachine;
import fr.frinn.custommachinery.common.network.SRefreshCustomMachineTilePacket;
import fr.frinn.custommachinery.common.util.MachineBlockState;
import fr.frinn.custommachinery.common.util.Utils;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.Identifier;
import net.minecraft.server.TickTask;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.ProblemReporter;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.ExperienceOrb;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockAndLightGetter;
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
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.level.storage.TagValueInput;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;
import net.neoforged.neoforge.network.PacketDistributor;
import net.neoforged.neoforge.transfer.fluid.FluidUtil;
import org.jetbrains.annotations.Nullable;

import java.util.Collections;
import java.util.Optional;

public class CustomMachineBlock extends Block implements EntityBlock {

    private static final StateArgumentPredicate<EntityType<?>> spawnPredicate = ((state, level, pos, type) -> state.isFaceSturdy(level, pos, Direction.UP) && state.getBlock() instanceof CustomMachineBlock machineBlock && machineBlock.getLightEmission(state, level, pos) < 14);

    public static Properties makeProperties(boolean occlusion) {
        if(occlusion)
            return Properties.ofFullCopy(Blocks.STONE).requiresCorrectToolForDrops().strength(3.5F).forceSolidOn().dynamicShape().isValidSpawn(spawnPredicate);
        else
            return Properties.ofFullCopy(Blocks.STONE).requiresCorrectToolForDrops().strength(3.5F).forceSolidOn().noOcclusion().dynamicShape().isValidSpawn(spawnPredicate);
    }

    public CustomMachineBlock(boolean occlusion) {
        super(makeProperties(occlusion));
    }

    @Override
    public RenderShape getRenderShape(BlockState state) {
        return RenderShape.MODEL;
    }

    @Override
    public InteractionResult useItemOn(ItemStack stack, BlockState state, Level level, BlockPos pos, Player player, InteractionHand hand, BlockHitResult hit) {
        BlockEntity tile = level.getBlockEntity(pos);
        if(tile instanceof CustomMachineTile machine) {
            if (player.getItemInHand(hand).is(CMRegistration.CONFIGURATION_CARD_ITEM.get()))
                return ConfigurationCardItem.pasteConfiguration(level, player, machine, player.getItemInHand(hand));

            if(machine.getComponentManager().getComponentHandler(CMRegistration.FLUID_MACHINE_COMPONENT.get()).map(h -> (FluidComponentHandler)h).map(fluidHandler -> FluidUtil.interactWithFluidHandler(player, hand, pos, fluidHandler.interactionFluidHandler, null)).orElse(false))
                return InteractionResult.SUCCESS;

            if(!machine.getGuiElements().isEmpty()) {
                if(player instanceof ServerPlayer serverPlayer)
                    CustomMachineContainer.open(serverPlayer, machine);
                return InteractionResult.SUCCESS;
            }
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
                CompoundTag inventory = stack.get(CMRegistration.MACHINE_INVENTORY_DATA);
                if(inventory != null) {
                    CompoundTag components = new CompoundTag();
                    components.put("componentManager", inventory);
                    machineTile.loadAdditional(TagValueInput.create(ProblemReporter.DISCARDING, level.registryAccess(), components));
                }
                if(level instanceof ServerLevel serverLevel && placer != null && placer.getItemInHand(InteractionHand.OFF_HAND) == stack)
                    level.getServer().submit(new TickTask(1, () -> PacketDistributor.sendToPlayersTrackingChunk(serverLevel, ChunkPos.containing(pos), new SRefreshCustomMachineTilePacket(pos, machine.getId())))).join();
            }
        });
    }

    //When placed by anything else than an entity
    @Override
    public void onPlace(BlockState state, Level level, BlockPos pos, BlockState oldState, boolean isMoving) {
        if(oldState.getBlock() == state.getBlock())
            return;
        Identifier id = CustomMachinery.CUSTOM_BLOCK_MACHINES.inverse().get(this);
        if(id != null && level.getBlockEntity(pos) instanceof CustomMachineTile machineTile)
            machineTile.refreshMachine(id);
    }

    //Drop the machine block, but only if the player has correct tool or if requires-tool is disabled.
    @Override
    public void playerDestroy(Level level, Player player, BlockPos pos, BlockState state, @Nullable BlockEntity blockEntity, ItemStack tool) {
        if(blockEntity instanceof CustomMachineTile machine && player.hasCorrectToolForDrops(MachineBlockState.CACHE.getUnchecked(machine.getAppearance()), level, pos))
            super.playerDestroy(level, player, pos, state, blockEntity, tool);
    }

    //Drop the machine's inventory when broken
    @Override
    protected void affectNeighborsAfterRemoval(BlockState state, ServerLevel level, BlockPos pos, boolean movedByPiston) {
        if(!state.is(state.getBlock()) && level.getBlockEntity(pos) instanceof CustomMachineTile machine && !machine.getAppearance().shouldKeepInventory()) {
            //Drop items
            machine.getComponentManager().getComponentHandler(CMRegistration.ITEM_MACHINE_COMPONENT.get())
                    .map(handler -> handler.getComponents().stream()
                            .filter(ItemMachineComponent::shouldDrop)
                            .map(component -> component.getItemStack().copy())
                            .filter(stack -> !stack.isEmpty())
                            .toList()
                    ).orElse(Collections.emptyList())
                    .forEach(stack -> Block.popResource(level, pos, stack));
            //Drop xp
            machine.getComponentManager().getComponent(CMRegistration.EXPERIENCE_MACHINE_COMPONENT.get())
                    .filter(component -> component.getXp() > 0)
                    .ifPresent(component -> ExperienceOrb.award(level, Vec3.atCenterOf(pos), component.getXp()));
        }
        super.affectNeighborsAfterRemoval(state, level, pos, movedByPiston);
    }

    @Override
    protected boolean canBeReplaced(BlockState state, Fluid fluid) {
        return false;
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
            return Utils.createTickerHelper(type, CMRegistration.CUSTOM_MACHINE_TILE.get(), CustomMachineTile::clientTick);
        else
            return Utils.createTickerHelper(type, CMRegistration.CUSTOM_MACHINE_TILE.get(), CustomMachineTile::serverTick);
    }

    @Override
    public boolean isSignalSource(BlockState state) {
        return true;
    }

    @Override
    public boolean hasAnalogOutputSignal(BlockState state) {
        return true;
    }

    @Override
    public int getAnalogOutputSignal(BlockState state, Level level, BlockPos pos, Direction direction) {
        BlockEntity tile = level.getBlockEntity(pos);
        if(tile instanceof CustomMachineTile machine)
            return machine.getComponentManager().getComponent(CMRegistration.REDSTONE_MACHINE_COMPONENT.get()).map(RedstoneMachineComponent::getComparatorInput).orElse(0);
        return 0;
    }

    @Override
    public int getDirectSignal(BlockState state, BlockGetter level, BlockPos pos, Direction side) {
        BlockEntity tile = level.getBlockEntity(pos);
        if(tile instanceof CustomMachineTile machine)
            return machine.getComponentManager().getComponent(CMRegistration.REDSTONE_MACHINE_COMPONENT.get()).map(component -> component.getPowerOutput(side.getOpposite())).orElse(0);
        return 0;
    }

    @Override
    public int getSignal(BlockState state, BlockGetter level, BlockPos pos, Direction side) {
        BlockEntity tile = level.getBlockEntity(pos);
        if(tile instanceof CustomMachineTile machine)
            return machine.getComponentManager().getComponent(CMRegistration.REDSTONE_MACHINE_COMPONENT.get()).map(component -> component.getPowerOutput(side.getOpposite())).orElse(0);
        return 0;
    }

    @Override
    public float getDestroyProgress(BlockState state, Player player, BlockGetter level, BlockPos pos) {
        return Optional.ofNullable(level.getBlockEntity(pos))
                .filter(tile -> tile instanceof CustomMachineTile)
                .map(tile -> (CustomMachineTile)tile)
                .map(CustomMachineTile::getAppearance)
                .map(appearance -> Utils.getMachineBreakSpeed(appearance, level, pos, player))
                .orElse(super.getDestroyProgress(state, player, level, pos));
    }

    @Override
    public VoxelShape getCollisionShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return Optional.ofNullable(level.getBlockEntity(pos))
                .filter(tile -> tile instanceof CustomMachineTile)
                .map(tile -> ((CustomMachineTile)tile).getAppearance().getCollisionShape().apply(state.getValue(BlockStateProperties.HORIZONTAL_FACING)))
                .orElse(super.getCollisionShape(state, level, pos, context));
    }

    @Override
    public VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return Optional.ofNullable(level.getBlockEntity(pos))
                .filter(tile -> tile instanceof CustomMachineTile)
                .map(tile -> ((CustomMachineTile)tile).getAppearance().getShape().apply(state.getValue(BlockStateProperties.HORIZONTAL_FACING)))
                .orElse(super.getShape(state, level, pos, context));
    }

    @SuppressWarnings("deprecation")
    @Override
    public ItemStack getCloneItemStack(LevelReader level, BlockPos pos, BlockState state, boolean includeData) {
        BlockEntity tile = level.getBlockEntity(pos);
        if(tile instanceof CustomMachineTile customMachineTile) {
            CustomMachine machine = customMachineTile.getMachine();
            return CustomMachineItem.makeMachineItem(machine.getId());
        }
        return super.getCloneItemStack(level, pos, state, includeData);
    }

    @Override
    public float getExplosionResistance(BlockState state, BlockGetter level, BlockPos pos, Explosion explosion) {
        return Optional.ofNullable(level.getBlockEntity(pos))
                .filter(tile -> tile instanceof CustomMachineTile)
                .map(tile -> ((CustomMachineTile)tile).getAppearance().getResistance())
                .orElse(super.getExplosionResistance(state, level, pos, explosion));
    }

    @Override
    public SoundType getSoundType(BlockState state, LevelReader level, BlockPos pos, @Nullable Entity entity) {
        if(level.getBlockEntity(pos) instanceof CustomMachineTile tile)
            return tile.getAppearance().getInteractionSound();
        return super.getSoundType(state, level, pos, entity);
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
            return manager.getComponent(CMRegistration.LIGHT_MACHINE_COMPONENT.get()).map(LightMachineComponent::getMachineLight).orElse(0);
        }
        return 0;
    }

    @Override
    public boolean shouldDisplayFluidOverlay(BlockState state, BlockAndLightGetter level, BlockPos pos, FluidState fluidState) {
        return true;
    }

    @Override
    public boolean canHarvestBlock(BlockState state, BlockGetter level, BlockPos pos, Player player) {
        return Optional.ofNullable(level.getBlockEntity(pos))
                .filter(blockEntity -> blockEntity instanceof CustomMachineTile)
                .map(blockEntity -> (CustomMachineTile)blockEntity)
                .map(machine -> super.canHarvestBlock(MachineBlockState.CACHE.getUnchecked(machine.getAppearance()), level, pos, player))
                .orElse(super.canHarvestBlock(state, level, pos, player));
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
