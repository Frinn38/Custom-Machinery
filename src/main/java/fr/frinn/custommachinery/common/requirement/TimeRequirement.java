package fr.frinn.custommachinery.common.requirement;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import fr.frinn.custommachinery.api.component.MachineComponentType;
import fr.frinn.custommachinery.api.crafting.CraftingResult;
import fr.frinn.custommachinery.api.crafting.ICraftingContext;
import fr.frinn.custommachinery.api.integration.jei.IDisplayInfo;
import fr.frinn.custommachinery.api.integration.jei.IDisplayInfoRequirement;
import fr.frinn.custommachinery.api.requirement.RequirementIOMode;
import fr.frinn.custommachinery.api.requirement.RequirementType;
import fr.frinn.custommachinery.apiimpl.requirement.AbstractRequirement;
import fr.frinn.custommachinery.apiimpl.util.IntRange;
import fr.frinn.custommachinery.common.component.TimeMachineComponent;
import fr.frinn.custommachinery.common.init.Registration;
import fr.frinn.custommachinery.common.util.Codecs;
import fr.frinn.custommachinery.common.util.TimeComparator;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.world.item.Items;

import java.util.List;

public class TimeRequirement extends AbstractRequirement<TimeMachineComponent> implements IDisplayInfoRequirement {

    public static final Codec<TimeRequirement> CODEC = RecordCodecBuilder.create(timeRequirementInstance ->
            timeRequirementInstance.group(
                    IntRange.CODEC.fieldOf("range").forGetter(requirement -> requirement.range)
            ).apply(timeRequirementInstance, TimeRequirement::new)
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
            return CraftingResult.error(new TranslatableComponent("custommachinery.requirements.time.error"));
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
        info.addTooltip(new TranslatableComponent("custommachinery.requirements.time.info").withStyle(ChatFormatting.AQUA));
        this.range.getRestrictions().forEach(restriction -> info.addTooltip(new TextComponent("* " + restriction.toFormattedString())));
        info.setItemIcon(Items.CLOCK);
    }
}
