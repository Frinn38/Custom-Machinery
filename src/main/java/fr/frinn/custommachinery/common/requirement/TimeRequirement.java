package fr.frinn.custommachinery.common.requirement;

import fr.frinn.custommachinery.api.codec.NamedCodec;
import fr.frinn.custommachinery.api.component.MachineComponentType;
import fr.frinn.custommachinery.api.crafting.CraftingResult;
import fr.frinn.custommachinery.api.crafting.ICraftingContext;
import fr.frinn.custommachinery.api.crafting.IRequirementList;
import fr.frinn.custommachinery.api.integration.jei.IDisplayInfo;
import fr.frinn.custommachinery.api.requirement.IRequirement;
import fr.frinn.custommachinery.api.requirement.RecipeRequirement;
import fr.frinn.custommachinery.api.requirement.RequirementIOMode;
import fr.frinn.custommachinery.api.requirement.RequirementType;
import fr.frinn.custommachinery.common.component.TimeMachineComponent;
import fr.frinn.custommachinery.common.init.Registration;
import fr.frinn.custommachinery.impl.util.IntRange;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.Items;

public record TimeRequirement(IntRange range) implements IRequirement<TimeMachineComponent> {

    public static final NamedCodec<TimeRequirement> CODEC = NamedCodec.record(timeRequirementInstance ->
            timeRequirementInstance.group(
                    IntRange.CODEC.fieldOf("range").forGetter(requirement -> requirement.range)
            ).apply(timeRequirementInstance, TimeRequirement::new), "Time requirement"
    );

    @Override
    public RequirementType<TimeRequirement> getType() {
        return Registration.TIME_REQUIREMENT.get();
    }

    @Override
    public MachineComponentType<TimeMachineComponent> getComponentType() {
        return Registration.TIME_MACHINE_COMPONENT.get();
    }

    @Override
    public RequirementIOMode getMode() {
        return RequirementIOMode.INPUT;
    }

    @Override
    public boolean test(TimeMachineComponent component, ICraftingContext context) {
        return this.range.contains((int)component.getTime());
    }

    @Override
    public void gatherRequirements(IRequirementList<TimeMachineComponent> list) {
        list.worldCondition(this::check);
    }

    private CraftingResult check(TimeMachineComponent component, ICraftingContext context) {
        if(test(component, context))
            return CraftingResult.success();
        else
            return CraftingResult.error(Component.translatable("custommachinery.requirements.time.error"));
    }


    @Override
    public void getDefaultDisplayInfo(IDisplayInfo info, RecipeRequirement<?, ?> requirement) {
        info.addTooltip(Component.translatable("custommachinery.requirements.time.info").withStyle(ChatFormatting.AQUA));
        this.range.getRestrictions().forEach(restriction -> info.addTooltip(Component.literal("* " + restriction.toFormattedString())));
        info.setItemIcon(Items.CLOCK);
    }
}
