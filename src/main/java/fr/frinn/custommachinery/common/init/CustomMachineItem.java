package fr.frinn.custommachinery.common.init;

import fr.frinn.custommachinery.CustomMachinery;
import fr.frinn.custommachinery.common.data.CustomMachine;
import fr.frinn.custommachinery.common.util.Utils;
import mcp.MethodsReturnNonnullByDefault;
import net.minecraft.block.Block;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.BlockItem;
import net.minecraft.item.Item;
import net.minecraft.item.ItemGroup;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.util.NonNullList;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.world.World;
import net.minecraftforge.common.util.Constants;

import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;
import java.util.List;
import java.util.Optional;

@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
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

    @Override
    public void addInformation(ItemStack stack, @Nullable World world, List<ITextComponent> tooltip, ITooltipFlag flag) {
        getMachine(stack).map(CustomMachine::getTooltips).ifPresent(tooltip::addAll);
    }
}
