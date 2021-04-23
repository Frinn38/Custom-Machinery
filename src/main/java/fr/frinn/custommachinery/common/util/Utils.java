package fr.frinn.custommachinery.common.util;

import net.minecraft.entity.player.PlayerEntity;

import java.util.Objects;

public class Utils {

    public static boolean canPlayerManageMachines(PlayerEntity player) {
        return player.hasPermissionLevel(Objects.requireNonNull(player.getServer()).getOpPermissionLevel());
    }
}
