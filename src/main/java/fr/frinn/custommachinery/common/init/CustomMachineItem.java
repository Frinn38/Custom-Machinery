package fr.frinn.custommachinery.common.init;

import fr.frinn.custommachinery.CustomMachinery;
import fr.frinn.custommachinery.common.data.CustomMachine;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.BlockItem;
import net.minecraft.item.Item;
import net.minecraft.item.ItemGroup;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.NonNullList;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.common.util.Constants;

import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;

public class CustomMachineItem extends BlockItem {

    public CustomMachineItem(Block block, Item.Properties properties) {
        super(block, properties);
    }

    @ParametersAreNonnullByDefault
    @Override
    protected boolean onBlockPlaced(BlockPos pos, World world, @Nullable PlayerEntity player, ItemStack stack, BlockState state) {
        if(stack.hasTag() && stack.getTag().contains("id", Constants.NBT.TAG_STRING)) {
            TileEntity tile = world.getTileEntity(pos);
            if(tile instanceof CustomMachineTile)
                ((CustomMachineTile)tile).setId(new ResourceLocation(stack.getTag().getString("id")));
        }
        return super.onBlockPlaced(pos, world, player, stack, state);
    }

    @ParametersAreNonnullByDefault
    @Override
    public void fillItemGroup(ItemGroup group, NonNullList<ItemStack> items) {
        if(this.isInGroup(group)) {
            CustomMachinery.MACHINES.keySet().forEach(id -> {
                ItemStack stack = new ItemStack(this);
                stack.getOrCreateTag().putString("id", id.toString());
                items.add(stack);
            });
        }
    }

    @ParametersAreNonnullByDefault
    @Override
    public void onCreated(ItemStack stack, World worldIn, PlayerEntity playerIn) {
        if(!stack.hasTag() || !stack.getTag().contains("id", Constants.NBT.TAG_STRING)) {
            CompoundNBT nbt = new CompoundNBT();
            nbt.putString("id", CustomMachineTile.DUMMY.toString());
            stack.setTag(nbt);
        }
        super.onCreated(stack, worldIn, playerIn);
    }

    @Override
    public String getTranslationKey(ItemStack stack) {
        if(stack.hasTag() && stack.hasTag() && stack.getTag().contains("id", Constants.NBT.TAG_STRING)) {
            CustomMachine machine = CustomMachinery.MACHINES.get(new ResourceLocation(stack.getTag().getString("id")));
            if(machine != null)
                return machine.getName();
        }
        return super.getTranslationKey(stack);
    }
}
