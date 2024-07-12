package fr.frinn.custommachinery.common.crafting.machine;

import com.google.common.collect.ImmutableList;
import fr.frinn.custommachinery.api.codec.NamedCodec;
import fr.frinn.custommachinery.api.crafting.IProcessor;
import fr.frinn.custommachinery.api.crafting.IProcessorTemplate;
import fr.frinn.custommachinery.api.crafting.ProcessorType;
import fr.frinn.custommachinery.api.guielement.IGuiElement;
import fr.frinn.custommachinery.api.machine.MachineStatus;
import fr.frinn.custommachinery.api.machine.MachineTile;
import fr.frinn.custommachinery.common.crafting.CraftingContext;
import fr.frinn.custommachinery.common.init.Registration;
import fr.frinn.custommachinery.common.machine.MachineAppearance;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.crafting.RecipeHolder;

import java.util.List;

public class MachineProcessor implements IProcessor {

    private final MachineTile tile;
    private boolean initialized = false;

    private final List<MachineProcessorCore> cores;

    public MachineProcessor(MachineTile tile, int amount, int recipeCheckCooldown) {
        this.tile = tile;
        //Use only for recipe searching, not recipe processing
        CraftingContext.Mutable mutableCraftingContext = new CraftingContext.Mutable(tile, tile.getUpgradeManager());
        ImmutableList.Builder<MachineProcessorCore> cores = ImmutableList.builder();
        for(int i = 0; i < amount; i++)
            cores.add(new MachineProcessorCore(this, tile, recipeCheckCooldown, mutableCraftingContext));
        this.cores = cores.build();
    }

    public List<MachineProcessorCore> getCores() {
        return this.cores;
    }

    @Override
    public void tick() {
        if(!this.initialized)
            this.init();

        this.cores.forEach(MachineProcessorCore::tick);
    }

    private void init() {
        this.initialized = true;
        this.cores.forEach(MachineProcessorCore::init);
    }

    public void setRunning() {
        this.tile.setStatus(MachineStatus.RUNNING);

        if(this.cores.size() == 1) {
            RecipeHolder<CustomMachineRecipe> currentRecipe = this.cores.getFirst().getCurrentRecipe();
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
    public void setMachineInventoryChanged() {
        this.cores.forEach(MachineProcessorCore::setMachineInventoryChanged);
    }

    @Override
    public void setSearchImmediately() {
        this.cores.forEach(MachineProcessorCore::setSearchImmediately);
    }

    public static class Template implements IProcessorTemplate<MachineProcessor> {

        public static final NamedCodec<Template> CODEC = NamedCodec.record(templateInstance ->
                templateInstance.group(
                        NamedCodec.intRange(1, Integer.MAX_VALUE).optionalFieldOf("amount", 1).forGetter(template -> template.amount),
                        NamedCodec.intRange(1, Integer.MAX_VALUE).optionalFieldOf("cooldown", 20).forGetter(template -> template.recipeCheckCooldown)
                ).apply(templateInstance, Template::new), "Machine processor"
        );

        public static final Template DEFAULT = new Template(1, 20);

        private final int amount;
        private final int recipeCheckCooldown;

        private Template(int amount, int cooldown) {
            this.amount = amount;
            this.recipeCheckCooldown = cooldown;
        }

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
