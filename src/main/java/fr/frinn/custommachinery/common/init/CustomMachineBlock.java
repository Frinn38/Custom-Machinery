package fr.frinn.custommachinery.common.init;

import fr.frinn.custommachinery.api.component.IMachineComponentManager;
import fr.frinn.custommachinery.common.data.CustomMachine;
import fr.frinn.custommachinery.common.data.component.ItemMachineComponent;
import fr.frinn.custommachinery.common.data.component.LightMachineComponent;
import fr.frinn.custommachinery.common.data.component.RedstoneMachineComponent;
import fr.frinn.custommachinery.common.data.component.handler.FluidComponentHandler;
import fr.frinn.custommachinery.common.network.NetworkManager;
import fr.frinn.custommachinery.common.network.SRefreshCustomMachineTilePacket;
import fr.frinn.custommachinery.common.util.Utils;
import mcp.MethodsReturnNonnullByDefault;
import net.minecraft.block.Block;
import net.minecraft.block.BlockRenderType;
import net.minecraft.block.BlockState;
import net.minecraft.block.material.Material;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.fluid.FluidState;
import net.minecraft.inventory.InventoryHelper;
import net.minecraft.inventory.container.Container;
import net.minecraft.inventory.container.INamedContainerProvider;
import net.minecraft.item.BlockItemUseContext;
import net.minecraft.item.ItemStack;
import net.minecraft.state.StateContainer;
import net.minecraft.state.properties.BlockStateProperties;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ActionResultType;
import net.minecraft.util.Direction;
import net.minecraft.util.Hand;
import net.minecraft.util.concurrent.TickDelayedTask;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.BlockRayTraceResult;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.util.math.shapes.ISelectionContext;
import net.minecraft.util.math.shapes.VoxelShape;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.world.Explosion;
import net.minecraft.world.IBlockDisplayReader;
import net.minecraft.world.IBlockReader;
import net.minecraft.world.World;
import net.minecraftforge.fluids.FluidUtil;
import net.minecraftforge.fluids.capability.CapabilityFluidHandler;
import net.minecraftforge.fml.network.NetworkHooks;
import net.minecraftforge.fml.network.PacketDistributor;

import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;
import java.util.Optional;

