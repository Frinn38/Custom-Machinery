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
import net.minecraft.ResourceLocationException;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.ByteArrayTag;
import net.minecraft.nbt.ByteTag;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.DoubleTag;
import net.minecraft.nbt.FloatTag;
import net.minecraft.nbt.IntArrayTag;
import net.minecraft.nbt.IntTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.LongArrayTag;
import net.minecraft.nbt.LongTag;
import net.minecraft.nbt.NumericTag;
import net.minecraft.nbt.ShortTag;
import net.minecraft.nbt.Tag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.FluidTags;
import net.minecraft.tags.TagKey;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffectUtil;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.DiggerItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TieredItem;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.common.ForgeHooks;
import net.minecraftforge.common.TierSortingRegistry;
import net.minecraftforge.event.ForgeEventFactory;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Map;
import java.util.Objects;
import java.util.Random;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class Utils {

    public static final Random RAND = new Random();

    public static boolean canPlayerManageMachines(Player player) {
        return player.hasPermissions(Objects.requireNonNull(player.getServer()).getOperatorUserPermissionLevel());
    }

    public static Vec3 vec3dFromBlockPos(BlockPos pos) {
        return new Vec3(pos.getX(), pos.getY(), pos.getZ());
    }

    public static boolean testNBT(CompoundTag nbt, @Nullable CompoundTag tested) {
        if(tested == null)
            return false;
        for (String key : tested.getAllKeys()) {
            if(!nbt.contains(key) || nbt.getTagType(key) != tested.getTagType(key) || !testINBT(nbt.get(key), tested.get(key)))
                return false;
        }
        return true;
    }

    public static <T extends Tag> boolean testINBT(@Nullable T inbt, @Nullable T tested) {
        if(inbt == null || tested == null)
            return false;
        return switch (inbt.getId()) {
            case Tag.TAG_BYTE -> ((ByteTag) inbt).getAsByte() == ((ByteTag) tested).getAsByte();
            case Tag.TAG_SHORT -> ((ShortTag) inbt).getAsShort() == ((ShortTag) tested).getAsShort();
            case Tag.TAG_INT -> ((IntTag) inbt).getAsInt() == ((IntTag) tested).getAsInt();
            case Tag.TAG_LONG -> ((LongTag) inbt).getAsLong() == ((LongTag) tested).getAsLong();
            case Tag.TAG_FLOAT -> ((FloatTag) inbt).getAsFloat() == ((FloatTag) tested).getAsFloat();
            case Tag.TAG_DOUBLE -> ((DoubleTag) inbt).getAsDouble() == ((DoubleTag) tested).getAsDouble();
            case Tag.TAG_BYTE_ARRAY -> ((ByteArrayTag) inbt).containsAll((ByteArrayTag) tested);
            case Tag.TAG_STRING -> inbt.getAsString().equals(tested.getAsString());
            case Tag.TAG_LIST -> ((ListTag) inbt).containsAll((ListTag) tested);
            case Tag.TAG_COMPOUND -> testNBT((CompoundTag) inbt, (CompoundTag) tested);
            case Tag.TAG_INT_ARRAY -> ((IntArrayTag) inbt).containsAll((IntArrayTag) tested);
            case Tag.TAG_LONG_ARRAY -> ((LongArrayTag) inbt).containsAll((LongArrayTag) tested);
            case Tag.TAG_ANY_NUMERIC -> ((NumericTag) inbt).getAsNumber().equals(((NumericTag) tested).getAsNumber());
            default -> false;
        };
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

    public static AABB rotateBox(AABB box, Direction to) {
        //Based on south, positive Z
        return switch (to) {
            case EAST -> //90° CCW
                    new AABB(box.minZ, box.minY, -box.minX, box.maxZ, box.maxY, -box.maxX);
            case NORTH -> //180° CCW
                    new AABB(-box.minX, box.minY, -box.minZ, -box.maxX, box.maxY, -box.maxZ);
            case WEST -> //270° CCW
                    new AABB(-box.minZ, box.minY, box.minX, -box.maxZ, box.maxY, box.maxX); //No changes
            default -> new AABB(box.minX, box.minY, box.minZ, box.maxX, box.maxY, box.maxZ);
        };
    }

    public static boolean isResourceNameValid(String resourceLocation) {
        try {
            ResourceLocation location = new ResourceLocation(resourceLocation);
            return true;
        } catch (ResourceLocationException e) {
            return false;
        }
    }

    public static float getMachineBreakSpeed(MachineAppearance appearance, BlockGetter world, BlockPos pos, Player player) {
        float digSpeed = getPlayerDigSpeed(appearance, player, pos);
        float hardness = appearance.getHardness();
        float canHarvest = canPlayerHarvestMachine(appearance, player, world, pos) ? 30 : 100;
        return digSpeed / hardness / canHarvest;
    }

    private static float getPlayerDigSpeed(MachineAppearance appearance, Player player, BlockPos pos) {
        float f = 1.0F;

        ItemStack stack = player.getMainHandItem();
        TagKey<Block> tool = appearance.getTool();
        TagKey<Block> level = appearance.getMiningLevel();
        if(!stack.isEmpty() && stack.getItem() instanceof DiggerItem diggerItem)
            if(diggerItem.blocks == tool && (diggerItem.getTier().getTag() == level || TierSortingRegistry.getTiersLowerThan(diggerItem.getTier()).stream().anyMatch(tier -> tier.getTag() == level)))
                f = diggerItem.getTier().getSpeed();

        if (f > 1.0F) {
            int i = EnchantmentHelper.getBlockEfficiency(player);
            ItemStack itemstack = player.getMainHandItem();
            if (i > 0 && !itemstack.isEmpty()) {
                f += (float)(i * i + 1);
            }
        }

        if (MobEffectUtil.hasDigSpeed(player)) {
            f *= 1.0F + (float)(MobEffectUtil.getDigSpeedAmplification(player) + 1) * 0.2F;
        }

        MobEffectInstance miningFatigue = player.getEffect(MobEffects.DIG_SLOWDOWN);
        if (miningFatigue != null) {
            switch (miningFatigue.getAmplifier()) {
                case 0 -> f *= 0.3F;
                case 1 -> f *= 0.09F;
                case 2 -> f *= 0.0027F;
                default -> f *= 8.1E-4F;
            }
        }

        if (player.isEyeInFluid(FluidTags.WATER) && !EnchantmentHelper.hasAquaAffinity(player))
            f /= 5.0F;

        if (!player.isOnGround())
            f /= 5.0F;

        f = net.minecraftforge.event.ForgeEventFactory.getBreakSpeed(player, player.level.getBlockState(pos), f, pos);
        return f;
    }

    public static boolean canPlayerHarvestMachine(MachineAppearance appearance, Player player, BlockGetter world, BlockPos pos) {
        BlockState state = world.getBlockState(pos);
        TagKey<Block> tool = appearance.getTool();
        TagKey<Block> level = appearance.getMiningLevel();

        if (tool.location().getPath().equals("hand"))
            return ForgeEventFactory.doPlayerHarvestCheck(player, state, true);

        ItemStack stack = player.getMainHandItem();
        if(stack.isEmpty())
            return ForgeEventFactory.doPlayerHarvestCheck(player, state, false);

        if(stack.getItem() instanceof TieredItem tieredItem) {
            boolean isToolLevelHighEnough = tieredItem.getTier().getTag() == level || TierSortingRegistry.getTiersLowerThan(tieredItem.getTier()).stream().anyMatch(tier -> tier.getTag() == level);
            if(isToolLevelHighEnough && tieredItem instanceof DiggerItem diggerItem)
                return ForgeEventFactory.doPlayerHarvestCheck(player, state, diggerItem.blocks == tool);
            return ForgeEventFactory.doPlayerHarvestCheck(player, state, isToolLevelHighEnough);
        }
        return ForgeEventFactory.doPlayerHarvestCheck(player, state, false);
    }

    public static ItemStack makeItemStack(Item item, int amount, @Nullable CompoundTag nbt) {
        ItemStack stack = new ItemStack(item, amount);
        stack.setTag(nbt == null ? null : nbt.copy());
        return stack;
    }

    public static int getPlayerInventoryItemStackAmount(Player player, IIngredient<Item> item, CompoundTag nbt) {
        return Stream.concat(player.getInventory().items.stream(), player.getInventory().offhand.stream())
                .filter(stack -> item.test(stack.getItem()) && testNBT(nbt, stack.getTag()))
                .mapToInt(ItemStack::getCount)
                .sum();
    }

    public static void moveStackFromPlayerInvToSlot(Player player, SlotItemComponent slot, IIngredient<Item> item, int amount, CompoundTag nbt) {
        AtomicInteger toMove = new AtomicInteger(amount);
        Stream.concat(player.getInventory().items.stream(), player.getInventory().offhand.stream())
                .filter(stack -> item.test(stack.getItem()) && testNBT(nbt, stack.getTag()) && slot.mayPlace(stack))
                .forEach(stack -> {
                    int canMove = Math.min(stack.getCount(), toMove.get());
                    ItemStack toInsert = stack.copy();
                    toInsert.setCount(canMove);
                    slot.set(toInsert);
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

    @SuppressWarnings("unchecked")
    @Nullable
    public static <E extends BlockEntity, A extends BlockEntity> BlockEntityTicker<A> createTickerHelper(BlockEntityType<A> p_152133_, BlockEntityType<E> p_152134_, BlockEntityTicker<? super E> p_152135_) {
        return p_152134_ == p_152133_ ? (BlockEntityTicker<A>)p_152135_ : null;
    }

    public static boolean hasBurnTime(ItemStack stack) {
        return ForgeHooks.getBurnTime(stack, RecipeType.SMELTING) > 0;
    }
}
