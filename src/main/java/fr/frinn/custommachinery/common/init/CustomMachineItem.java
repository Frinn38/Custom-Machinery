package fr.frinn.custommachinery.common.init;

import fr.frinn.custommachinery.CustomMachinery;
import fr.frinn.custommachinery.common.data.CustomMachine;
import fr.frinn.custommachinery.common.network.NetworkManager;
import fr.frinn.custommachinery.common.network.SRefreshCustomMachineTilePacket;
import fr.frinn.custommachinery.common.util.Utils;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.BlockItem;
import net.minecraft.item.Item;
import net.minecraft.item.ItemGroup;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.Hand;
import net.minecraft.util.NonNullList;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.concurrent.TickDelayedTask;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.world.World;
import net.minecraftforge.common.util.Constants;
import net.minecraftforge.fml.network.PacketDistributor;

import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;
import java.util.Optional;

@ParametersAreNonnullByDefault
public class CustomMachineItem extends BlockItem {

    public static final String MACHINE_TAG_KEY = "machine";

    public CustomMachineItem(Block block, Item.Properties properties) {
        super(block, properties);
    }

    public static Optional<CustomMachine> getMachine(ItemStack stack) {
        if(stack.getItem() == Registration.CUSTOM_MACHINE_ITEM.get() && stack.getTag() != null && stack.getTag().contains(MACHINE_TAG_KEY, Constants.NBT.TAG_STRING) && Utils.isResourceNameValid(stack.getTag().getString(MACHINE_TAG_KEY))) {
            ResourceLocation machineID = new ResourceLocation(stack.getTag().getString(MACHINE_TAG_KEY));
            if(machineID.equals(CustomMachine.DUMMY.getId()))
                return Optional.of(CustomMachine.DUMMY);
            return Optional.ofNullable(CustomMachinery.MACHINES.get(machineID));
        }
        return Optional.empty();
    }

    public static ItemStack makeMachineItem(ResourceLocation machineID) {
        CompoundNBT nbt = new CompoundNBT();
        nbt.putString(MACHINE_TAG_KEY, machineID.toString());
        ItemStack stack = Registration.CUSTOM_MACHINE_ITEM.get().getDefaultInstance();
        stack.setTag(nbt);
        return stack;
    }

    @Override
    protected boolean onBlockPlaced(BlockPos pos, World world, @Nullable PlayerEntity player, ItemStack stack, BlockState state) {
        getMachine(stack).ifPresent(machine -> {
            TileEntity tile = world.getTileEntity(pos);
            if(tile instanceof CustomMachineTile) {
                ((CustomMachineTile)tile).setId(machine.getId());
                if(!world.isRemote() && world.getServer() != null && player != null && player.getHeldItem(Hand.OFF_HAND) == stack)
                    world.getServer().enqueue(new TickDelayedTask(1, () -> NetworkManager.CHANNEL.send(PacketDistributor.TRACKING_CHUNK.with(() -> world.getChunkAt(pos)), new SRefreshCustomMachineTilePacket(pos, machine.getId()))));
            }
        });
        return true;
    }

    @Override
    public void fillItemGroup(ItemGroup group, NonNullList<ItemStack> items) {
        if(this.isInGroup(group))
            CustomMachinery.MACHINES.keySet().forEach(id -> items.add(makeMachineItem(id)));
    }

    @Override
    public void onCreated(ItemStack stack, World worldIn, PlayerEntity playerIn) {
        if(stack.getTag() == null || !stack.getTag().contains(MACHINE_TAG_KEY, Constants.NBT.TAG_STRING)) {
            CompoundNBT nbt = new CompoundNBT();
            nbt.putString(MACHINE_TAG_KEY, CustomMachine.DUMMY.getId().toString());
            stack.setTag(nbt);
        }
        super.onCreated(stack, worldIn, playerIn);
    }


    @Override
    public ITextComponent getDisplayName(ItemStack stack) {
        return getMachine(stack).map(CustomMachine::getName).orElse(super.getDisplayName(stack));
    }
}
