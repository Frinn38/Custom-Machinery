package fr.frinn.custommachinery.common.util;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.fluid.Fluid;
import net.minecraft.item.Item;
import net.minecraft.nbt.*;
import net.minecraft.tags.ITag;
import net.minecraft.tags.TagCollectionManager;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraftforge.common.util.Constants;

import javax.annotation.Nullable;
import java.util.Objects;

public class Utils {

    public static boolean canPlayerManageMachines(PlayerEntity player) {
        return player.hasPermissionLevel(Objects.requireNonNull(player.getServer()).getOpPermissionLevel());
    }

    public static ResourceLocation getItemTagID(ITag<Item> tag) {
        return TagCollectionManager.getManager().getItemTags().getValidatedIdFromTag(tag);
    }

    public static ResourceLocation getFluidTagID(ITag<Fluid> tag) {
        return TagCollectionManager.getManager().getFluidTags().getValidatedIdFromTag(tag);
    }

    public static Vector3d vec3dFromBlockPos(BlockPos pos) {
        return new Vector3d(pos.getX(), pos.getY(), pos.getZ());
    }

    public static boolean testNBT(CompoundNBT nbt, CompoundNBT tested) {
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
}
