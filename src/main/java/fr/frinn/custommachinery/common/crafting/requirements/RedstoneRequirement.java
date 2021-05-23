package fr.frinn.custommachinery.common.crafting.requirements;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import fr.frinn.custommachinery.common.crafting.CraftingResult;
import fr.frinn.custommachinery.common.data.component.MachineComponentType;
import fr.frinn.custommachinery.common.data.component.RedstoneMachineComponent;
import fr.frinn.custommachinery.common.init.Registration;
import fr.frinn.custommachinery.common.util.Codecs;
import fr.frinn.custommachinery.common.util.ComparatorMode;
import net.minecraft.util.text.TranslationTextComponent;

public class RedstoneRequirement extends AbstractTickableRequirement<RedstoneMachineComponent> {

    public static final Codec<RedstoneRequirement> CODEC = RecordCodecBuilder.create(redstoneRequirementInstance ->
            redstoneRequirementInstance.group(
                    Codec.INT.fieldOf("power").forGetter(requirement -> requirement.powerLevel),
                    Codecs.COMPARATOR_MODE_CODEC.optionalFieldOf("comparator", ComparatorMode.GREATER_OR_EQUALS).forGetter(requirement -> requirement.comparatorMode)
            ).apply(redstoneRequirementInstance, RedstoneRequirement::new)
    );

    private int powerLevel;
    private ComparatorMode comparatorMode;

    public RedstoneRequirement(int powerLevel, ComparatorMode comparatorMode) {
        super(MODE.INPUT);
        this.powerLevel = powerLevel;
        this.comparatorMode = comparatorMode;
    }

    @Override
    public RequirementType<RedstoneRequirement> getType() {
        return Registration.REDSTONE_REQUIREMENT.get();
    }

    @Override
    public boolean test(RedstoneMachineComponent component) {
        return this.comparatorMode.compare(component.getMachinePower(), this.powerLevel);
    }

    @Override
    public CraftingResult processStart(RedstoneMachineComponent component) {
        if(this.test(component))
            return CraftingResult.success();
        return CraftingResult.error(new TranslationTextComponent("custommachinery.requirements.redstone.error", this.powerLevel));
    }

    @Override
    public CraftingResult processEnd(RedstoneMachineComponent component) {
        return CraftingResult.pass();
    }

    @Override
    public MachineComponentType<RedstoneMachineComponent> getComponentType() {
        return Registration.REDSTONE_MACHINE_COMPONENT.get();
    }

    @Override
    public CraftingResult processTick(RedstoneMachineComponent component) {
        if(this.test(component))
            return CraftingResult.success();
        return CraftingResult.error(new TranslationTextComponent("custommachinery.requirements.redstone.error", this.powerLevel));
    }
}
