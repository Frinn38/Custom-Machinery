package fr.frinn.custommachinery.common.crafting.machine;

import com.mojang.datafixers.util.Pair;
import fr.frinn.custommachinery.api.codec.NamedCodec;
import fr.frinn.custommachinery.api.crafting.IProcessor;
import fr.frinn.custommachinery.api.crafting.IProcessorTemplate;
import fr.frinn.custommachinery.api.crafting.ProcessorType;
import fr.frinn.custommachinery.api.guielement.IGuiElement;
import fr.frinn.custommachinery.api.machine.MachineStatus;
import fr.frinn.custommachinery.api.machine.MachineTile;
import fr.frinn.custommachinery.api.network.ISyncable;
import fr.frinn.custommachinery.api.network.ISyncableStuff;
import fr.frinn.custommachinery.client.ClientHandler;
import fr.frinn.custommachinery.common.init.Registration;
import fr.frinn.custommachinery.common.machine.MachineAppearance;
import fr.frinn.custommachinery.common.network.syncable.IntegerSyncable;
import fr.frinn.custommachinery.common.upgrade.CoreModifier;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.Mth;
import net.minecraft.world.item.crafting.RecipeHolder;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Consumer;
import java.util.stream.Stream;

public class MachineProcessor implements IProcessor, ISyncableStuff {

    private final MachineTile tile;
    private final int coreAmount;
    private final int recipeCheckCooldown;
    private boolean initialized = false;

    private final List<MachineProcessorCore> cores = new ArrayList<>();

    public MachineProcessor(MachineTile tile, int amount, int recipeCheckCooldown) {
        this.tile = tile;
        this.coreAmount = amount;
        this.recipeCheckCooldown = recipeCheckCooldown;
        for(int i = 0; i < amount; i++)
            this.cores.add(new MachineProcessorCore(this, tile, recipeCheckCooldown, i + 1));
    }

    public List<MachineProcessorCore> getCores() {
        return this.cores;
    }

    public void setClientCoreCount(int count) {
        this.cores.clear();
        for(int i = 0; i < count; i++) {
            MachineProcessorCore core = new MachineProcessorCore(this, this.tile, this.recipeCheckCooldown, i + 1);
            core.init();
            this.cores.add(core);
        }
        ClientHandler.refreshMachineContainer();
    }

    public void refreshCoreCount(Stream<Pair<CoreModifier, Integer>> modifiers) {
        if(this.tile.getLevel() instanceof ServerLevel level) {
            AtomicInteger amount = new AtomicInteger(this.coreAmount);
            modifiers.forEach(pair -> amount.set((int)Mth.clamp(pair.getFirst().apply(amount.get(), pair.getSecond()), pair.getFirst().min(), pair.getFirst().max())));
            int currentAmount = this.cores.size();
            if(amount.get() > currentAmount) {
                for(int i = 0; i < amount.get() - currentAmount; i++) {
                    MachineProcessorCore core = new MachineProcessorCore(this, this.tile, this.recipeCheckCooldown, this.cores.size() + 1);
                    core.init();
                    this.cores.add(core);
                }
                this.tile.refreshMachineContainer();
                //PacketDistributor.sendToPlayersTrackingChunk(level, new ChunkPos(this.tile.getBlockPos()), new SMachineCoreCountChangePacket(this.tile.getBlockPos(), amount.get()));
            } else if(amount.get() < currentAmount) {
                for(int i = 0; i < currentAmount - amount.get(); i++)
                    this.cores.removeLast();
                this.tile.refreshMachineContainer();
                //PacketDistributor.sendToPlayersTrackingChunk(level, new ChunkPos(this.tile.getBlockPos()), new SMachineCoreCountChangePacket(this.tile.getBlockPos(), amount.get()));
            }
        }
    }

    @Override
    public void tick() {
        if(!this.initialized)
            this.init();

        this.cores.forEach(MachineProcessorCore::tick);

        if(this.tile.getStatus() == MachineStatus.RUNNING && this.cores.stream().noneMatch(core -> core.getCurrentRecipe() != null)) {
            this.tile.setStatus(MachineStatus.IDLE);
            this.tile.setCustomAppearance(null);
            this.tile.setCustomGuiElements(null);
        }
    }

