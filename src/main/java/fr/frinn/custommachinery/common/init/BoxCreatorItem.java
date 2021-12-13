package fr.frinn.custommachinery.common.init;

import fr.frinn.custommachinery.CustomMachinery;
import fr.frinn.custommachinery.common.util.Utils;
import mcp.MethodsReturnNonnullByDefault;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ItemUseContext;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.state.properties.BlockStateProperties;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.*;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.*;
import net.minecraft.util.text.event.ClickEvent;
import net.minecraft.util.text.event.HoverEvent;
import net.minecraft.world.World;

import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;
import java.util.List;

@MethodsReturnNonnullByDefault
@ParametersAreNonnullByDefault
public class BoxCreatorItem extends Item {

    private static final String FIRST_BLOCK_KEY = "firstBlock";
    private static final String SECOND_BLOCK_KEY = "secondBlock";

    public BoxCreatorItem(Properties properties) {
        super(properties);
    }

    @Nullable
    public static BlockPos getSelectedBlock(boolean first, ItemStack stack) {
        CompoundNBT nbt = stack.getChildTag(CustomMachinery.MODID);
        if(nbt != null && nbt.getLong(first ? FIRST_BLOCK_KEY : SECOND_BLOCK_KEY) != 0L)
            return BlockPos.fromLong(nbt.getLong(first ? FIRST_BLOCK_KEY : SECOND_BLOCK_KEY));
        return null;
    }

    public static void setSelectedBlock(boolean first, ItemStack stack, BlockPos pos) {
        long packed = pos.toLong();
        stack.getOrCreateChildTag(CustomMachinery.MODID).putLong(first ? FIRST_BLOCK_KEY : SECOND_BLOCK_KEY, packed);
    }

    @Override
    public void addInformation(ItemStack stack, @Nullable World world, List<ITextComponent> tooltip, ITooltipFlag flag) {
        super.addInformation(stack, world, tooltip, flag);
        BlockPos block1 = getSelectedBlock(true, stack);
        if(block1 != null)
            tooltip.add(new TranslationTextComponent("custommachinery.box_creator.first_block", block1.getCoordinatesAsString()));

        BlockPos block2 = getSelectedBlock(false, stack);
        if(block2 != null)
            tooltip.add(new TranslationTextComponent("custommachinery.box_creator.second_block", block2.getCoordinatesAsString()));

        tooltip.add(new TranslationTextComponent("custommachinery.box_creator.reset"));
    }

    @Override
    public ActionResultType onItemUse(ItemUseContext context) {
        if(context.getPlayer() == null || context.getWorld().isRemote)
            return ActionResultType.PASS;
        ItemStack stack = context.getItem();
        BlockPos pos = context.getPos();

        BlockPos block1 = getSelectedBlock(true, stack);
        if(block1 == null) {
            setSelectedBlock(true, stack, pos);
            context.getPlayer().sendMessage(new TranslationTextComponent("custommachinery.box_creator.select_second_block"), Util.DUMMY_UUID);
            return ActionResultType.SUCCESS;
        }

        BlockPos block2 = getSelectedBlock(false, stack);
        if(block2 == null) {
            setSelectedBlock(false, stack, pos);
            context.getPlayer().sendMessage(new TranslationTextComponent("custommachinery.box_creator.select_machine"), Util.DUMMY_UUID);
            return ActionResultType.SUCCESS;
        }
        TileEntity tile = context.getWorld().getTileEntity(pos);
        if(tile instanceof CustomMachineTile) {
            AxisAlignedBB aabb = new AxisAlignedBB(block1, block2);
            aabb = aabb.offset(-pos.getX(), -pos.getY(), -pos.getZ());
            Direction direction = context.getWorld().getBlockState(pos).get(BlockStateProperties.HORIZONTAL_FACING);
            aabb = Utils.rotateBox(aabb, direction);
            String boxString = "[" + (int)aabb.minX + ", " + (int)aabb.minY + ", " + (int)aabb.minZ + ", " + (int)aabb.maxX + ", " + (int)aabb.maxY + ", " + (int)aabb.maxZ + "]";
            ITextComponent boxText = new StringTextComponent(boxString).mergeStyle(Style.EMPTY.applyFormatting(TextFormatting.AQUA).setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new TranslationTextComponent("custommachinery.box_creator.copy"))).setClickEvent(new ClickEvent(ClickEvent.Action.COPY_TO_CLIPBOARD, boxString)));
            ITextComponent message = new TranslationTextComponent("custommachinery.box_creator.create_box", boxText);
            context.getPlayer().sendMessage(message, Util.DUMMY_UUID);
            stack.removeChildTag(CustomMachinery.MODID);
            return ActionResultType.SUCCESS;
        }
        return ActionResultType.PASS;
    }

    @Override
    public ActionResult<ItemStack> onItemRightClick(World world, PlayerEntity player, Hand hand) {
        ItemStack stack = player.getHeldItem(hand);
        if(player.isCrouching() && stack.getItem() == this) {
            stack.removeChildTag(CustomMachinery.MODID);
            return ActionResult.resultSuccess(stack);
        }
        return super.onItemRightClick(world, player, hand);
    }

    @Override
    public ITextComponent getHighlightTip(ItemStack stack, ITextComponent name) {
        if(getSelectedBlock(true, stack) == null)
            return new TranslationTextComponent("custommachinery.box_creator.select_first_block");
        else if(getSelectedBlock(false, stack) == null)
            return new TranslationTextComponent("custommachinery.box_creator.select_second_block");
        else return new TranslationTextComponent("custommachinery.box_creator.select_machine");
    }

    @Override
    public boolean hasEffect(ItemStack stack) {
        return true;
    }
}
