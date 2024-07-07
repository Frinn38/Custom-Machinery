package fr.frinn.custommachinery.common.util;

import fr.frinn.custommachinery.CustomMachinery;
import fr.frinn.custommachinery.common.init.CustomMachineBlock;
import fr.frinn.custommachinery.common.machine.CustomMachine;
import fr.frinn.custommachinery.common.machine.MachineAppearance;
import fr.frinn.custommachinery.common.util.ingredient.IIngredient;
import net.minecraft.ResourceLocationException;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.component.DataComponentMap;
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
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.event.EventHooks;
import org.jetbrains.annotations.Nullable;

import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.Arrays;
import java.util.Objects;
import java.util.Random;

public class Utils {

    public static final Random RAND = new Random();
    private static final NumberFormat NUMBER_FORMAT = new DecimalFormat("#,###");

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
            ResourceLocation location = ResourceLocation.parse(resourceLocation);
            return true;
        } catch (ResourceLocationException e) {
            return false;
        }
    }

    public static float getMachineBreakSpeed(MachineAppearance appearance, BlockGetter level, BlockPos pos, Player player) {
        float hardness = appearance.getHardness();
        if(hardness <= 0)
            return 0.0F;
        float digSpeed = player.getDigSpeed(MachineBlockState.CACHE.getUnchecked(appearance), pos);
        float canHarvest = EventHooks.doPlayerHarvestCheck(player, MachineBlockState.CACHE.getUnchecked(appearance), level, pos) ? 30 : 100;
        return digSpeed / hardness / canHarvest;
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

    public static String format(int number) {
        return NUMBER_FORMAT.format(number);
    }

    public static String format(long number) {
        return NUMBER_FORMAT.format(number);
    }

    public static String format(double number) {
        return NUMBER_FORMAT.format(number);
    }

    public static MutableComponent getBlockName(IIngredient<PartialBlockState> ingredient) {
        if(ingredient.getAll().size() == 1) {
            PartialBlockState partialBlockState = ingredient.getAll().get(0);
            if(partialBlockState.getBlockState().getBlock() instanceof CustomMachineBlock && partialBlockState.getNbt() != null && partialBlockState.getNbt().contains("machineID", Tag.TAG_STRING)) {
                ResourceLocation machineID = ResourceLocation.tryParse(partialBlockState.getNbt().getString("machineID"));
                if(machineID != null) {
                    CustomMachine machine = CustomMachinery.MACHINES.get(machineID);
                    if(machine != null)
                        return (MutableComponent)machine.getName();
                }
            }
            return partialBlockState.getName();
        }
        else return Component.literal(ingredient.toString());
    }

    public static long clamp(long value, long min, long max) {
        return value < min ? min : Math.min(value, max);
    }
}