    private void init() {
        this.initialized = true;
        this.cores.forEach(MachineProcessorCore::init);
        //if(this.cores.size() != this.coreAmount && this.tile.getLevel() instanceof ServerLevel level)
        //    TaskDelayer.enqueue(1, () -> PacketDistributor.sendToPlayersTrackingChunk(level, new ChunkPos(this.tile.getBlockPos()), new SMachineCoreCountChangePacket(this.tile.getBlockPos(), this.cores.size())));
    }

    public void setRunning() {
        this.tile.setStatus(MachineStatus.RUNNING);

        if(this.cores.size() == 1) {
            RecipeHolder<CustomMachineRecipe> currentRecipe = this.cores.getFirst().getCurrentRecipe();
            if(currentRecipe == null)
                return;
            MachineAppearance customAppearance = currentRecipe.value().getCustomAppearance(this.tile.getMachine().getAppearance(this.tile().getStatus()));
            if(customAppearance != null)
                this.tile.setCustomAppearance(customAppearance);

            List<IGuiElement> customGuiElements = currentRecipe.value().getCustomGuiElements(this.tile.getMachine().getGuiElements());
            if(customGuiElements != null && !customGuiElements.isEmpty())
                this.tile.setCustomGuiElements(customGuiElements);
        }
    }

    public void setError(Component message) {
        if(this.cores.stream().allMatch(core -> core.getError() != null || core.getCurrentRecipe() == null))
            this.tile.setStatus(MachineStatus.ERRORED, message);
        if(this.cores.size() == 1) {
            this.tile.setCustomAppearance(null);
            this.tile.setCustomGuiElements(null);
        }
    }

    @Override
    public void reset() {
        this.cores.forEach(MachineProcessorCore::reset);
        this.tile.setStatus(MachineStatus.IDLE);
        this.tile.setCustomAppearance(null);
        this.tile.setCustomGuiElements(null);
    }

    public MachineTile tile() {
        return this.tile;
    }

    @Override
    public ProcessorType<MachineProcessor> getType() {
        return Registration.MACHINE_PROCESSOR.get();
    }

    @Override
    public CompoundTag serialize() {
        CompoundTag nbt = new CompoundTag();
        nbt.putString("type", getType().getId().toString());
        ListTag cores = new ListTag();
        this.cores.forEach(core -> cores.add(core.serialize()));
        nbt.put("cores", cores);
        return nbt;
    }

    @Override
    public void deserialize(CompoundTag nbt) {
        if(nbt.contains("type", Tag.TAG_STRING) && !nbt.getString("type").equals(getType().getId().toString()))
            return;
        if(nbt.contains("cores", Tag.TAG_LIST)) {
            ListTag cores = nbt.getList("cores", Tag.TAG_COMPOUND);
            if(this.cores.size() == cores.size()) {
                for(int i = 0; i < this.cores.size(); i++)
                    this.cores.get(i).deserialize(cores.getCompound(i));
            }
        }
    }

    @Override
    public void getStuffToSync(Consumer<ISyncable<?, ?>> container) {
        container.accept(IntegerSyncable.create(this.cores::size, this::setClientCoreCount));
        this.cores.forEach(core -> core.getStuffToSync(container));
    }

    @Override
    public void setMachineInventoryChanged() {
        this.cores.forEach(MachineProcessorCore::setMachineInventoryChanged);
    }

    @Override
    public void setSearchImmediately() {
        this.cores.forEach(MachineProcessorCore::setSearchImmediately);
    }

    public record Template(int amount, int recipeCheckCooldown) implements IProcessorTemplate<MachineProcessor> {

        public static final NamedCodec<Template> CODEC = NamedCodec.record(templateInstance ->
                templateInstance.group(
                        NamedCodec.intRange(1, Integer.MAX_VALUE).optionalFieldOf("amount", 1).forGetter(template -> template.amount),
                        NamedCodec.intRange(1, Integer.MAX_VALUE).optionalFieldOf("cooldown", 20).forGetter(template -> template.recipeCheckCooldown)
                ).apply(templateInstance, Template::new), "Machine processor"
        );

        public static final Template DEFAULT = new Template(1, 20);

        @Override
        public ProcessorType<MachineProcessor> getType() {
            return Registration.MACHINE_PROCESSOR.get();
        }

        @Override
        public MachineProcessor build(MachineTile tile) {
            return new MachineProcessor(tile, this.amount, this.recipeCheckCooldown);
        }
    }
}
