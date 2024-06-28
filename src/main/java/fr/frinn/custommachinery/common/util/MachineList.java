package fr.frinn.custommachinery.common.util;

import fr.frinn.custommachinery.CustomMachinery;
import fr.frinn.custommachinery.api.machine.MachineTile;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.ChunkPos;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.fml.common.EventBusSubscriber.Bus;
import net.neoforged.neoforge.event.tick.ServerTickEvent;
import org.jetbrains.annotations.Nullable;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;
import java.util.Optional;

@EventBusSubscriber(modid = CustomMachinery.MODID, bus = Bus.GAME)
public class MachineList {

    private static final List<WeakReference<MachineTile>> LOADED_MACHINES = Collections.synchronizedList(new ArrayList<>());

    private static boolean needRefresh = false;

    public static void addMachine(MachineTile tile) {
        if(tile.getLevel() != null && !tile.getLevel().isClientSide())
            LOADED_MACHINES.add(new WeakReference<>(tile));
    }

    public static void refreshAllMachines() {
        getLoadedMachines().forEach(tile -> tile.refreshMachine(null));
    }

    public static void setNeedRefresh() {
        needRefresh = true;
    }

    public static Optional<MachineTile> findNearest(Player player, @Nullable ResourceLocation machine, int radius) {
        return getLoadedMachines().stream()
                .filter(tile -> tile.getLevel() == player.level() && tile.getBlockPos().closerThan(player.blockPosition(), radius) && (machine == null || machine.equals(tile.getMachine().getId())))
                .min(Comparator.comparingInt(tile -> tile.getBlockPos().distManhattan(player.blockPosition())));
    }

    public static Optional<MachineTile> findInSameChunk(MachineTile machine) {
        return getLoadedMachines().stream()
                .filter(tile -> tile != machine && tile.getLevel() == machine.getLevel() && new ChunkPos(tile.getBlockPos()).equals(new ChunkPos(machine.getBlockPos())))
                .findFirst();
    }

    public static List<MachineTile> getLoadedMachines() {
        Iterator<WeakReference<MachineTile>> iterator = LOADED_MACHINES.iterator();
        List<MachineTile> loadedMachines = new ArrayList<>();
        while(iterator.hasNext()) {
            MachineTile tile = iterator.next().get();
            if(tile == null || tile.isRemoved())
                iterator.remove();
            else
                loadedMachines.add(tile);
        }
        return loadedMachines;
    }

    @SubscribeEvent
    public static void serverTick(final ServerTickEvent.Post event) {
        if(needRefresh && event.hasTime()) {
            needRefresh = false;
            refreshAllMachines();
        }
    }
}
