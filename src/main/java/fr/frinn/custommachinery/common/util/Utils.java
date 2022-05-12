package fr.frinn.custommachinery.common.util;

import com.mojang.datafixers.util.Pair;
import fr.frinn.custommachinery.CustomMachinery;
import fr.frinn.custommachinery.api.component.handler.IComponentHandler;
import fr.frinn.custommachinery.common.data.MachineAppearance;
import fr.frinn.custommachinery.common.data.component.variant.item.UpgradeItemComponentVariant;
import fr.frinn.custommachinery.common.data.upgrade.RecipeModifier;
import fr.frinn.custommachinery.common.init.CustomMachineTile;
import fr.frinn.custommachinery.common.init.Registration;
import fr.frinn.custommachinery.common.util.ingredient.IIngredient;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.fluid.Fluid;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.TieredItem;
import net.minecraft.nbt.ByteArrayNBT;
import net.minecraft.nbt.ByteNBT;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.nbt.DoubleNBT;
import net.minecraft.nbt.FloatNBT;
import net.minecraft.nbt.INBT;
import net.minecraft.nbt.IntArrayNBT;
import net.minecraft.nbt.IntNBT;
import net.minecraft.nbt.ListNBT;
import net.minecraft.nbt.LongArrayNBT;
import net.minecraft.nbt.LongNBT;
import net.minecraft.nbt.NumberNBT;
import net.minecraft.nbt.ShortNBT;
import net.minecraft.potion.EffectInstance;
import net.minecraft.potion.EffectUtils;
import net.minecraft.potion.Effects;
import net.minecraft.tags.FluidTags;
import net.minecraft.tags.ITag;
import net.minecraft.tags.TagCollectionManager;
import net.minecraft.util.Direction;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.ResourceLocationException;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraft.world.IBlockReader;
import net.minecraftforge.common.ToolType;
import net.minecraftforge.common.util.Constants;
import net.minecraftforge.event.ForgeEventFactory;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.Map;
import java.util.Objects;
import java.util.Random;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class Utils {

    public static final Random RAND = new Random();

    public static boolean canPlayerManageMachines(PlayerEntity player) {
        return player.hasPermissionLevel(Objects.requireNonNull(player.getServer()).getOpPermissionLevel());
    }

    public static ResourceLocation getItemTagID(ITag<Item> tag) {
        return TagCollectionManager.getManager().getItemTags().getValidatedIdFromTag(tag);
    }

    public static ResourceLocation getFluidTagID(ITag<Fluid> tag) {
        return TagCollectionManager.getManager().getFluidTags().getValidatedIdFromTag(tag);
    }

    public static ResourceLocation getBlockTagID(ITag<Block> tag) {
        return TagCollectionManager.getManager().getBlockTags().getValidatedIdFromTag(tag);
    }

    public static ITag<Item> getItemTag(ResourceLocation loc) {
        return TagCollectionManager.getManager().getItemTags().get(loc);
    }

    public static ITag<Fluid> getFluidTag(ResourceLocation loc) {
        return TagCollectionManager.getManager().getFluidTags().get(loc);
    }

    public static ITag<Block> getBlockTag(ResourceLocation loc) {
        return TagCollectionManager.getManager().getBlockTags().get(loc);
    }

    public static Vector3d vec3dFromBlockPos(BlockPos pos) {
        return new Vector3d(pos.getX(), pos.getY(), pos.getZ());
    }

    public static boolean testNBT(CompoundNBT nbt, @Nullable CompoundNBT tested) {
        if(tested == null)
            return false;
        for (String key : tested.keySet()) {
            if(!nbt.contains(key) || nbt.getTagId(key) != tested.getTagId(key) || !testINBT(nbt.get(key), tested.get(key)))
                return false;
        }
        return true;
    }

    public static <T extends INBT> boolean testINBT(@Nullable T inbt, @Nullable T tested) {
        if(inbt == null || tested == null)
            return false;
        switch (inbt.getId()) {
            case Constants.NBT.TAG_BYTE:
                return ((ByteNBT)inbt).getByte() == ((ByteNBT)tested).getByte();
            case Constants.NBT.TAG_SHORT:
                return ((ShortNBT)inbt).getShort() == ((ShortNBT)tested).getShort();
            case Constants.NBT.TAG_INT:
                return ((IntNBT)inbt).getInt() == ((IntNBT)tested).getInt();
            case Constants.NBT.TAG_LONG:
                return ((LongNBT)inbt).getLong() == ((LongNBT)tested).getLong();
            case Constants.NBT.TAG_FLOAT:
                return ((FloatNBT)inbt).getFloat() == ((FloatNBT)tested).getFloat();
            case Constants.NBT.TAG_DOUBLE:
                return ((DoubleNBT)inbt).getDouble() == ((DoubleNBT)tested).getDouble();
            case Constants.NBT.TAG_BYTE_ARRAY:
                return ((ByteArrayNBT)inbt).containsAll((ByteArrayNBT)tested);
            case Constants.NBT.TAG_STRING:
                return inbt.getString().equals(tested.getString());
            case Constants.NBT.TAG_LIST:
                return ((ListNBT)inbt).containsAll((ListNBT)tested);
            case Constants.NBT.TAG_COMPOUND:
                return testNBT((CompoundNBT)inbt, (CompoundNBT)tested);
            case Constants.NBT.TAG_INT_ARRAY:
                return ((IntArrayNBT)inbt).containsAll((IntArrayNBT)tested);
            case Constants.NBT.TAG_LONG_ARRAY:
                return ((LongArrayNBT)inbt).containsAll((LongArrayNBT)tested);
            case Constants.NBT.TAG_ANY_NUMERIC:
                return ((NumberNBT)inbt).getAsNumber().equals(((NumberNBT)tested).getAsNumber());
            default:
                return false;
        }
    }

    public static Map<RecipeModifier, Integer> getModifiersForTile(CustomMachineTile tile) {
        return tile.componentManager.getComponentHandler(Registration.ITEM_MACHINE_COMPONENT.get())
                .map(IComponentHandler::getComponents)
                .orElse(new ArrayList<>())
                .stream()
                .filter(component -> component.getVariant() == UpgradeItemComponentVariant.INSTANCE)
                .map(component -> Pair.of(component.getItemStack().getItem(), component.getItemStack().getCount()))
                .flatMap(pair -> CustomMachinery.UPGRADES.stream().filter(upgrade -> upgrade.getItem() == pair.getFirst() && upgrade.getMachines().contains(tile.getMachine().getId())).flatMap(upgrade -> upgrade.getModifiers().stream()).map(modifier -> Pair.of(modifier, pair.getSecond())))
                .collect(Collectors.toMap(Pair::getFirst, Pair::getSecond));

    }

    public static AxisAlignedBB rotateBox(AxisAlignedBB box, Direction to) {
        //Based on south, positive Z
        switch (to) {
            case EAST: //90° CCW
                return new AxisAlignedBB(box.minZ, box.minY, -box.minX, box.maxZ, box.maxY, -box.maxX);
            case NORTH: //180° CCW
                return new AxisAlignedBB(-box.minX, box.minY, -box.minZ, -box.maxX, box.maxY, -box.maxZ);
            case WEST: //270° CCW
                return new AxisAlignedBB(-box.minZ, box.minY, box.minX, -box.maxZ, box.maxY, box.maxX);
            case SOUTH: //No changes
            default:
                return new AxisAlignedBB(box.minX, box.minY, box.minZ, box.maxX, box.maxY, box.maxZ);
        }
    }

    public static boolean isResourceNameValid(String resourceLocation) {
        try {
            ResourceLocation location = new ResourceLocation(resourceLocation);
            return true;
        } catch (ResourceLocationException e) {
            return false;
        }
    }

    public static float getMachineBreakSpeed(MachineAppearance appearance, IBlockReader world, BlockPos pos, PlayerEntity player) {
        float digSpeed = getPlayerDigSpeed(appearance, player, pos);
        float hardness = appearance.getHardness();
        float canHarvest = canPlayerHarvestMachine(appearance, player, world, pos) ? 30 : 100;
        return digSpeed / hardness / canHarvest;
    }

    private static float getPlayerDigSpeed(MachineAppearance appearance, PlayerEntity player, BlockPos pos) {
        float f = 1.0F;

        ItemStack stack = player.getHeldItemMainhand();
        ToolType tool = appearance.getTool();
        if(!stack.isEmpty() && stack.getToolTypes().contains(tool) && stack.getHarvestLevel(tool, player, player.world.getBlockState(pos)) >= appearance.getMiningLevel() && stack.getItem() instanceof TieredItem)
            f = ((TieredItem)stack.getItem()).getTier().getEfficiency();

        if (f > 1.0F) {
            int i = EnchantmentHelper.getEfficiencyModifier(player);
            ItemStack itemstack = player.getHeldItemMainhand();
            if (i > 0 && !itemstack.isEmpty()) {
                f += (float)(i * i + 1);
            }
        }

        if (EffectUtils.hasMiningSpeedup(player)) {
            f *= 1.0F + (float)(EffectUtils.getMiningSpeedup(player) + 1) * 0.2F;
        }

        EffectInstance miningFatigue = player.getActivePotionEffect(Effects.MINING_FATIGUE);
        if (miningFatigue != null) {
            switch (miningFatigue.getAmplifier()) {
                case 0:
                    f *= 0.3F;
                    break;
                case 1:
                    f *= 0.09F;
                    break;
                case 2:
                    f *= 0.0027F;
                    break;
                case 3:
                default:
                    f *= 8.1E-4F;
            }
        }

        if (player.areEyesInFluid(FluidTags.WATER) && !EnchantmentHelper.hasAquaAffinity(player))
            f /= 5.0F;

        if (!player.isOnGround())
            f /= 5.0F;

        f = net.minecraftforge.event.ForgeEventFactory.getBreakSpeed(player, player.world.getBlockState(pos), f, pos);
        return f;
    }

    public static boolean canPlayerHarvestMachine(MachineAppearance appearance, PlayerEntity player, IBlockReader world, BlockPos pos) {
        BlockState state = world.getBlockState(pos);
        ToolType tool = appearance.getTool();

        if (tool.getName().equals("hand"))
            return ForgeEventFactory.doPlayerHarvestCheck(player, state, true);

        ItemStack stack = player.getHeldItemMainhand();
        if(stack.isEmpty())
            return ForgeEventFactory.doPlayerHarvestCheck(player, state, false);

        int toolLevel = stack.getHarvestLevel(tool, player, state);
        if (toolLevel < 0)
            return ForgeEventFactory.doPlayerHarvestCheck(player, state, false);

        return ForgeEventFactory.doPlayerHarvestCheck(player, state, toolLevel >= appearance.getMiningLevel());
    }

    public static ItemStack makeItemStack(Item item, int amount, @Nullable CompoundNBT nbt) {
        ItemStack stack = new ItemStack(item, amount);
        stack.setTag(nbt == null ? null : nbt.copy());
        return stack;
    }

    public static int getPlayerInventoryItemStackAmount(PlayerEntity player, IIngredient<Item> item, CompoundNBT nbt) {
        return Stream.concat(player.inventory.mainInventory.stream(), player.inventory.offHandInventory.stream())
                .filter(stack -> item.test(stack.getItem()) && testNBT(nbt, stack.getTag()))
                .mapToInt(ItemStack::getCount)
                .sum();
    }

    public static void moveStackFromPlayerInvToSlot(PlayerEntity player, SlotItemComponent slot, IIngredient<Item> item, int amount, CompoundNBT nbt) {
        AtomicInteger toMove = new AtomicInteger(amount);
        Stream.concat(player.inventory.mainInventory.stream(), player.inventory.offHandInventory.stream())
                .filter(stack -> item.test(stack.getItem()) && testNBT(nbt, stack.getTag()) && slot.isItemValid(stack))
                .forEach(stack -> {
                    int canMove = Math.min(stack.getCount(), toMove.get());
                    ItemStack toInsert = stack.copy();
                    toInsert.setCount(canMove);
                    slot.putStack(toInsert);
                    stack.shrink(canMove);
                    toMove.getAndAdd(-canMove);
                });
    }

    public static int toInt(long l) {
        try {
            return Math.toIntExact(l);
        } catch (ArithmeticException e) {
            return Integer.MAX_VALUE;
        }
    }
}
