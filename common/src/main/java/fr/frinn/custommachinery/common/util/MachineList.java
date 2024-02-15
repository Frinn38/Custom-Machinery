package fr.frinn.custommachinery.common.util;

import dev.architectury.event.events.common.TickEvent;
import fr.frinn.custommachinery.api.machine.MachineTile;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.entity.player.Player;
import org.jetbrains.annotations.Nullable;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

public class MachineList {

    private static final List<WeakReference<MachineTile>> LOADED_MACHINES = Collections.synchronizedList(new ArrayList<>());

    private static boolean needRefresh = false;

    static {
        TickEvent.ServerLevelTick.SERVER_POST.register(MachineList::serverTick);
    }

    public static void addMachine(MachineTile tile) {
        if(tile.getLevel() != null && !tile.getLevel().isClientSide())
            LOADED_MACHINES.add(new WeakReference<>(tile));
    }

    public static void refreshAllMachines() {
        final List<WeakReference<MachineTile>> copy = List.copyOf(LOADED_MACHINES);
        copy.forEach(weak -> {
            MachineTile tile = weak.get();
            if(tile != null)
                tile.refreshMachine(null);
            else
                LOADED_MACHINES.remove(weak);
        });
    }

    public static void setNeedRefresh() {
        needRefresh = true;
    }

    public static Optional<MachineTile> findNearest(Player player, @Nullable ResourceLocation machine, int radius) {
        return LOADED_MACHINES.stream()
                .filter(ref -> ref.get() != null)
                .map(ref -> Objects.requireNonNull(ref.get()))
                .filter(tile -> tile.getLevel() == player.level && tile.getBlockPos().closerThan(player.blockPosition(), radius) && (machine == null || machine.equals(tile.getMachine().getId())))
                .min(Comparator.comparingInt(tile -> tile.getBlockPos().distManhattan(player.blockPosition())));
    }

    private static void serverTick(final MinecraftServer server) {
        if(needRefresh) {
            needRefresh = false;
            refreshAllMachines();
        }
    }
}
