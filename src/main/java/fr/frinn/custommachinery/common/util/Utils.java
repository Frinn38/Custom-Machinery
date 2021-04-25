package fr.frinn.custommachinery.common.util;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.tags.ITag;
import net.minecraft.tags.TagCollectionManager;
import net.minecraft.util.ResourceLocation;

import java.util.Objects;

public class Utils {

    public static boolean canPlayerManageMachines(PlayerEntity player) {
        return player.hasPermissionLevel(Objects.requireNonNull(player.getServer()).getOpPermissionLevel());
    }

    public static ResourceLocation getItemTagID(ITag<Item> tag) {
        return TagCollectionManager.getManager().getItemTags().getDirectIdFromTag(tag);
    }
}
