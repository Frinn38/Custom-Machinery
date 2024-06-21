package fr.frinn.custommachinery.common.requirement;

import fr.frinn.custommachinery.api.codec.NamedCodec;
import fr.frinn.custommachinery.api.component.MachineComponentType;
import fr.frinn.custommachinery.api.crafting.CraftingResult;
import fr.frinn.custommachinery.api.crafting.ICraftingContext;
import fr.frinn.custommachinery.api.integration.jei.IDisplayInfo;
import fr.frinn.custommachinery.api.integration.jei.IDisplayInfoRequirement;
import fr.frinn.custommachinery.api.requirement.RequirementIOMode;
import fr.frinn.custommachinery.api.requirement.RequirementType;
import fr.frinn.custommachinery.common.component.TimeMachineComponent;
import fr.frinn.custommachinery.common.init.Registration;
import fr.frinn.custommachinery.impl.requirement.AbstractRequirement;
import fr.frinn.custommachinery.impl.util.IntRange;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.Items;

public class TimeRequirement extends AbstractRequirement<TimeMachineComponent> implements IDisplayInfoRequirement {

    public static final NamedCodec<TimeRequirement> CODEC = NamedCodec.record(timeRequirementInstance ->
            timeRequirementInstance.group(
                    IntRange.CODEC.fieldOf("range").forGetter(requirement -> requirement.range)
            ).apply(timeRequirementInstance, TimeRequirement::new), "Time requirement"
    );

    private final IntRange range;

    public TimeRequirement(IntRange range) {
        super(RequirementIOMode.INPUT);
        this.range = range;
    }

    @Override
    public RequirementType<TimeRequirement> getType() {
        return Registration.TIME_REQUIREMENT.get();
    }

    @Override
    public boolean test(TimeMachineComponent component, ICraftingContext context) {
        return this.range.contains((int)component.getTime());
    }

    @Override
    public CraftingResult processStart(TimeMachineComponent component, ICraftingContext context) {
        if(test(component, context))
            return CraftingResult.success();
        else
            return CraftingResult.error(Component.translatable("custommachinery.requirements.time.error"));
    }

    @Override
    public CraftingResult processEnd(TimeMachineComponent component, ICraftingContext context) {
        return CraftingResult.pass();
    }

    @Override
    public MachineComponentType<TimeMachineComponent> getComponentType() {
        return Registration.TIME_MACHINE_COMPONENT.get();
    }

    @Override
    public void getDisplayInfo(IDisplayInfo info) {
        info.addTooltip(Component.translatable("custommachinery.requirements.time.info").withStyle(ChatFormatting.AQUA));
        this.range.getRestrictions().forEach(restriction -> info.addTooltip(Component.literal("* " + restriction.toFormattedString())));
        info.setItemIcon(Items.CLOCK);
    }
}
