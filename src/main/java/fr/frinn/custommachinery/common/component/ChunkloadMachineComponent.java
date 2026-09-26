package fr.frinn.custommachinery.common.component;

import fr.frinn.custommachinery.api.codec.NamedCodec;
import fr.frinn.custommachinery.api.component.ComponentIOMode;
import fr.frinn.custommachinery.api.component.IMachineComponentManager;
import fr.frinn.custommachinery.api.component.IMachineComponentTemplate;
import fr.frinn.custommachinery.api.component.ISerializableComponent;
import fr.frinn.custommachinery.api.component.ITickableComponent;
import fr.frinn.custommachinery.api.component.MachineComponentType;
import fr.frinn.custommachinery.common.init.CMRegistration;
import fr.frinn.custommachinery.common.util.MachineList;
import fr.frinn.custommachinery.common.util.TaskDelayer;
import fr.frinn.custommachinery.impl.component.AbstractMachineComponent;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.chunk.LevelChunk;
import net.minecraft.world.level.chunk.status.ChunkStatus;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;

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
        return CMRegistration.CHUNKLOAD_MACHINE_COMPONENT.get();
    }

    @Override
    public void onRemoved() {
        if(getManager().getLevel() instanceof ServerLevel level && !getManager().getTile().isUnloaded())
            this.setInactive(level);
    }

    @Override
    public void init() {
        if(this.active && getManager().getLevel() instanceof ServerLevel level) {
            ChunkPos pos = ChunkPos.containing(getManager().getTile().getBlockPos());
            int radius = Math.max(this.currentRadius, this.defaultRadius.get());
            if(level.getChunk(pos.x(), pos.z(), ChunkStatus.EMPTY, false) instanceof LevelChunk)
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
    public void serialize(ValueOutput output) {
        output.putBoolean("active", this.active);
        output.putInt("radius", this.currentRadius);
    }

    @Override
    public void deserialize(ValueInput input) {
        this.active = input.getBooleanOr("active", this.active);
        this.currentRadius = input.getIntOr("radius", this.currentRadius);
    }

    /** ChunkLoader stuff **/

    public void setActive(int radius) {
        if(getManager().getLevel() instanceof ServerLevel level) {
            if(this.active)
                this.setInactive(level);

            this.active = true;
            this.currentRadius = radius;

            BlockPos machinePos = getManager().getTile().getBlockPos();
            ChunkPos chunk = ChunkPos.containing(machinePos);
            level.setChunkForced(chunk.x(), chunk.z(), true);
            level.getChunkSource().addTicketWithRadius(CMRegistration.MACHINE_TICKET_TYPE.get(), chunk, radius + 1);
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
        ChunkPos chunk = ChunkPos.containing(machinePos);
        if(MachineList.findInSameChunk(getManager().getTile()).isEmpty())
            level.setChunkForced(chunk.x(), chunk.z(), false);
        level.getChunkSource().removeTicketWithRadius(CMRegistration.MACHINE_TICKET_TYPE.get(), chunk, this.currentRadius + 1);
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
            return CMRegistration.CHUNKLOAD_MACHINE_COMPONENT.get();
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
