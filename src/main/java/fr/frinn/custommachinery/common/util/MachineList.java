package fr.frinn.custommachinery.common.util;

import fr.frinn.custommachinery.CustomMachinery;
import fr.frinn.custommachinery.api.machine.MachineTile;
import net.minecraftforge.event.OnDatapackSyncEvent;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.TickEvent.Phase;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.LogicalSide;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber.Bus;
import net.minecraftforge.server.ServerLifecycleHooks;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

@EventBusSubscriber(modid = CustomMachinery.MODID, bus = Bus.FORGE)
public class MachineList {

    private static final List<WeakReference<MachineTile>> LOADED_MACHINES = new ArrayList<>();

    private static boolean needRefresh = false;

    public static void addMachine(MachineTile tile) {
        if(tile.getLevel() != null && !tile.getLevel().isClientSide())
            LOADED_MACHINES.add(new WeakReference<>(tile));
    }

    public static void refreshAllMachines() {
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

    @SubscribeEvent
    public static void serverTick(final TickEvent.ServerTickEvent event) {
        if(needRefresh && event.side == LogicalSide.SERVER && event.phase == Phase.END && event.haveTime() && ServerLifecycleHooks.getCurrentServer() != null) {
            needRefresh = false;
            refreshAllMachines();
        }
    }

    @SubscribeEvent
    public static void syncDatapack(final OnDatapackSyncEvent event) {
        if(event.getPlayer() == null)
            setNeedRefresh();
    }
}
