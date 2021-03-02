package fr.frinn.custommachinery.common.init;

import fr.frinn.custommachinery.common.data.CustomMachine;
import net.minecraft.block.Block;
import net.minecraft.block.BlockRenderType;
import net.minecraft.block.BlockState;
import net.minecraft.block.material.Material;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.item.BlockItemUseContext;
import net.minecraft.item.ItemStack;
import net.minecraft.state.StateContainer;
import net.minecraft.state.properties.BlockStateProperties;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ActionResultType;
import net.minecraft.util.Hand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.BlockRayTraceResult;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.world.IBlockReader;
import net.minecraft.world.World;
import net.minecraftforge.energy.CapabilityEnergy;
import net.minecraftforge.fluids.FluidUtil;
import net.minecraftforge.fluids.capability.CapabilityFluidHandler;
import net.minecraftforge.fml.network.NetworkHooks;

import javax.annotation.Nullable;

public class CustomMachineBlock extends Block {

    public CustomMachineBlock() {
        super(Properties.create(Material.ROCK).notSolid());
    }

    @SuppressWarnings("deprecation")
    @Override
    public BlockRenderType getRenderType(BlockState state) {
        return BlockRenderType.INVISIBLE;
    }

    @SuppressWarnings("deprecation")
    @Override
    public ActionResultType onBlockActivated(BlockState state, World world, BlockPos pos, PlayerEntity player, Hand hand, BlockRayTraceResult hit) {
        TileEntity tile = world.getTileEntity(pos);
        if(tile instanceof CustomMachineTile) {
            CustomMachineTile machine = (CustomMachineTile)tile;
            machine.getCapability(CapabilityEnergy.ENERGY).ifPresent(energy -> energy.receiveEnergy(500, false));
            if(machine.getCapability(CapabilityFluidHandler.FLUID_HANDLER_CAPABILITY).map(fluidHandler -> FluidUtil.interactWithFluidHandler(player, hand, fluidHandler)).orElse(false)) {
                return ActionResultType.SUCCESS;
            }
            if(!world.isRemote()) {
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

    @Override
    protected void fillStateContainer(StateContainer.Builder<Block, BlockState> builder) {
        super.fillStateContainer(builder);
        builder.add(BlockStateProperties.HORIZONTAL_FACING);
    }

    @Nullable
    @Override
    public BlockState getStateForPlacement(BlockItemUseContext context) {
        return getDefaultState().with(BlockStateProperties.HORIZONTAL_FACING, context.getPlacementHorizontalFacing());
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
}
