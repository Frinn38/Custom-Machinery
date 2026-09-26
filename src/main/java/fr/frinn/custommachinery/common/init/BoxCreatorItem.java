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
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.component.TooltipDisplay;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.phys.AABB;

import java.util.function.Consumer;

public class BoxCreatorItem extends Item {

    public BoxCreatorItem(Properties properties) {
        super(properties);
    }

    public static BlockPos getSelectedBlock(boolean first, ItemStack stack) {
        Pair<BlockPos, BlockPos> selectedBlocks = stack.get(CMRegistration.BOX_CREATOR_DATA);
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
        stack.update(CMRegistration.BOX_CREATOR_DATA, Pair.of(BlockPos.ZERO, BlockPos.ZERO), pair -> first ? Pair.of(pos, pair.getSecond()) : Pair.of(pair.getFirst(), pos));
    }

    @Override
    public void appendHoverText(ItemStack stack, TooltipContext context, TooltipDisplay display, Consumer<Component> builder, TooltipFlag tooltipFlag) {
        super.appendHoverText(stack, context, display, builder, tooltipFlag);
        BlockPos block1 = getSelectedBlock(true, stack);
        if(block1 != BlockPos.ZERO)
            builder.accept(Component.translatable("custommachinery.box_creator.first_block", block1.toShortString()).withStyle(ChatFormatting.BLUE));
        else
            builder.accept(Component.translatable("custommachinery.box_creator.select_first_block").withStyle(ChatFormatting.BLUE));

        BlockPos block2 = getSelectedBlock(false, stack);
        if(block2 != BlockPos.ZERO)
            builder.accept(Component.translatable("custommachinery.box_creator.second_block", block2.toShortString()).withStyle(ChatFormatting.RED));
        else
            builder.accept(Component.translatable("custommachinery.box_creator.select_second_block").withStyle(ChatFormatting.RED));

        if(block1 != BlockPos.ZERO && block2 != BlockPos.ZERO)
            builder.accept(Component.translatable("custommachinery.box_creator.select_machine").withStyle(ChatFormatting.GREEN));

        builder.accept(Component.translatable("custommachinery.box_creator.reset").withStyle(ChatFormatting.GOLD));
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
        } else if(block1 != BlockPos.ZERO && block2 != BlockPos.ZERO && !context.getLevel().isClientSide()) {
            AABB aabb = new AABB(block1.getX(), block1.getY(), block1.getZ(), block2.getX(), block2.getY(), block2.getZ());
            aabb = aabb.move(-pos.getX(), -pos.getY(), -pos.getZ());
            Direction direction = context.getLevel().getBlockState(pos).getValue(BlockStateProperties.HORIZONTAL_FACING);
            aabb = Utils.rotateBox(aabb, direction.getOpposite());
            String boxString = "[" + (int)aabb.minX + ", " + (int)aabb.minY + ", " + (int)aabb.minZ + ", " + (int)aabb.maxX + ", " + (int)aabb.maxY + ", " + (int)aabb.maxZ + "]";
            Component boxText = Component.literal(boxString).withStyle(Style.EMPTY.applyFormat(ChatFormatting.AQUA).withHoverEvent(new HoverEvent.ShowText(Component.translatable("custommachinery.box_creator.copy"))).withClickEvent(new ClickEvent.CopyToClipboard(boxString)));
            Component message = Component.translatable("custommachinery.box_creator.create_box", boxText);
            context.getPlayer().sendSystemMessage(message);
            return InteractionResult.SUCCESS;
        }
        return InteractionResult.PASS;
    }

    @Override
    public boolean mineBlock(ItemStack stack, Level level, BlockState state, BlockPos pos, LivingEntity owner) {
        return false;
    }

    @Override
    public InteractionResult use(Level level, Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);
        if(player.isCrouching() && stack.getItem() == this) {
            stack.remove(CMRegistration.BOX_CREATOR_DATA);
            return InteractionResult.SUCCESS.heldItemTransformedTo(stack);
        }
        return super.use(level, player, hand);
    }

    @Override
    public boolean isFoil(ItemStack stack) {
        return true;
    }
}
