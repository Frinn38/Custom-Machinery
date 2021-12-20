package fr.frinn.custommachinery.common.crafting.requirement;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import fr.frinn.custommachinery.api.codec.CodecLogger;
import fr.frinn.custommachinery.api.component.MachineComponentType;
import fr.frinn.custommachinery.api.crafting.CraftingResult;
import fr.frinn.custommachinery.api.crafting.ICraftingContext;
import fr.frinn.custommachinery.api.integration.jei.IDisplayInfo;
import fr.frinn.custommachinery.api.integration.jei.IDisplayInfoRequirement;
import fr.frinn.custommachinery.api.requirement.ITickableRequirement;
import fr.frinn.custommachinery.api.requirement.RequirementIOMode;
import fr.frinn.custommachinery.api.requirement.RequirementType;
import fr.frinn.custommachinery.apiimpl.requirement.AbstractRequirement;
import fr.frinn.custommachinery.common.data.component.RedstoneMachineComponent;
import fr.frinn.custommachinery.common.init.Registration;
import fr.frinn.custommachinery.common.util.Codecs;
import fr.frinn.custommachinery.common.util.ComparatorMode;
import net.minecraft.util.text.TranslationTextComponent;

public class RedstoneRequirement extends AbstractRequirement<RedstoneMachineComponent> implements ITickableRequirement<RedstoneMachineComponent>, IDisplayInfoRequirement {

    public static final Codec<RedstoneRequirement> CODEC = RecordCodecBuilder.create(redstoneRequirementInstance ->
            redstoneRequirementInstance.group(
                    Codec.INT.fieldOf("power").forGetter(requirement -> requirement.powerLevel),
                    CodecLogger.loggedOptional(Codecs.COMPARATOR_MODE_CODEC,"comparator", ComparatorMode.GREATER_OR_EQUALS).forGetter(requirement -> requirement.comparatorMode),
                    CodecLogger.loggedOptional(Codec.BOOL,"jei", true).forGetter(requirement -> requirement.jeiVisible)
            ).apply(redstoneRequirementInstance, (power, comparator, jei) -> {
                    RedstoneRequirement requirement = new RedstoneRequirement(power, comparator);
                    requirement.setJeiVisible(jei);
                    return requirement;
            })
    );

    private final int powerLevel;
    private final ComparatorMode comparatorMode;
    private boolean jeiVisible = true;

    public RedstoneRequirement(int powerLevel, ComparatorMode comparatorMode) {
        super(RequirementIOMode.INPUT);
        this.powerLevel = powerLevel;
        this.comparatorMode = comparatorMode;
    }

    @Override
    public RequirementType<RedstoneRequirement> getType() {
        return Registration.REDSTONE_REQUIREMENT.get();
    }

    @Override
    public boolean test(RedstoneMachineComponent component, ICraftingContext context) {
        int powerLevel = (int)context.getModifiedValue(this.powerLevel, this, null);
        return this.comparatorMode.compare(component.getMachinePower(), powerLevel);
    }

    @Override
    public CraftingResult processStart(RedstoneMachineComponent component, ICraftingContext context) {
        int powerLevel = (int)context.getModifiedValue(this.powerLevel, this, null);
        if(this.test(component, context))
            return CraftingResult.success();
        return CraftingResult.error(new TranslationTextComponent("custommachinery.requirements.redstone.error", powerLevel));
    }

    @Override
    public CraftingResult processEnd(RedstoneMachineComponent component, ICraftingContext context) {
        return CraftingResult.pass();
    }

    @Override
    public MachineComponentType<RedstoneMachineComponent> getComponentType() {
        return Registration.REDSTONE_MACHINE_COMPONENT.get();
    }

    @Override
    public CraftingResult processTick(RedstoneMachineComponent component, ICraftingContext context) {
        int powerLevel = (int)context.getModifiedValue(this.powerLevel, this, null);
        if(this.test(component, context))
            return CraftingResult.success();
        return CraftingResult.error(new TranslationTextComponent("custommachinery.requirements.redstone.error", powerLevel));
    }

    @Override
    public void setJeiVisible(boolean jeiVisible) {
        this.jeiVisible = jeiVisible;
    }

    @Override
    public void getDisplayInfo(IDisplayInfo info) {
        info.setVisible(this.jeiVisible)
                .addTooltip(new TranslationTextComponent("custommachinery.requirements.redstone.info", new TranslationTextComponent(this.comparatorMode.getTranslationKey()), this.powerLevel));
    }
}
