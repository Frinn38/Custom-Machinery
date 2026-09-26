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
import fr.frinn.custommachinery.common.init.CMRegistration;
import fr.frinn.custommachinery.common.machine.MachineAppearance;
import fr.frinn.custommachinery.common.network.syncable.IntegerSyncable;
import fr.frinn.custommachinery.common.upgrade.CoreModifier;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.Mth;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import net.minecraft.world.level.storage.ValueOutput.ValueOutputList;

import java.util.ArrayList;
import java.util.Collections;
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
        if(this.tile.getLevel() instanceof ServerLevel) {
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
            } else if(amount.get() < currentAmount) {
                for(int i = 0; i < currentAmount - amount.get(); i++)
                    this.cores.removeLast();
                this.tile.refreshMachineContainer();
            }
        }
    }

    @Override
    public void tick() {
        if(!this.initialized)
            this.init();

        this.cores.forEach(MachineProcessorCore::tick);

        if(this.tile.getStatus() != MachineStatus.IDLE && this.cores.stream().noneMatch(core -> core.getCurrentRecipe() != null)) {
            this.tile.setStatus(MachineStatus.IDLE);
            this.tile.setCustomAppearance(null);
            this.tile.setCustomGuiElements(Collections.emptyList());
        }
    }

    private void init() {
        this.initialized = true;
        this.cores.forEach(MachineProcessorCore::init);
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
            if(!customGuiElements.isEmpty())
                this.tile.setCustomGuiElements(customGuiElements);
        }
    }

    public void setError(Component message) {
        if(this.cores.stream().allMatch(core -> core.getError() != null || core.getCurrentRecipe() == null))
            this.tile.setStatus(MachineStatus.ERRORED, message);
        if(this.cores.size() == 1) {
            this.tile.setCustomAppearance(null);
            this.tile.setCustomGuiElements(Collections.emptyList());
        }
    }

    @Override
    public void reset() {
        this.cores.forEach(MachineProcessorCore::reset);
        this.tile.setStatus(MachineStatus.IDLE);
        this.tile.setCustomAppearance(null);
        this.tile.setCustomGuiElements(Collections.emptyList());
    }

    public MachineTile tile() {
        return this.tile;
    }

    @Override
    public ProcessorType<MachineProcessor> getType() {
        return CMRegistration.MACHINE_PROCESSOR.get();
    }

    @Override
    public void serialize(ValueOutput output) {
        output.putString("type", getType().getId().toString());
        ValueOutputList cores = output.childrenList("cores");
        this.cores.forEach(core -> core.serialize(cores.addChild()));
    }

    @Override
    public void deserialize(ValueInput input) {
        if(!input.getStringOr("type", "").equals(getType().getId().toString()))
            return;
        List<ValueInput> cores = input.childrenListOrEmpty("cores").stream().toList();
        if(this.cores.size() == cores.size()) {
            for(int i = 0; i < this.cores.size(); i++)
                this.cores.get(i).deserialize(cores.get(i));
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
            return CMRegistration.MACHINE_PROCESSOR.get();
        }

        @Override
        public MachineProcessor build(MachineTile tile) {
            return new MachineProcessor(tile, this.amount, this.recipeCheckCooldown);
        }
    }
}
