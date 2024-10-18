package fr.frinn.custommachinery.common.init;

import fr.frinn.custommachinery.CustomMachinery;
import fr.frinn.custommachinery.PlatformHelper;
import fr.frinn.custommachinery.common.machine.CustomMachine;
import fr.frinn.custommachinery.common.util.Utils;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.advancements.CriteriaTriggers;
import net.minecraft.core.BlockPos;
import net.minecraft.core.NonNullList;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.gameevent.GameEvent;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.Optional;

public class CustomMachineItem extends BlockItem {

    public static final String MACHINE_TAG_KEY = "machine";

    @Nullable
    private final ResourceLocation machineID;

    public CustomMachineItem(Block block, Item.Properties properties, @Nullable ResourceLocation machineID) {
        super(block, properties);
        this.machineID = machineID;
    }

    public static Optional<CustomMachine> getMachine(ItemStack stack) {
        if(stack.getItem() instanceof CustomMachineItem customMachineItem && customMachineItem.machineID != null)
            return Optional.ofNullable(CustomMachinery.MACHINES.get(customMachineItem.machineID));
        else if(stack.getItem() == Registration.CUSTOM_MACHINE_ITEM.get() && stack.getTag() != null && stack.getTag().contains(MACHINE_TAG_KEY, Tag.TAG_STRING) && Utils.isResourceNameValid(stack.getTag().getString(MACHINE_TAG_KEY))) {
            ResourceLocation machineID = new ResourceLocation(stack.getTag().getString(MACHINE_TAG_KEY));
            if(machineID.equals(CustomMachine.DUMMY.getId()))
                return Optional.of(CustomMachine.DUMMY);
            return Optional.ofNullable(CustomMachinery.MACHINES.get(machineID));
        }
        return Optional.empty();
    }

    public static ItemStack makeMachineItem(ResourceLocation machineID) {
        if(CustomMachinery.CUSTOM_BLOCK_MACHINES.containsKey(machineID))
            return CustomMachinery.CUSTOM_BLOCK_MACHINES.get(machineID).asItem().getDefaultInstance();

        ItemStack stack = Registration.CUSTOM_MACHINE_ITEM.get().getDefaultInstance();
        CompoundTag nbt = new CompoundTag();
        nbt.putString(MACHINE_TAG_KEY, machineID.toString());
        stack.setTag(nbt);
        return stack;
    }

    @Override
    public void fillItemCategory(CreativeModeTab group, NonNullList<ItemStack> items) {
        if(group == this.category || group == CreativeModeTab.TAB_SEARCH) {
            if(this.machineID != null)
                items.add(this.getDefaultInstance());
            else
                CustomMachinery.MACHINES.keySet().stream()
                        .filter(id -> !CustomMachinery.CUSTOM_BLOCK_MACHINES.containsKey(id))
                        .forEach(id -> items.add(makeMachineItem(id)));
        }
    }

    @Override
    public Component getName(ItemStack stack) {
        return getMachine(stack).map(CustomMachine::getName).orElse(super.getName(stack));
    }

    @Override
    public void appendHoverText(ItemStack stack, @Nullable Level world, List<Component> tooltip, TooltipFlag flag) {
        getMachine(stack).map(CustomMachine::getTooltips).ifPresent(tooltip::addAll);
    }

    @Override
    public InteractionResult place(BlockPlaceContext blockPlaceContext) {
        if (!blockPlaceContext.canPlace()) {
            return InteractionResult.FAIL;
        } else {
            BlockPlaceContext blockPlaceContext2 = this.updatePlacementContext(blockPlaceContext);
            if (blockPlaceContext2 == null) {
                return InteractionResult.FAIL;
            } else {
                BlockState blockState = this.getPlacementState(blockPlaceContext2);
                if (blockState == null) {
                    return InteractionResult.FAIL;
                } else if (!this.placeBlock(blockPlaceContext2, blockState)) {
                    return InteractionResult.FAIL;
                } else {
                    BlockPos blockPos = blockPlaceContext2.getClickedPos();
                    Level level = blockPlaceContext2.getLevel();
                    Player player = blockPlaceContext2.getPlayer();
                    ItemStack itemStack = blockPlaceContext2.getItemInHand();
                    BlockState blockState2 = level.getBlockState(blockPos);
                    if (blockState2.is(blockState.getBlock())) {
                        this.updateCustomBlockEntityTag(blockPos, level, player, itemStack, blockState2);
                        blockState2.getBlock().setPlacedBy(level, blockPos, blockState2, player, itemStack);
                        if (player instanceof ServerPlayer) {
                            CriteriaTriggers.PLACED_BLOCK.trigger((ServerPlayer)player, blockPos, itemStack);
                        }
                    }

                    if(blockState2.getBlock() instanceof CustomMachineBlock machineBlock) {
                        SoundType soundType = machineBlock.getSoundType(blockState, level, blockPos, player);
                        level.playSound(player, blockPos, soundType.getPlaceSound(), SoundSource.BLOCKS, (soundType.getVolume() + 1.0F) / 2.0F, soundType.getPitch() * 0.8F);
                        level.gameEvent(player, GameEvent.BLOCK_PLACE, blockPos);
                    }

                    if (player == null || !player.getAbilities().instabuild) {
                        itemStack.shrink(1);
                    }

                    return InteractionResult.sidedSuccess(level.isClientSide);
                }
            }
        }
    }
}
