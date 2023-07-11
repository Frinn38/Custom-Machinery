package fr.frinn.custommachinery.common.init;

import dev.architectury.registry.menu.MenuRegistry;
import fr.frinn.custommachinery.PlatformHelper;
import fr.frinn.custommachinery.api.component.IMachineComponentManager;
import fr.frinn.custommachinery.common.component.ItemMachineComponent;
import fr.frinn.custommachinery.common.component.LightMachineComponent;
import fr.frinn.custommachinery.common.component.RedstoneMachineComponent;
import fr.frinn.custommachinery.common.component.handler.FluidComponentHandler;
import fr.frinn.custommachinery.common.machine.CustomMachine;
import fr.frinn.custommachinery.common.network.SRefreshCustomMachineTilePacket;
import fr.frinn.custommachinery.common.util.MachineBlockState;
import fr.frinn.custommachinery.common.util.Utils;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.server.TickTask;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.Containers;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Explosion;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.EntityBlock;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.material.Material;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.jetbrains.annotations.Nullable;

import java.util.Optional;

public abstract class CustomMachineBlock extends Block implements EntityBlock {

    private static final StateArgumentPredicate<EntityType<?>> spawnPredicate = ((state, level, pos, type) -> state.isFaceSturdy(level, pos, Direction.UP) && state.getBlock() instanceof CustomMachineBlock machineBlock && machineBlock.getLightEmission(state, level, pos) < 14);

    public CustomMachineBlock() {
        super(Properties.of(Material.STONE).requiresCorrectToolForDrops().strength(3.5F).noOcclusion().dynamicShape().isValidSpawn(spawnPredicate));
    }

    @SuppressWarnings("deprecation")
    @Override
    public RenderShape getRenderShape(BlockState state) {
        return RenderShape.MODEL;
    }

    @SuppressWarnings("deprecation")
    @Override
    public InteractionResult use(BlockState state, Level level, BlockPos pos, Player player, InteractionHand hand, BlockHitResult hit) {
        BlockEntity tile = level.getBlockEntity(pos);
        if(tile instanceof CustomMachineTile machine) {
            if(machine.getComponentManager().getComponentHandler(Registration.FLUID_MACHINE_COMPONENT.get()).map(h -> (FluidComponentHandler)h).map(fluidHandler -> fluidHandler.getCommonFluidHandler().interactWithFluidHandler(player, hand)).orElse(false)) {
                return InteractionResult.SUCCESS;
            }
            if(!level.isClientSide() && !machine.getMachine().getGuiElements().isEmpty()) {
                MenuRegistry.openExtendedMenu((ServerPlayer) player, new MenuProvider() {
                    @Override
                    public Component getDisplayName() {
                        return machine.getMachine().getName();
                    }

                    @Override
                    public AbstractContainerMenu createMenu(int id, Inventory inv, Player player) {
                        return new CustomMachineContainer(id, inv, machine);
                    }
                }, buf -> buf.writeBlockPos(pos));
                return InteractionResult.SUCCESS;
            }
            return InteractionResult.SUCCESS;
        }
        return super.use(state, level, pos, player, hand, hit);
    }

    @Override
    public void setPlacedBy(Level level, BlockPos pos, BlockState state, @Nullable LivingEntity placer, ItemStack stack) {
        CustomMachineItem.getMachine(stack).ifPresent(machine -> {
            BlockEntity tile = level.getBlockEntity(pos);
            if(tile instanceof CustomMachineTile) {
                ((CustomMachineTile)tile).setId(machine.getId());
                if(!level.isClientSide() && level.getServer() != null && placer != null && placer.getItemInHand(InteractionHand.OFF_HAND) == stack)
                    level.getServer().tell(new TickTask(1, () -> new SRefreshCustomMachineTilePacket(pos, machine.getId()).sendToChunkListeners(level.getChunkAt(pos))));
            }
        });
    }

    @Override
    public ItemStack getCloneItemStack(BlockGetter level, BlockPos pos, BlockState state) {
        BlockEntity tile = level.getBlockEntity(pos);
        if(tile instanceof CustomMachineTile customMachineTile) {
            CustomMachine machine = customMachineTile.getMachine();
            return CustomMachineItem.makeMachineItem(machine.getId());
        }
        return super.getCloneItemStack(level, pos, state);
    }

