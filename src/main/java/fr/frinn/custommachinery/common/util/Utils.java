package fr.frinn.custommachinery.common.util;

import fr.frinn.custommachinery.CustomMachinery;
import fr.frinn.custommachinery.common.init.CustomMachineBlock;
import fr.frinn.custommachinery.common.machine.CustomMachine;
import fr.frinn.custommachinery.common.machine.MachineAppearance;
import net.minecraft.ChatFormatting;
import net.minecraft.ResourceLocationException;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.Tag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.material.Fluids;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.common.crafting.SizedIngredient;
import net.neoforged.neoforge.event.EventHooks;
import net.neoforged.neoforge.fluids.crafting.SizedFluidIngredient;
import org.jetbrains.annotations.Nullable;

import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.Iterator;
import java.util.List;
import java.util.Objects;
import java.util.Random;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Utils {

    public static final Random RAND = new Random();
    private static final NumberFormat NUMBER_FORMAT = new DecimalFormat("#,###");

    public static boolean canPlayerManageMachines(Player player) {
        return player.hasPermissions(Objects.requireNonNull(player.getServer()).getOperatorUserPermissionLevel());
    }

    public static Vec3 vec3dFromBlockPos(BlockPos pos) {
        return new Vec3(pos.getX(), pos.getY(), pos.getZ());
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

    public static MutableComponent blockIngredientName(List<BlockIngredient> ingredients) {
        MutableComponent builder = Component.empty();
        for(Iterator<BlockIngredient> iterator = ingredients.iterator(); iterator.hasNext(); ) {
            BlockIngredient blockIngredient = iterator.next();
            PartialBlockState partialBlockState = blockIngredient.state();
            if(partialBlockState != null) {
                if (partialBlockState.getBlockState().getBlock() instanceof CustomMachineBlock && partialBlockState.getNbt() != null && partialBlockState.getNbt().contains("machineID", Tag.TAG_STRING)) {
                    ResourceLocation machineID = ResourceLocation.tryParse(partialBlockState.getNbt().getString("machineID"));
                    if (machineID != null) {
                        CustomMachine machine = CustomMachinery.MACHINES.get(machineID);
                        if (machine != null)
                            builder.append(machine.getName());
                    }
                } else {
                    if(blockIngredient.not())
                        builder.append(Component.translatable("custommachinery.requirements.structure.not", partialBlockState.getName()));
                    else
                        builder.append(partialBlockState.getName());
                }
            }
            else builder.append(Component.literal(ingredients.getFirst().toString()));

            if(iterator.hasNext())
                builder.append(Component.translatable("custommachinery.requirements.structure.or").withStyle(ChatFormatting.GRAY));
        }
        return builder;
    }

    public static long clamp(long value, long min, long max) {
        return value < min ? min : Math.min(value, max);
    }

    public static Component itemIngredientName(SizedIngredient ingredient) {
        if(ingredient.getItems().length == 0)
            return Items.AIR.getDescription();
        else
            return ingredient.getItems()[0].getHoverName();
    }

    public static Component fluidIngredientName(SizedFluidIngredient ingredient) {
        if(ingredient.getFluids().length == 0)
            return Fluids.EMPTY.getFluidType().getDescription();
        else
            return ingredient.getFluids()[0].getHoverName();
    }

    public static String incrementLastNumber(String input) {
        if(input == null || input.isEmpty())
            return "";

        Pattern p = Pattern.compile("(\\d+)(?!.*\\d)");
        Matcher m = p.matcher(input);

        if(m.find()) {
            String numberStr = m.group(1);
            int width = numberStr.length();
            int number = Integer.parseInt(numberStr);
            number++;

            // format with zero-padding to preserve width
            String newNumber = String.format("%0" + width + "d", number);

            return input.substring(0, m.start()) + newNumber + input.substring(m.end());
        } else
            return input + "1";
    }
}
