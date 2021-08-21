package fr.frinn.custommachinery.common.data;

import com.mojang.datafixers.util.Either;
import com.mojang.serialization.Codec;
import fr.frinn.custommachinery.api.machine.MachineStatus;
import fr.frinn.custommachinery.common.util.Codecs;
import net.minecraft.util.IStringSerializable;

import java.util.Arrays;
import java.util.function.Function;
import java.util.stream.Collectors;

public class MachineAppearanceManager {

    public static final Codec<MachineAppearanceManager> CODEC = Codec.either(Codec.simpleMap(Codecs.STATUS_CODEC, MachineAppearance.CODEC, IStringSerializable.createKeyable(MachineStatus.values())).codec(), MachineAppearance.CODEC)
            .xmap(either -> either.map(map -> {
                MachineAppearance idle = map.getOrDefault(MachineStatus.IDLE, MachineAppearance.DEFAULT);
                MachineAppearance running = map.getOrDefault(MachineStatus.RUNNING, idle);
                MachineAppearance errored = map.getOrDefault(MachineStatus.ERRORED, idle);
                MachineAppearance paused = map.getOrDefault(MachineStatus.PAUSED, idle);
                return new MachineAppearanceManager(idle, running, errored, paused);
            }, appearance -> new MachineAppearanceManager(appearance, appearance, appearance, appearance)
            ), manager -> Either.left(Arrays.stream(MachineStatus.values()).collect(Collectors.toMap(Function.identity(), manager::getAppearance))))
            .stable();

    private MachineAppearance idle;
    private MachineAppearance running;
    private MachineAppearance errored;
    private MachineAppearance paused;

    public MachineAppearanceManager(MachineAppearance idle, MachineAppearance running, MachineAppearance errored, MachineAppearance paused) {
        this.idle = idle;
        this.running = running;
        this.errored = errored;
        this.paused = paused;
    }

    public MachineAppearance getAppearance(MachineStatus status) {
        switch (status) {
            case IDLE:
                return this.idle;
            case RUNNING:
                return this.running;
            case ERRORED:
                return this.errored;
            case PAUSED:
                return this.paused;
        }
        throw new IllegalArgumentException("Invalid machine status: " + status);
    }
}