@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public class CustomMachineBlock extends Block {

    public CustomMachineBlock() {
        super(Properties.create(Material.ROCK).setRequiresTool().hardnessAndResistance(3.5F).notSolid());
    }

    @SuppressWarnings("deprecation")
    @Override
    public BlockRenderType getRenderType(BlockState state) {
        return BlockRenderType.MODEL;
    }

    @SuppressWarnings("deprecation")
    @Override
    public ActionResultType onBlockActivated(BlockState state, World world, BlockPos pos, PlayerEntity player, Hand hand, BlockRayTraceResult hit) {
        TileEntity tile = world.getTileEntity(pos);
        if(tile instanceof CustomMachineTile) {
            CustomMachineTile machine = (CustomMachineTile)tile;
            if(machine.componentManager.getComponentHandler(Registration.FLUID_MACHINE_COMPONENT.get()).map(h -> (FluidComponentHandler)h).map(fluidHandler -> FluidUtil.interactWithFluidHandler(player, hand, fluidHandler.getInteractionHandler())).orElse(false)) {
                return ActionResultType.SUCCESS;
            }
            if(!world.isRemote() && !machine.getMachine().getGuiElements().isEmpty()) {
                NetworkHooks.openGui((ServerPlayerEntity) player, new INamedContainerProvider() {
                    @Override
                    public ITextComponent getDisplayName() {
                        return machine.getMachine().getName();
                    }

                    @Override
                    public Container createMenu(int id, PlayerInventory inv, PlayerEntity player) {
                        return new CustomMachineContainer(id, inv, machine);
                    }
                }, pos);
                return ActionResultType.SUCCESS;
            }
            return ActionResultType.SUCCESS;
        }
        return super.onBlockActivated(state, world, pos, player, hand, hit);
    }

    @Override
    public void onBlockPlacedBy(World world, BlockPos pos, BlockState state, @Nullable LivingEntity placer, ItemStack stack) {
        CustomMachineItem.getMachine(stack).ifPresent(machine -> {
            TileEntity tile = world.getTileEntity(pos);
            if(tile instanceof CustomMachineTile) {
                ((CustomMachineTile)tile).setId(machine.getId());
                if(!world.isRemote() && world.getServer() != null && placer != null && placer.getHeldItem(Hand.OFF_HAND) == stack)
                    world.getServer().enqueue(new TickDelayedTask(1, () -> NetworkManager.CHANNEL.send(PacketDistributor.TRACKING_CHUNK.with(() -> world.getChunkAt(pos)), new SRefreshCustomMachineTilePacket(pos, machine.getId()))));
            }
        });
    }

    @Override
    public ItemStack getPickBlock(BlockState state, RayTraceResult target, IBlockReader world, BlockPos pos, PlayerEntity player) {
        TileEntity tile = world.getTileEntity(pos);
        if(tile instanceof CustomMachineTile) {
            CustomMachineTile customMachineTile = (CustomMachineTile)tile;
            CustomMachine machine = customMachineTile.getMachine();
            return CustomMachineItem.makeMachineItem(machine.getId());
        }
        return super.getPickBlock(state, target, world, pos, player);
    }

    @Override
    public void onBlockHarvested(World world, BlockPos pos, BlockState state, PlayerEntity player) {
        super.onBlockHarvested(world, pos, state, player);
        if(!world.isRemote && !player.isCreative()) {
            TileEntity tile = world.getTileEntity(pos);
            if(tile instanceof CustomMachineTile) {
                CustomMachineTile machine = (CustomMachineTile) tile;
                machine.componentManager.getComponentHandler(Registration.ITEM_MACHINE_COMPONENT.get()).ifPresent(handler -> handler.getComponents().stream().map(ItemMachineComponent::getItemStack).filter(stack -> stack != ItemStack.EMPTY).forEach(stack -> InventoryHelper.spawnItemStack(world, pos.getX(), pos.getY(), pos.getZ(), stack)));
                if(Utils.canPlayerHarvestMachine(machine.getMachine().getAppearance(machine.getStatus()), player, world, pos))
                    InventoryHelper.spawnItemStack(world, pos.getX(), pos.getY(), pos.getZ(), CustomMachineItem.makeMachineItem(machine.getId()));
            }
        }
    }

    @Override
    protected void fillStateContainer(StateContainer.Builder<Block, BlockState> builder) {
        super.fillStateContainer(builder);
        builder.add(BlockStateProperties.HORIZONTAL_FACING);
    }

    @Nullable
    @Override
    public BlockState getStateForPlacement(BlockItemUseContext context) {
        return getDefaultState().with(BlockStateProperties.HORIZONTAL_FACING, context.getPlacementHorizontalFacing().getOpposite());
    }

    @Override
    public int getLightValue(BlockState state, IBlockReader world, BlockPos pos) {
        TileEntity tile = world.getTileEntity(pos);
        if(tile instanceof CustomMachineTile) {
            IMachineComponentManager manager = ((CustomMachineTile) tile).componentManager;
            return manager.getComponent(Registration.LIGHT_MACHINE_COMPONENT.get()).map(LightMachineComponent::getMachineLight).orElse(0);
        }
        return 0;
    }

    @Override
    public boolean hasTileEntity(BlockState state) {
        return true;
    }

    @Nullable
    @Override
    public TileEntity createTileEntity(BlockState state, IBlockReader world) {
        return Registration.CUSTOM_MACHINE_TILE.get().create();
    }

    @Override
    public boolean canConnectRedstone(BlockState state, IBlockReader world, BlockPos pos, @Nullable Direction side) {
        return true;
    }

    @SuppressWarnings("deprecation")
    @Override
    public boolean canProvidePower(BlockState state) {
        return true;
    }

    @SuppressWarnings("deprecation")
    @Override
    public boolean hasComparatorInputOverride(BlockState state) {
        return true;
    }

    @SuppressWarnings("deprecation")
    @Override
    public int getComparatorInputOverride(BlockState state, World world, BlockPos pos) {
        TileEntity tile = world.getTileEntity(pos);
        if(tile instanceof CustomMachineTile)
            return ((CustomMachineTile)tile).componentManager.getComponent(Registration.REDSTONE_MACHINE_COMPONENT.get()).map(RedstoneMachineComponent::getComparatorInput).orElse(0);
        return 0;
    }

    @SuppressWarnings("deprecation")
    @Override
    public int getStrongPower(BlockState state, IBlockReader world, BlockPos pos, Direction side) {
        TileEntity tile = world.getTileEntity(pos);
        if(tile instanceof CustomMachineTile)
            return ((CustomMachineTile)tile).componentManager.getComponent(Registration.REDSTONE_MACHINE_COMPONENT.get()).map(RedstoneMachineComponent::getPowerOutput).orElse(0);
        return 0;
    }

    @SuppressWarnings("deprecation")
    @Override
    public int getWeakPower(BlockState state, IBlockReader world, BlockPos pos, Direction side) {
        TileEntity tile = world.getTileEntity(pos);
        if(tile instanceof CustomMachineTile)
            return ((CustomMachineTile)tile).componentManager.getComponent(Registration.REDSTONE_MACHINE_COMPONENT.get()).map(RedstoneMachineComponent::getPowerOutput).orElse(0);
        return 0;
    }

    @Override
    public boolean shouldDisplayFluidOverlay(BlockState state, IBlockDisplayReader world, BlockPos pos, FluidState fluidState) {
        return true;
    }

    @SuppressWarnings("deprecation")
    @Override
    public float getPlayerRelativeBlockHardness(BlockState state, PlayerEntity player, IBlockReader world, BlockPos pos) {
        return Optional.ofNullable(world.getTileEntity(pos))
                .filter(tile -> tile instanceof CustomMachineTile)
                .map(tile -> (CustomMachineTile)tile)
                .map(tile -> tile.getMachine().getAppearance(tile.getStatus()))
                .map(appearance -> Utils.getMachineBreakSpeed(appearance, world, pos, player))
                .orElse(super.getPlayerRelativeBlockHardness(state, player, world, pos));
    }

    @Override
    public float getExplosionResistance(BlockState state, IBlockReader world, BlockPos pos, Explosion explosion) {
        return Optional.ofNullable(world.getTileEntity(pos))
                .filter(tile -> tile instanceof CustomMachineTile)
                .map(tile -> ((CustomMachineTile)tile).getMachine().getAppearance(((CustomMachineTile)tile).getStatus()).getResistance())
                .orElse(super.getExplosionResistance(state, world, pos, explosion));
    }

    @SuppressWarnings("deprecation")
    @Override
    public VoxelShape getShape(BlockState state, IBlockReader world, BlockPos pos, ISelectionContext context) {
        return Optional.ofNullable(world.getTileEntity(pos))
                .filter(tile -> tile instanceof CustomMachineTile)
                .map(tile -> ((CustomMachineTile)tile).getMachine().getAppearance(((CustomMachineTile)tile).getStatus()).getShape())
                .orElse(super.getShape(state, world, pos, context));
    }
}
