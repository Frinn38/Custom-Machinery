package fr.frinn.custommachinery.common.crafting.requirements;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import fr.frinn.custommachinery.api.component.MachineComponentType;
import fr.frinn.custommachinery.api.utils.CodecLogger;
import fr.frinn.custommachinery.common.crafting.CraftingContext;
import fr.frinn.custommachinery.common.crafting.CraftingResult;
import fr.frinn.custommachinery.common.data.component.RedstoneMachineComponent;
import fr.frinn.custommachinery.common.init.Registration;
import fr.frinn.custommachinery.common.integration.jei.IDisplayInfoRequirement;
import fr.frinn.custommachinery.common.integration.jei.RequirementDisplayInfo;
import fr.frinn.custommachinery.common.util.Codecs;
import fr.frinn.custommachinery.common.util.ComparatorMode;
import net.minecraft.util.text.TranslationTextComponent;

public class RedstoneRequirement extends AbstractTickableRequirement<RedstoneMachineComponent> implements IDisplayInfoRequirement<RedstoneMachineComponent> {

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

    private int powerLevel;
    private ComparatorMode comparatorMode;
    private boolean jeiVisible = true;

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
    public boolean test(RedstoneMachineComponent component, CraftingContext context) {
        int powerLevel = (int)context.getModifiedvalue(this.powerLevel, this, null);
        return this.comparatorMode.compare(component.getMachinePower(), powerLevel);
    }

    @Override
    public CraftingResult processStart(RedstoneMachineComponent component, CraftingContext context) {
        int powerLevel = (int)context.getModifiedvalue(this.powerLevel, this, null);
        if(this.test(component, context))
            return CraftingResult.success();
        return CraftingResult.error(new TranslationTextComponent("custommachinery.requirements.redstone.error", powerLevel));
    }

    @Override
    public CraftingResult processEnd(RedstoneMachineComponent component, CraftingContext context) {
        return CraftingResult.pass();
    }

    @Override
    public MachineComponentType<RedstoneMachineComponent> getComponentType() {
        return Registration.REDSTONE_MACHINE_COMPONENT.get();
    }

    @Override
    public CraftingResult processTick(RedstoneMachineComponent component, CraftingContext context) {
        int powerLevel = (int)context.getModifiedvalue(this.powerLevel, this, null);
        if(this.test(component, context))
            return CraftingResult.success();
        return CraftingResult.error(new TranslationTextComponent("custommachinery.requirements.redstone.error", powerLevel));
    }

    @Override
    public void setJeiVisible(boolean jeiVisible) {
        this.jeiVisible = jeiVisible;
    }

    @Override
    public RequirementDisplayInfo getDisplayInfo() {
        return new RequirementDisplayInfo()
                .setVisible(this.jeiVisible)
                .addTooltip(new TranslationTextComponent("custommachinery.requirements.redstone.info", new TranslationTextComponent(this.comparatorMode.getTranslationKey()), this.powerLevel));
    }
}
