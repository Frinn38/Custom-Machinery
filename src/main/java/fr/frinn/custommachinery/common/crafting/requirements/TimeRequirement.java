package fr.frinn.custommachinery.common.crafting.requirements;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import fr.frinn.custommachinery.api.components.MachineComponentType;
import fr.frinn.custommachinery.common.crafting.CraftingContext;
import fr.frinn.custommachinery.common.crafting.CraftingResult;
import fr.frinn.custommachinery.common.data.component.TimeMachineComponent;
import fr.frinn.custommachinery.common.init.Registration;
import fr.frinn.custommachinery.common.integration.jei.IDisplayInfoRequirement;
import fr.frinn.custommachinery.common.integration.jei.RequirementDisplayInfo;
import fr.frinn.custommachinery.common.util.Codecs;
import fr.frinn.custommachinery.common.util.TimeComparator;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.util.text.TranslationTextComponent;

import java.util.List;

public class TimeRequirement extends AbstractRequirement<TimeMachineComponent> implements IDisplayInfoRequirement<TimeMachineComponent> {

    public static final Codec<TimeRequirement> CODEC = RecordCodecBuilder.create(timeRequirementInstance ->
            timeRequirementInstance.group(
                    Codecs.TIME_COMPARATOR_CODEC.listOf().fieldOf("times").forGetter(requirement -> requirement.times),
                    Codec.BOOL.optionalFieldOf("jei", true).forGetter(requirement -> requirement.jeiVisible)
            ).apply(timeRequirementInstance, (times, jei) -> {
                    TimeRequirement requirement = new TimeRequirement(times);
                    requirement.setJeiVisible(jei);
                    return requirement;
            })
    );

    private List<TimeComparator> times;
    private boolean jeiVisible = true;

    public TimeRequirement(List<TimeComparator> times) {
        super(MODE.INPUT);
        this.times = times;
    }

    @Override
    public RequirementType<TimeRequirement> getType() {
        return Registration.TIME_REQUIREMENT.get();
    }

    @Override
    public boolean test(TimeMachineComponent component, CraftingContext context) {
        return this.times.stream().allMatch(comparator -> comparator.compare((int)component.getTime()));
    }

    @Override
    public CraftingResult processStart(TimeMachineComponent component, CraftingContext context) {
        if(test(component, context))
            return CraftingResult.success();
        else
            return CraftingResult.error(new TranslationTextComponent("custommachinery.requirements.time.error"));
    }

    @Override
    public CraftingResult processEnd(TimeMachineComponent component, CraftingContext context) {
        return CraftingResult.pass();
    }

    @Override
    public MachineComponentType<TimeMachineComponent> getComponentType() {
        return Registration.TIME_MACHINE_COMPONENT.get();
    }

    @Override
    public void setJeiVisible(boolean jeiVisible) {
        this.jeiVisible = jeiVisible;
    }

    @Override
    public RequirementDisplayInfo getDisplayInfo() {
        RequirementDisplayInfo info = new RequirementDisplayInfo();
        if(!this.times.isEmpty()) {
            info.addTooltip(new TranslationTextComponent("custommachinery.requirements.time.info").mergeStyle(TextFormatting.AQUA));
            this.times.forEach(time -> info.addTooltip(new StringTextComponent("* ").appendSibling(time.getText())));
        }
        info.setVisible(this.jeiVisible);
        return info;
    }
}
