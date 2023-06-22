package fr.frinn.custommachinery.common.init;

import fr.frinn.custommachinery.CustomMachinery;
import fr.frinn.custommachinery.common.util.Utils;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.ClickEvent;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.HoverEvent;
import net.minecraft.network.chat.Style;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.phys.AABB;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class BoxCreatorItem extends Item {

    private static final String FIRST_BLOCK_KEY = "firstBlock";
    private static final String SECOND_BLOCK_KEY = "secondBlock";

    public BoxCreatorItem(Properties properties) {
        super(properties);
    }

    @Nullable
    public static BlockPos getSelectedBlock(boolean first, ItemStack stack) {
        CompoundTag nbt = stack.getTagElement(CustomMachinery.MODID);
        if(nbt != null && nbt.getLong(first ? FIRST_BLOCK_KEY : SECOND_BLOCK_KEY) != 0L)
            return BlockPos.of(nbt.getLong(first ? FIRST_BLOCK_KEY : SECOND_BLOCK_KEY));
        return null;
    }

    public static void setSelectedBlock(boolean first, ItemStack stack, BlockPos pos) {
        long packed = pos.asLong();
        stack.getOrCreateTagElement(CustomMachinery.MODID).putLong(first ? FIRST_BLOCK_KEY : SECOND_BLOCK_KEY, packed);
    }

    @Override
    public void appendHoverText(ItemStack stack, @Nullable Level world, List<Component> tooltip, TooltipFlag flag) {
        super.appendHoverText(stack, world, tooltip, flag);
        BlockPos block1 = getSelectedBlock(true, stack);
        if(block1 != null)
            tooltip.add(Component.translatable("custommachinery.box_creator.first_block", block1.toShortString()).withStyle(ChatFormatting.BLUE));
        else
            tooltip.add(Component.translatable("custommachinery.box_creator.select_first_block").withStyle(ChatFormatting.BLUE));

        BlockPos block2 = getSelectedBlock(false, stack);
        if(block2 != null)
            tooltip.add(Component.translatable("custommachinery.box_creator.second_block", block2.toShortString()).withStyle(ChatFormatting.RED));
        else
            tooltip.add(Component.translatable("custommachinery.box_creator.select_second_block").withStyle(ChatFormatting.RED));

        if(block1 != null && block2 != null)
            tooltip.add(Component.translatable("custommachinery.box_creator.select_machine").withStyle(ChatFormatting.GREEN));

        tooltip.add(Component.translatable("custommachinery.box_creator.reset").withStyle(ChatFormatting.GOLD));
    }

    @Override
    public InteractionResult useOn(UseOnContext context) {
        if(context.getPlayer() == null)
            return InteractionResult.PASS;
        ItemStack stack = context.getItemInHand();
        BlockPos pos = context.getClickedPos();

        BlockPos block1 = getSelectedBlock(true, stack);
        BlockPos block2 = getSelectedBlock(false, stack);

        if(!context.getLevel().getBlockState(pos).is(Registration.CUSTOM_MACHINE_BLOCK.get())) {
            setSelectedBlock(false, stack, pos);
            return InteractionResult.SUCCESS;
        } else if(block1 != null && block2 != null && !context.getLevel().isClientSide()) {
            AABB aabb = new AABB(block1, block2);
            aabb = aabb.move(-pos.getX(), -pos.getY(), -pos.getZ());
            Direction direction = context.getLevel().getBlockState(pos).getValue(BlockStateProperties.HORIZONTAL_FACING);
            aabb = Utils.rotateBox(aabb, direction);
            String boxString = "[" + (int)aabb.minX + ", " + (int)aabb.minY + ", " + (int)aabb.minZ + ", " + (int)aabb.maxX + ", " + (int)aabb.maxY + ", " + (int)aabb.maxZ + "]";
            Component boxText = Component.literal(boxString).withStyle(Style.EMPTY.applyFormat(ChatFormatting.AQUA).withHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, Component.translatable("custommachinery.box_creator.copy"))).withClickEvent(new ClickEvent(ClickEvent.Action.COPY_TO_CLIPBOARD, boxString)));
            Component message = Component.translatable("custommachinery.box_creator.create_box", boxText);
            context.getPlayer().sendSystemMessage(message);
            return InteractionResult.SUCCESS;
        }
        return InteractionResult.PASS;
    }

    @Override
    public boolean canAttackBlock(BlockState state, Level level, BlockPos pos, Player player) {
        setSelectedBlock(true, player.getMainHandItem(), pos);
        return false;
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);
        if(player.isCrouching() && stack.getItem() == this) {
            stack.removeTagKey(CustomMachinery.MODID);
            return InteractionResultHolder.success(stack);
        }
        return super.use(level, player, hand);
    }

    @Override
    public boolean isFoil(ItemStack stack) {
        return true;
    }
}