    @Override
    public void playerDestroy(Level level, Player player, BlockPos pos, BlockState state, @Nullable BlockEntity blockEntity, ItemStack tool) {
        super.playerDestroy(level, player, pos, state, blockEntity, tool);
        if(!level.isClientSide && !player.isCreative() && blockEntity instanceof CustomMachineTile machine) {
            machine.getComponentManager().getComponentHandler(Registration.ITEM_MACHINE_COMPONENT.get()).ifPresent(handler -> handler.getComponents().stream().map(ItemMachineComponent::getItemStack).filter(stack -> stack != ItemStack.EMPTY).forEach(stack -> Containers.dropItemStack(level, pos.getX(), pos.getY(), pos.getZ(), stack)));
            if(player.hasCorrectToolForDrops(MachineBlockState.CACHE.getUnchecked(machine.getMachine().getAppearance(machine.getStatus()))))
                Containers.dropItemStack(level, pos.getX(), pos.getY(), pos.getZ(), CustomMachineItem.makeMachineItem(machine.getId()));
        }
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
        return PlatformHelper.createMachineTile(pos, state);
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
                .map(tile -> tile.getMachine().getAppearance(tile.getStatus()))
                .map(appearance -> Utils.getMachineBreakSpeed(appearance, level, pos, player))
                .orElse(super.getDestroyProgress(state, player, level, pos));
    }

    @SuppressWarnings("deprecation")
    @Override
    public VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return Optional.ofNullable(level.getBlockEntity(pos))
                .filter(tile -> tile instanceof CustomMachineTile)
                .map(tile -> ((CustomMachineTile)tile).getMachine().getAppearance(((CustomMachineTile)tile).getStatus()).getShape().apply(state.getValue(BlockStateProperties.HORIZONTAL_FACING)))
                .orElse(super.getShape(state, level, pos, context));
    }

    /**
     * On forge side this is called by ForgeCustomMachineBlock#getExplosionResistance which override the method added in IForgeBlock.
     * On fabric side this is called by CM Mixin ExplosionDamageCalculatorMixin.
     */
    public float getExplosionResistance(BlockState state, BlockGetter level, BlockPos pos, Explosion explosion) {
        return Optional.ofNullable(level.getBlockEntity(pos))
                .filter(tile -> tile instanceof CustomMachineTile)
                .map(tile -> ((CustomMachineTile)tile).getMachine().getAppearance(((CustomMachineTile)tile).getStatus()).getResistance())
                .orElse(super.getExplosionResistance());
    }

    /**
     * On forge side this is called by ForgeCustomMachineBlock#getSoundType which override the method added in IForgeBlock.
     * On fabric side this is called by various CM methods or mixins depending on the interaction type :
     * Breaking : LevelRendererMixin#custommachinery$getBrokenSoundType
     * Placing : CustomMachineItem#place
     * Stepping : EntityMixin#custommachinery$playStepSound
     * Falling : LivingEntityMixin#custommachinery$getFallSound
     * Hitting : MultiplayerGameModeMixin#custommachinery$getHitSound
     */
    public SoundType getSoundType(BlockState state, LevelReader level, BlockPos pos, @Nullable Entity entity) {
        return Optional.ofNullable(level.getBlockEntity(pos))
                .filter(blockEntity -> blockEntity instanceof CustomMachineTile)
                .map(tile -> ((CustomMachineTile)tile).getMachine().getAppearance(((CustomMachineTile)tile).getStatus()).getInteractionSound())
                .orElse(super.getSoundType(state));
    }

    /**
     * On Forge side this is called by ForgeCustomMachineBlock#getLightEmission which override the method added in IForgeBlock.
     * On Fabric side this is called by various CM mixins :
     * LevelMixin#custommachinery$getLightEmission
     * BlockGetterMixin#custommachinery$getLightEmission
     * LevelRendererMixin#custommachinery$getLightEmission
     * LevelChunkMixin#custommachinery$getLightEmission
     * ModelBlockRendererMixin#custommachinery$getLightEmission
     * ProtoChunkMixin#custommachinery$getLightEmission
     */
    public int getLightEmission(BlockState state, BlockGetter level, BlockPos pos) {
        BlockEntity tile = level.getBlockEntity(pos);
        if(tile instanceof CustomMachineTile) {
            IMachineComponentManager manager = ((CustomMachineTile) tile).getComponentManager();
            return manager.getComponent(Registration.LIGHT_MACHINE_COMPONENT.get()).map(LightMachineComponent::getMachineLight).orElse(0);
        }
        return 0;
    }
}
