package fr.frinn.custommachinery.common.util;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.fluid.Fluid;
import net.minecraft.item.Item;
import net.minecraft.tags.ITag;
import net.minecraft.tags.TagCollectionManager;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.vector.Vector3d;

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
}
