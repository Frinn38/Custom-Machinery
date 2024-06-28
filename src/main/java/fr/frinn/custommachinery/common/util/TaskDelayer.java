package fr.frinn.custommachinery.common.util;

import com.mojang.datafixers.util.Pair;
import fr.frinn.custommachinery.CustomMachinery;
import net.minecraft.server.MinecraftServer;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.fml.common.EventBusSubscriber.Bus;
import net.neoforged.neoforge.event.tick.ServerTickEvent;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

@EventBusSubscriber(modid = CustomMachinery.MODID, bus = Bus.GAME)
public class TaskDelayer {

    private static final List<Pair<AtomicInteger, Runnable>> tasks = new ArrayList<>();

    @SubscribeEvent
    public static void serverTick(final ServerTickEvent.Post event) {
        Iterator<Pair<AtomicInteger, Runnable>> iterator = tasks.iterator();
        while(iterator.hasNext()) {
            Pair<AtomicInteger, Runnable> pair = iterator.next();
            if(pair.getFirst().addAndGet(-1) < 0) {
                pair.getSecond().run();
                iterator.remove();
            }
        }
    }

    public static void enqueue(int ticks, Runnable task) {
        tasks.add(Pair.of(new AtomicInteger(ticks), task));
    }
}
