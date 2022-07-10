package fr.frinn.custommachinery.common.init;

import fr.frinn.custommachinery.CustomMachinery;
import fr.frinn.custommachinery.common.machine.CustomMachine;
import fr.frinn.custommachinery.common.util.Utils;
import net.minecraft.core.NonNullList;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;

import javax.annotation.Nullable;
import java.util.List;
import java.util.Optional;

public class CustomMachineItem extends BlockItem {

    public static final String MACHINE_TAG_KEY = "machine";

    public CustomMachineItem(Block block, Item.Properties properties) {
        super(block, properties);
    }

    public static Optional<CustomMachine> getMachine(ItemStack stack) {
        if(stack.getItem() == Registration.CUSTOM_MACHINE_ITEM.get() && stack.getTag() != null && stack.getTag().contains(MACHINE_TAG_KEY, Tag.TAG_STRING) && Utils.isResourceNameValid(stack.getTag().getString(MACHINE_TAG_KEY))) {
            ResourceLocation machineID = new ResourceLocation(stack.getTag().getString(MACHINE_TAG_KEY));
            if(machineID.equals(CustomMachine.DUMMY.getId()))
                return Optional.of(CustomMachine.DUMMY);
            return Optional.ofNullable(CustomMachinery.MACHINES.get(machineID));
        }
        return Optional.empty();
    }

    public static ItemStack makeMachineItem(ResourceLocation machineID) {
        CompoundTag nbt = new CompoundTag();
        nbt.putString(MACHINE_TAG_KEY, machineID.toString());
        ItemStack stack = Registration.CUSTOM_MACHINE_ITEM.get().getDefaultInstance();
        stack.setTag(nbt);
        return stack;
    }

    @Override
    public void fillItemCategory(CreativeModeTab group, NonNullList<ItemStack> items) {
        if(this.allowdedIn(group))
            CustomMachinery.MACHINES.keySet().forEach(id -> items.add(makeMachineItem(id)));
    }

    @Override
    public void onCraftedBy(ItemStack stack, Level worldIn, Player playerIn) {
        if(stack.getTag() == null || !stack.getTag().contains(MACHINE_TAG_KEY, Tag.TAG_STRING)) {
            CompoundTag nbt = new CompoundTag();
            nbt.putString(MACHINE_TAG_KEY, CustomMachine.DUMMY.getId().toString());
            stack.setTag(nbt);
        }
        super.onCraftedBy(stack, worldIn, playerIn);
    }


    @Override
    public Component getName(ItemStack stack) {
        return getMachine(stack).map(CustomMachine::getName).orElse(super.getName(stack));
    }

    @Override
    public void appendHoverText(ItemStack stack, @Nullable Level world, List<Component> tooltip, TooltipFlag flag) {
        getMachine(stack).map(CustomMachine::getTooltips).ifPresent(tooltip::addAll);
    }
}
