package fr.frinn.custommachinery.common.util;

import dev.architectury.event.events.common.TickEvent;
import fr.frinn.custommachinery.api.machine.MachineTile;
import net.minecraft.server.MinecraftServer;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

public class MachineList {

    private static final List<WeakReference<MachineTile>> LOADED_MACHINES = Collections.synchronizedList(new ArrayList<>());

    private static boolean needRefresh = false;

    static {
        TickEvent.ServerLevelTick.SERVER_POST.register(MachineList::serverTick);
    }

    public static synchronized void addMachine(MachineTile tile) {
        if(tile.getLevel() != null && !tile.getLevel().isClientSide())
            LOADED_MACHINES.add(new WeakReference<>(tile));
    }

    public static synchronized void refreshAllMachines() {
        Iterator<WeakReference<MachineTile>> iterator = LOADED_MACHINES.iterator();
        while (iterator.hasNext()) {
            MachineTile tile = iterator.next().get();
            if(tile != null)
                tile.refreshMachine(null);
            else
                iterator.remove();
        }
    }

    public static void setNeedRefresh() {
        needRefresh = true;
    }

    private static void serverTick(final MinecraftServer server) {
        if(needRefresh) {
            needRefresh = false;
            refreshAllMachines();
        }
    }
}
