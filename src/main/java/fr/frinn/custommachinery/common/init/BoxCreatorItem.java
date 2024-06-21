package fr.frinn.custommachinery.common.init;

import com.mojang.datafixers.util.Pair;
import fr.frinn.custommachinery.common.util.Utils;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
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

import java.util.List;

public class BoxCreatorItem extends Item {

    public BoxCreatorItem(Properties properties) {
        super(properties);
    }

    public static BlockPos getSelectedBlock(boolean first, ItemStack stack) {
        Pair<BlockPos, BlockPos> selectedBlocks = stack.get(Registration.BOX_CREATOR_DATA);
        if(selectedBlocks == null)
            return BlockPos.ZERO;
        else
            return first ? selectedBlocks.getFirst() : selectedBlocks.getSecond();
    }

    /**
     * First block (blue) is set via CustomMachinery#boxRendererLeftClick
     * Second block (red) is set via this class use and useOn methods
     */
    public static void setSelectedBlock(boolean first, ItemStack stack, BlockPos pos) {
        stack.update(Registration.BOX_CREATOR_DATA, Pair.of(BlockPos.ZERO, BlockPos.ZERO), pair -> first ? Pair.of(pos, pair.getSecond()) : Pair.of(pair.getFirst(), pos));
    }

    @Override
    public void appendHoverText(ItemStack stack, TooltipContext context, List<Component> tooltip, TooltipFlag flag) {
        super.appendHoverText(stack, context, tooltip, flag);
        BlockPos block1 = getSelectedBlock(true, stack);
        if(block1 != BlockPos.ZERO)
            tooltip.add(Component.translatable("custommachinery.box_creator.first_block", block1.toShortString()).withStyle(ChatFormatting.BLUE));
        else
            tooltip.add(Component.translatable("custommachinery.box_creator.select_first_block").withStyle(ChatFormatting.BLUE));

        BlockPos block2 = getSelectedBlock(false, stack);
        if(block2 != BlockPos.ZERO)
            tooltip.add(Component.translatable("custommachinery.box_creator.second_block", block2.toShortString()).withStyle(ChatFormatting.RED));
        else
            tooltip.add(Component.translatable("custommachinery.box_creator.select_second_block").withStyle(ChatFormatting.RED));

        if(block1 != BlockPos.ZERO && block2 != BlockPos.ZERO)
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

        if(!(context.getLevel().getBlockState(pos).getBlock() instanceof CustomMachineBlock)) {
            setSelectedBlock(false, stack, pos);
            return InteractionResult.SUCCESS;
        } else if(block1 != null && block2 != null && !context.getLevel().isClientSide()) {
            AABB aabb = new AABB(block1.getX(), block1.getY(), block1.getZ(), block2.getX(), block2.getY(), block2.getZ());
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
        return false;
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);
        if(player.isCrouching() && stack.getItem() == this) {
            stack.remove(Registration.BOX_CREATOR_DATA);
            return InteractionResultHolder.success(stack);
        }
        return super.use(level, player, hand);
    }

    @Override
    public boolean isFoil(ItemStack stack) {
        return true;
    }
}
