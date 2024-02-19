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
import net.minecraft.core.Vec3i;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.TicketType;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.chunk.ChunkStatus;
import net.minecraft.world.level.chunk.LevelChunk;

public class ChunkloadMachineComponent extends AbstractMachineComponent implements ISerializableComponent, ITickableComponent {

    private boolean active;
    private int radius;
    private int tempo = -1;

    public ChunkloadMachineComponent(IMachineComponentManager manager, boolean active, int radius) {
        super(manager, ComponentIOMode.NONE);
        this.active = active;
        this.radius = radius;
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
            if(level.getChunk(pos.x, pos.z, ChunkStatus.EMPTY, false) instanceof LevelChunk)
                this.setActive(level, this.radius);
            else
                TaskDelayer.enqueue(1, () -> this.setActive(level, this.radius));
        }
    }

    @Override
    public void serverTick() {
        if(this.tempo >= 0 && this.tempo-- == 0)
            this.setInactive((ServerLevel) getManager().getLevel());
    }

    @Override
    public void serialize(CompoundTag nbt) {
        nbt.putBoolean("active", this.active);
        nbt.putInt("radius", this.radius);
    }

    @Override
    public void deserialize(CompoundTag nbt) {
        if(nbt.contains("active", CompoundTag.TAG_BYTE))
            this.active = nbt.getBoolean("active");
        if(nbt.contains("radius", CompoundTag.TAG_INT))
            this.radius = nbt.getInt("radius");
    }

    /** ChunkLoader stuff **/

    private static final TicketType<BlockPos> MACHINE_CHUNKLOADER = TicketType.create("custom_machine", Vec3i::compareTo, 0);

    public void setActive(ServerLevel level, int radius) {
        if(this.active)
            this.setInactive(level);

        this.active = true;
        this.radius = radius;

        BlockPos machinePos = getManager().getTile().getBlockPos();
        ChunkPos chunk = new ChunkPos(machinePos);
        level.setChunkForced(chunk.x, chunk.z, true);
        level.getChunkSource().addRegionTicket(MACHINE_CHUNKLOADER, chunk, radius + 1, machinePos);
    }

    public void setActiveWithTempo(ServerLevel level, int radius, int tempo) {
        this.tempo = Math.max(this.tempo, tempo);
        if(!this.active || this.radius < radius)
            this.setActive(level, radius);
    }

    public void setInactive(ServerLevel level) {
        this.active = false;

        BlockPos machinePos = getManager().getTile().getBlockPos();
        ChunkPos chunk = new ChunkPos(machinePos);
        if(MachineList.findInSameChunk(getManager().getTile()).isEmpty())
            level.setChunkForced(chunk.x, chunk.z, false);
        level.getChunkSource().removeRegionTicket(MACHINE_CHUNKLOADER, chunk, this.radius + 1, machinePos);
    }

    public boolean isActive() {
        return this.active;
    }

    public int getRadius() {
        return this.radius;
    }

    public static class Template implements IMachineComponentTemplate<ChunkloadMachineComponent> {

        public static final NamedCodec<Template> CODEC = NamedCodec.record(templateInstance ->
                templateInstance.group(
                        NamedCodec.intRange(1, 32).optionalFieldOf("radius", 1).forGetter(template -> template.radius)
                ).apply(templateInstance, Template::new), "Chunkload machine component template"
        );

        private final int radius;

        public Template(int radius) {
            this.radius = radius;
        }

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
