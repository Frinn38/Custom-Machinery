package fr.frinn.custommachinery.common.init;

import fr.frinn.custommachinery.CustomMachinery;
import fr.frinn.custommachinery.common.util.Utils;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ItemUseContext;
import net.minecraft.state.properties.BlockStateProperties;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.*;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.*;
import net.minecraft.util.text.event.ClickEvent;
import net.minecraft.util.text.event.HoverEvent;
import net.minecraft.world.World;
import net.minecraftforge.common.util.Constants;
import org.lwjgl.system.CallbackI;

import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;
import java.util.Arrays;
import java.util.List;

@ParametersAreNonnullByDefault
public class BoxCreatorItem extends Item {

    private static final String FIRST_BLOCK_KEY = "firstBlock";
    private static final String SECOND_BLOCK_KEY = "secondBlock";

    public BoxCreatorItem(Properties properties) {
        super(properties);
    }

    @Override
    public void addInformation(ItemStack stack, @Nullable World world, List<ITextComponent> tooltip, ITooltipFlag flag) {
        super.addInformation(stack, world, tooltip, flag);
        if(stack.getOrCreateChildTag(CustomMachinery.MODID).contains(FIRST_BLOCK_KEY, Constants.NBT.TAG_INT_ARRAY) && stack.getOrCreateChildTag(CustomMachinery.MODID).getIntArray(FIRST_BLOCK_KEY).length == 3) {
            int[] array = stack.getOrCreateChildTag(CustomMachinery.MODID).getIntArray(FIRST_BLOCK_KEY);
            tooltip.add(new TranslationTextComponent("custommachinery.box_creator.first_block", Arrays.toString(array)));
        }
        if(stack.getOrCreateChildTag(CustomMachinery.MODID).contains(SECOND_BLOCK_KEY, Constants.NBT.TAG_INT_ARRAY) && stack.getOrCreateChildTag(CustomMachinery.MODID).getIntArray(SECOND_BLOCK_KEY).length == 3) {
            int[] array = stack.getOrCreateChildTag(CustomMachinery.MODID).getIntArray(SECOND_BLOCK_KEY);
            tooltip.add(new TranslationTextComponent("custommachinery.box_creator.second_block", Arrays.toString(array)));
        }
    }

    @Override
    public ActionResultType onItemUse(ItemUseContext context) {
        if(context.getPlayer() == null || context.getWorld().isRemote)
            return ActionResultType.PASS;
        ItemStack stack = context.getItem();
        int[] array = new int[]{context.getPos().getX(), context.getPos().getY(), context.getPos().getZ()};
        if(!stack.getOrCreateChildTag(CustomMachinery.MODID).contains(FIRST_BLOCK_KEY, Constants.NBT.TAG_INT_ARRAY)) {
            stack.getOrCreateChildTag(CustomMachinery.MODID).putIntArray(FIRST_BLOCK_KEY, array);
            context.getPlayer().sendMessage(new TranslationTextComponent("custommachinery.box_creator.select_second_block"), Util.DUMMY_UUID);
            return ActionResultType.SUCCESS;
        }
        else if(!stack.getOrCreateChildTag(CustomMachinery.MODID).contains(SECOND_BLOCK_KEY, Constants.NBT.TAG_INT_ARRAY)) {
            stack.getOrCreateChildTag(CustomMachinery.MODID).putIntArray(SECOND_BLOCK_KEY, array);
            context.getPlayer().sendMessage(new TranslationTextComponent("custommachinery.box_creator.select_machine"), Util.DUMMY_UUID);
            return ActionResultType.SUCCESS;
        }
        else {
            BlockPos pos = context.getPos();
            TileEntity tile = context.getWorld().getTileEntity(pos);
            if(tile instanceof CustomMachineTile) {
                int[] array1 = stack.getOrCreateChildTag(CustomMachinery.MODID).getIntArray(FIRST_BLOCK_KEY);
                BlockPos pos1 = new BlockPos(array1[0], array1[1], array1[2]);
                int[] array2 = stack.getOrCreateChildTag(CustomMachinery.MODID).getIntArray(SECOND_BLOCK_KEY);
                BlockPos pos2 = new BlockPos(array2[0], array2[1], array2[2]);
                AxisAlignedBB aabb = new AxisAlignedBB(pos1, pos2);
                aabb = aabb.offset(-pos.getX(), -pos.getY(), -pos.getZ());
                Direction direction = context.getWorld().getBlockState(pos).get(BlockStateProperties.HORIZONTAL_FACING);
                aabb = Utils.rotateBox(aabb, direction);
                String boxString = "[" + (int)aabb.minX + ", " + (int)aabb.minY + ", " + (int)aabb.minZ + ", " + (int)aabb.maxX + ", " + (int)aabb.maxY + ", " + (int)aabb.maxZ + "]";
                ITextComponent boxText = new StringTextComponent(boxString).mergeStyle(Style.EMPTY.applyFormatting(TextFormatting.AQUA).setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new TranslationTextComponent("custommachinery.box_creator.copy"))).setClickEvent(new ClickEvent(ClickEvent.Action.COPY_TO_CLIPBOARD, boxString)));
                ITextComponent message = new TranslationTextComponent("custommachinery.box_creator.create_box", boxText);
                context.getPlayer().sendMessage(message, Util.DUMMY_UUID);
                stack.getChildTag(CustomMachinery.MODID).remove(FIRST_BLOCK_KEY);
                stack.getChildTag(CustomMachinery.MODID).remove(SECOND_BLOCK_KEY);
                return ActionResultType.SUCCESS;
            }
        }
        return ActionResultType.PASS;
    }

    @Override
    public ActionResult<ItemStack> onItemRightClick(World world, PlayerEntity player, Hand hand) {
        if(player.isCrouching()) {
            ItemStack stack = player.getHeldItem(hand);
            if(stack.getItem() == this) {
                stack.getOrCreateChildTag(CustomMachinery.MODID).remove(FIRST_BLOCK_KEY);
                stack.getOrCreateChildTag(CustomMachinery.MODID).remove(SECOND_BLOCK_KEY);
            }
        }
        return super.onItemRightClick(world, player, hand);
    }

    @Override
    public ITextComponent getHighlightTip(ItemStack stack, ITextComponent name) {
        if(stack.getChildTag(CustomMachinery.MODID) != null && !stack.getChildTag(CustomMachinery.MODID).contains(FIRST_BLOCK_KEY, Constants.NBT.TAG_INT_ARRAY))
            return new TranslationTextComponent("custommachinery.box_creator.select_first_block");
        else return new TranslationTextComponent("custommachinery.box_creator.reset");
    }
}
