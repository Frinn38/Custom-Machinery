package fr.frinn.custommachinery.common.util;

import com.mojang.datafixers.util.Pair;
import dev.architectury.event.events.common.TickEvent;
import net.minecraft.server.MinecraftServer;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

public class TaskDelayer {

    private static final List<Pair<AtomicInteger, Runnable>> tasks = new ArrayList<>();

    static  {
        TickEvent.SERVER_POST.register(TaskDelayer::serverTick);
    }

    private static void serverTick(MinecraftServer server) {
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
