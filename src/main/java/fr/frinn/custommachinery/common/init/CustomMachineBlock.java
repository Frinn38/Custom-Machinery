package fr.frinn.custommachinery.common.init;

import fr.frinn.custommachinery.common.data.CustomMachine;
import fr.frinn.custommachinery.common.data.component.ItemMachineComponent;
import fr.frinn.custommachinery.common.data.component.handler.ItemComponentHandler;
import net.minecraft.block.Block;
import net.minecraft.block.BlockRenderType;
import net.minecraft.block.BlockState;
import net.minecraft.block.material.Material;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.inventory.InventoryHelper;
import net.minecraft.item.BlockItemUseContext;
import net.minecraft.item.ItemStack;
import net.minecraft.state.StateContainer;
import net.minecraft.state.properties.BlockStateProperties;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ActionResultType;
import net.minecraft.util.Direction;
import net.minecraft.util.Hand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.BlockRayTraceResult;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.world.IBlockReader;
import net.minecraft.world.World;
import net.minecraftforge.fluids.FluidUtil;
import net.minecraftforge.fluids.capability.CapabilityFluidHandler;
import net.minecraftforge.fml.network.NetworkHooks;

import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;

public class CustomMachineBlock extends Block {

    public CustomMachineBlock() {
        super(Properties.create(Material.ROCK).setRequiresTool().hardnessAndResistance(3.5F).notSolid());
    }

    @SuppressWarnings("deprecation")
    @Override
    @ParametersAreNonnullByDefault
    public BlockRenderType getRenderType(BlockState state) {
        return BlockRenderType.INVISIBLE;
    }

    @SuppressWarnings("deprecation")
    @ParametersAreNonnullByDefault
    @Override
    public ActionResultType onBlockActivated(BlockState state, World world, BlockPos pos, PlayerEntity player, Hand hand, BlockRayTraceResult hit) {
        TileEntity tile = world.getTileEntity(pos);
        if(tile instanceof CustomMachineTile) {
            CustomMachineTile machine = (CustomMachineTile)tile;
            if(machine.getCapability(CapabilityFluidHandler.FLUID_HANDLER_CAPABILITY).map(fluidHandler -> FluidUtil.interactWithFluidHandler(player, hand, fluidHandler)).orElse(false)) {
                return ActionResultType.SUCCESS;
            }
            if(!world.isRemote() && !machine.getMachine().getGuiElements().isEmpty()) {
                NetworkHooks.openGui((ServerPlayerEntity)player, machine, pos);
                return ActionResultType.SUCCESS;
            }
            return ActionResultType.SUCCESS;
        }
        return super.onBlockActivated(state, world, pos, player, hand, hit);
    }

    @Override
    public ItemStack getPickBlock(BlockState state, RayTraceResult target, IBlockReader world, BlockPos pos, PlayerEntity player) {
        TileEntity tile = world.getTileEntity(pos);
        if(tile instanceof CustomMachineTile) {
            CustomMachineTile customMachineTile = (CustomMachineTile)tile;
            CustomMachine machine = customMachineTile.getMachine();
            ItemStack stack = new ItemStack(Registration.CUSTOM_MACHINE_ITEM.get());
            stack.getOrCreateTag().putString("id", machine.getId().toString());
            return stack;
        }
        return super.getPickBlock(state, target, world, pos, player);
    }

    @ParametersAreNonnullByDefault
    @Override
    public void onBlockHarvested(World world, BlockPos pos, BlockState state, PlayerEntity player) {
        super.onBlockHarvested(world, pos, state, player);
        if(!world.isRemote && !player.isCreative()) {
            TileEntity tile = world.getTileEntity(pos);
            if(tile instanceof CustomMachineTile) {
                CustomMachineTile machine = (CustomMachineTile) tile;
                machine.componentManager.getComponent(Registration.ITEM_MACHINE_COMPONENT.get()).ifPresent(component -> ((ItemComponentHandler)component).getComponents().stream().map(ItemMachineComponent::getItemStack).filter(stack -> stack != ItemStack.EMPTY).forEach(stack -> InventoryHelper.spawnItemStack(world, pos.getX(), pos.getY(), pos.getZ(), stack)));
                ItemStack machineItem = new ItemStack(Registration.CUSTOM_MACHINE_ITEM.get());
                machineItem.getOrCreateTag().putString("id", machine.getMachine().getId().toString());
                InventoryHelper.spawnItemStack(world, pos.getX(), pos.getY(), pos.getZ(), machineItem);
            }
        }
    }

    @ParametersAreNonnullByDefault
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
            CustomMachineTile machineTile = (CustomMachineTile)tile;
            return machineTile.getLightValue();
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
    @ParametersAreNonnullByDefault
    @Override
    public boolean canProvidePower(BlockState state) {
        return true;
    }

    @SuppressWarnings("deprecation")
    @ParametersAreNonnullByDefault
    @Override
    public boolean hasComparatorInputOverride(BlockState state) {
        return true;
    }

    @SuppressWarnings("deprecation")
    @ParametersAreNonnullByDefault
    @Override
    public int getComparatorInputOverride(BlockState state, World world, BlockPos pos) {
        TileEntity tile = world.getTileEntity(pos);
        if(tile instanceof CustomMachineTile)
            return ((CustomMachineTile)tile).redstoneManager.getComparatorInput();
        return 0;
    }

    @SuppressWarnings("deprecation")
    @ParametersAreNonnullByDefault
    @Override
    public int getStrongPower(BlockState state, IBlockReader world, BlockPos pos, Direction side) {
        TileEntity tile = world.getTileEntity(pos);
        if(tile instanceof CustomMachineTile)
            return ((CustomMachineTile)tile).redstoneManager.getPowerOutput();
        return 0;
    }

    @SuppressWarnings("deprecation")
    @ParametersAreNonnullByDefault
    @Override
    public int getWeakPower(BlockState state, IBlockReader world, BlockPos pos, Direction side) {
        TileEntity tile = world.getTileEntity(pos);
        if(tile instanceof CustomMachineTile)
            return ((CustomMachineTile)tile).redstoneManager.getPowerOutput();
        return 0;
    }
}
