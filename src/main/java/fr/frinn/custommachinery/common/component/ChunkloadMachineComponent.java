package fr.frinn.custommachinery.common.component;

import fr.frinn.custommachinery.api.codec.NamedCodec;
import fr.frinn.custommachinery.api.component.ComponentIOMode;
import fr.frinn.custommachinery.api.component.IMachineComponentManager;
import fr.frinn.custommachinery.api.component.IMachineComponentTemplate;
import fr.frinn.custommachinery.api.component.ISerializableComponent;
import fr.frinn.custommachinery.api.component.ITickableComponent;
import fr.frinn.custommachinery.api.component.MachineComponentType;
import fr.frinn.custommachinery.common.init.Registration;
import fr.frinn.custommachinery.common.util.MachineList;
import fr.frinn.custommachinery.common.util.TaskDelayer;
import fr.frinn.custommachinery.impl.component.AbstractMachineComponent;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.Vec3i;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.TicketType;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.chunk.LevelChunk;
import net.minecraft.world.level.chunk.status.ChunkStatus;

import java.util.function.Supplier;

public class ChunkloadMachineComponent extends AbstractMachineComponent implements ISerializableComponent, ITickableComponent {

    private boolean active;
    private final Supplier<Integer> defaultRadius; //Radius set by the component without recipe running.
    private int currentRadius; //Currently applied radius, either by the component or by a recipe running.
    private int tempo = -1;

    public ChunkloadMachineComponent(IMachineComponentManager manager, boolean active, int radius) {
        super(manager, ComponentIOMode.NONE);
        this.active = active;
        this.defaultRadius = this.upgradeableI(radius, "radius", 1, 32, this::setActive);
    }

    public ChunkloadMachineComponent(IMachineComponentManager manager) {
        this(manager, false, 1);
    }

    @Override
    public MachineComponentType<ChunkloadMachineComponent> getType() {
        return Registration.CHUNKLOAD_MACHINE_COMPONENT.get();
    }

    @Override
    public void onRemoved() {
        if(getManager().getLevel() instanceof ServerLevel level && !getManager().getTile().isUnloaded())
            this.setInactive(level);
    }

    @Override
    public void init() {
        if(this.active && getManager().getLevel() instanceof ServerLevel level) {
            ChunkPos pos = new ChunkPos(getManager().getTile().getBlockPos());
            int radius = Math.max(this.currentRadius, this.defaultRadius.get());
            if(level.getChunk(pos.x, pos.z, ChunkStatus.EMPTY, false) instanceof LevelChunk)
                this.setActive(radius);
            else
                TaskDelayer.enqueue(1, () -> this.setActive(radius));
        }
    }

    @Override
    public void serverTick() {
        if(this.tempo >= 0 && this.tempo-- == 0)
            this.setInactive((ServerLevel) getManager().getLevel());
    }

    @Override
    public void serialize(CompoundTag nbt, HolderLookup.Provider registries) {
        nbt.putBoolean("active", this.active);
        nbt.putInt("radius", this.currentRadius);
    }

    @Override
    public void deserialize(CompoundTag nbt, HolderLookup.Provider registries) {
        if(nbt.contains("active", CompoundTag.TAG_BYTE))
            this.active = nbt.getBoolean("active");
        if(nbt.contains("radius", CompoundTag.TAG_INT))
            this.currentRadius = nbt.getInt("radius");
    }

    /** ChunkLoader stuff **/

    private static final TicketType<BlockPos> MACHINE_CHUNKLOADER = TicketType.create("custom_machine", Vec3i::compareTo, 0);

    public void setActive(int radius) {
        if(getManager().getLevel() instanceof ServerLevel level) {
            if(this.active)
                this.setInactive(level);

            this.active = true;
            this.currentRadius = radius;

            BlockPos machinePos = getManager().getTile().getBlockPos();
            ChunkPos chunk = new ChunkPos(machinePos);
            level.setChunkForced(chunk.x, chunk.z, true);
            level.getChunkSource().addRegionTicket(MACHINE_CHUNKLOADER, chunk, radius + 1, machinePos);
        }
    }

    public void setActiveWithTempo(int radius, int tempo) {
        this.tempo = Math.max(this.tempo, tempo);
        if(!this.active || this.currentRadius < radius)
            this.setActive(radius);
    }

    public void setInactive(ServerLevel level) {
        this.active = false;

        BlockPos machinePos = getManager().getTile().getBlockPos();
        ChunkPos chunk = new ChunkPos(machinePos);
        if(MachineList.findInSameChunk(getManager().getTile()).isEmpty())
            level.setChunkForced(chunk.x, chunk.z, false);
        level.getChunkSource().removeRegionTicket(MACHINE_CHUNKLOADER, chunk, this.currentRadius + 1, machinePos);
    }

    public boolean isActive() {
        return this.active;
    }

    public int getRadius() {
        return this.currentRadius;
    }

    public record Template(
            int radius
    ) implements IMachineComponentTemplate<ChunkloadMachineComponent> {

        public static final NamedCodec<Template> CODEC = NamedCodec.record(templateInstance ->
                templateInstance.group(
                        NamedCodec.intRange(1, 32).optionalFieldOf("radius", 1).forGetter(template -> template.radius)
                ).apply(templateInstance, Template::new), "Chunkload machine component template"
        );

        @Override
        public MachineComponentType<ChunkloadMachineComponent> getType() {
            return Registration.CHUNKLOAD_MACHINE_COMPONENT.get();
        }

        @Override
        public String getId() {
            return "chunkload";
        }

        @Override
        public boolean canAccept(Object ingredient, boolean isInput, IMachineComponentManager manager) {
            return false;
        }

        @Override
        public ChunkloadMachineComponent build(IMachineComponentManager manager) {
            return new ChunkloadMachineComponent(manager, true, this.radius);
        }
    }
}
